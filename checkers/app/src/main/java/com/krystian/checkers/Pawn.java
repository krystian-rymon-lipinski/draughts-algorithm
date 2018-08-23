package com.krystian.checkers;

public class Pawn {
    private int position;
    private boolean isWhite;
    private boolean isQueen;
    private int takeLength; //to check which pawn has the longest take and has to be moved
    private int possiblePosition;

    public Pawn(int position, boolean isWhite, boolean isQueen) {
        this.position = position;
        this.isWhite = isWhite;
        this.isQueen = isQueen;
        this.takeLength = 0;
        this.possiblePosition = position;
    }

    public int getPosition() { return position; }
    public boolean getIsWhite() { return isWhite; }
    public boolean getIsQueen() { return isQueen; }
    public int getTakeLength() { return takeLength; }
    public int getPossiblePosition() { return possiblePosition; }

    public void setPosition(int position) { this.position = position; }
    public void setIsWhite(boolean isWhite) { this.isWhite = isWhite; }
    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }
    public void setTakeLength(int takeLength) { this.takeLength = takeLength; }
    public void setPossiblePosition(int possiblePosition) { this.possiblePosition = possiblePosition; }

}
