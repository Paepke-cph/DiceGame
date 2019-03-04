package game;

import java.util.Random;

public class DiceRoll implements Comparable<DiceRoll> {
    private static Random rnd = new Random(System.currentTimeMillis());

    private int d1,d2;
    private int value;
    
    private static DiceRoll lastDiceRoll = null;
    private static DiceRoll currentDiceRoll = null;
    
    private DiceRoll() {
        this.d1 = randomDiceInt();
        this.d2 = randomDiceInt();
        value = evaluateDiceRollValue();
    }
    
    public DiceRoll(String lie) {
        String[] str = lie.split(" ");
        this.d1 = Integer.parseInt(str[0]);
        this.d2 = Integer.parseInt(str[1]);
        value = evaluateDiceRollValue();
    }

    public static DiceRoll rollTheDice() {
        lastDiceRoll = currentDiceRoll;
        currentDiceRoll = new DiceRoll();
        return currentDiceRoll;
    }
    
    public static DiceRoll getLastRoll() {
        return lastDiceRoll;
    }
    
    public static DiceRoll getCurrentRoll() {
        return currentDiceRoll;
    }
    
    public static void setCurrentRoll(DiceRoll dr) {
        currentDiceRoll = dr;
    }
    
    public static void disableCurrentRoll() {
        currentDiceRoll = null;
    }

    
    public int getValue() {
        return value;
    }
    
    /**
     * Check if the dice roll is actually the last rolled dice or if it is faked.
     * @param dr
     * @return TRUE if the diceroll is NOT fake - FALSE If the DiceRoll is fake.
     */
    
    public static boolean checkRoll(DiceRoll dr) {
        return dr == lastDiceRoll;
    }
    
    private int evaluateDiceRollValue() {
        int temp_d1 = d1;
        d1 = Math.max(d1, d2);
        d2 = Math.min(temp_d1, d2);
        String temp = d1 +""+ d2;
        int sum = Integer.parseInt(temp);
        // par.
        if(d1 == d2) {
            sum += 100;
        }
        // Meyer
        else if(d1 == 2 && d2 == 1) {
           sum += 300;
        }
        // Lille Meyer
        else if(d1 == 3 && d2 == 1) {
            sum += 200;
        }
        return sum;
    }
    public static void cleanUp() {
        currentDiceRoll = null;
        lastDiceRoll = null;
    }
    
    private static int randomDiceInt() {
        return (rnd.nextInt(6) + 1);
    }
    
    @Override
    public String toString() {
        return d1 + " " + d2;
    }


    @Override
    public int compareTo(DiceRoll o) {
        if (value < o.getValue()) return -1;
        else if (value > o.getValue()) return 1;
        else return 0;
    }
}
