package com.example.demo.DemorabbitMQ;

import io.vertx.core.Vertx;

import java.util.concurrent.atomic.AtomicInteger;

public class DemoApplication {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RabbitProducer client = new RabbitProducer(vertx);
        int numOfJobs = 100;
        int numOfFracs = 30;
        //Create new thread to send mess when connected successfully to rabbitmq
        Thread newThread = new Thread(() -> {
            while (!client.isConnected()) {
            }
            for (int i = 0; i < numOfJobs; i++) {
                client.sendMessage("EXP 2 " + i);
            }
            for (int i = 0; i < numOfFracs; i++) {
                client.sendMessage("FRAC " + i);
            }
        });
        newThread.start();

        // Check can we connect to rabbitMQ ?
        client.connect().onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });

        RabbitConsumer consumer1 = new RabbitConsumer(vertx, "worker1");
        RabbitConsumer consumer2 = new RabbitConsumer(vertx, "worker2");
        RabbitConsumer consumer3 = new RabbitConsumer(vertx, "worker3");

        // Observer to get worker counter
        AtomicInteger totalCounter = new AtomicInteger();
        Observer observerCounter = () -> {
            totalCounter.getAndIncrement();
            if (totalCounter.get() == numOfJobs) {
                System.out.println(consumer1.getNameWorker() + " " + consumer1.getCounter());
                System.out.println(consumer2.getNameWorker() + " " + consumer2.getCounter());
                System.out.println(consumer3.getNameWorker() + " " + consumer3.getCounter());
            }
        };

        consumer1.addObserver(observerCounter);
        consumer2.addObserver(observerCounter);
        consumer3.addObserver(observerCounter);
    }
}
