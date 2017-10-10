package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {
    private ArrayList<ClientThread> clientThreadArrayList;
    private SimpleDateFormat dateFormat; // used to time-stamp the errors I hopefully wont get....
    private int portNumber; // what port we at?
    private boolean serverOnOff; // do we kill it?
    private int clientID; // a new id for a new connection

    // constructor
    Main(int portNumber){
        clientThreadArrayList = new ArrayList<>();
        this.portNumber = portNumber;
        dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    void startServer(){
        serverOnOff = true;
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            while(serverOnOff){
                System.out.println("The Server is waiting for clients ot connect on Port Number: " + portNumber);
                Socket socket = serverSocket.accept();

                if(!serverOnOff){ // if server turns off on us
                    break;
                }
            }
        }catch (IOException IOE){
            String message  = dateFormat.format(new Date()) + " Exception occured when adding new ServerSocket: " + IOE + "\n";
            System.out.println(message);
        }
    }

    void stopServer(){
        serverOnOff = false;
    }

    private synchronized void boardcastMessages(String messageFromClient){

    }

    synchronized void clientLogOut(int idToRemove){

    }

    public static void main(String[] args){
        int portNumber = 8080; // set as a default port number
        switch (args.length){
            case 1:
                try{
                    portNumber = Integer.parseInt(args[0]);
                }catch (Exception E){
                    System.out.println("Port Number entered is invalid.");
                    System.out.println("ERROR: " + E);
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("ERROR: invalid port Number");
                return;
        }

        // create server object and start it
        Main main = new Main(portNumber);
        main.startServer();
    }


}
