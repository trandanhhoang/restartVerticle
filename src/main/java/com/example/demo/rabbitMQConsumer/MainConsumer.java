package com.example.demo.rabbitMQConsumer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class MainConsumer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.fileSystem().readFile("src/main/resources/config.json", result -> {
            JsonObject data = new JsonObject(result.result());
            if (result.succeeded()) {
                JsonArray lis = data.getJsonArray("deploy2");
                for(Object s : lis){
                    JsonObject innerList = new JsonObject(s.toString());
                    DeploymentOptions options = new DeploymentOptions().setConfig(innerList);
                    vertx.deployVerticle(innerList.getString("name"), options);
                }
            } else {
                System.err.println("Fail to read file" + result.cause());
            }
        });
    }
}
