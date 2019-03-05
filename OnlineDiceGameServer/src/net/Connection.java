package net;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection extends Thread {
    private ConnectionHandler connectionHandler;
    private Socket connectionSocket;
    private ConcurrentLinkedQueue<Message> messageQueue;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    
    private boolean isRunning;
    private RemoteUser user;
    
    public Connection(ConnectionHandler connectionHandler, Socket connectionSocket, ConcurrentLinkedQueue<Message> messageQueue) {
        this.connectionHandler = connectionHandler;
        this.connectionSocket = connectionSocket;
        this.messageQueue = messageQueue;
        try {
            inputStream = new DataInputStream(connectionSocket.getInputStream());
            String username = inputStream.readUTF();
            user = new RemoteUser(this, username);
            user.setConnected(true);
            
            outputStream = new DataOutputStream(connectionSocket.getOutputStream());
            outputStream.writeUTF("Connected\nA Game will start when there are enough players");
            isRunning = true;
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Message getMessageByUser(LocalTime timeStamp) {
        for (Message message : messageQueue) {
            if(message.user == user) {
                messageQueue.remove(message);
                if(message.timeStamp.compareTo(timeStamp) > 0)
                    return message;
                else
                    return null;
            }
        }
        return null;
    }
    
    public String getIPAddress() {
        return connectionSocket.getInetAddress().getHostAddress();
    }
    
    public DataOutputStream getOutputStream() {
        return outputStream;
    }
    
    public DataInputStream getInputStream() {
        return inputStream;
    }
    
    public RemoteUser getUser(){
        return user;
    }
    
    @Override
    public void run() {
        while(isRunning) {
            try {
                String inputData = inputStream.readUTF();
                if(inputData == null || inputData.equals(connectionHandler.DEBUG_EXIT_STRING))
                    isRunning = false;
                else
                    if(inputData.toLowerCase().equals("lives")) {
                        int lives = user.getLives();
                        if(lives >= 0)
                            outputStream.writeUTF("You currently have " + lives + " lives left!");
                        else 
                            outputStream.writeUTF("You aren't currently conencted to a game!");
                    }
                    else
                        messageQueue.add(new Message(user,inputData,LocalTime.now()));
            
            }catch (EOFException ex) {
		if(isRunning)
                    isRunning = false;
            } catch (IOException ex) {
                connectionHandler.dropConnection(this);
                isRunning = false;
            }
        }
        user.setConnected(false);
        connectionHandler.dropConnection(this);
    }

    void cleanUp() throws IOException {
        outputStream.close();
        inputStream.close();
        connectionSocket.close();
    }
}
