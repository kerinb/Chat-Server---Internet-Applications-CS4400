package com.company;

import java.util.List;

public class RequestTypeNode{
	private String clientName;
	private Integer joinId;
	private String chatRoomId;
	private RequestType requestType;
	private List<String> requestReceivedFromClient;
	
	public RequestTypeNode(String clientName, String chatRoomId, List<String> requestReceivedFromClient,
			RequestType requestType){
		this.clientName = clientName;
		this.requestReceivedFromClient = requestReceivedFromClient;
		this.requestType = requestType;
		this.chatRoomId = chatRoomId;
	}
	
	public String getName(){return this.clientName;}
	public void setName(String clientName){this.clientName = clientName;}

	public List<String> getRequestsReceivedFromClient(){return requestReceivedFromClient;}

	public String getChatRoomId(){return chatRoomId;}
	public void setChatRoomId(String chatRoomId){this.chatRoomId = chatRoomId;}
	
	public RequestType getRequestType(){return this.requestType;}
	public void setRequestType(RequestType requestType){this.requestType = requestType;}
	
	public Integer getJoinId(){return joinId;}
	public void setJoinId(Integer joinId){this.joinId = joinId;}
	
	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("Client Name: %s\n", this.getName()));
		stringBuilder.append(String.format("JoinId: %s\n", this.getJoinId()));
		return stringBuilder.toString();
	}
}
