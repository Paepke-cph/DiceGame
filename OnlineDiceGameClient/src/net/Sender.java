package net;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.DataOutputStream;
import java.io.IOException;
import onlinedicegameclient.Client;

public class Sender extends Thread {
    public static final String HELP_TEXT = "Here are a list of commands you can execute:"
            + "\n\"lives\"\t\t:\tDisplays the current number of lives left"
            + "\n\"exit\"\t\t:\tExits the program"
            + "\n\"help\"\t\t:\tDisplays this help screen";
    
    
    private DataOutputStream outputStream;
    private boolean isRunning;
    private Scanner userInput;

    private Client client;
    private String username;
    
    private ConcurrentLinkedQueue<String> outgoingMessageQueue;
    private ConcurrentLinkedQueue<String> incommingMessageQueue;

    public Sender(String username,
            Client client,
            DataOutputStream outputStream,
            ConcurrentLinkedQueue<String> incommingMessageQueue,
            ConcurrentLinkedQueue<String> outgoingMessageQueue) {
        this.incommingMessageQueue = incommingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.outputStream = outputStream;
        this.client = client;
        this.username = username;
        userInput = new Scanner(System.in);
        isRunning = true;
        this.start();
    }

    public void cleanUp() throws IOException {
        outputStream.close();
    }
    
    @Override
    public void run() {
        try {
            outputStream.writeUTF(username);
            while(isRunning) {
                if(!outgoingMessageQueue.isEmpty()) {
                    String input = outgoingMessageQueue.poll();
                    while(input != null && input.length() > 0) {
                        if(input.equals("exit")) {
                            isRunning = false;
                            client.shouldClose();
                        }
                        else if(input.equals("help")) {
                            incommingMessageQueue.add(HELP_TEXT);
                        }
                        else {
                            outputStream.writeUTF(input);                    
                        }
                        input = "";
                    }
                }
            }
        } 
        catch (IOException ex) {		
        }
    }
}