package net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.concurrent.ConcurrentLinkedQueue;

import onlinedicegameclient.Client;

public class Receiver extends Thread {
    private DataInputStream inputStream;
    private Client client;
    private boolean isRunning;
    private ConcurrentLinkedQueue<String> incommingMessageQueue;

    public Receiver(Client client,
            DataInputStream inputStream,
            ConcurrentLinkedQueue<String> incommingMessageQueue) {
        this.incommingMessageQueue = incommingMessageQueue;
        this.inputStream = inputStream;
        this.client = client;
        isRunning = true;
        this.start();
    }

    public void cleanUp() throws IOException {
        inputStream.close();
    }
    
    @Override
    public void run() {
        try {
            while(isRunning) {
                String str = inputStream.readUTF();
                if(!str.equals("#CONNECTION TEST#")) {
                    incommingMessageQueue.add(str);
                }
            }
        }
        catch (EOFException ex) {
        }
        catch (IOException ex) {
        }
    }
}