package com.company;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientThread extends Thread {
	private ClientNode clientNode;

	private int serverPortNumber;

	private RequestType requestType;

	private List<String> messagesRecievedFromClient;

	private static final String SPLIT_MESSAGE_PATTERN = ": ";
	private static final String START_MESSAGE_IDENTIFIER = "HELO ";

	// Constructor
	public ClientThread(ClientNode clientNode, RequestType requestType, List<String> messagesFromClient) {
		super();
		this.clientNode = clientNode;
		this.messagesRecievedFromClient = messagesFromClient;
		this.requestType = requestType;
	}

	private void joinChatRoom() {
		String chatRoomRequestedToJoin = this.clientNode.getChatRoomId();
		ChatRoom chatRoomRequested = ChatServer.getRequestedChatRoomIfIsThere(chatRoomRequestedToJoin);

		if (this.clientNode.getChatRoomId() == null) {
			this.clientNode.setMemberId(ChatServer.clientId.getAndIncrement());
		}

		setChatRoomData(chatRoomRequested);

		String responseToSentToClient = String.format(ResponceFromServer.JOIN_CHATROOM.getValue(),
				this.clientNode.getChatRoomId(), 0, this.serverPortNumber, this.clientNode.getChatRoomId(),
				this.clientNode.getMemberId());
		responseToClientNode(responseToSentToClient);
		assert chatRoomRequested != null;
		chatRoomRequested.broadcastMessageToChatRoom(
				String.format("A new client called %s has joined the chatroom!", clientNode.getName()));
	}

	private void leaveCurrentChatRoom() {
		String chatRoomToLeave = this.clientNode.getChatRoomId();
		ChatRoom chatRoom = ChatServer.getRequestedChatRoomIfIsThere(chatRoomToLeave);
		if (chatRoom != null) {
			try {
				chatRoom.removeClientFromChatRoom(this.clientNode);
			} catch (Exception e) {
				ErrorHandler.printError(e.getMessage(), " occurred when trying to leave current chatroom: ");

			}
		}
		String responseForClient = String.format(ResponceFromServer.LEAVE_CHATROOM.getValue(),
				this.clientNode.getChatRoomId(), this.clientNode.getMemberId());
		responseToClientNode(responseForClient);
	}

	private void hello() {
		String response = String.format(ResponceFromServer.HELO.getValue(),
				this.messagesRecievedFromClient.get(3).split(START_MESSAGE_IDENTIFIER)[1], Resources.SERVER_IP,
				this.serverPortNumber, Resources.STUDENT_ID);
		responseToClientNode(response);
	}

	private void chat() {
		String messageToSend = this.messagesRecievedFromClient.get(3).split(SPLIT_MESSAGE_PATTERN, 0)[1];
		ChatRoom chatRoom = ChatServer.getRequestedChatRoomIfIsThere(this.clientNode.getChatRoomId());

		if (chatRoom != null) {
			String responseToSendToClients = String.format(ResponceFromServer.CHAT.getValue(), chatRoom.getChatRoomId(),
					this.clientNode.getMemberId(), this.clientNode.getName(), messageToSend);
			chatRoom.broadcastMessageToChatRoom(responseToSendToClients);
		}
	}

	private void responseToClientNode(String response) {
		try {
			this.clientNode.getConnection().getOutputStream().write(response.getBytes());
		} catch (IOException e) {
			ErrorHandler.printError(e.getMessage(), " occurred when trying to respond to client: ");
		}
	}

	private void setChatRoomData(ChatRoom chatRoomRequested) {
		if (chatRoomRequested != null) {
			chatRoomRequested.addNewClientToChatRoom(clientNode);
			ChatServer.getAllActiveChatRooms().get(chatRoomRequested).add(clientNode);
		} else {
			chatRoomRequested = createNewChatRoom();
			chatRoomRequested.addNewClientToChatRoom(clientNode);
			ChatServer.getAllActiveChatRooms().put(chatRoomRequested, chatRoomRequested.getListOfClientsInChatRoom());
		}
	}

	private ChatRoom createNewChatRoom() {
		return new ChatRoom(clientNode.getChatRoomId());
	}

	private void killClientService() {
		ChatServer.killChatService(new AtomicBoolean((true)));
	}

	private void disconnect() {
		String leaveChatRoomMessage = this.clientNode.getChatRoomId();
		ChatRoom chatRoom = ChatServer.getRequestedChatRoomIfIsThere(leaveChatRoomMessage);
		try {
			if (chatRoom == null)
				throw new AssertionError();
			chatRoom.removeClientFromChatRoom(clientNode);
		} catch (Exception e) {
			ErrorHandler.printError(e.getMessage(), " occurred when trying to disconnect: ");
		}
		ChatServer.updateClientListing(RequestType.Disconnect, clientNode);
	}

	@Override
	public void run() {
		try {
			switch (this.requestType) {
			case JoinChatroom:
				joinChatRoom();
				break;
			case HelloText:
				hello();
				break;
			case Chat:
				chat();
				break;
			case LeaveChatroom:
				leaveCurrentChatRoom();
				break;
			case Disconnect:
				disconnect();
				break;
			case KillService:
				killClientService();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			ErrorHandler.printError(e.getMessage(), " occurred when trying to run client thread: ");
		}
	}
}
