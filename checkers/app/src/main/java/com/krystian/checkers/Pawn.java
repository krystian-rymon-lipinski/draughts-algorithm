package com.krystian.checkers;

public class Pawn {
    private int position;
    private boolean isQueen;
    private boolean isWhite;

    public Pawn(int position, boolean isQueen, boolean isWhite) {
        this.position = position;
        this.isQueen = isQueen;
        this.isWhite = isWhite;
    }

    public int getPosition() { return position; }
    public boolean getIsQueen() { return isQueen; }
    public boolean getIsWhite() { return isWhite; }

    public void setPosition(int position) { this.position = position; }
    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }
    public void setIsWhite(boolean isWhite) { this.isWhite = isWhite; }
}
