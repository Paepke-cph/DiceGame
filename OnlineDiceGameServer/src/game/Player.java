package game;

import net.RemoteUser;

public class Player {
    private int lives;
    private boolean isAlive;
    
    public RemoteUser user;
    
    public Player(RemoteUser user) {
        lives = 6;
        isAlive = true;
        this.user = user;
        this.user.setPlayer(this);
    }
    
    public void reduceLife() {
        lives--;
        if(lives <= 0) {
            isAlive = false;
            user.sendMessage("You have no more lives left!"
                    + "\nThanks for playing"
                    + "\nA new game will start as soon as the current game is done!");
        }
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public int getLives() {
        return lives;
    }
    
    public static Player[] generatePlayerArray(RemoteUser[] users) {
        Player[] players = new Player[users.length];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(users[i]);
        }
        return players;
    }
}
