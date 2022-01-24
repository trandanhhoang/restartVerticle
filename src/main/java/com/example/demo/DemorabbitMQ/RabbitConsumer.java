package com.example.demo.DemorabbitMQ;

import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitConsumer {
    RabbitMQClient client;
    Observer observer;
    int counter = 0;
    private final String nameWorker;

    public RabbitConsumer(Vertx vertx, String nameWorker) {
        this.nameWorker = nameWorker;
        RabbitMQOptions config = new RabbitMQOptions();
        // full amqp uri
        config.setUri("amqp://localhost:5673");
        client = RabbitMQClient.create(vertx, config);
        client.queueDeclare("queue2", false, false, true);


        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("CONSUMER successfully connected!");
                client.basicConsumer("queue2", rabbitMQConsumerAsyncResult -> {
                    if (rabbitMQConsumerAsyncResult.succeeded()) {
                        System.out.println("RabbitMQ consumer created !");
                        RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                        mqConsumer.handler(message -> {
                            String msg = message.body().toString();
//                            System.out.println("Got message from : " + nameWorker + " " + msg);
                            counter++;
                            notifyObserver();
                        });
                    } else {
                        rabbitMQConsumerAsyncResult.cause().printStackTrace();
                    }
                });
            } else {
                System.out.println("CONSUMER Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    public String getNameWorker() {
        return nameWorker;
    }

    public int getCounter() {
        return counter;
    }

    public void addObserver(Observer observer) {
        this.observer = observer;
    }

    public void notifyObserver() {
        observer.update();
    }
}

