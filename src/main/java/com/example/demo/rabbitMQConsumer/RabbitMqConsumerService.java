package com.example.demo.rabbitMQConsumer;

import com.example.demo.rabbitMQ.Consumer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;


public class RabbitMqConsumerService extends Consumer {
    String QUEUE_NAME;
    String CONFIG_RABBIT_MQ;
    @Override
    public void start() throws Exception {
        CONFIG_RABBIT_MQ = config().getString("config_rabbit_mq");
        QUEUE_NAME = config().getString("queue_name");
        createClient(CONFIG_RABBIT_MQ, vertx, QUEUE_NAME);
        super.start();
    }

    @Override
    public void handleMess(RabbitMQMessage message, Handler<JsonObject> callback) {
        System.out.println("check mess" + message.body());
        JsonObject messageJson = message.body().toJsonObject();
        String data = messageJson.getString("data");
        JsonObject response = new JsonObject()
                .put("data", "Replied from B to A :" + data)
                .put("idContext", messageJson.getString("idContext"));
        callback.handle(response);
    }
}
