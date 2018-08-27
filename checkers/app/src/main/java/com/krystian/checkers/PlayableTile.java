package com.krystian.checkers;

public class PlayableTile {
    private int value;
    private int isTaken;

    public PlayableTile(int value, int isTaken) {
        this.value = value; //1 - 50
        this.isTaken = isTaken; //0 = free, 1 = white pawn, -1 = brown pawn, 2 = white queen, -2 = brown queen
    }

    public int getValue() { return value; }
    public int getIsTaken() { return isTaken; }

    public void setValue(int value) { this.value = value; }
    public void setIsTaken(int isTaken) { this.isTaken = isTaken; }
}

