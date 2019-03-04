package input;

import java.util.Scanner;

import net.ConnectionHandler;
import onlinedicegameserver.Main;

public class Input extends Thread{
    private Main main;
    private ConnectionHandler connectionHandler;
    private Scanner scanner;
    private boolean isRunning;
    
    public Input(Main main, ConnectionHandler connectionHandler) {
        this.main = main;
        this.connectionHandler = connectionHandler;
        scanner = new Scanner(System.in);
        isRunning = true;
        this.start();
    }
    
    @Override
    public void run() {
        while(isRunning) {
            String input = scanner.nextLine().toLowerCase();
            if(input.equals("dump")) {
                connectionHandler.messageDumpToScreen();
            }
            else if(input.equals("ls")) {
                connectionHandler.listUsers();
            }
            else if(input.split(" ").length == 2) {
                System.out.println("Not implemented yet!");
                //String[] parts = input.split(" ");
                //if(parts[0].equals("drop"))
                    //connectionHandler.removeUserByName(parts[1]);
            }
            else if(input.equals("exit")) {
                main.shouldClose();
            }
        }
    }
}
