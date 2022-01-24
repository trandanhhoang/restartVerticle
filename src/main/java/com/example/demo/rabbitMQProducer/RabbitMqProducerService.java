package com.example.demo.rabbitMQProducer;

import com.example.demo.rabbitMQ.Producer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class RabbitMqProducerService extends Producer {
    String QUEUE_NAME = "queue2";
    String QUEUE_NAME_REPLY = "queue2.reply-to";
    String CONFIG_RABBIT_MQ = "amqp://localhost:5673";

    @Override
    public void start() throws Exception {
        createClient(CONFIG_RABBIT_MQ, vertx, QUEUE_NAME);
        setQueueReply(QUEUE_NAME_REPLY);
        super.start();
        connect().onSuccess(foo -> {
            consume().onSuccess(ar -> {
                ar.handler(msg -> {
                    System.out.println(msg.body());
                    handlerMessageAfterReplied(new JsonObject(msg.body()));
                });
            });
        });
        // consumer data from apigateway
        vertx.eventBus().consumer("rabbitMQ.fromAPIToProducer").handler(msg -> {
            JsonObject msgJson = new JsonObject(msg.body().toString());
            Buffer message = Buffer.buffer(msgJson.toString());
            produce(message);
        });
    }

    public void handlerMessageAfterReplied(JsonObject message) {
        vertx.eventBus().publish("rabbitMQ.fromProducerToAPI", message);
    }

}
