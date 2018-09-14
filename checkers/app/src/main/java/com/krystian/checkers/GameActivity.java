/*===========TABLE INDEXES: 0-49; EVERYTHING ELSE VALUES (POSITIONS, ID, ETC): 1-50==========

                                                 brown
                                |   | 1 |   | 2 |   | 3 |   | 4 |   | 5 |
                                | 6 |   | 7 |   | 8 |   | 9 |   | 10|   |
                                |   | 11|   | 12|   | 13|   | 14|   | 15|
                                | 16|   | 17|   | 18|   | 19|   | 20|   |
                                |   | 21|   | 22|   | 23|   | 24|   | 25|
                                | 26|   | 27|   | 28|   | 29|   | 30|   |
                                |   | 31|   | 32|   | 33|   | 34|   | 35|
                                | 36|   | 37|   | 38|   | 39|   | 40|   |
                                |   | 41|   | 42|   | 43|   | 44|   | 45|
                                | 46|   | 47|   | 48|   | 49|   | 50|   |
                                                 white
    - taking pawns (if possible) is mandatory; longest take is mandatory - doesn't matter if it's a queen or not
    */

package com.krystian.checkers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int NUMBER_OF_PAWNS = 20; //both white and brown
    public final static int NUMBER_OF_TILES = 100;
    public final static int NUMBER_OF_PLAYABLE_TILES = 50;

    GridLayout board;
    View[] playableTileView = new View[NUMBER_OF_PLAYABLE_TILES];
    PlayableTile[] playableTile = new PlayableTile[NUMBER_OF_PLAYABLE_TILES];
    int[][] diagonal = new int[19][]; //for queen moves
    ArrayList<Pawn> whitePawn = new ArrayList<>();
    ArrayList<Pawn> brownPawn = new ArrayList<>();
    ArrayList<Integer> possibleMove = new ArrayList<>();
    boolean whiteMove = true;
    Pawn chosenPawn; //to set new position and check possible moves for a specific (marked) pawn
    Pawn consideredPawn; //to check mandatory moves for every pawn
    boolean mandatoryPawn = false; //is there a pawn (or more) that has to take another one(s)?
    int takeNumber = 0; //to show possible moves during multiple taking (if there are more branches from specific node)


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = (GridLayout) findViewById(R.id.board);
        setDiagonals(); //for queen moves and pawn/queen takings
        measureBoard(); //and draw it with pawns after that
    }

    public void measureBoard() {
        board.post(new Runnable() {
            public void run() {
                int width = board.getWidth();
                int height = board.getHeight();
                drawBoard(width, height);  //to calculate width and height of a single tile
            }
        });
    }

    public void drawBoard(int width, int height) {
        View[] tile = new View[NUMBER_OF_TILES];
        int brownTileCounter = 0; //to set id value for game mechanics and listeners
        for (int i = 0; i < tile.length; i++) {
            tile[i] = new View(this);
            tile[i].setLayoutParams(new LinearLayout.LayoutParams(width / 10, height / 10)); //10 x 10 tiles board
            if ((i % 2 == 0 && (i / 10) % 2 == 0) || (i % 2 != 0 && (i / 10) % 2 != 0))  //which tiles should be white
                tile[i].setBackgroundColor(getResources().getColor(R.color.whiteTile));
            else {
                playableTileView[brownTileCounter] = tile[i];
                playableTileView[brownTileCounter].setId(brownTileCounter + 1);
                playableTileView[brownTileCounter].setOnClickListener(this);
                brownTileCounter++;
            }

            board.addView(tile[i]);
        }
        createPawns();
    }

    public void createPawns() {
        for(int i=0; i<NUMBER_OF_PAWNS; i++) { //create pawns
            whitePawn.add(new Pawn( NUMBER_OF_PLAYABLE_TILES-i, true, false));
            brownPawn.add(new Pawn( i+1, false, false));
        }
        for(int i=0; i<NUMBER_OF_PLAYABLE_TILES; i++) { //create playableTiles
            if(i>=0 && i < 20) playableTile[i] = new PlayableTile((i+1), -1); //-1 is brown pawn
            else if(i>=30 && i <50) playableTile[i] = new PlayableTile((i+1), 1); //1 is white pawn
            else playableTile[i] = new PlayableTile((i+1), 0); //0 means tile is empty
        }
        drawPawns();
    }

    public void drawPawns() { //will be useful after every move
        for(int i=0; i<playableTileView.length; i++) {
            if(playableTile[i].getIsTaken() == 1) playableTileView[i].setBackgroundResource(R.drawable.white_pawn);
            else if(playableTile[i].getIsTaken() == -1) playableTileView[i].setBackgroundResource(R.drawable.brown_pawn);
            else if(playableTile[i].getIsTaken() == 2) playableTileView[i].setBackgroundResource(R.drawable.white_queen);
            else if(playableTile[i].getIsTaken() == -2) playableTileView[i].setBackgroundResource(R.drawable.brown_queen);
            else playableTileView[i].setBackgroundResource(0);
        }

        int longestTake = 0;
        if(whiteMove) {
            for (Pawn wPawn : whitePawn) {
                wPawn.setPawnTree(new DecisionTree(wPawn.getPosition()));
                consideredPawn = wPawn;
                checkMandatoryMove(wPawn.getPosition(), -1);

                if(mandatoryPawn) {
                    if(consideredPawn.getPawnTree() != null) { //there is a branch then
                        takeLongestBranch();
                        if(consideredPawn.getPawnTree().getLongestBranch() >= longestTake) {
                            longestTake = consideredPawn.getPawnTree().getLongestBranch();
                        }
                    }
                }
            }
            chooseFinalPawn(longestTake);
        }
        else {
            for (Pawn bPawn : brownPawn) {
                bPawn.setPawnTree(new DecisionTree(bPawn.getPosition()));
                consideredPawn = bPawn;
                checkMandatoryMove(bPawn.getPosition(), 1);

                if(mandatoryPawn) {
                    if(consideredPawn.getPawnTree() != null) { //there is a branch then
                        takeLongestBranch();
                        if(consideredPawn.getPawnTree().getLongestBranch() >= longestTake) {
                            longestTake = consideredPawn.getPawnTree().getLongestBranch();
                        }
                    }
                }
            }
            chooseFinalPawn(longestTake); //the one(s) with the longest taking possible
        }
    }

    public void checkMandatoryMove(int position, int takenPawn) { //takenPawn = -1/-2 means brown pawn/queen can be taken
        consideredPawn.getPawnTree().setTakeLength(consideredPawn.getPawnTree().getTakeLength() + 1);
        if(!consideredPawn.getIsQueen()) {
            int rowImpact = (position-1)/5%2;
            checkUpTaking(position, rowImpact, takenPawn);
            checkUpTaking(position, rowImpact, 2*takenPawn);
            checkDownTaking(position, rowImpact, takenPawn);
            checkDownTaking(position, rowImpact, 2*takenPawn);
        }
        else checkQueenTakes(position);

        if(consideredPawn.getPawnTree().nodeList.size() == 1) //no takings after checking - only root node
            consideredPawn.setPawnTree(null);
        else {
            mandatoryPawn = true;
            checkTreeNodes(0, 0, 0); //no more takes in this branch -> bind nodes
        }
    }

    public void checkUpTaking(int position, int rowImpact, int takenPawn) { //taken pawn: -1 = brown, 1 = white
        if (position > 10) { //if you'll try to take from the last row - array out of bounds
            if (position % 5 != 0 && playableTile[position - 1 - 4 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[position - 1 - 9].getIsTaken() == 0) {
                checkTreeNodes(position, position - 9, position-4-rowImpact); //checking possible mandatory moves before clicking pawn
            }
            if ((position - 1) % 5 != 0 && playableTile[position - 1 - 5 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[position - 1 - 11].getIsTaken() == 0) {
                checkTreeNodes(position, position - 11, position-5-rowImpact);
            }
        }
    }

    public void checkDownTaking(int position, int rowImpact, int takenPawn) {
        if (position <= 40) {
            if ((position - 1) % 5 != 0 && playableTile[position - 1 + 5 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[position - 1 + 9].getIsTaken() == 0) {
                checkTreeNodes(position, position + 9, position+5-rowImpact);
            }
            if (position % 5 != 0 && playableTile[position - 1 + 6 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[position - 1 + 11].getIsTaken() == 0) {
                checkTreeNodes(position, position + 11, position+6-rowImpact);
            }
        }
    }

    public void checkTreeNodes(int link, int position, int takenPawnPosition) {
        DecisionTree thisTree = consideredPawn.getPawnTree(); //for better readability; last added tree - build for considered pawn
        //Log.v("Current", ""+thisTree.getCurrentNode().getPosition());
        //Log.v("Previous", ""+thisTree.getPreviousNode().getPosition());
        if(position != 0) {

            if(thisTree.getPreviousNode().takenPawnPosition.size() != 0) { //does this node have any already checked branches?
                boolean branchAlreadyChecked = true;
                    for (Integer nodeBranch : thisTree.getPreviousNode().takenPawnPosition) {
                        if (takenPawnPosition != nodeBranch)
                            branchAlreadyChecked = false; //pawn hasn't been  included in any branch yet
                        else { //TO DO: don't check the same pawn when taking is longer (node lvl 1 and 4 may be the same pawn for queen)
                            //TO DO: don't check square taking (lvl 1 and 5 is the same pawn - taking for eternity)
                            if(consideredPawn.getIsQueen()) { //there can be more ways to take the same pawn by queen
                                if(thisTree.getCurrentNode().getLevel() == 0) {
                                    branchAlreadyChecked = false;
                                    break;
                                }
                                else {
                                    branchAlreadyChecked = true; //reverse take - tile is closer
                                    break;
                                }
                            }
                            else {
                                branchAlreadyChecked = true; //normal pawn can take a pawn only in one way
                                break;
                            }
                        }
                    }

                if(!branchAlreadyChecked) {
                    thisTree.getCurrentNode().takenPawnPosition.add(takenPawnPosition);
                    thisTree.setPreviousNode(thisTree.getCurrentNode());
                    thisTree.nodeList.add(new TreeNode(position, link, thisTree.getTakeLength()));
                    thisTree.setCurrentNode(thisTree.nodeList.get(thisTree.nodeList.size() - 1)); //set new node as a current one
                }
            }

            else {
                thisTree.getCurrentNode().takenPawnPosition.add(takenPawnPosition);
                thisTree.setPreviousNode(thisTree.getCurrentNode());
                thisTree.nodeList.add(new TreeNode(position, link, thisTree.getTakeLength()));
                thisTree.setCurrentNode(thisTree.nodeList.get(thisTree.nodeList.size() - 1)); //set new node as a current one
            }

            if (thisTree.getCurrentNode().getLevel() == thisTree.getTakeLength()) {
                Log.v("Current Node", "" + thisTree.getCurrentNode().getPosition() + " " + thisTree.getCurrentNode().getLink() + " " + thisTree.getCurrentNode().getLevel()+ " " +thisTree.getCurrentNode().takenPawnPosition);
                //Log.v("Previous Node", "" + thisTree.getPreviousNode().getPosition() + " " + thisTree.getPreviousNode().getLink() + " " + thisTree.getPreviousNode().getLevel()+ " " +thisTree.getPreviousNode().takenPawnPosition);
                if (consideredPawn.getIsWhite()) checkMandatoryMove(thisTree.getCurrentNode().getPosition(), -1);
                else checkMandatoryMove(thisTree.getCurrentNode().getPosition(), 1);
            }
        }
        else {
            //Log.v("End one", ""+thisTree.getCurrentNode().getPosition());
            if(thisTree.getCurrentNode().takenPawnPosition.size() == 0) bindTreeNodes(thisTree, thisTree.getCurrentNode()); //no more takings in this branch
            thisTree.setTakeLength(thisTree.getTakeLength() - 1); //go back one node to check different branch
            for(TreeNode node : thisTree.nodeList) {
                if(node.getPosition() == thisTree.getCurrentNode().getLink()) {
                    thisTree.setCurrentNode(node);
                    if(consideredPawn.getIsQueen()) checkDiagonals(thisTree.getCurrentNode().getPosition());
                    break; //diagonals must be checked again because node has changed; indexes aren't important here - but arrays are
                }
            }
            for(TreeNode node : thisTree.nodeList) {
                if(node.getPosition() == thisTree.getCurrentNode().getLink()) {
                    thisTree.setPreviousNode(node);
                    break;
                }
            }
        }
    }

    public void bindTreeNodes(DecisionTree thisTree, TreeNode lastNode) {
        thisTree.treeBranch.add(new ArrayList<Integer>()); //for recursion
        thisTree.treeBranch.get(thisTree.treeBranch.size() - 1).add(lastNode.getPosition());
        while (lastNode.getLevel() != 1) {
            for (TreeNode node : thisTree.nodeList) {
                if (node.getPosition() == lastNode.getLink() && node.getLevel() == lastNode.getLevel() - 1) {
                    thisTree.treeBranch.get(thisTree.treeBranch.size() - 1).add(0, node.getPosition());
                    lastNode = node;
                }
            }
        }

        for(ArrayList<Integer> branch : thisTree.treeBranch) {
            Log.v("Branch",""+branch);
        }
    }

    public void takeLongestBranch() {
        DecisionTree thisTree = consideredPawn.getPawnTree();
        for(ArrayList<Integer> branch : thisTree.treeBranch) {
            if(branch.size() >= thisTree.getLongestBranch()) {
                thisTree.setLongestBranch(branch.size());
            }
        }

        ArrayList<ArrayList<Integer>> branchesToRemove = new ArrayList<>();
        for(ArrayList<Integer> branch : thisTree.treeBranch) {
            if (branch.size() < thisTree.getLongestBranch()) {
                branchesToRemove.add(branch);
            }
        }
        thisTree.treeBranch.removeAll(branchesToRemove);
    }

    public void chooseFinalPawn(int longestTake) {
        if(whiteMove) {
            for(Pawn wPawn: whitePawn) {
                if(wPawn.getPawnTree() != null && wPawn.getPawnTree().getLongestBranch() < longestTake) {
                    wPawn.setPawnTree(null);
                }
            }
        }
        else {
            for(Pawn bPawn : brownPawn) {
                if(bPawn.getPawnTree() != null && bPawn.getPawnTree().getLongestBranch() < longestTake) {
                    bPawn.setPawnTree(null);
                }
            }
        }
    }

    public void onClick(View view) {
        if(whiteMove) {
            if (playableTile[view.getId() - 1].getIsTaken() > 0) { //white pawn (or queen) has just been clicked
                possibleMove.clear();
                for (Pawn wPawn : whitePawn) {
                    markPawn(wPawn, view);
                    if(mandatoryPawn) {
                        if(wPawn.getPawnTree() != null) markPossibleMove();
                    }
                    else markPossibleMove(); //there are no mandatory moves
                }
            }
            else makeMove(view); //white pawn was chosen before - this is setting his destination
            //TO DO: do not send whole object - just int with destination will do
        }
        else {
            if (playableTile[view.getId() - 1].getIsTaken() < 0) {
                possibleMove.clear();
                for (Pawn bPawn : brownPawn) {
                    markPawn(bPawn, view);
                    if(mandatoryPawn) {
                        if(bPawn.getPawnTree() != null) markPossibleMove();
                    }
                    markPossibleMove();
                }
            }
            else makeMove(view);
        }
    }

    public void markPawn(Pawn wPawn, View view) {
        playableTileView[wPawn.getPosition() - 1].getBackground().setAlpha(255);
        if (wPawn.getPosition() == view.getId()) {
            if(whiteMove) playableTileView[wPawn.getPosition() -1].setBackgroundResource(R.drawable.white_pawn); //in multiple takings
            else playableTileView[wPawn.getPosition() -1].setBackgroundResource(R.drawable.brown_pawn); //to show pawn instead of green cell (possible move)
            playableTileView[wPawn.getPosition() - 1].getBackground().setAlpha(70);
            chosenPawn = wPawn;
            checkPossibleMoves(wPawn);
        }
    }

    public void markPossibleMove() {
        for(View tile : playableTileView) {  //mark legal moves
            if (playableTile[tile.getId()-1].getIsTaken() == 0)
                tile.setBackgroundColor(getResources().getColor(R.color.brownTile)); //un-mark possible moves if just switching pawn
            for (Integer move : possibleMove)
                if (tile.getId() == move)
                    tile.setBackgroundColor(getResources().getColor(R.color.possibleMove));
        }
    }

    public void makeMove(View view) {
        boolean validMove = false;
        if(chosenPawn != null) { //a pawn has been clicked - so it can be moved (or not - if it has no possible moves)
            for (Integer move : possibleMove) {
                if (view.getId() == move) { //chosen tile is a valid move
                    playableTile[chosenPawn.getPosition() - 1].setIsTaken(0); //free previous position
                    playableTileView[chosenPawn.getPosition() - 1].getBackground().setAlpha(255);
                    if(!chosenPawn.getIsQueen()) {
                        if(whiteMove) playableTile[view.getId() - 1].setIsTaken(1); //set pawn on new position
                        else playableTile[view.getId() - 1].setIsTaken(-1);
                    }
                    else {
                        if(whiteMove) playableTile[view.getId() - 1].setIsTaken(2);
                        else playableTile[view.getId() - 1].setIsTaken(-2);
                    }

                    if(chosenPawn.getPawnTree() != null) { //a pawn has been taken then
                        TreeNode searchedNode = chosenPawn.getPawnTree().nodeList.get(0); //just to initialize
                        int[] previousFirstDiagonal; int[] previousSecondDiagonal; //node's link diagonals
                        int linkIndex = 0; int positionIndex = 0;

                        for(TreeNode node : chosenPawn.getPawnTree().nodeList) {
                            if(node.getPosition() == move) {
                                searchedNode = node; //need to find diagonals of position and link of this node
                            } //and if there's is a pawn of different color between them - that's the one to take
                        } //that way it's the same method for queen and regular pawn
                        checkDiagonals(searchedNode.getLink());
                        previousFirstDiagonal = chosenPawn.getFirstDiagonal();
                        previousSecondDiagonal = chosenPawn.getSecondDiagonal();
                        checkDiagonals(searchedNode.getPosition());
                        if(previousFirstDiagonal == chosenPawn.getFirstDiagonal()) {
                            for(int i=0; i < chosenPawn.getFirstDiagonal().length; i++) {
                                if(chosenPawn.getFirstDiagonal()[i] == searchedNode.getLink()) linkIndex = i;
                                else if(chosenPawn.getFirstDiagonal()[i] == searchedNode.getPosition()) positionIndex = i;
                            }
                            for(int i = 0; i < chosenPawn.getFirstDiagonal().length; i++) {
                                if( ((i > linkIndex && i < positionIndex) || (i < linkIndex && i > positionIndex))
                                        && playableTile[chosenPawn.getFirstDiagonal()[i]-1].getIsTaken()!=0) {
                                    takePawn(chosenPawn.getFirstDiagonal()[i]);
                                    break; //only one pawn to take for one click
                                }
                            }
                        }
                        else if(previousSecondDiagonal == chosenPawn.getSecondDiagonal()) {
                            for(int i=0; i < chosenPawn.getSecondDiagonal().length; i++) {
                                if(chosenPawn.getSecondDiagonal()[i] == searchedNode.getLink()) linkIndex = i;
                                else if(chosenPawn.getSecondDiagonal()[i] == searchedNode.getPosition()) positionIndex = i;
                            }
                            for(int i = 0; i < chosenPawn.getSecondDiagonal().length; i++) {
                                if( ((i > linkIndex && i < positionIndex) || (i < linkIndex && i > positionIndex))
                                        && playableTile[chosenPawn.getSecondDiagonal()[i]-1].getIsTaken()!=0) {
                                    takePawn(chosenPawn.getSecondDiagonal()[i]);
                                    break;
                                }
                            }
                        }
                    }

                    chosenPawn.setPosition(view.getId());
                    validMove = true;
                    break;
                }
            }

            if(validMove) {
                if(mandatoryPawn) {
                    possibleMove.clear();
                    takeNumber++;
                    checkPossibleMoves(chosenPawn); //there might be multiple taking
                    if(possibleMove.size() != 0) {
                        markPawn(chosenPawn, view);
                        markPossibleMove();
                    }
                    else endMove();
                }
                else endMove();
            }
        }
    }

    public void endMove() {
        if(chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 0) {
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(2); //white pawn promoted
            chosenPawn.setIsQueen(true); //pawn in the last row
        }
        else if(!chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 9) {
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(-2);
            chosenPawn.setIsQueen(true);
        }

        chosenPawn = null;
        whiteMove = !whiteMove;
        mandatoryPawn = false;
        takeNumber = 0;
        //if(pawnToTake.size() > 0 ) pawnToTake.clear();
        drawPawns();
    }

    public void takePawn(int pos) { //isWhite = true means white just took a brown pawn
        playableTile[pos - 1].setIsTaken(0);
        if(whiteMove) {
            for (Pawn bPawn : brownPawn) {
                if (bPawn.getPosition() == pos) {
                    brownPawn.remove(bPawn);
                    break;
                }
            }
        }
        else {
            for (Pawn wPawn : whitePawn) {
                if (wPawn.getPosition() == pos) {
                    whitePawn.remove(wPawn);
                    break;
                }
            }
        }
    }

    public void checkPossibleMoves(Pawn pawn) {
        int pos = pawn.getPosition(); //it's too long to write it in every condition n times
        int rowImpact = (pos-1)/5%2; //is row even or odd? 0-9 range; pawns pos: 0-4, 5-9... 45-49; helps with modulo

        if(!mandatoryPawn)
            if(!pawn.getIsQueen()) {
                if (whiteMove) checkWhiteMove(pos, rowImpact); //left/right; there are no mandatory takes
                else checkBrownMove(pos, rowImpact);
            }
            else checkQueenMoves();
        else {
            if(chosenPawn.getPawnTree() != null) {
                for (TreeNode node : chosenPawn.getPawnTree().nodeList) { //pawn is somewhere on its decision tree
                    if (node.getPosition() == chosenPawn.getPosition()) {  //if not - it's another pawn clicked
                        for (ArrayList<Integer> branch : chosenPawn.getPawnTree().treeBranch) {
                            if (takeNumber == 0) {
                                possibleMove.add(branch.get(takeNumber));
                            } else {
                                if (branch.get(takeNumber - 1) == pawn.getPosition() && branch.size() > takeNumber) {
                                    possibleMove.add(branch.get(takeNumber));
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public void checkWhiteMove(int pos, int rowImpact) {
        if ( (rowImpact != 0 || pos%5 != 0) && playableTile[pos-1-4-rowImpact].getIsTaken() == 0) //check if pawn is at the side and if not - check if tile is free to go
            possibleMove.add(pos-4-rowImpact); //rightMove
        if ( (rowImpact != 1 || (pos-1)%5 != 0) && playableTile[pos-1-5-rowImpact].getIsTaken() == 0)
            possibleMove.add(pos-5-rowImpact); //leftMove
    }

    public void checkBrownMove(int pos, int rowImpact) {
        if ((rowImpact != 1 || (pos - 1) % 5 != 0) && playableTile[pos - 1 + 5 - rowImpact].getIsTaken() == 0)
            possibleMove.add(pos + 5 - rowImpact); //rightMove; brown perspective;
        if ((rowImpact != 0 || pos % 5 != 0) && playableTile[pos - 1 + 6 - rowImpact].getIsTaken() == 0)
            possibleMove.add(pos + 6 - rowImpact); //leftMove
    }

    public void setDiagonals() {
        diagonal[0] = new int[]{1, 6};
        diagonal[1] = new int[]{2, 7, 11, 16};
        diagonal[2] = new int[]{3, 8, 12, 17, 21, 26};
        diagonal[3] = new int[]{4, 9, 13, 18, 22, 27, 31, 36};
        diagonal[4] = new int[]{5, 10, 14, 19, 23, 28, 32, 37, 41, 46};
        diagonal[5] = new int[]{15, 20, 24, 29, 33, 38, 42, 47};
        diagonal[6] = new int[]{25, 30, 34, 39, 43, 48};
        diagonal[7] = new int[]{35, 40, 44, 49};
        diagonal[8] = new int[]{45, 50};
        diagonal[9] = new int[]{46};
        diagonal[10] = new int[]{36, 41, 47};
        diagonal[11] = new int[]{26, 31, 37, 42, 48};
        diagonal[12] = new int[]{16, 21, 27, 32, 38, 43, 49};
        diagonal[13] = new int[]{6, 11, 17, 22, 28, 33, 39, 44, 50};
        diagonal[14] = new int[]{1, 7, 12, 18, 23, 29, 34, 40, 45};
        diagonal[15] = new int[]{2, 8, 13, 19, 24, 30, 35};
        diagonal[16] = new int[]{3, 9, 14, 20, 25};
        diagonal[17] = new int[]{4, 10, 15};
        diagonal[18] = new int[]{5};
    }

    public int[] checkDiagonals(int position) {
        Pawn queen;
        if(chosenPawn == null) queen = consideredPawn;
        else queen = chosenPawn;
        Log.v("Position", ""+position);
        int firstDiagonalIndex = 0;
        int secondDiagonalIndex = 0;
        for (int i = 0; i < diagonal.length; i++) {
            for (int j = 0; j < diagonal[i].length; j++) {
                if (i < 9 && diagonal[i][j] == position) {
                    queen.setFirstDiagonal(diagonal[i]);
                    firstDiagonalIndex = j;
                } else if (i >= 9 && diagonal[i][j] == position) {
                    queen.setSecondDiagonal(diagonal[i]);
                    secondDiagonalIndex = j;
                }
            }
        }

        return new int[]{firstDiagonalIndex, secondDiagonalIndex};
    }

    public void checkQueenTakes(int position) {
        checkDownRightTaking(position); //all functions' names are from whites perspective
        checkUpRightTaking(position);
        checkDownLeftTaking(position);
        checkUpLeftTaking(position);
    }

    public void checkDownRightTaking(int position) { //from smaller to higher indexes of second diagonal index
        int secondDiagonalIndex = checkDiagonals(position)[1];
        if(whiteMove) {
            while (secondDiagonalIndex < consideredPawn.getSecondDiagonal().length - 2) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() < 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkDownRightFinish(position, consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1], secondDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() == 0) //empty tile
                    secondDiagonalIndex++; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
        else {
            while (secondDiagonalIndex < consideredPawn.getSecondDiagonal().length - 2) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() > 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkDownRightFinish(position, consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1], secondDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() == 0) //empty tile
                    secondDiagonalIndex++; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
    }

    public void checkDownRightFinish(int currentPosition, int takenPawnPosition, int secondDiagonalIndex) {
        ArrayList<Integer> newPosition = new ArrayList<>();
        while (secondDiagonalIndex < consideredPawn.getSecondDiagonal().length) {
            if(consideredPawn.getSecondDiagonal()[secondDiagonalIndex] > takenPawnPosition) { //you must land behind taken pawn to make proper move
                if(playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex] - 1].getIsTaken() == 0) {
                    //checkTreeNodes(currentPosition, consideredPawn.getSecondDiagonal()[secondDiagonalIndex], takenPawnPosition);
                    newPosition.add(consideredPawn.getSecondDiagonal()[secondDiagonalIndex]);
                }
                else break;
            }
            secondDiagonalIndex++;
        }
        //That's new! And maybe it is even working!
        for(int i=0; i<newPosition.size(); i++) {
            checkDiagonals(newPosition.get(i)); //to set diagonals for every node after taken pawn
            checkTreeNodes(currentPosition, newPosition.get(i), takenPawnPosition);
        }
    }

    public void checkDownLeftTaking(int position) { //from smaller to higher indexes of first diagonal index
        int firstDiagonalIndex = checkDiagonals(position)[0];
        if(whiteMove) {
            while (firstDiagonalIndex < consideredPawn.getFirstDiagonal().length - 2) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() < 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkDownLeftFinish(position, consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1], firstDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() == 0) //empty tile
                    firstDiagonalIndex++; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
        else {
            while (firstDiagonalIndex < consideredPawn.getFirstDiagonal().length - 2) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() > 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkDownLeftFinish(position, consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1], firstDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() == 0) //empty tile
                    firstDiagonalIndex++; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
    }

    public void checkDownLeftFinish(int currentPosition, int takenPawnPosition, int firstDiagonalIndex) {
        ArrayList<Integer> newPosition = new ArrayList<>();
        while (firstDiagonalIndex < consideredPawn.getFirstDiagonal().length) {
            if(consideredPawn.getFirstDiagonal()[firstDiagonalIndex] > takenPawnPosition) { //you must land after taken pawn to make proper move
                if(playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex] - 1].getIsTaken() == 0) {
                    //checkTreeNodes(currentPosition, consideredPawn.getFirstDiagonal()[firstDiagonalIndex], takenPawnPosition);
                    newPosition.add(consideredPawn.getFirstDiagonal()[firstDiagonalIndex]);
                } //it is a valid move; there might be another take possible
                else break;
            }
            firstDiagonalIndex++;
        }
        for(int i=0; i<newPosition.size(); i++) {
            checkDiagonals(newPosition.get(i));
            checkTreeNodes(currentPosition, newPosition.get(i), takenPawnPosition);
        }
    }

    public void checkUpRightTaking(int position) { //from higher to smaller indexes of first diagonal index
        int firstDiagonalIndex = checkDiagonals(position)[0];
        if(whiteMove) {
            while (firstDiagonalIndex > 1) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() < 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkUpRightFinish(position, consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1], firstDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() == 0) //empty tile
                    firstDiagonalIndex--; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
        else {
            while (firstDiagonalIndex > 1) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() > 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkUpRightFinish(position, consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1], firstDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() == 0) //empty tile
                    firstDiagonalIndex--; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
    }

    public void checkUpRightFinish(int currentPosition, int takenPawnPosition, int firstDiagonalIndex) {
        ArrayList<Integer> newPosition = new ArrayList<>();

        while (firstDiagonalIndex >= 0) {
            if(consideredPawn.getFirstDiagonal()[firstDiagonalIndex] < takenPawnPosition) { //you must land after taken pawn to make proper move
                if(playableTile[consideredPawn.getFirstDiagonal()[firstDiagonalIndex] - 1].getIsTaken() == 0) {
                    //checkTreeNodes(currentPosition, consideredPawn.getFirstDiagonal()[firstDiagonalIndex], takenPawnPosition);
                    newPosition.add(consideredPawn.getFirstDiagonal()[firstDiagonalIndex]);
                } //it is a valid move; there might be another take possible
                else break;
            }
            firstDiagonalIndex--;
        }
        for(int i=0; i<newPosition.size(); i++) {
            checkDiagonals(newPosition.get(i));
            checkTreeNodes(currentPosition, newPosition.get(i), takenPawnPosition);
        }
    }

    public void checkUpLeftTaking(int position) { //from higher to smaller indexes of second diagonal index
        int secondDiagonalIndex = checkDiagonals(position)[1];
        if(whiteMove) {
            while (secondDiagonalIndex > 1) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() < 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkUpLeftFinish(position, consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1], secondDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() == 0) //empty tile
                    secondDiagonalIndex--; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
        else {
            while (secondDiagonalIndex > 1) { //at least two tiles to the edge
                if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() > 0) { //there is possible pawn to take
                    if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break; //but there's no room to land after it
                    else { //or is it??
                        checkUpLeftFinish(position, consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1], secondDiagonalIndex);
                        break;
                    }
                }
                else if (playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() == 0) //empty tile
                    secondDiagonalIndex--; //but there can be a pawn to take a couple tiles further
                else break;
            }
        }
    }

    public void checkUpLeftFinish(int currentPosition, int takenPawnPosition, int secondDiagonalIndex) {
        ArrayList<Integer> newPosition = new ArrayList<>();
        while (secondDiagonalIndex >= 0) {
            if(consideredPawn.getSecondDiagonal()[secondDiagonalIndex] < takenPawnPosition) { //you must land after taken pawn to make proper move
                if(playableTile[consideredPawn.getSecondDiagonal()[secondDiagonalIndex] - 1].getIsTaken() == 0) {
                    //checkTreeNodes(currentPosition, consideredPawn.getSecondDiagonal()[secondDiagonalIndex], takenPawnPosition);
                    newPosition.add(consideredPawn.getSecondDiagonal()[secondDiagonalIndex]);
                }
                else break;
            }
            secondDiagonalIndex--;
        }
        for(int i=0; i<newPosition.size(); i++) {
            checkDiagonals(newPosition.get(i));
            checkTreeNodes(currentPosition, newPosition.get(i), takenPawnPosition);
        }
    }

    public void checkQueenMoves() { //TO DO: considered pawn will do (you mean chosen?)
        int[] indexes = checkDiagonals(chosenPawn.getPosition());
        while(indexes[0] != 0) {
            if(playableTile[chosenPawn.getFirstDiagonal()[indexes[0]-1] - 1].getIsTaken() == 0)
                possibleMove.add(chosenPawn.getFirstDiagonal()[indexes[0]-1]);
            else break;
            indexes[0]--;

        }
        while(indexes[1] != 0) {
            if(playableTile[chosenPawn.getSecondDiagonal()[indexes[1]-1] - 1].getIsTaken() == 0)
                possibleMove.add(chosenPawn.getSecondDiagonal()[indexes[1]-1]);
            else break;
            indexes[1]--;
        }
        while(indexes[0] != chosenPawn.getFirstDiagonal().length - 1) {
            if(chosenPawn.getFirstDiagonal()[indexes[0]] >= chosenPawn.getPosition()) { //restoring index after decrementing it
                if(playableTile[chosenPawn.getFirstDiagonal()[indexes[0]+1] - 1].getIsTaken() == 0)
                    possibleMove.add(chosenPawn.getFirstDiagonal()[indexes[0]+1]);
                else break;
            }
            indexes[0]++;
        }
        while(indexes[1] != chosenPawn.getSecondDiagonal().length - 1) {
            if(chosenPawn.getSecondDiagonal()[indexes[1]] >= chosenPawn.getPosition()) { //restoring index after decrementing it
                if(playableTile[chosenPawn.getSecondDiagonal()[indexes[1]+1] - 1].getIsTaken() == 0)
                    possibleMove.add(chosenPawn.getSecondDiagonal()[indexes[1]+1]);
                else break;
            }
            indexes[1]++;
        }
    }

}

