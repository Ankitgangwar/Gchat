package com.example.gchat.models;

public class ModelChat {
    String message,receiver,sender,timestamp;
    boolean mstatus;

    public ModelChat() {
    }

    public ModelChat(String message, String receiver, String sender, String timestamp, boolean mstatus) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.mstatus = mstatus;
    }

    public ModelChat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMstatus() {
        return mstatus;
    }

    public void setMstatus(boolean mstatus) {
        this.mstatus = mstatus;
    }
}
