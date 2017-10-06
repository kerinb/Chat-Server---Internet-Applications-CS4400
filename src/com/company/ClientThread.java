package com.company;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private ObjectOutputStream sOutput; // write on the socket
    private ObjectInputStream sInput; // read from the socket
    private Socket socket; // the socket in question

    private String server, username;
    private int portNumber;

}
