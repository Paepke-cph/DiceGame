package net;

import java.time.LocalTime;

public class Message {
    public RemoteUser user;
    public String message;
    public LocalTime timeStamp;
    
    public Message(RemoteUser user, String message, LocalTime timeStamp) {
        this.user = user;
        this.message = message;
        this.timeStamp = timeStamp;
    }
    
    @Override
    public String toString() {
        return "Time:" + timeStamp + "- " + user.getUsername() + ": " + message;
    }
}
