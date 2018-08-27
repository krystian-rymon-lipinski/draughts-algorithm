package com.krystian.checkers;

public class Pawn {
    private int position;
    private boolean isWhite;
    private boolean isQueen;
    private int takeLength; //to check which pawn has the longest take and has to be moved
    private int oldPosition; //in multiple taking - to prevent checking take-reverse take for eternity
    private int[] firstDiagonal; //to calculate possible moves when pawn becomes queen
    private int[] secondDiagonal;

    public Pawn(int position, boolean isWhite, boolean isQueen) {
        this.position = position;
        this.isWhite = isWhite;
        this.isQueen = isQueen;
        this.takeLength = 0;
        this.oldPosition = position;
        this.firstDiagonal = null;
        this.secondDiagonal = null;
    }

    public Pawn(Pawn pawn) {
        this.position = pawn.getPosition();
        this.isWhite = pawn.getIsWhite();
        this.isQueen = pawn.getIsQueen();
        this.takeLength = pawn.getTakeLength();
        this.oldPosition = pawn.getOldPosition();
    }

    public int getPosition() { return position; }
    public boolean getIsWhite() { return isWhite; }
    public boolean getIsQueen() { return isQueen; }
    public int getTakeLength() { return takeLength; }
    public int getOldPosition() { return oldPosition; }
    public int[] getFirstDiagonal() { return firstDiagonal; }
    public int[] getSecondDiagonal() { return secondDiagonal; }

    public void setPosition(int position) { this.position = position; }
    public void setIsWhite(boolean isWhite) { this.isWhite = isWhite; }
    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }
    public void setTakeLength(int takeLength) { this.takeLength = takeLength; }
    public void setOldPosition(int possiblePosition) { this.oldPosition = possiblePosition; }
    public void setFirstDiagonal(int[] firstDiagonal) { this.firstDiagonal = firstDiagonal; }
    public void setSecondDiagonal(int[] secondDiagonal) { this.secondDiagonal = secondDiagonal; }

}
