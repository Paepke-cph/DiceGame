package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionHandler extends Thread {
   public final String DEBUG_EXIT_STRING = "EXIT";
    
    private CopyOnWriteArrayList<Connection> connections;
    private ConcurrentLinkedQueue<Message> messageQueue;
    
    private int port;
    private boolean isRunning;
    
    public ConnectionHandler(int port) {
        this.port = port;
        connections = new CopyOnWriteArrayList<>();
        messageQueue = new ConcurrentLinkedQueue<>();
        displayServerInfo(port);
    }
    
    public void displayServerInfo(int port) {
        System.out.println("Server Information: ");
        System.out.println("PORT:\n\t" + port);
        System.out.println("IP Address info:");
        Enumeration e = null;
        try {
           e = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException ex) {
           Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(e != null && e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                System.out.println("\t"+i.getHostAddress());
            }
        }
        System.out.println("\nServer Commands: ");
        System.out.println("exit\t\t\t:\tShuts down the server");
        System.out.println("ls\t\t\t:\tLists all the users connected");
        System.out.println("dump\t\t\t:\tDisplays all messages in the queue");
        System.out.println("drop [name of user]\t:\tDisconnects user");
        System.out.println("____________________________________________________________________________________\n");
    }
    
    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(port);
            isRunning = true;
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(isRunning) {
            try {
                if(listenerSocket != null) {
                    Connection con = new Connection(this, listenerSocket.accept(), messageQueue);
                    con.start();
                    connections.add(con);
                    System.out.println("Connection from: " + con.getIPAddress());
                }
            } catch (IOException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(listenerSocket != null)
            try {
                listenerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void dropConnection(Connection connection) {
        if(connections.contains(connection)) {
            System.out.println("Removing connection: " + connection.getIPAddress());
            System.out.println("User: " + connection.getUser().getUsername());
            connections.remove(connection);
        }
    }
    
    public RemoteUser[] getUsersAsArray() {
        RemoteUser[] users = new RemoteUser[connections.size()];
        for (int i = 0; i < users.length; i++) {
            users[i] = connections.get(i).getUser();
        }
        return users;
    }
    
    public RemoteUser[] getActiveUsersAsArray() {
        int active = 0;
        for (Connection connection : connections) {
            if(connection.getUser().isConnected())
                active++;
        }
        int index = 0;
        RemoteUser[] users = new RemoteUser[active];
        for (Connection connection : connections) {
            if(connection.getUser().isConnected()) {
                users[index] = connection.getUser();
                index++;
            }
        }
        return users;
    }
    
    public void listUsers() {
        if(!connections.isEmpty()) {
            for (Connection connection : connections) {
                System.out.println(connection.getUser());
            }
        }
        else {
            System.out.println("No users connected");
        }
    }
    
    public void messageDumpToScreen() {
        while(!messageQueue.isEmpty()) {
            Message next = messageQueue.poll();
            System.out.println(next);
        }
        System.out.println("Message queue is empty");
    }
    
    private Message getNextMessage() {
        if(!messageQueue.isEmpty()) {
            return messageQueue.poll();
        }
        else
            return null;
    }

    public boolean isReadyForGame() {
        return connections.size() >= 2;
    }

    public void cleanUp() {
        for (Connection connection : connections) {
            try {
                connection.cleanUp();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
