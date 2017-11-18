package main.java;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



public class ClientThread extends Thread {

	private static final String SPLIT_PATTERN = ": ";
	private static final String HELO_IDENTIFIER = "HELO ";
	private static final int UNDEFINED_JOIN_ID = -1;
	private static final String JOIN_CHATROOM_IDENTIFIER = "JOIN_CHATROOM: ";
	private static final String CHAT_IDENTIFIER = "CHAT: ";
	private static final String LEAVE_CHATROOM_IDENTIFIER = "LEAVE_CHATROOM: ";
	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
	private static final String CLIENT_NAME_IDENTIFIER = "CLIENT_NAME: ";
	public static final String STUDENT_ID = "14310166";

	private volatile ConnectedClient connectionObject;
	private int joinId;
	private boolean disconnected;

	public ClientThread(Socket clientSocket) {
		ErrorAndPrintHandler.printString("Creating new runnable task for client connection...");
		try {
			this.joinId = ChatServer.nextClientId.getAndIncrement();
			this.connectionObject = new ConnectedClient(clientSocket,
					new PrintWriter(clientSocket.getOutputStream(), true),
					new BufferedInputStream(clientSocket.getInputStream()), this.joinId);
			this.disconnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (!this.disconnected) {
				try {
					RequestTypeNode clientNode = packageRequestTypeNode();
					if (clientNode == null) {
						ErrorAndPrintHandler.printString(String.format("Could not process invalid request"));
						if (this.disconnected == true) {
							ErrorAndPrintHandler.printString(String
									.format("Could not process invalid request. Disconnected is true: returning..."));
							return;
						} else {
							continue;
						}
					}
					dealWithRequest(clientNode);
				} catch (Exception e) {
					if (this.disconnected == true) {
						ErrorAndPrintHandler.printString(
								"Caught exception in run method, and disconnected == true: exiting.");
						return;
					}
				}
			}
		} catch (Exception e) {
			ErrorAndPrintHandler.printString(String.format("%s", e));
			e.printStackTrace();
		} finally {
			ErrorAndPrintHandler.printString(String.format("Exiting thread %s", this.getId()));
		}
	}

