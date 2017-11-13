package com.company;

import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
	private static final String PATTERN_SPLITTER = ": ";
	private static final String HELO = "HELO ";
	private static final int UNKNOWN_JOIN_ID = -1;
	private static final String JOIN_CHATROOM = "JOIN_CHATROOM: ";
	private static final String CHATROOM_IDENTIFIER = "CHAT: ";
	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
	private static final String CLIENT_NAME = "CLIENT_NAME: ";
	private static final String STUDENT_ID = "14310166"; 


	ConnectedClient connectedClient;
	List<ChatRoom> chatRooms = null;
	private int joinId;
	private String clientName = null;
	
	public ClientThread(Socket socket){
		try{
			this.joinId = ChatServer.nextClientId.getAndIncrement();
			this.connectedClient = new ConnectedClient(joinId, socket, new BufferedInputStream(socket.getInputStream()), new PrintWriter(socket.getOutputStream()));
		}catch(IOException e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when creating new client thread");
		}
	}
	
	@Override
	public void run(){
		while(true){
			try{
				List<String> dataReceivedFromClient = getEntireMessageSentByClient(this.socket);
				RequestType requestType = actionRequestedByClient(dataReceivedFromClient);
				if(requestType == null){
					ErrorAndPrintHandler.printString("Could not parse request: Null value");
					return;
				}
				RequestTypeNode requestTypeNode = ExtractInfoFromClient(this.socket, requestType, dataReceivedFromClient);
				if(requestTypeNode == null){
					ErrorAndPrintHandler.printString("Could not parse request type: Null value");
					return;
				}
				handleRequestByClient(requestTypeNode, requestType, dataReceivedFromClient);
			}catch(Exception e){
				ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when running Client Thread");
			}
		}
	}

	private void handleRequestByClient(RequestTypeNode requestTypeNode, RequestType requestType,
			List<String> dataReceivedFromClient) {
		if(requestTypeNode == null){
			ErrorAndPrintHandler.printString("requestTypeNode was null: invalid value");
			return;
		}
		switch(requestType){
		case JoinChatroom:
			joinChatRoom(requestTypeNode);
			break;
		case Chat:
			chat(requestTypeNode);
			break;	
		case LeaveChatroom:
			leaveChatRoom(requestTypeNode);
			break;	
		case KillService:
			killService(requestTypeNode);
			break;
		case Disconnect:
			disconnect(requestTypeNode);
			break;	
		default:
			ErrorAndPrintHandler.printString(String.format("Invalid Request: will not be processed\n%s", requestType));
		}
	}

	private void disconnect(RequestTypeNode requestTypeNode) {
		String chatRoom = requestTypeNode.getChatRoomId();
		ErrorAndPrintHandler.printString(String.format("%s is disconnecting from server", requestTypeNode.getName()));
		ChatRoom chatRoomOnRecord = ChatServer.getChatRoomByIdIfExist(requestTypeNode.getChatRoomId());
		chatRoomOnRecord.removeClientRecord(socket, requestTypeNode);
	}

	private void killService(RequestTypeNode requestTypeNode) {
		ErrorAndPrintHandler.printString(String.format("Client: %s requested to kill server", requestTypeNode.getName()));
		ChatServer.setRunningValue(false);
		try{
			wait(10000);
		}catch(InterruptedException e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Issue with killing service");
		}
		if(!ChatServer.getServerSocket().isClosed()){
			handleKillServiceError(requestTypeNode, ErrorMessages.KillService);
		}
	}

	private void handleKillServiceError(RequestTypeNode requestTypeNode, ErrorMessages killservice) {
		String errorMessageToPrint = ResponceFromServer.ERROR.getValue() + killservice.getValue();
		try{
			this.socket.getOutputStream().write(errorMessageToPrint.getBytes());;
		}catch(IOException e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Couldnt communicate failed killserver error to client");
		}
	}

	private void leaveChatRoom(RequestTypeNode requestTypeNode) {
		String chatRoomRequestedToLeave =  requestTypeNode.getChatRoomId();
		ErrorAndPrintHandler.printString(String.format("Client: %s is leaving chatroom: %s\n", requestTypeNode.getName(), requestTypeNode.getChatRoomId()));
		try{
			String chatRoomToLeave = chatRoomRequestedToLeave;
			ChatRoom leaveChatRoom = ChatServer.getChatRoomByIdIfExist(chatRoomToLeave);
			if(leaveChatRoom != null){
				if(clientInChatRoom(leaveChatRoom)){
					String messageToClient = String.format("Client: %s has left chatRoom: %s", requestTypeNode.getName(), requestTypeNode.getChatRoomId());
					writeToClient(messageToClient);
				}
				String clientExitFromChatroom = String.format("%s has left chatroom\n", requestTypeNode.getName());
				String message = String.format(ResponceFromServer.CHAT.getValue(), leaveChatRoom.getChatRoomRef(), requestTypeNode.getName(), clientExitFromChatroom);
				leaveChatRoom.broadcastMessageToEntireChatRoom(message);
				
				if(clientInChatRoom(leaveChatRoom)){
					leaveChatRoom.removeClientRecord(socket, requestTypeNode);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private boolean clientInChatRoom(ChatRoom chatRoom) {
		for(ConnectedClient connectedClient : chatRoom.getListOfAllConnectedClients()){
			if(connectedClient.getSocket().equals(this.socket)){
				return true;
			}
		}
		return false;
	}

	private void chat(RequestTypeNode requestTypeNode) {
		
	}

	private void joinChatRoom(RequestTypeNode requestTypeNode) {
		String ChatRoomToJoin = requestTypeNode.getChatRoomId();
		ErrorAndPrintHandler.printString(String.format("Joining ChatRoom: %s", requestTypeNode.getChatRoomId()));
		try{
			String chatRoomToJoin = ChatRoomToJoin;
			ChatRoom requestedChatRoomToJoin = ChatServer.getChatRoomByIdIfExist(chatRoomToJoin);
			if(this.joinId == UNKNOWN_JOIN_ID){
				this.joinId = ChatServer.nextClientId.getAndIncrement();
			}
			if(requestedChatRoomToJoin == null){
				requestedChatRoomToJoin = createChatRoom(chatRoomToJoin);
				requestedChatRoomToJoin.addClientRecord(this.socket, requestTypeNode, this.printWriter);
				ChatServer.getListOfAllActiveChatRooms().add(requestedChatRoomToJoin);
			}else{
				try{
					requestedChatRoomToJoin.addClientRecord(this.socket, requestTypeNode, this.printWriter);
				}catch(Exception e){
					e.printStackTrace();
					ErrorAndPrintHandler.printError(e.getMessage(), "Alread a member of chat room - resend join request");
					writeJoinRequestAndBroadcast(requestedChatRoomToJoin, requestTypeNode);
					return;
				}
			}
			writeJoinRequestAndBroadcast(requestedChatRoomToJoin, requestTypeNode);
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "ErrorJoing Chatroom");
			e.printStackTrace();
		}
	}

	private void writeJoinRequestAndBroadcast(ChatRoom requestedChatRoomToJoin, RequestTypeNode requestTypeNode) {
		String messageToClient = String.format(ResponceFromServer.JOIN.getValue(), requestedChatRoomToJoin.getChatRoomId(), ChatServer.getServerIp(),
				ChatServer.getServerPort(), requestedChatRoomToJoin.getChatRoomRef(),  this.joinId);
		writeToClient(messageToClient);
		
		String messageToBroadCast = String.format("Client: %s jhas joined the chatroom %s\n", requestTypeNode.getName(), requestTypeNode.getChatRoomId());
		String message = String.format(ResponceFromServer.CHAT.getValue(), requestedChatRoomToJoin.getChatRoomRef(), requestTypeNode.getName(), messageToBroadCast);
		requestedChatRoomToJoin.broadcastMessageToEntireChatRoom(message);
	}

	private void writeToClient(String messageToBroadCast) {
		try{
			this.printWriter.print(messageToBroadCast);
			this.printWriter.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private ChatRoom createChatRoom(String chatRoomToJoin) {
		ChatRoom chatRoom = new ChatRoom(chatRoomToJoin, ChatServer.nextChatRoomId.getAndIncrement());
		ErrorAndPrintHandler.printString("Created a new chatroom" + chatRoom.getChatRoomId());
		return chatRoom;
	}

	private RequestTypeNode ExtractInfoFromClient(Socket socket, RequestType requestType,
			List<String> dataReceivedFromClient) {
		switch(requestType){
		case JoinChatroom:
			return new RequestTypeNode(dataReceivedFromClient.get(3).split(CLIENT_NAME, 0)[1], dataReceivedFromClient.get(0).split(JOIN_CHATROOM, 0)[1],
					dataReceivedFromClient, requestType);
			break;
		case Chat:
			return new RequestTypeNode(clientName, joinId, clientName);
		case LeaveChatroom:
			return new RequestTypeNode(clientName, joinId, clientName);
		case KillService:
			return new RequestTypeNode(clientName, joinId, clientName);
		case Disconnect:
			return new RequestTypeNode(clientName, joinId, clientName);
		default:
			ErrorAndPrintHandler.printString(String.format("Invalid Request: will not be processed\n%s", requestType));
			return null;
	}

	private RequestType actionRequestedByClient(List<String> dataReceivedFromClient) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<String> getEntireMessageSentByClient(Socket socket2) {
		// TODO Auto-generated method stub
		return null;
	}
}