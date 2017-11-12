package com.company;

import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient {
	private Socket socket;
	private PrintWriter printWriter; 
	
	public ConnectedClient(Socket socket, PrintWriter printWriter){
		this.socket = socket;
		this.printWriter = printWriter;
	}
	
	public PrintWriter getPrintWriter(){return this.printWriter;}
	public void setPrintWriter(PrintWriter printWriter){this.printWriter = printWriter;}
	
	public Socket getSocket(){return this.socket;}
	public void setSocket(Socket socket){this.socket = socket;}
}
