package main.java;

public enum RequestType {
	HELOP_TEXT("HELO text\n"), 
	JOIN_CHATROOM("JOIN_CHATROOM"), 
	CHAT("CHAT"), 
	LEAVE_CHATROOM("LEAVE_CHATROOM"), 
	KILL_SERVICE("KILL_SERVICE"),
	DISCONNECT("DISCONNECT"),
	HELO("HELO"),
	Null("");

	private String value;

	RequestType(final String value) {this.value = value;}
	public String getValue() {return this.value;}
}
