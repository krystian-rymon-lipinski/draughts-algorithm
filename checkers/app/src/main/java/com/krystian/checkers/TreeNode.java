package com.krystian.checkers;

public class TreeNode {
    private int position; //possible position of a pawn
    private int link; //previous position of a pawn - to bind nodes
    private int level; //how deep in a tree is this node

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


