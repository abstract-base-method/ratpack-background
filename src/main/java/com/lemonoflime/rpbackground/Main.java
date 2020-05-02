package com.lemonoflime.rpbackground;

import com.lemonoflime.rpbackground.modules.StorageModule;
import ratpack.guice.Guice;
import ratpack.handling.RequestLogger;
import ratpack.health.HealthCheckHandler;
import ratpack.server.RatpackServer;

public class Main {
    public static void main(String[] args) throws Exception {
        RatpackServer.start(server -> server
            .serverConfig(conf -> conf
                .sysProps()
                .env()
            )
            .registry(Guice.registry(i -> i
                .bind(HealthCheckHandler.class)
                .module(StorageModule.class)
            ))
            .handlers(chain -> chain
                .get("health", HealthCheckHandler.class)
                .all(RequestLogger.ncsa())
                .get(ctx -> ctx.render("Welcome to the demo app!"))
                .insert(StorageModule.StorageHttpApi.class)
            )
        );
    }
}
