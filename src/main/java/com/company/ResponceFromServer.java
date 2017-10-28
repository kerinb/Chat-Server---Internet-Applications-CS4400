package com.company;

public enum ResponceFromServer {

    JOIN_CHATROOM("JOINED_CHATROOM: %s\n" + "SERVER_IP: %s\n" + "PORT: %s\n" + "ROOM_REF: %s\n" + "JOIN_ID: %s"),
    LEAVE_CHATROOM("LEFT_CHATROOM: %s\n" + "JOIN_ID: %s"),
    CHAT("CHAT: %s\n" + "JOIN_ID: %s\n" + "CLIENT_NAME: %s\n" + "MESSAGE: %s\n\n"),
    DISCONNECT("DISCONNECT: %s\n" + "PORT: %s\n" + "CLIENT_NAME: %s"),
    HELO("HELO %s\n" + "IP: %s\n" + "Port: %s\n" + "StudentID: %s");

    private String value;

    ResponceFromServer(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
