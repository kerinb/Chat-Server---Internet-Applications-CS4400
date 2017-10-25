package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientThread extends Thread {
    private ClientNode clientNode;
    private int serverPortNumber;
    private RequestType requestType;
    private List<String> messagesRecievedFromClient;

    private static final String SPLIT_MESSAGE_PATTERN = ": ";
    private static final String MESSAGE_IDENTIFIER = "HELO ";

    // Constructor
    public  ClientThread(ClientNode clientNode, RequestType requestType, List<String> messagesFromClient){
        super();
        this.clientNode = clientNode;
        this.messagesRecievedFromClient = messagesFromClient;
        this.requestType = requestType;
    }

    private void joinChatRoom(){
        String chatRoomRequestedToJoin = this.clientNode.getChatRoomId();
        ChatRoom chatRoomRequested = ChatServer.getRequestedChatRoomIfIsThere(chatRoomRequestedToJoin);

        if (this.clientNode.getChatRoomId() == null){
            this.clientNode.setMemberId(ChatServer.clientId.getAndIncrement());
        }

        setChatRoomData(chatRoomRequested);

        String responseToSentToClient = String.format(ResponceFromServer.JOIN_CHATROOM.getValue(),
                this.clientNode.getChatRoomId(), 0, this.serverPortNumber, this.clientNode.getChatRoomId(),
                this.clientNode.getMemberId());
        responseToClientNode(responseToSentToClient);
        chatRoomRequested.broadcastMessageToChatRoom(
                String.format("A new client called %s has joined the chatroom!", clientNode.getName()));
    }

    private void responseToClientNode(String response){
        try {
            this.clientNode.getConnection().getOutputStream().write(response.getBytes());
        }catch (IOException IOE){
            String ErrorMessage = String.format("ERROR: " + IOE + " \n OCCURRED: " + ErrorHandler.getTodaysDate());
            System.out.println(ErrorMessage);
        }
    }

    private void setChatRoomData(ChatRoom chatRoomRequested) {
        if(chatRoomRequested!=null){
            chatRoomRequested.addNewClientToChatRoom(clientNode);
            ChatServer.getAllActiveChatRooms().get(chatRoomRequested).add(clientNode);
        }else{
            chatRoomRequested = createNewChatRoom();
            chatRoomRequested.addNewClientToChatRoom(clientNode);
            ChatServer.getAllActiveChatRooms().put(chatRoomRequested, chatRoomRequested.getListOfClientsInChatRoom());
        }
    }

    private ChatRoom createNewChatRoom(){
        return new ChatRoom(clientNode.getChatRoomId());
    }

    private void killClientService(){
        ChatServer.killChatService(new AtomicBoolean((true)));
    }
}
