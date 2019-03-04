package game;

import java.time.LocalTime;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.UserDisconnectedException;

public class Meyer {
    private Random random;
    private int currentPlayerIndex;
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
    
    public static void notEnoughPlayersToPlay(Player[] players) {
        String message = "Waiting for more players to begin the game!";
        for (Player player : players) {
            player.user.sendMessage(message);
        }
    }
    
    public void introduceGame() {
        
        for (Player player : players) {
            String result = "Welcome " + player.user.getUsername()
                    + "\nThe game will begin when there are enough players connected!"; 
            player.user.sendMessage(result);
        }
    }
    
    public void play() {
        announceStartingPlayer();
        
        while(!gameDone) {
            // Roll the two die.
            DiceRoll dr = DiceRoll.rollTheDice();
            // DiceRoll which might be a fake - or the real one.
            // Check if this is the very first run of the game....
            if(DiceRoll.rolls < 1){
                try {
                    System.out.println("Only display Roll");
                    displayedDiceRoll = displayRoll(dr);
                }
                catch (UserDisconnectedException ex) {
                    gameDone = connectedPlayerCount() == 0;
                }
            }
            else {
                try {
                    if(DiceRoll.getLastDiceRoll() != null)
                        checkLastDiceRoll(displayedDiceRoll);
                                for (Player player : players) {
                    if(player != currentPlayer)
                        player.user.sendMessage("\n" + currentPlayer.user.getUsername() + " is rolling the dice!");
                    }
                    displayedDiceRoll = displayRoll(dr);                    
                    checkRollValue(currentPlayer);
                } 
                catch (UserDisconnectedException ex) {
                    gameDone = connectedPlayerCount() <= 1;
                }
            }
            gameDone = minPlayerAliveCheck();
            
            if(!gameDone) {
                setNextPlayer(false);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Meyer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }        
        announceWinner();
        DiceRoll.cleanUp();
    }
    
    private void checkLastDiceRoll(DiceRoll dr) throws UserDisconnectedException {
        // Ask if the currentPlayer wants to dispute the last DiceRoll.
        currentPlayer.user.sendMessage("\nDo you believe " + lastPlayer.user.getUsername()
                                        + "'s roll"
                                        +"\nType in: \"yes\" or \"no\": ");
        boolean wantsToBelieve = currentPlayer.user.getChoiceFromUser("yes","no",LocalTime.now());
        if(!wantsToBelieve) {
            // TRUE if last roll was not a lie - FALSE otherwise.
            boolean notLie = DiceRoll.checkRoll(dr);
            if(notLie)
                currentPlayer.reduceLife();
            else
                lastPlayer.reduceLife();
            
            for (Player player : players) {
                player.user.sendMessage("\n" + currentPlayer.user.getUsername()
                        + " doesn't believe " + lastPlayer.user.getUsername()
                        + "'s roll");
                if(notLie) {
                    player.user.sendMessage("The roll was not a lie!\n"
                    + currentPlayer.user.getUsername() + " is down to "
                    + currentPlayer.getLives() + " lives.\n");
                }
                else {
                    player.user.sendMessage("The roll was a lie!\n"
                    + lastPlayer.user.getUsername() + " is down to "
                    + lastPlayer.getLives() + " lives.\n");
                }
            }
            DiceRoll.setLastDiceRoll(null);
        }
        else {
            // If the current player doesn't want to dispute the last roll, set the last roll to be THE actual last roll.
            DiceRoll.setLastDiceRoll(dr);
        }
    }
    
    private DiceRoll displayRoll(DiceRoll dr) throws UserDisconnectedException {
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
            if(player != currentPlayer) {
                player.user.sendMessage("\n" + currentPlayer.user.getUsername()
                        + " rolled: " + dice);
            }
        }
        return dice;
    }

    private boolean checkRollValue(Player player) {
        if(DiceRoll.getLastDiceRoll() != null) {        
            if(displayedDiceRoll.compareTo(DiceRoll.getLastDiceRoll()) == -1) {
                player.reduceLife();
                for (Player pla : players) {
                    pla.user.sendMessage("The value of " + pla.user.getUsername()
                    + "'s dice roll is less than the last roll."
                    + "\n" + pla.user.getUsername() + " is down to " + pla.getLives() + " lives!");
                }
                DiceRoll.rolls = -1;
                return false;
            }
        }
        return true;
    }
    
    private int connectedPlayerCount() {
        int counter = 0;
        for (Player player : players) {
            if(player.user.isConnected()) {
                counter++;
            }
        }
        return counter;
    }
    
    private boolean minPlayerAliveCheck() {
        int counter = 0;
        for (Player player : players) {
            if(player.isAlive() && player.user.isConnected())
                counter++;
        }
        return (counter < 2);
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
    }
    
    private void announceStartingPlayer() {
        currentPlayer = players[currentPlayerIndex];
        for (Player player : players) {
            if(player == currentPlayer)
                player.user.sendMessage("\nYou are the starting player\n");
            else
                player.user.sendMessage("\nStarting player: "
                        + currentPlayer.user.getUsername() + "\n");
        }
    }
    
    private void announceWinner() {
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
    }

    private int getRandomPlayerIndex() {
        return random.nextInt(players.length);
    }
}
