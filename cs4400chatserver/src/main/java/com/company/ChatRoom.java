package com.company;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChatRoom implements Comparable<ChatRoom>{
	private List<ConnectedClient>  listOfAllConnectedClients;
	private String chatRoomId;
	private Integer chatRoomRef;
	
	// ChatRoom constructor
	public ChatRoom(String RoomId, int chatRoomRef){
		chatRoomId = RoomId;
		this.chatRoomRef = chatRoomRef;
		this.listOfAllConnectedClients = new ArrayList<ConnectedClient>();
	}
	
	public List<ConnectedClient> getListOfAllConnectedClients(){return listOfAllConnectedClients;}
	public Integer getChatRoomRef(){return chatRoomRef;}
	public String getChatRoomId(){return chatRoomId;}

	public void addClientRecord(Socket socket, RequestTypeNode requestTypeNode, PrintWriter printWriter){
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket().equals(socket)){
				return;
			}
		}
		listOfAllConnectedClients.add(new ConnectedClient(socket, printWriter));
		System.out.println("Client " +requestTypeNode.getName() +  " was added to chatroom!");
	}
	
	public void removeClientRecord(Socket socket, RequestTypeNode requestTypeNode){
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket().equals(socket)){
				this.listOfAllConnectedClients.remove(connectedClient);
				System.out.println("Client " +requestTypeNode.getName() +  " was removed from chatroom!");
				return;
			}
		}
		System.out.println("Client " +requestTypeNode.getName() +  " was not part of chatroom!");
	}

	@Override
	public int compareTo(ChatRoom o) {
		if(this.getChatRoomRef() < o.getChatRoomRef()){
			return -1;
		}else if(this.getChatRoomRef() > o.getChatRoomRef()){
			return 1;
		}
		return 0;
	}
	
	public synchronized void broadcastMessageToEntireChatRoom(String messageToBroadCast){
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket() != null){
				connectedClient.getPrintWriter().print(messageToBroadCast);
				connectedClient.getPrintWriter().flush();
			}
		}
		System.out.println("Broadcasted messgae to chatroom");
	}
	
	
}
