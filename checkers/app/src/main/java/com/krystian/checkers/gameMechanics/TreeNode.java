package com.krystian.checkers.gameMechanics;

import java.util.ArrayList;

public class TreeNode {
    private int position; //possible position of a pawn
    private int link; //previous position of a pawn - to bind nodes
    private int level; //how deep in a tree is this node
    public ArrayList<Integer> takenPawnPosition = new ArrayList<>(); //there might be more than 2 branches from a node (max 4);
    // it's good to know which ones has already been checked

    public TreeNode(int position, int link, int level) {
        this.position = position;
        this.link = link;
        this.level = level;
    }

    public int getPosition() { return position; }
    public int getLink() { return link; }
    public int getLevel() { return level; }

    public void setPosition(int position) { this.position = position; }
    public void setLink(int link) { this.link = link; }
    public void setLevel(int level) { this.level = level; }
}


