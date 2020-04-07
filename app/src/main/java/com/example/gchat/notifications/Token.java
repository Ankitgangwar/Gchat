package com.example.gchat.notifications;

public class Token {
    //fcm tpken
    String token;

    public Token() {
    }

    public Token(String token){
        this.token=token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
