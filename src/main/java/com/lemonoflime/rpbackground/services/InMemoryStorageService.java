package com.lemonoflime.rpbackground.services;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.lemonoflime.rpbackground.persistence.SimpleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.*;
import ratpack.exec.util.SerialBatch;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

// singleton annotation so the registry knows to only hold one instance of this class for services requiring it
@Singleton
/*
 * Note: This class is just an example, there is a lot left unhandled here... things like:
 *   - what happens when an error occurs, how do you ensure the command doesn't have to be sent again by a caller to get it to actually persist/delete? -- you lose the work!
 *   - what happens if there is work outstanding in addToQueue/deleteFromQueue when the server shuts down? -- you lose the work!
 *   - what happens to operations that get added to addToQueue/deleteFromQueue after lines 95/96 and 101/102 respectively? -- they never get applied!
 * and more! So use this as a guide and example of background execution but not as your production caching server
 */
public class InMemoryStorageService implements StorageService {
    // a helpful logger
    private final Logger log = LoggerFactory.getLogger(InMemoryStorageService.class);
    // Note: the next three variables are private static final to ensure they are scoped correctly across the class
    // the "source of truth" for what is actually in storage
    private static final Map<String, SimpleObject> storage = new ConcurrentHashMap<>();
    /*
     *  a map for storing the objects to be added/updated on the next run
     *
     * Note: it is a map so any updates that occur between runs are made the value that gets persisted
     */
    private static final Map<String, SimpleObject> addToQueue = new ConcurrentHashMap<>();
    /*
     * Similar to the addToQueue object but a simpler set to prevent duplicate remove operations from hitting the
     * map unnecessarily
     */
    private static final Set<String> deleteFromQueue = new ConcurrentSkipListSet<>();
    /*
     * This is the actual "caller" that calls the code and executes it. We will be using the Ratpack implementation
     * of a scheduled executor so we can subscribe to promises/operations and utilize the power of those constructs
     *
     * Note: we don't initialize it here because we won't have access to its' value until the server is ready to start
     * the service
     */
    private ScheduledExecutorService executor;

    // schedule the process method for 1 second from "now"
    private void schedule() {
        executor.schedule(this::process, 1, TimeUnit.SECONDS);
    }

    // This method actually sets up the operation to be executed on the event loop
    private void process() {
        Execution
            // fork the execution away from wherever we are in the VM to do our work
            .fork()
            // on completion of the syncDB method we reschedule ourselves
            .onComplete(a -> schedule())
            // if an error occurs during our sync we log it but continue to reschedule
            .onError(e -> log.error("sync process encountered error", e))
            /*
             * we hook into the start of a syncDB run to log we are working on things.
             *
             * Note: This is important, you don't want to be looking at your app in production wondering if the
             * background task is still running!
             */
            .onStart(a -> log.info("starting DB Sync"))
            /*
             * here we hand the generated Operation returned from syncDB to the forked execution which subscribes to it
             * and executes it.
             */
            .start(syncDB());
    }

    // onStart is a hook into the system startup allowing us to begin work when the server starts
    @Override
    public void onStart(StartEvent event) {
        log.info("storage service starting");
        // set the executor to the bound executor within the registry of the application
        executor = event.getRegistry().get(ExecController.class).getExecutor();
        // perform the initial scheduling of syncDB -- remember onComplete of schedule reschedules execution
        schedule();
    }

    private Operation syncDB() {
        return Blocking
            // first we iterate through the addToQueue and generate a list of promises to modify storage then clear the queue
            .get(() -> {
                List<Promise<Void>> operations = Lists.newArrayList();
                addToQueue.forEach((id, obj) -> operations.add(directPersist(obj).promise()));
                addToQueue.clear();
                return operations;
            })
            // then we hand that list to another operation to add more operations removing objects from storage and clear its' queue
            .blockingMap(ops -> {
                deleteFromQueue.forEach(id -> ops.add(directDelete(id).promise()));
                deleteFromQueue.clear();
                return ops;
            })
            // we then subscribe to all those events in the "batch" and call yieldAll to render the results of them
            .flatMap(ops -> SerialBatch.of(ops).yieldAll())
            // next we time the processing work and log it
            .time(d -> log.info("processing took {}s", d.getSeconds()))
            /*
             * finally we iterate through those execResult objects to make sure we didn't get any errors & if we did log them
             */
            .operation(results -> results.forEach(result -> {
                if (result.isError()) {
                    log.error("received error while applying change", result.getThrowable());
                }
            }));
    }

    @Override
    public Promise<SimpleObject> retrieve(String id) {
        // super simple lookup in the map for a value
        return Promise.value(storage.get(id));
    }

    @Override
    public Operation persist(SimpleObject simpleObject) {
        // since we're wanting to avoid touching storage because it could be slow we're just going to store it in the queue to be processed eventually
        return Operation.of(() -> addToQueue.put(simpleObject.getId(), simpleObject));
    }

    private Operation directPersist(SimpleObject simpleObject) {
        // this method is only for the service to be called by syncDB actually performing the persist operation
        return Operation.of(() -> storage.put(simpleObject.getId(), simpleObject));
    }

    @Override
    public Operation delete(String id) {
        // since we're wanting to avoid touching storage because it could be slow we're just going to store it in the queue to be processed eventually
        return Operation.of(() -> deleteFromQueue.add(id));
    }

    private Operation directDelete(String id) {
        // this method is only for the service to be called by syncDB actually performing the remove operation
        return Operation.of(() -> storage.remove(id));
    }

    @Override
    public void onStop(StopEvent event) {
        // you could hook into the shutdown here and stop the task, handle outstanding operations, etc.
        log.warn("storage service stopping");
    }
}
