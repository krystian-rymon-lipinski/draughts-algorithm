package com.krystian.checkers;

public class Pawn {
    private int position;
    private boolean isWhite;
    private boolean isQueen;

    public Pawn(int position, boolean isWhite, boolean isQueen) {
        this.position = position;
        this.isWhite = isWhite;
        this.isQueen = isQueen;
    }

    public int getPosition() { return position; }
    public boolean getIsWhite() { return isWhite; }
    public boolean getIsQueen() { return isQueen; }

    public void setPosition(int position) { this.position = position; }
    public void setIsWhite(boolean isWhite) { this.isWhite = isWhite; }
    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }

}
