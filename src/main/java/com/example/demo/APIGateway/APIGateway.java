package com.example.demo.APIGateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

public class APIGateway extends AbstractVerticle {
    private static final int PORT = 8079;

    private final Map<String, RoutingContext> mapReplyAPI = new HashMap<>();
    private final Map<String, String> deploymentID = new HashMap<>();
    private final Map<String, JsonObject> nameConfig = new HashMap<>();

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        vertx.eventBus().consumer("undeploy.deployment").handler(msg -> {
            JsonObject msgJson = new JsonObject(msg.body().toString());
            System.out.println("deployment " + msgJson);
            String nameVerticle = msgJson.getString("name");
            String idVerticle = msgJson.getString("id");
            deploymentID.put(nameVerticle, idVerticle);
        });

        vertx.eventBus().consumer("undeploy.mapNameConfig").handler(msg -> {
            JsonObject msgJson = new JsonObject(msg.body().toString());
            System.out.println("deployment " + msgJson);
            String nameVerticle = msgJson.getString("name");
            JsonObject config = msgJson.getJsonObject("config");
            nameConfig.put(nameVerticle, config);
        });
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        //createRouter
        router.route("/api*").handler(BodyHandler.create());
        router.post("/api/reply").handler(this::replyDemo);
        router.get("/api/undeploy").handler(this::unDeploy);
        router.get("/api/checkID").handler(this::checkID);

        vertx.createHttpServer().requestHandler(router).listen(PORT, ar -> {
            if (ar.succeeded()) {
                System.out.println("Server start on " + PORT);
            } else {
                System.out.println("Cant start server ");
            }
        });

        vertx.eventBus().consumer("rabbitMQ.fromProducerToAPI").handler(msg -> {
            JsonObject message = new JsonObject(msg.body().toString());
            String idContext = message.getString("idContext");

            mapReplyAPI.remove(idContext).response()
                    .putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(message));
        });
    }

    private void checkID(RoutingContext ctx) {
        System.out.println("deploymentID array " + deploymentID);
    }

    private void unDeploy(RoutingContext ctx) {
        String undeployName = ctx.request().params().get("name");

        vertx.undeploy(deploymentID.remove(undeployName), res -> {
            if (res.succeeded()) {
                System.out.println(undeployName + " undeploy successfully");
                DeploymentOptions options = new DeploymentOptions().setConfig(nameConfig.get(undeployName));
                vertx.deployVerticle(nameConfig.get(undeployName).getString("name"), options, res2 -> {
                    if (res2.succeeded()) {
                        JsonObject deployment = new JsonObject()
                                .put("name", undeployName)
                                .put("id", res2.result());
                        vertx.eventBus().publish("undeploy.deployment", deployment);
                        System.out.println(undeployName + " restart successfully");
                    } else {
                        System.out.println("Deployment failed");
                    }
                });
            } else {
                System.out.println("Cant deploy" + undeployName);
            }
        });
    }

    private void replyDemo(RoutingContext context) {
        int idIndex = context.toString().indexOf("@");
        String id = context.toString().substring(idIndex);
        JsonObject data = new JsonObject(context.getBody());
        data.put("idContext", id);
        System.out.println("data of Hoang" + data);
        //đưa vào HashMap
        mapReplyAPI.put(id, context);
        System.out.println(mapReplyAPI);
        // use catch map
        vertx.setTimer(5000, idTime -> {
            if (mapReplyAPI.containsKey(id)) {
                mapReplyAPI.remove(id);
                System.out.println(mapReplyAPI);
                context.response().setStatusCode(500).end();
            }
        });
        // send over event bus
        vertx.eventBus().publish("rabbitMQ.fromAPIToProducer", data);
    }

}
