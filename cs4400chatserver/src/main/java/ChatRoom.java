package src.main.java;

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

	public void addClientRecordToChatRoom(ConnectedClient connectedClient, RequestTypeNode requestTypeNode){
		ErrorAndPrintHandler.printString(String.format("Adding Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
		for(ConnectedClient connectedClient1 : listOfAllConnectedClients){
			if(connectedClient1 == connectedClient){
				return;
			}
		}
		listOfAllConnectedClients.add(new ConnectedClient(chatRoomRefNumber, connectedClient.getSocket(), null, connectedClient.getPrintWriter()));
		ErrorAndPrintHandler.printString(String.format("Added Client: %s to chatRoom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
	}
	
	public void removeClientRecord(ConnectedClient connectedClient2, RequestTypeNode requestTypeNode) throws Exception{
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
				try{
					connectedClient.getPrintWriter().print(messageToBroadCast);
					connectedClient.getPrintWriter().flush();
				}catch(Exception e){
					ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when trying to broadcast message to chatrom");
					e.printStackTrace();
				}
			}
		}
		System.out.println("Broadcasted messgae to chatroom");
	}
}