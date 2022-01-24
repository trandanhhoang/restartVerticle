package com.example.demo.message;

import java.util.ArrayList;
import java.util.List;

public class MessageDaoImpl implements IMessageDao {

    private final List<Chat> data;

    public MessageDaoImpl() {
        data = new ArrayList<>();
        data.add(new Chat("1", 5));
        data.add(new Chat("2", 2));
        data.add(new Chat("3", 5));
    }

    @Override
    public Chat findChatById(String chatId) {
        for (Chat chat : data) {
            if (chat.getChatId().equalsIgnoreCase(chatId)) {
                return chat;
            }
        }
        return null;
    }

}
