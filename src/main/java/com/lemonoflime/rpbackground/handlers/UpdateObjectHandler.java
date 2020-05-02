package com.lemonoflime.rpbackground.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lemonoflime.rpbackground.persistence.SimpleObject;
import com.lemonoflime.rpbackground.services.StorageService;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;

@Singleton
public class UpdateObjectHandler implements Handler {
    private final StorageService storageService;

    @Inject
    public UpdateObjectHandler(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx
            .parse(SimpleObject.class)
            .nextOp(storageService::persist)
            .then(obj -> ctx.getResponse().status(Status.ACCEPTED).send());
    }
}
