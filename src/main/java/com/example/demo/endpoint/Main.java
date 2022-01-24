package com.example.demo.endpoint;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AppEndpoint());
        // get deployment ID and send to foo Verticle
    }
}
