package com.lemonoflime.rpbackground.modules;

import com.google.inject.AbstractModule;
import com.lemonoflime.rpbackground.handlers.CreateObjectHandler;
import com.lemonoflime.rpbackground.handlers.DeleteObjectHandler;
import com.lemonoflime.rpbackground.handlers.GetObjectHandler;
import com.lemonoflime.rpbackground.handlers.UpdateObjectHandler;
import com.lemonoflime.rpbackground.services.InMemoryStorageService;
import com.lemonoflime.rpbackground.services.StorageService;
import ratpack.func.Action;
import ratpack.handling.Chain;

@SuppressWarnings("PointlessBinding")
public class StorageModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StorageService.class).to(InMemoryStorageService.class);
        bind(GetObjectHandler.class);
        bind(CreateObjectHandler.class);
        bind(UpdateObjectHandler.class);
        bind(DeleteObjectHandler.class);
        bind(StorageHttpApi.class);
    }

    public static class StorageHttpApi implements Action<Chain> {
        @Override
        public void execute(Chain chain) {
            chain.path("config/:id?", ctx -> ctx
                .byMethod(method -> method
                    .get(GetObjectHandler.class)
                    .post(CreateObjectHandler.class)
                    .put(UpdateObjectHandler.class)
                    .delete(DeleteObjectHandler.class)
                )
            );
        }
    }
}
