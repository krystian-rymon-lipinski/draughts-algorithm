package com.krystian.checkers;

public class Pawn {
    private int position;
    private boolean isWhite;
    private boolean isQueen;
    private int[] firstDiagonal; //to calculate possible and mandatory moves when pawn becomes queen
    private int[] secondDiagonal;

    private DecisionTree pawnTree; //to check every legal branch of taking to choose the longest one

    public Pawn(int position, boolean isWhite, boolean isQueen) {
        this.position = position;
        this.isWhite = isWhite;
        this.isQueen = isQueen;
        this.firstDiagonal = null;
        this.secondDiagonal = null;
    }

    public Pawn(Pawn pawn) {
        this.position = pawn.getPosition();
        this.isWhite = pawn.getIsWhite();
        this.isQueen = pawn.getIsQueen();
    }

    public int getPosition() { return position; }
    public boolean getIsWhite() { return isWhite; }
    public boolean getIsQueen() { return isQueen; }
    public int[] getFirstDiagonal() { return firstDiagonal; }
    public int[] getSecondDiagonal() { return secondDiagonal; }
    public DecisionTree getPawnTree() { return pawnTree; }

    public void setPosition(int position) { this.position = position; }
    public void setIsWhite(boolean isWhite) { this.isWhite = isWhite; }
    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }
    public void setFirstDiagonal(int[] firstDiagonal) { this.firstDiagonal = firstDiagonal; }
    public void setSecondDiagonal(int[] secondDiagonal) { this.secondDiagonal = secondDiagonal; }
    public void setPawnTree(DecisionTree pawnTree) { this.pawnTree = pawnTree; }

}
