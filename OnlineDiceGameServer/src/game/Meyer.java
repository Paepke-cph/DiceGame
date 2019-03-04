package game;

import java.time.LocalTime;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.UserDisconnectedException;

public class Meyer {
    private Random random;
    //private RemoteUser[] users;
    private int currentPlayerIndex = -1;
    private boolean gameDone;
    
    private Player[] players;
    private Player currentPlayer;
    private Player lastPlayer;
    
    // Die to be referenced, when check if the last roll is a lie or not.
    private DiceRoll displayedDiceRoll;
    
    
    public Meyer(Player[] players) {
        random = new Random(System.currentTimeMillis());
        this.players = players;
        currentPlayerIndex = getRandomPlayerIndex();
        currentPlayer = null;
        displayedDiceRoll = null;
        
        gameDone = false;
    }
    
    public void introduceGame() {
        
        for (Player player : players) {
            String result = "Welcome " + player.user.getUsername()
                    + "\nThe game will begin when there are enough players connected!"; 
            player.user.sendMessage(result);
        }
    }
    
    public static void notEnoughPlayersToPlay(Player[] players) {
        String message = "Waiting for more players to begin the game!";
        for (Player player : players) {
            player.user.sendMessage(message);
        }
    }
    
    public void play() {
        // STARTING PHASE
        // Sets the current Player.
        currentPlayer = players[currentPlayerIndex];
        String messageToAllPlayers = "\nStarting player: " + currentPlayer.user.getUsername() + "\n";
        for (Player player : players) {
            if(player == player) {
                player.user.sendMessage("\nYou are the starting player\n");
            }
            else {
                player.user.sendMessage(messageToAllPlayers);
            }
        }
        
        // MAIN GAME LOOP!
        while(!gameDone) {
            // Roll the two die.
            DiceRoll dr = DiceRoll.rollTheDice();
            // DiceRoll which might be a fake - or the real one.
            // Check if this is the very first run of the game....
            if(DiceRoll.getLastRoll() == null){
                try {    
                    displayedDiceRoll = displayRoll(dr);
                }
                catch (UserDisconnectedException ex) {
                    // If a player disconnects, try to ignore him and jump to the next player.
                    int counter = 0;
                    for (Player player : players) {
                        if(player.user.isConnected()) {
                            counter++;
                        }
                    }
                    if(counter == 0)
                        gameDone = true;
                }
            }
            else {
                try {
                    if(checkRollValue(lastPlayer))
                        checkLastRoll(displayedDiceRoll);

                    displayedDiceRoll = displayRoll(dr);                    
                    checkRollValue(currentPlayer);
                } 
                catch (UserDisconnectedException ex) {
                    // If a player disconnects, try to ignore him and jump to the next player.
                    int counter = 0;
                    for (Player player : players) {
                        if(player.user.isConnected()) {
                            counter++;
                        }
                    }
                    if(counter == 0)
                        gameDone = true;
                }
            }
            // Check how many players are alive!
            if(getNumberOfAlivePlayers() < 2) {
                gameDone = true;
                break;
            }
            // Advance to the next player in the array
            setNextPlayer(false);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Meyer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Announce Winner of the game.
        Player winner;
        for (Player player : players) {
            if(player.isAlive())
                winner = player;
        }
        for (Player player : players) {
            player.user.sendMessage("Congratulations "
                    + player.user.getUsername()
                    + "\n YOU HAVE WON THE GAME!");
        }
        
        DiceRoll.cleanUp();
    }
    
    public boolean checkRollValue(Player player) {
        if(displayedDiceRoll.compareTo(DiceRoll.getLastRoll()) == -1) {
            player.reduceLife();
            for (Player pla : players) {
                pla.user.sendMessage("The value of " + pla.user.getUsername()
                + "'s dice roll is less than the last roll."
                + "\n" + pla.user.getUsername() + " is down to " + pla.getLives() + " lives!");
            }
            DiceRoll.disableCurrentRoll();
            return false;
        }
        return true;
    }
    
    public int getNumberOfAlivePlayers() {
        int counter = 0;
        for (Player player : players) {
            if(player.isAlive() && player.user.isConnected())
                counter++;
        }
        return counter;
    }
    
    public void checkLastRoll(DiceRoll dr) throws UserDisconnectedException {
        // Check if value is greater than last roll.
        System.out.println(currentPlayer.user.getUsername());
        currentPlayer.user.sendMessage("Do you believe " + lastPlayer.user.getUsername()
                                        + "'s roll"
                                        +"\nType in: \"yes\" or \"no\": ");
        boolean wantsToBelieve = currentPlayer.user.getChoiceFromUser("yes","no",LocalTime.now());
        if(!wantsToBelieve) {
            boolean notLie = DiceRoll.checkRoll(dr);
            int counter = 0;
            for (Player player : players) {
                player.user.sendMessage(player.user.getUsername()
                        + " doesn't believe " + lastPlayer.user.getUsername()
                        + "'s roll");
                if(notLie) {
                    // We don't want to reduce the life each time we send a message.
                    // TODO(Benjamin): Clean this up!!!
                    if(counter == 0) {
                        player.reduceLife();
                        counter++;
                    }
                    player.user.sendMessage("The roll was not a lie!\n"
                    + player.user.getUsername() + " is down to "
                    + player.getLives() + " lives.\n");
                }
                else {
                    // We don't want to reduce the life each time we send a message.
                    // TODO(Benjamin): Clean this up!!!
                    if(counter == 0) {
                        lastPlayer.reduceLife();
                        counter++;
                    }
                    player.user.sendMessage("The roll was a lie!\n"
                    + lastPlayer.user.getUsername() + " is down to "
                    + lastPlayer.getLives() + " lives.\n");
                }
            }
        }
        else {
            DiceRoll.setCurrentRoll(dr);
        }
    }
    
    public DiceRoll displayRoll(DiceRoll dr) throws UserDisconnectedException {
        DiceRoll dice;
        currentPlayer.user.sendMessage("\nYour roll:\n" 
                + dr.toString()
                + "\nDo you want to lie about your roll?"
                + "\nType in \"yes\" or \"no\": ");
        boolean wantsToLie = currentPlayer.user.getChoiceFromUser("yes", "no", LocalTime.now());
        if(wantsToLie) {
            // Point dice to the faked roll.
            currentPlayer.user.sendMessage("Please enter your lie using the format [first number] [seconds number]"
                    + "\n without the [ and ]");
            String lie = currentPlayer.user.getDiceRollFromUser(LocalTime.now());
            dice = new DiceRoll(lie);
            System.out.println(dice);
        }
        else {
            // Point dice to the real roll.
            dice = dr;
        }
        // Inform all players about the roll.
        for (Player player : players) {
            if(player != player) {
                player.user.sendMessage("\n" + player.user.getUsername()
                        + " rolled: " + dice);
            }
        }
        return dice;
    }
    
    private void setNextPlayer(boolean dcDetected) {
        if(!dcDetected)
            lastPlayer = currentPlayer;
        currentPlayerIndex++;
        if(currentPlayerIndex % (players.length) == 0) {
            currentPlayerIndex = 0;
        }
        currentPlayer = players[currentPlayerIndex];
        // If the new currentPlayer is not alive, move on to the next one.
        if(!currentPlayer.isAlive() || !currentPlayer.user.isConnected())
            setNextPlayer(true);
        System.out.println("Current Player: " + currentPlayer.user.getUsername());
    }
    
    private int getRandomPlayerIndex() {
        return random.nextInt(players.length);
    }
}
