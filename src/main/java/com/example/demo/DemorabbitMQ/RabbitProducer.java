package com.example.demo.DemorabbitMQ;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitProducer {
    RabbitMQClient client;

    public RabbitProducer(Vertx vertx) {
        RabbitMQOptions config = new RabbitMQOptions();
        // full amqp uri
        config.setUri("amqp://localhost:5673");
        client = RabbitMQClient.create(vertx, config);

        client.addConnectionEstablishedCallback(promise -> {
            client.exchangeDeclare("exchange", "fanout", true, false)
                    .compose(v -> {
                        return client.queueDeclare("queue2", false, false, true);
                    })
                    .compose(declareOk -> {
                        return client.queueBind(declareOk.getQueue(), "exchange", "");
                    })
                    .onComplete(promise);
        });
    }

    public Future<Void> connect() {
        return client.start();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void sendMessage(String msg) {
        Buffer message = Buffer.buffer(msg);

        client.confirmSelect(confirmResult -> {
            if (confirmResult.succeeded()) {
                client.basicPublish("exchange", "", message, pubResult -> {
                    if (pubResult.succeeded()) {
                        // Check the message got confirmed by the broker.
                        client.waitForConfirms(waitResult -> {
                            if (waitResult.succeeded()) {
//                                System.out.println("Message published !");
                            } else {
                                System.out.println("check 1");
                                waitResult.cause().printStackTrace();
                            }
                        });
                    } else {
                        System.out.println("check 2");
                        pubResult.cause().printStackTrace();
                    }
                });
            } else {
                System.out.println("check 3");
                confirmResult.cause().printStackTrace();
            }
        });
    }

}

