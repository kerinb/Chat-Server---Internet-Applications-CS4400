package com.company;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Constant;
import org.omg.CORBA.ServerRequest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;
import java.text.SimpleDateFormat;

public class ChatRoom {
    private String chatRoomId;
    private ConcurrentSkipListSet<ClientNode> listOfClientsInChatRoom;

    // ChatRoom constructor
    public ChatRoom(String chatRoomId){
        this.chatRoomId = chatRoomId;
        this.listOfClientsInChatRoom = new ConcurrentSkipListSet<>();
    }

    public ConcurrentSkipListSet<ClientNode> getListOfClientsInChatRoom() {
        return listOfClientsInChatRoom;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void addNewClientToChatRoom(ClientNode clientNode){
        if(!listOfClientsInChatRoom.contains(clientNode)){
            listOfClientsInChatRoom.add(clientNode);
            String messageToBroadcast = String.format(ResponceFromServer.JOIN_CHATROOM.getValue(), this.chatRoomId,
                    Resources.SERVER_IP, ChatServer.getServerPortNumber(), this.chatRoomId, clientNode.getMemberId());
            broadcastMessageToChatRoom(messageToBroadcast);
        }
    }

    public void broadcastMessageToChatRoom(String messageToBroadcast){
        try {
            for (ClientNode clientNode : listOfClientsInChatRoom) {
                PrintStream broadcastStreamToAllClients = new PrintStream(clientNode.getConnection().getOutputStream());
                broadcastStreamToAllClients.print(messageToBroadcast);
            }
        }catch (IOException IOE){
            String ErrorMessage = String.format("ERROR: " + IOE + " \n OCCURRED: " + ErrorHandler.getTodaysDate());
            System.out.println(ErrorMessage);
        }
    }

    public void removeClientFromChatRoom(ClientNode clientNode) throws Exception {
        if(!listOfClientsInChatRoom.contains(clientNode)){
            listOfClientsInChatRoom.add(clientNode);
            String messageToBroadcast = String.format("Client %s has left the chat room at %s", clientNode.getName(),
                    ErrorHandler.getTodaysDate());
            broadcastMessageToChatRoom(messageToBroadcast);
        }
    }
}
