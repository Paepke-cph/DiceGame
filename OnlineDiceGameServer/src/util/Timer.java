package util;

import java.util.concurrent.TimeUnit;

public class Timer {
    private long startTime;
    
    public Timer() {
        startTime = System.nanoTime();
    }
    
    public void restartTime() {
        startTime = System.nanoTime();
    }
    
    public long elapsedSeconds() {
        long currentTime = System.nanoTime();
        long delta = currentTime - startTime;
        return TimeUnit.SECONDS.convert(delta, TimeUnit.NANOSECONDS);
    }
    
}
