package main.java;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient implements Comparable<ConnectedClient>{
	private int id; 
	private volatile Socket socket;
	private volatile PrintWriter printWriter; 
	private volatile BufferedInputStream bufferedInputStream;
	
	public ConnectedClient(int joinId, Socket socket2, BufferedInputStream bufferedInputStream, PrintWriter printWriter2){
		this.socket  = socket2;
		this.printWriter = printWriter2;
		this.bufferedInputStream = bufferedInputStream;
		this.id= joinId;
	}
	
	public int getId(){return this.id;}
	public void setId(int id){this.id = id;}
	public PrintWriter getPrintWriter(){return this.printWriter;}
	public void setPrintWriter(PrintWriter printWriter){this.printWriter = printWriter;}
	public Socket getSocket(){return this.socket;}
	public void setSocket(Socket socket){this.socket = socket;}
	public BufferedInputStream getBufferedReader(){return this.bufferedInputStream;}
	public void setBufferedReader(BufferedInputStream bufferedInputStream){this.bufferedInputStream = bufferedInputStream;}

	@Override
	public int compareTo(ConnectedClient o) {
		if(this.getId()>o.getId()){return 1;}
		else if(this.getId()< o.getId()){return -1;}
		return 0;
	}
}
