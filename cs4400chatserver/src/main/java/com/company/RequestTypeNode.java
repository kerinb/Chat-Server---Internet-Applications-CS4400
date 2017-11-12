package com.company;

import java.util.List;

public class RequestTypeNode implements Comparable<RequestTypeNode>{
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
	
	public Integer getJoinId(){return joinId;}
	public void setJoinId(Integer joinId){this.joinId = joinId;}

	@Override
	public int compareTo(RequestTypeNode o) {
		if(this.getJoinId() < o.getJoinId()){
			return -1;
		}else if(this.getJoinId() > o.getJoinId()){
			return 1;
		}
		return 0;
	}
	
	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("Client Name: %s\n", this.getName()));
		stringBuilder.append(String.format("JoinId: %s\n", this.getJoinId()));
		return stringBuilder.toString();
	}
}
