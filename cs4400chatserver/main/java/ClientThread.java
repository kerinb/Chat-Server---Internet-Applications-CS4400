package main.java;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ClientThread extends Thread {
 	private static final String PATTERN_SPLITTER = ": ";
 	private static final String HELO_ID = "HELO ";
 	private static final String JOIN_CHATROOM_ID = "JOIN_CHATROOM: ";
 	private static final String CHAT_ID = "CHAT: ";
 	private static final String LEAVE_CHATROOM_ID = "LEAVE_CHATROOM: ";
 	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
 	private static final String CLIENT_NAME_ID = "CLIENT_NAME: ";
 	private static final String STUDENT_ID = "14310166"; 
 	private static final int UNKNOWN_JOIN_ID = -1;
 
 	ConnectedClient connectedClient;
 	private int joinId;
 	private boolean connected;
 	
 	public ClientThread(Socket socket){
 		try{
 			ErrorAndPrintHandler.printString("Creating a new thread....");
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
 				ErrorAndPrintHandler.printString(String.format("Running Thread: ", this.getId()));
 				while(this.connected){
 					try{
 						RequestTypeNode requestTypeNode  = clientRequestNode();
 						if(requestTypeNode == null){
 							ErrorAndPrintHandler.printString("Couldnt read: invalid request type given");
 							if(this.connected == false){
 								ErrorAndPrintHandler.printString("Connected = false...\n shutting server down....");
 								return;
 							}else{
 								continue;
 							}
 						}
 						handleRequestByClient(requestTypeNode);
 					}catch(Exception e){
 						if(this.connected == false){
 							ErrorAndPrintHandler.printString("Ecpection caught with Connected = false...\n "
 									+ "shutting server down....");
 							return;
 						}
 						ErrorAndPrintHandler.printError(e.getMessage(), "Exception occurered when running thread\n");
 						e.printStackTrace();
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
 			ErrorAndPrintHandler.printString("Message from client: " + messageFromClient + " \nCleint number: " 
 			+ this.getName());
 			if(messageFromClient == null){
 				ErrorAndPrintHandler.printString("null message cent by cleint");
 				return null;
 			}
 			ErrorAndPrintHandler.printString("Getting Request Type for request");
 			RequestType requestType = actionRequestedByClient(messageFromClient);
 			ErrorAndPrintHandler.printString("Got Request Type for request");
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
 		case JOIN_CHATROOM:
 			ErrorAndPrintHandler.printString("Calling Join function");
 			joinChatRoom(requestTypeNode);
 			ChatServer.addClientToServer(this.connectedClient, requestTypeNode);
 			break;
 		case CHAT:
 			ErrorAndPrintHandler.printString("Calling Chat function");
 			chat(requestTypeNode);
 			break;	
 		case LEAVE_CHATROOM:
 			ErrorAndPrintHandler.printString("Calling leave function");
 			leaveChatRoom(requestTypeNode);
 			break;	
 		case KILL_SERVICE:
 			ErrorAndPrintHandler.printString("Calling kill server function");
 			killService(requestTypeNode);
 			break;
 		case DISCONNECT:
 			ErrorAndPrintHandler.printString("Calling disconnect function");
 			disconnect(requestTypeNode);
 			break;
 		case HELO: 
 			ErrorAndPrintHandler.printString("Calling helo function");
 			helo(requestTypeNode);
 			break;
 		default:
 			handlerErrorProcessingRequest(ErrorMessages.InvalidRequest, requestTypeNode);
 		}
 	}
 
 	private void handlerErrorProcessingRequest(ErrorMessages invalidrequest, RequestTypeNode requestTypeNode) {
 		String errorMessage = String.format(ResponceFromServer.ERROR.getValue(), invalidrequest.getValue(),
 				invalidrequest.getDescription());
 		try{
 			this.connectedClient.getPrintWriter().write(errorMessage);
 		}catch(Exception e){
 			e.printStackTrace();
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
 		} catch (Exception e) {
 			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred When disconnecting from Server");
 			e.printStackTrace();
 		}
 	}
 
 	private synchronized void killService(RequestTypeNode requestTypeNode) {
 		ErrorAndPrintHandler.printString(String.format("Client: %s requested to kill server", requestTypeNode.getName()));
 		ChatServer.setRunningValue(false);
 		try{
 			join();// wait for thread to die... 
 		}catch(InterruptedException e){
 			e.printStackTrace();
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
 	
 	private void helo(RequestTypeNode requestTypeNode){
 		try{
 			String messageToClient = makeHeloResponce(requestTypeNode);
 			writeToClient(messageToClient);
 		}catch(Exception e){
 			e.printStackTrace();
 			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when saying helo");
 		}
 	}
 	
 	private static String makeHeloResponce(RequestTypeNode requestTypeNode) {
 		return String.format(ResponceFromServer.HELO.getValue(), requestTypeNode.getRequestsReceivedFromClient()
 				.get(0).split(HELO_ID)[1]
 				.replaceAll("\n", ""), ChatServer.serverIP, ChatServer.serverPort, STUDENT_ID);
 	}
 
 	private void leaveChatRoom(RequestTypeNode requestTypeNode) {
 		String chatRoomRequestedToLeave =  requestTypeNode.getChatRoomId();
 		ErrorAndPrintHandler.printString(String.format("Client: %s is leaving chatroom: %s\n", requestTypeNode.getName(), 
 				requestTypeNode.getChatRoomId()));
 		try{
 			ChatRoom leaveChatRoom = ChatServer.getChatRoomByRefIfExist((Integer.parseInt(chatRoomRequestedToLeave)));
 			if(leaveChatRoom != null){
 				//ErrorAndPrintHandler.printString(String.format("%s", clientInChatRoom(leaveChatRoom))); 
 				if(clientInChatRoom(leaveChatRoom)){
 					String messageToClient = String.format(ResponceFromServer.LEAVE.getValue(), 
 							leaveChatRoom.getChatRoomRef(), this.joinId);
 					ErrorAndPrintHandler.printString("message to client:" + messageToClient); 
 					writeToClient(messageToClient);
 				}
 				
 				String clientExitFromChatroom = String.format("%s has left chatroom", requestTypeNode.getName());
 				String message = String.format(ResponceFromServer.CHAT.getValue(), leaveChatRoom.getChatRoomRef(), 
 						requestTypeNode.getName(), 
 						clientExitFromChatroom);
 				leaveChatRoom.broadcastMessageToEntireChatRoom(message);
 				
 				if(clientInChatRoom(leaveChatRoom)){
 					leaveChatRoom.removeClientRecord(this.connectedClient, requestTypeNode);
 				}
 			}
 			ErrorAndPrintHandler.printString("NULL CHATROOM VALUE TO LEAVE"); 
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
 		ChatRoom chatRoomOnRecord = ChatServer.getChatRoomByRefIfExist(Integer.parseInt(requestTypeNode.getChatRoomId()));
 		if(chatRoomOnRecord != null){
 			String broadcastMEssage = String.format(ResponceFromServer.CHAT.getValue(), chatRoomOnRecord.getChatRoomRef(),
 					requestTypeNode.getName(), chatMessage);
 			chatRoomOnRecord.broadcastMessageToEntireChatRoom(broadcastMEssage);
 		}
 		ErrorAndPrintHandler.printString("chatroom non existant...");
 		return;
 	}
 	
 	private void joinChatRoom(RequestTypeNode requestTypeNode) {
 		String ChatRoomToJoin = requestTypeNode.getChatRoomId();
 		ErrorAndPrintHandler.printString(String.format("Joining ChatRoom: %s", requestTypeNode.getChatRoomId()));
 		try{
 			ChatRoom requestedChatRoomToJoin = ChatServer.getChatRoomByIdIfExist(ChatRoomToJoin);
 			if(this.joinId == UNKNOWN_JOIN_ID){
 				this.joinId = ChatServer.nextClientId.getAndIncrement();
 			}
 			
 			if(requestedChatRoomToJoin == null){
 				requestedChatRoomToJoin = createNewChatRoom(ChatRoomToJoin);
 				ErrorAndPrintHandler.printString(String.format("Created ChatRoom %s ",ChatRoomToJoin ));
 				requestedChatRoomToJoin.addClientRecordToChatRoom(this.connectedClient, requestTypeNode);
 				ChatServer.getListOfAllActiveChatRooms().add(requestedChatRoomToJoin);
 			}else{
 				ErrorAndPrintHandler.printString(String.format("ChatRoom: %s alread exist... Adding Client: %s to chatroom", 
 						requestTypeNode.getChatRoomId(), requestTypeNode.getName(), requestTypeNode.getChatRoomId()));
 				try{
 					requestedChatRoomToJoin.addClientRecordToChatRoom(this.connectedClient, requestTypeNode);
 				}catch(Exception e){
 					e.printStackTrace();
 					ErrorAndPrintHandler.printError(e.getMessage(), "Alread a member of chat room - resend join request");
 					writeJoinRequestAndBroadcast(requestedChatRoomToJoin, requestTypeNode);
 					return;
 				}
 			}
 			ErrorAndPrintHandler.printString("Sending join responce to server....");
 			writeJoinRequestAndBroadcast(requestedChatRoomToJoin, requestTypeNode);
 		}catch(Exception e){
 			ErrorAndPrintHandler.printError(e.getMessage(), "ErrorJoing Chatroom");
 			e.printStackTrace();
 		}
 	}
 
 	private void writeJoinRequestAndBroadcast(ChatRoom requestedChatRoomToJoin, 
 			RequestTypeNode requestTypeNode) {
 		String messageToClient = String.format(ResponceFromServer.JOIN.getValue(),
 				requestedChatRoomToJoin.getChatRoomId(), ChatServer.serverIP,
 				ChatServer.serverPort, requestedChatRoomToJoin.getChatRoomRef(),  this.joinId);
		writeToClient(messageToClient);
 		
 		String messageToBroadCast = String.format("%s has joined chatroom %s", requestTypeNode.getName(),
 				requestTypeNode.getChatRoomId());
 		String message = String.format(ResponceFromServer.CHAT.getValue(), requestedChatRoomToJoin.getChatRoomRef(),
 				requestTypeNode.getName(), 
 				messageToBroadCast);
 		requestedChatRoomToJoin.broadcastMessageToEntireChatRoom(message);
 	}
 
 	private synchronized void writeToClient(String messageToBroadCast) {
 		ErrorAndPrintHandler.printString("Transmitting a message client: " + messageToBroadCast);
 		try{
 			this.connectedClient.getPrintWriter().write(messageToBroadCast);
 			this.connectedClient.getPrintWriter().flush();
 			ErrorAndPrintHandler.printString("Message sent to client.....\n");
 		}catch(Exception e){
 			e.printStackTrace();
 			ErrorAndPrintHandler.printString("failed to send message to client.....\n");
 		}
 	}
 
 	private ChatRoom createNewChatRoom(String chatRoomToJoin) {
 		ChatRoom chatRoom = new ChatRoom(chatRoomToJoin, ChatServer.nextChatRoomId.getAndIncrement());
 		ErrorAndPrintHandler.printString("Created a new chatroom" + chatRoom.getChatRoomId());
 		return chatRoom;
 	}
 
 	public RequestTypeNode getInfoFromClient(RequestType requestType, List<String> message) throws IOException {
 		switch (requestType) {
 		case JOIN_CHATROOM:
 			ErrorAndPrintHandler.printString("Getting info from client - JOIN");
 			return new RequestTypeNode(message.get(3).split(CLIENT_NAME_ID, 0)[1],
 					message.get(0).split(JOIN_CHATROOM_ID, 0)[1], message, requestType);
 		case CHAT:
 			ErrorAndPrintHandler.printString("Getting info from client - CHAT");
 			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
 			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_ID, 0)[1],
 					message.get(0).split(CHAT_ID, 0)[1], message, requestType);
 		case LEAVE_CHATROOM:
 			ErrorAndPrintHandler.printString("Getting info from client - LEAVE");
 			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
 			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_ID, 0)[1],
 					message.get(0).split(LEAVE_CHATROOM_ID, 0)[1], message, requestType);
 		case DISCONNECT:
 			ErrorAndPrintHandler.printString("Getting info from client - DISCONNECT");
 			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_ID, 0)[1], null, message, requestType);
 		case HELO:
 			ErrorAndPrintHandler.printString("Helo client node created");
 			return new RequestTypeNode(null, null, message, requestType);
 		case KILL_SERVICE:
 			ErrorAndPrintHandler.printString("Getting info from client - KILL SERVER");
 			return new RequestTypeNode(null, null, message, requestType);
 		case Null:
 			ErrorAndPrintHandler.printString("Getting info from client - NULL");
 			return null;
 		default:
 			ErrorAndPrintHandler.printString("Null clientnode created: no match with expected request types");
 			return null;
 		}
 	}
 	
 
 	private RequestType actionRequestedByClient(List<String> dataReceivedFromClient) {
 		String request = parseRequestType(dataReceivedFromClient);
 		try{
 			RequestType requestType = RequestType.valueOf(request);
 			ErrorAndPrintHandler.printString("Parsed request is: " + requestType.getValue());
 			return requestType;
 		}catch(Exception e){
 			return null;
 		}
 	}
 
 	private String parseRequestType(List<String> dataReceivedFromClient) {
 		String[] requestTypeString = dataReceivedFromClient.get(0).split(PATTERN_SPLITTER, 0);
 		if(requestTypeString[0].contains("HELO")){
 			String temp = requestTypeString[0];
 			String[] splitString = temp.split(" ", 0);
 			requestTypeString[0] = splitString[0];
 		}
 		return requestTypeString[0];
 	}
 
 	private List<String> getEntireMessageSentByClient() {
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		try {
 			int result = this.connectedClient.getBufferedReader().read();
 			while ((result != -1) && (this.connectedClient.getBufferedReader().available() > 0)) {
 				outputStream.write((byte) result);
 				result = this.connectedClient.getBufferedReader().read();
 			}
 			String fromClient = outputStream.toString("UTF-8");
 			List<String> lines = getAsArrayList(fromClient);
 			return lines;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
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