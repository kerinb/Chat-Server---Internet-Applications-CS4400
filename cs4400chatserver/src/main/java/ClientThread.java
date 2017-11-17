package main.java;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
	private static final String PATTERN_SPLITTER = ": ";
	private static final String HELO = "HELO ";
	private static final String JOIN_CHATROOM = "JOIN_CHATROOM: ";
	private static final String CHAT= "CHAT: ";
	private static final String CHATROOM_IDENTIFIER = "CHAT: ";
	private static final String LEAVE_CHATROOM = "LEAVE_CHATROOM: ";
	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
	private static final String CLIENT_NAME = "CLIENT_NAME: ";
	private static final String STUDENT_ID = "14310166"; 
	private static final int UNKNOWN_JOIN_ID = -1;

	ConnectedClient connectedClient;
	private int joinId;
	private boolean connected;
	
	public ClientThread(Socket socket){
		try{
			this.joinId = ChatServer.nextClientId.getAndIncrement();
			this.connected = true;
			this.connectedClient = new ConnectedClient(joinId, socket, new BufferedInputStream(socket.getInputStream()),
					new PrintWriter(socket.getOutputStream()));
		}catch(IOException e){
			e.printStackTrace();
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when creating new client thread");
		}
	}
	
	@Override
	public void run(){
			try{
				while(this.connected){
					try{
						RequestTypeNode requestTypeNode  = clientRequestNode();
						if(requestTypeNode == null){
							if(this.connected == false){
								ErrorAndPrintHandler.printString("Couldnt read: invalid request type given");
								return;
							}else{
								continue;
							}
						}
						handleRequestByClient(requestTypeNode);
					}catch(Exception e){
						ErrorAndPrintHandler.printError(e.getMessage(), "Exception occurered when running thread");;
					}
				}
			}catch(Exception e){
				e.getStackTrace();
			}finally{
				ErrorAndPrintHandler.printString(String.format("Thread: %s finished...", this.getId()));
			}
	}

	private RequestTypeNode clientRequestNode() {
		try{
			List<String>  messageFromClient = getEntireMessageSentByClient();
			if(messageFromClient == null){
				ErrorAndPrintHandler.printString("null message cent by cleint");
				return null;
			}
			RequestType requestType = actionRequestedByClient(messageFromClient);
			if(requestType == null){
				ErrorAndPrintHandler.printString("null message cent by cleint");
				return null;
			}
			return getInfoFromClient(requestType, messageFromClient);
			
		}catch(IOException e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when packaging client request");
			e.printStackTrace();
			return null;
		}
	}

	private synchronized void handleRequestByClient(RequestTypeNode requestTypeNode) {
		if(requestTypeNode == null){
			ErrorAndPrintHandler.printString("requestTypeNode was null: invalid value");
			return;
		}
		switch(requestTypeNode.getRequestType()){
		case JoinChatroom:
			joinChatRoom(requestTypeNode);
			ChatServer.addClientToServer(this.connectedClient, requestTypeNode);
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
			ErrorAndPrintHandler.printString(String.format("Invalid Request: will not be processed\n%s", 
					requestTypeNode.getRequestType()));
		}
	}

	private synchronized void disconnect(RequestTypeNode requestTypeNode)  {
		this.connected = false;
		ErrorAndPrintHandler.printString(String.format("Disconnecting thread %s from server......", this.getId()));
		try {
			ChatServer.removeClientFromServer(requestTypeNode, connectedClient);
			this.connectedClient.getSocket().close();
			this.connectedClient.getBufferedReader().close();
			this.connectedClient.getPrintWriter().flush();
			this.connectedClient.getPrintWriter().close();
		} catch (IOException e) {
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred When disconnecting from Server");
			e.printStackTrace();
		}
	}

	private synchronized void killService(RequestTypeNode requestTypeNode) {
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
			this.connectedClient.getPrintWriter().write(errorMessageToPrint);
			}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Couldnt communicate failed killserver error to client");
		}
	}
	
	private static void helo(RequestTypeNode requestTypeNode){
		try{
			ErrorAndPrintHandler.printString(getHelo(requestTypeNode.getRequestsReceivedFromClient()));
		}catch(Exception e){
			e.printStackTrace();
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when saying helo");
		}
	}
	
	private static String getHelo(List<String> requestsReceivedFromClient) {
		return String.format(ResponceFromServer.HELO.getValue(), requestsReceivedFromClient.get(0).split(HELO)[1].replaceAll("\n", ""),
				ChatServer.serverIP,ChatServer.serverPort,STUDENT_ID);
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
					leaveChatRoom.removeClientRecord(this.connectedClient, requestTypeNode);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean clientInChatRoom(ChatRoom chatRoom) {
		for(ConnectedClient connectedClient : chatRoom.getListOfAllConnectedClients()){
			if(connectedClient.getSocket().equals(this.connectedClient.getSocket())){
				return true;
			}
		}
		return false;
	}

	private void chat(RequestTypeNode requestTypeNode) {
		String chatMessage = requestTypeNode.getRequestsReceivedFromClient().get(3).split(PATTERN_SPLITTER, 0)[1];
		ChatRoom chatRoomOnRecord = ChatServer.getChatRoomByRefIfExist(requestTypeNode.getChatRoomId());
		if(chatRoomOnRecord == null){
			ErrorAndPrintHandler.printString("chatroom non existant...");
			return;
		}
		String responce = String.format(ResponceFromServer.CHAT.getValue(), chatRoomOnRecord.getChatRoomRef(),
				requestTypeNode.getName(), chatMessage);
		ErrorAndPrintHandler.printString(responce);
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
				requestedChatRoomToJoin = createNewChatRoom(chatRoomToJoin);
				requestedChatRoomToJoin.addClientRecord(this.connectedClient.getSocket(), requestTypeNode, this.connectedClient.getPrintWriter());
				ChatServer.getListOfAllActiveChatRooms().add(requestedChatRoomToJoin);
			}else{
				ErrorAndPrintHandler.printString(String.format("ChatRoom: %s alread exist... Adding Client: %s to chatroom", requestTypeNode.getChatRoomId()
						, requestTypeNode.getName(), requestTypeNode.getChatRoomId()));
				try{
					requestedChatRoomToJoin.addClientRecord(this.connectedClient.getSocket(), requestTypeNode, this.connectedClient.getPrintWriter());
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
			this.connectedClient.getPrintWriter().write(messageToBroadCast);
			this.connectedClient.getPrintWriter().flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private ChatRoom createNewChatRoom(String chatRoomToJoin) {
		ChatRoom chatRoom = new ChatRoom(chatRoomToJoin, ChatServer.nextChatRoomId.getAndIncrement());
		ErrorAndPrintHandler.printString("Created a new chatroom" + chatRoom.getChatRoomId());
		return chatRoom;
	}

	public RequestTypeNode getInfoFromClient(RequestType requestType, List<String> message) throws IOException {
		switch (requestType) {
		case JoinChatroom:
			return new RequestTypeNode(message.get(3).split(CLIENT_NAME, 0)[1],
					message.get(0).split(JOIN_CHATROOM, 0)[1], message, requestType);
		case Chat:
			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME, 0)[1],
					message.get(0).split(CHAT, 0)[1], message, requestType);
		case LeaveChatroom:
			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME, 0)[1],
					message.get(0).split(LEAVE_CHATROOM, 0)[1], message, requestType);
		case Disconnect:
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME, 0)[1], null, message, requestType);
		case HELO:
			ErrorAndPrintHandler.printString("Helo client node created");
			return new RequestTypeNode(null, null, message, requestType);
		case KillService:
			return new RequestTypeNode(null, null, message, requestType);
		case Null:
			return null;
		default:
			ErrorAndPrintHandler.printString("Null clientnode created: no match with expected request types");
			return null;
		}
	}
	

	private RequestType actionRequestedByClient(List<String> dataReceivedFromClient) {
		String[] request = dataReceivedFromClient.get(0).split(PATTERN_SPLITTER,0);
		if(request[0].contains(HELO)){
			String temp = request[0];
			String[] split =  temp.split(" ", 0);
			request[0] = split[0];
		}
		String val = request[0];
		RequestType requestType = RequestType.valueOf(val);
		ErrorAndPrintHandler.printString("Request Type: " + val);
		return requestType;
	}

	private List<String> getEntireMessageSentByClient() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int res = this.connectedClient.getBufferedReader().read();
			while(res != -1){
				out.write((byte) res);
				res = this.connectedClient.getBufferedReader().read();
			}
			String fromClient = out.toString("UTF-8");
			List<String> message = getAsArrayList(fromClient);
			return message;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private List<String> getAsArrayList(String fromClient) {
		String[] lines = fromClient.split("\n");
		List<String> linesToReturn = new ArrayList<String>();
		for(String line: lines){
			linesToReturn.add(line);
		}
		return linesToReturn;
	}
}