	private RequestTypeNode packageRequestTypeNode() {
		try {
			List<String> receivedFromClient = getFullMessageFromClient();
			if (receivedFromClient == null) {
				ErrorAndPrintHandler.printString(String.format("Couldn't read the message sent by client %s", this.joinId));
				return null;
			}
			RequestType requestType = requestedAction(receivedFromClient);
			if (requestType == null) {
				ErrorAndPrintHandler.printString("Could not parse request type: invalid");
				return null;
			}
			return extractClientInfo(requestType, receivedFromClient);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private synchronized void dealWithRequest(RequestTypeNode clientNode) throws Exception {
		if (clientNode == null) {
			ErrorAndPrintHandler.printString("Null client node");
		}
		switch (clientNode.getRequestType()) {
		case JOIN_CHATROOM:
			joinChatroom(clientNode);
			ChatServer.addClientToServer(this.connectionObject, clientNode);
			return;
		case HELO:
			sayHello(clientNode);
			return;
		case LEAVE_CHATROOM:
			leaveChatroom(clientNode);
			return;
		case CHAT:
			chat(clientNode);
			return;
		case DISCONNECT:
			disconnect(clientNode);
			return;
		case KILL_SERVICE:
			killService(clientNode);
			return;
		default:
			ErrorAndPrintHandler.printString("Invalid request: "+clientNode);
			return;
		}
	}

	private synchronized void disconnect(RequestTypeNode clientNode) {
		this.disconnected = true;
		ErrorAndPrintHandler.printString(String.format("Disconnecting thread %s", this.getId()));
		try {
			ChatServer.removeClientFromServer(clientNode, this.connectionObject);
			this.connectionObject.getSocket().close();
			this.connectionObject.getBufferedReader().close();
			this.connectionObject.getPrintWriter().flush();
			this.connectionObject.getPrintWriter().close();
			ErrorAndPrintHandler.printString(String.format("Client %s port closed", clientNode.getName()));
		} catch (Exception e) {
			ErrorAndPrintHandler.printString("Exception occurred when trying to close the socket: " + e.getMessage());
		}
	}

	private synchronized void killService(RequestTypeNode clientNode) {
		ErrorAndPrintHandler.printString(
				String.format("Client %s requested to kill service", clientNode.getName()));
		ChatServer.setRunningValue(false);
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!ChatServer.getServerSocket().isClosed()) {
			handleRequestProcessingError(ErrorMessages.KillService, clientNode);
		}
	}

	private void handleRequestProcessingError(ErrorMessages killservice, RequestTypeNode clientNode) {
		String errorResponse = String.format(ResponceFromServer.ERROR.getValue(), killservice.getValue(),
				killservice.getDescription());
		try {
			this.connectionObject.getPrintWriter().write(errorResponse);
		} catch (Exception e) {
			String temporaryErrorMessageHolder = errorResponse;
			errorResponse = "Failed to communicate failure response to client: " + temporaryErrorMessageHolder
					+ ". Exception thrown: " + e.getMessage();
			e.printStackTrace();
		}
		ErrorAndPrintHandler.printString(errorResponse);
	}

	private void joinChatroom(RequestTypeNode clientNode) {
		String chatroomRequested = clientNode.getChatRoomId();
		ErrorAndPrintHandler.printString(
				String.format("Client %s joining chatroom %s", clientNode.getName(), chatroomRequested));
		try {
			String requestedChatroomToJoin = chatroomRequested;
			ChatRoom requestedChatroom = ChatServer
					.getChatRoomByRefIfExist(Integer.parseInt(requestedChatroomToJoin));
			if (this.joinId == UNDEFINED_JOIN_ID) {
				this.joinId = ChatServer.nextClientId.getAndIncrement();
			}

			if (requestedChatroom == null) {
				requestedChatroom = createChatroom(chatroomRequested);
				ErrorAndPrintHandler.printString(
						String.format("Chatroom %s was created!", requestedChatroom.getChatRoomId()));
				requestedChatroom.addClientRecordToChatRoom(this.connectionObject, clientNode);
				// update server records
				ChatServer.getListOfAllActiveChatRooms().add(requestedChatroom);
			} else {
				ErrorAndPrintHandler.printString(String.format("Chatroom %s already exists.. Will add client %s",
						requestedChatroom.getChatRoomId(), clientNode.getName()));
				try {
					requestedChatroom.addClientRecordToChatRoom(this.connectionObject, clientNode);
				} catch (Exception e) {
					e.printStackTrace();
					ErrorAndPrintHandler.printString(String.format("%s was already a member of %s - resending JOIN response",
							clientNode, requestedChatroom.getChatRoomId()));
					writeJoinResponseToClientAndBroadcastMessageInChatroom(requestedChatroom, clientNode);
					return;
				}
			}
			ErrorAndPrintHandler.printString(String.format("Sending join response to client %s", clientNode.getName()));
			writeJoinResponseToClientAndBroadcastMessageInChatroom(requestedChatroom, clientNode);
		} catch (Exception e) {
			e.printStackTrace();
			handleRequestProcessingError(ErrorMessages.JoinChatroom, clientNode);
		}
		ErrorAndPrintHandler.printString("Finished in join method");

	}

	private ChatRoom createChatroom(String chatroomRequested) {
		ChatRoom chatroom = new ChatRoom(chatroomRequested, ChatServer.nextChatRoomId.getAndIncrement());
		ErrorAndPrintHandler.printString(String.format("Created new chatroom %s", chatroom.getChatRoomId()));
		return chatroom;
	}

	private void writeJoinResponseToClientAndBroadcastMessageInChatroom(ChatRoom requestedChatroom,
			RequestTypeNode clientNode) {
		String responseToClient = String.format(ResponceFromServer.JOIN.getValue(), requestedChatroom.getChatRoomId(),
				ChatServer.serverIP, ChatServer.serverPort, requestedChatroom.getChatRoomRef(), this.joinId);
		writeResponseToClient(responseToClient);

		// Broadcast message in ChatRoom
		String clientJoinedChatroomMessage = String.format("%s has joined this chatroom", clientNode.getName());
		String chatMessage = String.format(ResponceFromServer.CHAT.getValue(), requestedChatroom.getChatRoomRef(),
				clientNode.getName(), clientJoinedChatroomMessage);
		requestedChatroom.broadcastMessageToEntireChatRoom(chatMessage);
	}

	private void leaveChatroom(RequestTypeNode clientNode) {
		String chatroomRequested = clientNode.getChatRoomId();
		ErrorAndPrintHandler.printString(
				String.format("Client %s leaving chatroom %s", clientNode.getName(), chatroomRequested));
		try {
			String requestedChatroomToLeave = chatroomRequested;
			ChatRoom existingChatroom = ChatServer
					.getChatRoomByRefIfExist(Integer.parseInt(requestedChatroomToLeave));

			if (existingChatroom != null) {
				// First, send leave response to client in question
				if (clientPresentInChatroom(existingChatroom)) {
					String responseToClient = String.format(ResponceFromServer.LEAVE.getValue(),
							existingChatroom.getChatRoomRef(), this.joinId);
					writeResponseToClient(responseToClient);
				}
				String clientLeftChatroomMessage = String.format("%s has left this chatroom", clientNode.getName());
				String chatMessage = String.format(ResponceFromServer.CHAT.getValue(), existingChatroom.getChatRoomRef(),
						clientNode.getName(), clientLeftChatroomMessage);

				existingChatroom.broadcastMessageToEntireChatRoom(chatMessage);

				if (clientPresentInChatroom(existingChatroom)) {
					existingChatroom.removeClientRecord(this.connectionObject, clientNode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean clientPresentInChatroom(ChatRoom existingChatroom) {
		for (ConnectedClient record : existingChatroom.getListOfAllConnectedClients()) {
			if (record.getSocket().equals(this.connectionObject.getSocket())) {
				return true;
			}
		}
		return false;
	}

	private void sayHello(RequestTypeNode clientNode) {
		ErrorAndPrintHandler.printString("Going to say hello!");
		try {
			String response = constructHelloResponse(clientNode.getRequestsReceivedFromClient());
			writeResponseToClient(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String constructHelloResponse(List<String> receivedFromClient) {
		String helloResponse = String.format(ResponceFromServer.HELO.getValue(),
				receivedFromClient.get(0).split(HELO_IDENTIFIER)[1].replaceAll("\n", ""), ChatServer.serverIP,
				ChatServer.serverPort, STUDENT_ID);
		return helloResponse;
	}

	private void chat(RequestTypeNode clientNode) throws IOException {
		String message = clientNode.getRequestsReceivedFromClient().get(3).split(SPLIT_PATTERN, 0)[1];
		ChatRoom chatroomAlreadyOnRecord = ChatServer
				.getChatRoomByIdIfExist(clientNode.getChatRoomId());
		if (chatroomAlreadyOnRecord != null) {
			String responseToClient = String.format(ResponceFromServer.CHAT.getValue(),
					chatroomAlreadyOnRecord.getChatRoomRef(), clientNode.getName(), message);
			chatroomAlreadyOnRecord.broadcastMessageToEntireChatRoom(responseToClient);
			return;
		}
		ErrorAndPrintHandler.printString(String.format("Client %s chatting in chatroom %s", clientNode.getName(),
				clientNode.getChatRoomId()));

	}

	private synchronized void writeResponseToClient(String response) {
		ErrorAndPrintHandler.printString(String.format("Writing response to client: %s", response));
		try {
			this.connectionObject.getPrintWriter().write(response);
			this.connectionObject.getPrintWriter().flush();
			ErrorAndPrintHandler.printString("Response sent to client successfully");
		} catch (Exception e) {
			e.printStackTrace();
			ErrorAndPrintHandler.printString("Failed to write response to client: " + response);
		}
	}

	public List<String> getFullMessageFromClient() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			int result = this.connectionObject.getBufferedReader().read();
			while ((result != -1) && (this.connectionObject.getBufferedReader().available() > 0)) {
				outputStream.write((byte) result);
				result = this.connectionObject.getBufferedReader().read();
			}
			// Assuming UTF-8 encoding
			String inFromClient = outputStream.toString("UTF-8");
			List<String> lines = getRequestStringAsArrayList(inFromClient);
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<String> getRequestStringAsArrayList(String inFromClient) {
		String[] linesArray = inFromClient.split("\n");
		List<String> lines = new ArrayList<String>();
		for (String line : linesArray) {
			lines.add(line);
		}
		return lines;
	}

	public RequestTypeNode extractClientInfo(RequestType requestType, List<String> message) throws IOException {
		switch (requestType) {
		case JOIN_CHATROOM:
			return new RequestTypeNode(message.get(3).split(CLIENT_NAME_IDENTIFIER, 0)[1],
					message.get(0).split(JOIN_CHATROOM_IDENTIFIER, 0)[1], message, requestType);
		case CHAT:
			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1],
					message.get(0).split(CHAT_IDENTIFIER, 0)[1], message, requestType);
		case LEAVE_CHATROOM:
			this.joinId = Integer.parseInt(message.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]);
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1],
					message.get(0).split(LEAVE_CHATROOM_IDENTIFIER, 0)[1], message, requestType);
		case DISCONNECT:
			return new RequestTypeNode(message.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1], null, message, requestType);
		case HELO:
			ErrorAndPrintHandler.printString("Helo client node created");
			return new RequestTypeNode(null, null, message, requestType);
		case KILL_SERVICE:
			return new RequestTypeNode(null, null, message, requestType);
		case Null:
			return null;
		default:
			ErrorAndPrintHandler.printString("Null clientnode created: no match with expected request types");
			return null;
		}
	}

	public RequestType requestedAction(List<String> message) throws IOException {
		String requestType = parseRequestTypeType(message);
		try {
			RequestType requestType1 = RequestType.valueOf(requestType);
			ErrorAndPrintHandler.printString("The parsed request type is " + requestType1.getValue());
			return requestType1;
		} catch (Exception e) {
			ErrorAndPrintHandler.printString("Error occurred trying to fetch the request type");
			return null;
		}
	}

	private String parseRequestTypeType(List<String> message) throws IOException {
		String[] requestType = message.get(0).split(SPLIT_PATTERN, 0);
		if (requestType[0].contains("HELO")) {
			String temp = requestType[0];
			String[] splitString = temp.split(" ", 0);
			requestType[0] = splitString[0];
		}
		ErrorAndPrintHandler.printString(String.format("Parsed request type '%s", requestType[0]));

		return requestType[0];
	}
}