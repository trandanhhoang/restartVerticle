package com.example.demo.message;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class MessageService extends AbstractVerticle {

    private final IMessageDao messageDao;

    public MessageService() {
        messageDao = new MessageDaoImpl();
    }

    @Override
    public void start() throws Exception {
        //get a reference to EventBus instance
        EventBus bus = vertx.eventBus();

        //assign a consumer
        bus.consumer("consumer.service.messages.getchat").handler(msg -> {
            JsonObject request = JsonObject.mapFrom(msg.body());

            System.out.println("MessageService received from EventBus: \n" + request.toString());

            Chat result = messageDao.findChatById(request.getString("chatId"));
            msg.reply(JsonObject.mapFrom(result));
        });
    }
}
