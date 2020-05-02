# Demo Repo: Ratpack-Background

This is a simple demo of creating a background service in Ratpack - Java. 

## What is this?

I spend a lot of time writing ratpack services and every single time I reach for how to build a background service 
I end up having to remember how to do it, so this is a demo of how to do it.

## Understanding what is happening

Sometimes even with the power and speed of Ratpack aren't what you need, or you need to process some task outside of an
HTTP request, like updating a cache, reading from a messaging queue, etc. This project serves as an example on how to accomplish 
a scheduled task using Ratpack with the event loop. Note that the same type of implementation could be done for event processing
without the need for a schedule, but we won't cover that today (contact me if you'd like to see that)! 

Basically the scenario is I need some cache server and for some reason I need to do it myself; maybe I want to be able to 
query for objects in a way I can't otherwise do, or I want more control over the persistence rules. In this example I am 
just going to hold them in a map and asynchronously update the data within using our background task. This will make CRUD 
operations very quick and prevent a call held up making concurrent modifications to the map. There is a risk of data not 
being persisted correctly since multiple calls to the same key will only persist the final operation. That is why this 
repo should serve **_only as an example_**.

## Where the action is

[`src/main/java/com/lemonoflime/rpbackground/services/InMemoryStorageService.java`](https://github.com/lemoney/ratpack-background/blob/master/src/main/java/com/lemonoflime/rpbackground/services/InMemoryStorageService.java) 
contains a guided documentation walk-through of the code but there is a CRUD API to interact with objects in memory 
contained by the service. 

## Poking at it

To see all this together working run `./gradlew run` to start the server then in another shell run `groovy generator.groovy` 
which will start pumping data into the server. what you should see is ~600 RPS from the server with the async writes to 
the `storage` map in the `InMemoryStorageService`.

