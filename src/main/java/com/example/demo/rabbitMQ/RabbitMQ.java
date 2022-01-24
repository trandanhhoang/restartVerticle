package com.example.demo.rabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;

public abstract class RabbitMQ extends AbstractVerticle {
    protected RabbitMQClient client;
    private String QUEUE_NAME;
    protected BasicProperties properties;

    public void createClient(String configUri, Vertx vertx, String queueName) {
        QUEUE_NAME = queueName;
        RabbitMQOptions config = new RabbitMQOptions();
        config.setUri(configUri);
        client = RabbitMQClient.create(vertx, config);
    }

    public BasicProperties getBasicProperties() {
        return properties;
    }

    public void setBasicProperties(BasicProperties props) {
        properties = props;
    }

    public void setQueueReply(String queueReplyName) {
        properties = new AMQP.BasicProperties().builder().replyTo(queueReplyName).build();
    }

    public Future<AMQP.Queue.DeclareOk> declareQueue(String queueName) {
        return client.queueDeclare(queueName, true, true, true)
                .onSuccess(as -> System.out.println("Queue reply declared"))
                .onFailure(as -> System.out.println("Can't declare queue reply " + as.getCause()));
    }

    abstract void handleMess(RabbitMQMessage message, Handler<JsonObject> callback);

    public Future<RabbitMQConsumer> consume() {
        return client.basicConsumer(QUEUE_NAME).map(res -> {
            res.handler(msg -> {
                System.out.println("is it message " + msg.body());
                handleMess(msg, response -> {
                    Buffer replyQueueMsg = Buffer.buffer(response.toString());
                    setBasicProperties(msg.properties());
                    produce(replyQueueMsg);
                });
            });
            return res;
        }).onSuccess(ar ->
                System.out.println("Consumer sucessfully 2")
        ).onFailure(ar -> System.out.println("i;m here" + ar.getCause()));
    }

    public void produce(Buffer message) {
        client.confirmSelect(confirmResult -> {
            if (confirmResult.succeeded()) {
                //                basicPublish(String exchange, String routingKey, com.rabbitmq.client.BasicProperties properties, Buffer body,Handler handler)
                client.basicPublish("", QUEUE_NAME, properties, message, pubResult -> {
                    if (pubResult.succeeded()) {
                        client.waitForConfirms(waitResult -> {
                            if (waitResult.succeeded()) {
                                System.out.println("Producer published message !");
                            } else {
                                waitResult.cause().printStackTrace();
                            }
                        });
                    } else {
                        pubResult.cause().printStackTrace();
                    }
                });
            } else {
                confirmResult.cause().printStackTrace();
            }
        });
    }

    public RabbitMQClient getClient() {
        return client;
    }
}
