package net;

import game.Player;
import java.io.IOException;
import java.time.LocalTime;

public class RemoteUser {
    private static int userCount = 0;
    private final int ID;
    
    private boolean isConnected;
    private String username;
    private Connection connection;
    private Player player;
    
    public RemoteUser(Connection connection, String username) {
        ID = ++userCount;
        this.connection = connection;
        this.username = username;
        isConnected = true;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public int getLives() {
        if(player == null)
            return -1;
        else
            return player.getLives();
    }
    
    public void sendMessage(String data) {
        try {
            connection.getOutputStream().writeUTF(data);
        } catch (IOException ex) {
            isConnected = false;
        }
    }
    
    public String getDiceRollFromUser(LocalTime timeStamp) {
        String data = null;
        while(data == null) {
            Message msg = connection.getMessageByUser(timeStamp);
            if(msg != null) {
                data = msg.message;
                String[] parts = data.split(" ");
                if(parts.length == 2) {
                    try {
                        int d1 = Integer.parseInt(parts[0]);
                        int d2 = Integer.parseInt(parts[1]);
                        if((d1 > 0 && d1 < 7) && (d2 > 0 && d2 < 7)) {
                            return data;
                        }
                        else {
                            data = null;
                        }
                    }
                    catch (NumberFormatException e) {
                        data = null;
                    }
                }
                data = null;
            }
        }
        return null;
    }
    
    /**
     * Given a choice between two options the user must respond to one of them.
     * @param option1 The first option
     * @param option2 The second option
     * @return TRUE if option1 is the choice - FALSE if option2 is the choice.
     * @throws net.UserDisconnectedException
     */
    public boolean getChoiceFromUser(String option1, String option2, LocalTime timeStamp) throws UserDisconnectedException {
        String data = null;
        while(data == null || (data.equals(option1) && data.equals(option2))) {
            if(!isConnected())
                throw new UserDisconnectedException();
            Message msg = connection.getMessageByUser(timeStamp);
            if(msg != null) {
                data = msg.message;
                if(data != null && data.equals(option1))
                    return true;
                else if(data != null && data.equals(option2))
                    return false;
            } 
        }
        return false;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        sendMessage("#CONNECTION TEST#");
        return isConnected;
    }
    
    public void setConnected(boolean state) {
        isConnected = state;
    }
    
    @Override
    public String toString() {
        return ID + "] User: " + username + " | " + "Connected: " + isConnected;
    }
}
