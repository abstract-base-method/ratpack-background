package com.lemonoflime.rpbackground.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lemonoflime.rpbackground.services.StorageService;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;

@Singleton
public class DeleteObjectHandler implements Handler {
    private final StorageService storageService;

    @Inject
    public DeleteObjectHandler(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String id = ctx.getAllPathTokens().get("id");
        if (id == null) {
            ctx.getResponse().status(Status.BAD_REQUEST).send();
        } else {
            storageService
                .delete(id)
                .then(() -> ctx.getResponse().status(Status.ACCEPTED).send());
        }
    }
}
