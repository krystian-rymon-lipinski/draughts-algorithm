package com.krystian.checkers;

public class PlayableTile {
    private int value;
    private int isTaken;

    public PlayableTile(int value, int isTaken) {
        this.value = value;
        this.isTaken = isTaken;
    }

    public int getValue() { return value; }
    public int getIsTaken() { return isTaken; }

    public void setValue(int value) { this.value = value; }
    public void setIsTaken(int isTaken) { this.isTaken = isTaken; }
}

