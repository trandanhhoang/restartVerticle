package com.example.demo.rabbitMQ;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;

public class Producer extends RabbitMQ {
    @Override
    public void start() throws Exception {
        client.start().onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("Producer rabbit successfully connected!");
                declareQueue("queue2.reply-to");
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    @Override
    void handleMess(RabbitMQMessage message, Handler<JsonObject> callback) {
        System.out.println(message.body());
    }

    public Future<Void> connect(){
        return client.start();
    }

    public void handleMessAfterConsume(JsonObject message,Handler<Void> handler){};

    public Future<RabbitMQConsumer> consume() {
        return client.basicConsumer(properties.getReplyTo())
                .onSuccess(ar -> {
                    System.out.println("Consumer successfully");
                })
                .onFailure(err -> System.out.println("Fail consumer" + err.getCause()));
    }

}
