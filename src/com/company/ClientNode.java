package com.company;

import java.net.Socket;

public class ClientNode {
    private String clientName;
    private String chatRoomId;
    private Integer memberId;
    private Socket connection;

    // constructor
    public ClientNode(String clientName, String chatRoomId, Integer memberId, Socket connection){
        this.clientName = clientName;
        this.chatRoomId = chatRoomId;
        this. memberId = memberId;
        this.connection = connection;
    }

    public String getName(){return this.clientName;}
    public String getChatRoomId() {return chatRoomId;}
    public Integer getMemberId() {return memberId;}
    public Socket getConnection() {return connection;}

    public void setName(String clientName){this.clientName = clientName;}
    public void setChatRoomId(String chatRoomId) {this.chatRoomId = chatRoomId;}
    public void setMemberId(int memberId) {this. memberId = memberId;}
    public void setConnection(Socket connection) {this.connection = connection;}
}
