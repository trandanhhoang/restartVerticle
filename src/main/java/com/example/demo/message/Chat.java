package com.example.demo.message;

public class Chat {

    private String chatId;
    private int numOfMessages;

    public Chat(String chatId, int numOfMessages) {
        this.chatId = chatId;
        this.numOfMessages = numOfMessages;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getNumOfMessages() {
        return numOfMessages;
    }

    public void setNumOfMessages(int numOfMessages) {
        this.numOfMessages = numOfMessages;
    }
}
