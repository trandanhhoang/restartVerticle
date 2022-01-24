package com.example.demo.rabbitMQ;

import com.rabbitmq.client.BasicProperties;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;

public abstract class Consumer extends RabbitMQ {
    @Override
    public void start() throws Exception {
        client.start().onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("Consumer RabbitMQ successfully connected!");
                super.consume();
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    @Override
    public void produce(Buffer message) {
        BasicProperties props = super.getBasicProperties();
        client.basicPublish("", props.getReplyTo(), props, message, pubResult -> {
            if (pubResult.succeeded()) {
                //No ACK so no need confirm
                System.out.println("Message published from queue " + props.getReplyTo() + " :" + message);
            } else {
                pubResult.cause().printStackTrace();
            }
        });
    }

    public abstract void handleMess(RabbitMQMessage message, Handler<JsonObject> callback);
}
