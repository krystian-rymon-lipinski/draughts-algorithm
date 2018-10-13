package com.krystian.checkers.AI_algorithm;

import com.krystian.checkers.gameMechanics.Pawn;

import java.util.ArrayList;

public class GameNode {
    private Pawn pawn; //which pawn has been moved
    public ArrayList<Integer> moveList; //list of moves to get to considered board state
    private boolean isThereTaking; //was there taking after moves from moveList (is there a need to restore pawn(s))
    private boolean canWhiteTakeAfter;
    private int lengthOfWhiteTaking; //

    public GameNode(Pawn pawn, ArrayList<Integer> moveList, boolean isThereTaking) {
        this.pawn = pawn;
        this.moveList = moveList;
        this.isThereTaking = isThereTaking;
    }

    public GameNode(GameNode node) {
        this.pawn = node.getPawn();
        this.moveList = node.moveList;
        this.isThereTaking = node.getIsThereTaking();
    }

    public Pawn getPawn() {return pawn;}
    public boolean getIsThereTaking() { return isThereTaking; }
    public boolean getCanWhiteTakeAfter() { return canWhiteTakeAfter; }
    public int getLengthOfWhiteTaking() { return lengthOfWhiteTaking; }

    public void setPawn(Pawn pawn) {this.pawn= pawn;}
    public void setCanWhiteTakeAfter(boolean canWhiteTakeAfter) { this.canWhiteTakeAfter = canWhiteTakeAfter; }
    public void setLengthOfWhiteTaking(int lengthOfWhiteTaking) { this.lengthOfWhiteTaking = lengthOfWhiteTaking; }
}


