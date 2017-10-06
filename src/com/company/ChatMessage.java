package com.company;

import java.io.Serializable;

public class ChatMessage implements Serializable{
    private String clientMessage;
    private final int MESSAGE = 0, LOGOUT = 1, INCHATROOM = 2;
    private int type;

    // Constructor
    ChatMessage(int type, String message){
        this.clientMessage = message;
        this.type = type;
    }

    // getters
    public int getType(){ return type; }
    public String getClientMessage(){ return clientMessage; }
}
