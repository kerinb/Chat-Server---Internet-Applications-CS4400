package com.company;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient {
	private int id; 
	
	private volatile Socket socket;
	private volatile PrintWriter printWriter; 
	private volatile BufferedReader bufferedReader;
	
	public ConnectedClient(int id, Socket socket, BufferedReader bufferedReader, 
			PrintWriter printWriter){
		this.id = id;
		this.socket = socket;
		this.bufferedReader = bufferedReader;
		this.printWriter = printWriter;
	}
	
	public int getId(){return this.id;}
	public void setId(int id){this.id = id;}
	
	public PrintWriter getPrintWriter(){return this.printWriter;}
	public void setPrintWriter(PrintWriter printWriter){this.printWriter = printWriter;}
	
	public Socket getSocket(){return this.socket;}
	public void setSocket(Socket socket){this.socket = socket;}
	
	public BufferedReader getBufferedReader(){return this.bufferedReader;}
	public void setBufferedReader(BufferedReader bufferedReader){this.bufferedReader = bufferedReader;}
}
