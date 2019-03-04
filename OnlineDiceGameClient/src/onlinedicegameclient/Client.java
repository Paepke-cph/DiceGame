package onlinedicegameclient;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.Sender;
import net.Receiver;
import ui.Window;

public class Client{
    private Window window;
    
    private final int SERVER_PORT = 7008;
    private Socket socket;
    private Sender sender;
    private Receiver receiver;
    
    private ConcurrentLinkedQueue<String> incommingMessageQueue;
    private ConcurrentLinkedQueue<String> outgoingMessageQueue;
    private boolean isRunning = true;
    
    public Client() {
        incommingMessageQueue = new ConcurrentLinkedQueue<>();
        outgoingMessageQueue = new ConcurrentLinkedQueue<>();
        isRunning = true;
        
        Window window = new Window(this,
                incommingMessageQueue,
                outgoingMessageQueue);
    }
    
    public boolean makeConnection(String[] data) {
        boolean isConnected = false;
        try {
            socket = new Socket(data[0], SERVER_PORT);    
            sender = new Sender(data[1],
                    this,
                    new DataOutputStream(socket.getOutputStream()),
                    incommingMessageQueue,
                    outgoingMessageQueue);
            receiver = new Receiver(this,
                    new DataInputStream(socket.getInputStream()),
                    incommingMessageQueue);
            isConnected = true;
        }
        catch (UnknownHostException ex) {
            System.out.println("Could not find Host!");
        }
        catch (IOException ex) {
        }
        finally {
            return isConnected;
        }
    }
    
    public void shouldClose() {
        try {
            if(sender != null && receiver != null && socket != null) {
                sender.cleanUp();
                receiver.cleanUp();
                socket.close();                
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
    
    public static void main (String args[]) {
        Client client = new Client();
    }
}
