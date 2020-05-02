package com.lemonoflime.rpbackground.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lemonoflime.rpbackground.services.StorageService;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import ratpack.jackson.Jackson;

@Singleton
public class GetObjectHandler implements Handler {
    private final StorageService storageService;

    @Inject
    public GetObjectHandler(StorageService storageService) {
        this.storageService = storageService;

    }

    @Override
    public void handle(Context ctx) throws Exception {
        String id = ctx.getAllPathTokens().get("id");
        if (id == null) {
            ctx.getResponse().status(Status.BAD_REQUEST).send();
        } else {
            storageService
                .retrieve(id)
                .then(obj -> {
                    if (obj == null) {
                        ctx.getResponse().status(Status.NOT_FOUND).send();
                    } else {
                        ctx.render(Jackson.json(obj));
                    }
                });
        }
    }
}
