package com.example.demo.rabbitMQProducer;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MainProducer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.fileSystem().readFile("src/main/resources/config.json", result -> {
            JsonObject data = new JsonObject(result.result());
            if (result.succeeded()) {
                JsonArray lis = data.getJsonArray("deploy2");
                for (Object s : lis) {
                    // create config
                    JsonObject innerList = new JsonObject(s.toString());
                    DeploymentOptions options = new DeploymentOptions().setConfig(innerList);
                    // deploy verticles
                    vertx.deployVerticle(innerList.getString("name"), options, res -> {
                        if (res.succeeded()) {
                            // map NAME with ID (NAME IS UN_DEPLOY_NAME IN CONFIG)-> a short name of verticle
                            JsonObject deployment = new JsonObject()
                                    .put("name",innerList.getString("un_deploy_name"))
                                    .put("id",res.result());
                            vertx.eventBus().publish("undeploy.deployment", deployment);
                            // map name with config
                            JsonObject mapNameConfig = new JsonObject()
                                    .put("name",innerList.getString("un_deploy_name"))
                                    .put("config",innerList);
                            vertx.eventBus().publish("undeploy.mapNameConfig", mapNameConfig);
                        } else {
                            System.out.println("Deployment failed");
                        }
                    });
                }
            } else {
                System.err.println("Fail to read file" + result.cause());
            }
        });
    }
}
