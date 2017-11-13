package com.company;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Comparable<ChatRoom>{
	private List<ConnectedClient>  listOfAllConnectedClients;
	
	private String chatRoomId;
	
	private Integer chatRoomRefNumber;
	
	// ChatRoom constructor
	public ChatRoom(String RoomId, int chatRoomRef){
		chatRoomId = RoomId;
		this.chatRoomRefNumber = chatRoomRef;
		this.listOfAllConnectedClients = new ArrayList<ConnectedClient>();
	}
	
	public List<ConnectedClient> getListOfAllConnectedClients(){return listOfAllConnectedClients;}
	public Integer getChatRoomRef(){return chatRoomRefNumber;}
	public String getChatRoomId(){return chatRoomId;}

	public void addClientRecord(Socket socket, RequestTypeNode requestTypeNode, PrintWriter printWriter){
		ErrorAndPrintHandler.printString(String.format("Adding Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket().equals(socket)){
				return;
			}
		}
		listOfAllConnectedClients.add(new ConnectedClient(chatRoomRefNumber, socket, null, printWriter));
		ErrorAndPrintHandler.printString(String.format("Added Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
	}
	
	public void removeClientRecord(ConnectedClient connectedClient2, RequestTypeNode requestTypeNode){
		ErrorAndPrintHandler.printString(String.format("Removing Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket().equals(connectedClient2)){
				this.listOfAllConnectedClients.remove(connectedClient);
				System.out.println("Client " +requestTypeNode.getName() +  " was removed from chatroom!");
				return;
			}
		}
		ErrorAndPrintHandler.printString(String.format("Removed Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));	}

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
		ErrorAndPrintHandler.printString(String.format("Preparing to broadcast %s to chatroom", messageToBroadCast));
		for(ConnectedClient connectedClient : listOfAllConnectedClients){
			if(connectedClient.getSocket() != null){
				connectedClient.getPrintWriter().print(messageToBroadCast);
				connectedClient.getPrintWriter().flush();
			}
		}
		System.out.println("Broadcasted messgae to chatroom");
	}
}