package com.krystian.checkers.AI_algorithm;

import com.krystian.checkers.gameMechanics.Pawn;
import com.krystian.checkers.gameMechanics.PlayableTile;
import java.util.ArrayList;

public class GameTree {
    private PlayableTile[] boardState = new PlayableTile[50];
    public ArrayList<Pawn> whiteState = new ArrayList<>();
    public ArrayList<Pawn> brownState = new ArrayList<>();

    public ArrayList<GameNode> gameNodeList = new ArrayList<>(); //all found nodes
    public ArrayList<GameNode> bestNodeList = new ArrayList<>(); //just the best ones
    private GameNode currentNode = null;
    private boolean allNodesFound = false;

    public ArrayList<Pawn> pawnToRestore = new ArrayList<>(); //if there was taking in gameNode, but there are others to check
    public ArrayList<Integer> restoringPawnIndex = new ArrayList<>();

    public GameTree(PlayableTile[] currentBoard, ArrayList<Pawn> whitePawn, ArrayList<Pawn> brownPawn) {
        this.boardState = currentBoard;
        this.whiteState = whitePawn;
        this.brownState = brownPawn;

    }

    public GameNode getCurrentNode() {return currentNode;}
    public PlayableTile[] getBoardState() {return boardState;}
    public boolean getAllNodesFound() { return allNodesFound; }
    public void setCurrentNode(GameNode currentNode) {this.currentNode = currentNode;}
    public void setAllNodesFound(boolean allNodesFound) { this.allNodesFound = allNodesFound; }
}
