package com.example.demo.rabbitMQProducer;

import com.example.demo.rabbitMQ.Producer;
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

public class RabbitMqProducerService extends Producer {
    private static final int PORT = 8079;
    String QUEUE_NAME = "queue2";
    String QUEUE_NAME_REPLY = "queue2.reply-to";
    String CONFIG_RABBIT_MQ = "amqp://localhost:5673";
    Context context;

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

        createClient(CONFIG_RABBIT_MQ, vertx, QUEUE_NAME);
        setQueueReply(QUEUE_NAME_REPLY);
        super.start();
        connect().onSuccess(foo -> {
            consume().onSuccess(ar -> {
                ar.handler(msg -> {
                    System.out.println(msg.body());
                    handlerMessage(new JsonObject(msg.body()));
                });
            });
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
                vertx.deployVerticle(nameConfig.get(undeployName).getString("name"),options, res2 -> {
                    if (res2.succeeded()) {
                        JsonObject deployment = new JsonObject()
                                .put("name",undeployName)
                                .put("id",res2.result());
                        vertx.eventBus().publish("undeploy.deployment", deployment);
                        System.out.println(undeployName + " restart successfully");
                    } else {
                        System.out.println("Deployment failed");
                    }
                });
            }else{
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

        sendMessage(data);


    }

    public void handlerMessage(JsonObject message) {
        String idContext = message.getString("idContext");
        mapReplyAPI.remove(idContext).response()
                .putHeader("content-type", "application/json;charset=UTF-8")
                .end(Json.encodePrettily(message));
    }


    public void sendMessage(JsonObject msg) {
        Buffer message = Buffer.buffer(msg.toString());
        produce(message);
    }
}
