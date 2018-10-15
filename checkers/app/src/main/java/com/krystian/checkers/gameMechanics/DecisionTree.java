package com.krystian.checkers.gameMechanics;


import java.util.ArrayList;

public class DecisionTree { //possible paths of multiple or simple taking; one decision tree is one pawn
    private int root;
    private int takeLength;
    private int longestBranch;
    private TreeNode currentNode;
    private TreeNode previousNode; //to know how to link not-bound nodes and make branches
    public ArrayList<TreeNode> nodeList = new ArrayList<>(); //not-bound nodes
    public ArrayList<ArrayList<Integer>> treeBranch = new ArrayList<>(); //bound nodes

    public DecisionTree(int root) {
        this.root = root;
        this.takeLength= 0;
        this.longestBranch = 0;
        nodeList.add(new TreeNode(root, root, 0));
        this.currentNode = nodeList.get(0);
        this.previousNode = nodeList.get(0);
    }

    public int getRoot() { return root; }
    public int getTakeLength() { return takeLength; }
    public int getLongestBranch() { return longestBranch; }
    public TreeNode getCurrentNode() { return currentNode; }
    public TreeNode getPreviousNode() { return previousNode; }

    public void setRoot(int root) { this.root = root; }
    public void setTakeLength(int takeLength) { this.takeLength = takeLength; }
    public void setLongestBranch(int longestBranch) { this.longestBranch = longestBranch; }
    public void setCurrentNode(TreeNode currentNode) { this.currentNode = currentNode; }
    public void setPreviousNode(TreeNode previousNode) { this.previousNode = previousNode; }
}



