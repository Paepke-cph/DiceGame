package onlinedicegameserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.ConnectionHandler;
import input.Input;
import util.Timer;
import game.Meyer;
import game.Player;

public class Main {
    private final int WAIT_TIME_SECONDS = 60;
    
    private ConnectionHandler connectionHandler;
    private Input input;
    
    private static boolean isRunning = false;
    
    public Main() {
        // Setup Server access.
        connectionHandler = new ConnectionHandler(7008);
        connectionHandler.start();
        
        isRunning = true;
    }
    
    public void run() {
        // Setup server input.
        input = new Input(this, connectionHandler);
        Timer timer = new Timer();
        while(isRunning) {
            // Grab all players currently connected to the server.
            Player[] ply = Player.generatePlayerArray(connectionHandler.getUsersAsArray());
            timer.restartTime();
            if(connectionHandler.isReadyForGame()) {
                // Create the game using the ready players.
                Meyer meyer = new Meyer(ply);
                // Introduce all players to the game.
                meyer.introduceGame();
                // Start the game.
                meyer.play();            
            }
            else {
                // Announce that there are not enough players in the queue for the game.
                Meyer.notEnoughPlayersToPlay(ply);
            }
            // 20-second timer after each game as been played.
            if (timer.elapsedSeconds() < WAIT_TIME_SECONDS) {
                long delta = (WAIT_TIME_SECONDS - timer.elapsedSeconds());
                System.out.println("Waiting " + delta + " secs");
                try {
                    Thread.sleep(delta * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void shouldClose() {
        connectionHandler.cleanUp();
        System.exit(0);
    }
    
    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }
}
