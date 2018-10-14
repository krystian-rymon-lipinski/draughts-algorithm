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

package com.krystian.checkers.gameMechanics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import com.krystian.checkers.AI_algorithm.GameNode;
import com.krystian.checkers.AI_algorithm.GameTree;
import com.krystian.checkers.R;
import com.krystian.checkers.database.GameDatabaseHelper;


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
    GameTree gameTree = null; //to check moves or cpu

    String whiteMoves = ""; //for database saving using checkers notation
    String brownMoves = "";
    String boardStates = "";


    public GameActivity() {};

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
        if(whitePawn.size() != 0 && brownPawn.size() != 0) checkForMoves();
        checkGameState();
        /*
        if(whiteMove) { //TO DO: make one for loop with one ArrayList
            //if(whiteMove) list = whitePawn and so on
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
            checkForBestMove();
        }
        */
    }

    public void checkGameState() {

        if(whitePawn.size() == 0 || brownPawn.size() == 0) { //a game just ended
            try {
                GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.query("STATS", new String[]{"PLAYED", "WON"}, null, null, null, null, null);
                cursor.moveToFirst();
                int gamesPlayed = cursor.getInt(0) + 1; //just finished game - need to update database
                int gamesWon = cursor.getInt(1);
                if(whitePawn.size() == 0) {
                    Toast.makeText(this, "Przegrana!", Toast.LENGTH_SHORT).show();
                    brownMoves += "X"; //notation for the end of a game
                }
                else if(brownPawn.size() == 0) {
                    Toast.makeText(this, "Wygrana!", Toast.LENGTH_SHORT).show();
                    whiteMoves += "X";
                    gamesWon++;
                }
                ContentValues statsUpdate = new ContentValues();
                statsUpdate.put("PLAYED", gamesPlayed);
                statsUpdate.put("WON", gamesWon);
                db.update("STATS", statsUpdate, null, null); //update games number (only one record in this table)

                long gameIndex = DatabaseUtils.queryNumEntries(db, "GAMES") + 1;
                ContentValues gamesUpdate = new ContentValues();
                gamesUpdate.put("NUMBER", gameIndex);
                gamesUpdate.put("NAME", "Partia #"+gameIndex);
                gamesUpdate.put("WHITE", whiteMoves);
                gamesUpdate.put("BROWN", brownMoves);
                gamesUpdate.put("BOARD", boardStates);
                db.insert("GAMES", null, gamesUpdate); //add another game for analysis

                cursor.close();
                db.close();
            } catch(SQLiteException e) {
                Toast.makeText(this, "Brak dostępu do bazy danych", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void checkForMoves() {
        int longestTake = 0;
        if(whiteMove) { //TO DO: make one for loop with one ArrayList
            //if(whiteMove) list = whitePawn and so on
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
            if(gameTree != null) gameTree.getCurrentNode().setLengthOfWhiteTaking(longestTake);
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
            if(gameTree == null) checkForBestMove();

        }
    }

    public PlayableTile[] createBoardDeepCopy(PlayableTile[] board) {
        PlayableTile[] currentBoard = new PlayableTile[50];
        for(int i=0; i<board.length; i++) //deep copies of tiles and pawns
            currentBoard[i] = new PlayableTile(board[i]);
        return currentBoard;
    }

    public ArrayList<Pawn> createWhiteDeepCopy(ArrayList<Pawn> whiteState) {
        ArrayList<Pawn> currentWhite = new ArrayList<>();
        for(int i=0; i<whiteState.size(); i++)
            currentWhite.add(new Pawn(whiteState.get(i)));
        return currentWhite;
    }

    public ArrayList<Pawn> createBrownDeepCopy(ArrayList<Pawn> brownState) {
        ArrayList<Pawn> currentBrown = new ArrayList<>();
        for(int i=0; i<brownState.size(); i++)
            currentBrown.add(new Pawn(brownState.get(i)));
        return currentBrown;
    }

    public ArrayList<Integer> createMoveListDeepCopy(ArrayList<Integer> moveList) {
        ArrayList<Integer> currentMoveList = new ArrayList<>();
        for(int i=0; i<moveList.size(); i++)
            currentMoveList.add(new Integer(moveList.get(i)));
        return currentMoveList;
    }

    public void checkForBestMove() {
        if(gameTree == null) {
            gameTree = new GameTree(createBoardDeepCopy(playableTile),
                    createWhiteDeepCopy(whitePawn), createBrownDeepCopy(brownPawn)); //current state to go back to
        }

        checkForMoves(); //what moves are possible in this state
        searchForNodes(brownPawn, gameTree); //create game nodes for each possible move
        checkGameNodes(); //find the situation after such moves
        chooseBestGameNode(); //and assess which move is best (or rather which are the worst and should not be considered
        gameTree.setAllNodesFound(true);

        for(GameNode node : gameTree.gameNodeList) {
            Log.v("First level nodes", " "+node.getPawn().getPosition()+ " "+node.moveList);
        }

        for(GameNode node : gameTree.bestNodeList) {
            Log.v("Chosen nodes", " "+node.getPawn().getPosition()+ " "+node.moveList);
        }

        Random rand = new Random();
        int r = rand.nextInt(gameTree.bestNodeList.size());
        GameNode chosenNode = gameTree.bestNodeList.get(r);
        resetBoardState(); //go back to current state to make a proper move
        chosenPawn = chosenNode.getPawn();

        if(!chosenNode.getIsThereTaking()) {
            possibleMove.add(chosenNode.moveList.get(0));
            Log.v("Pawn moved", ""+chosenPawn.getPosition());
            Log.v("Move done", ""+chosenNode.moveList.get(0));
            makeMove(chosenNode.moveList.get(0)); //make proper move as brown
        }
        else {
            mandatoryPawn = true;
            for(Integer move : chosenNode.moveList) {
                takeNumber = chosenNode.moveList.indexOf(move);
                possibleMove.add(move);
                Log.v("Pawn moved", ""+chosenPawn.getPosition());
                Log.v("Move done", ""+move);
                makeMove(move); //make proper take(s) as brown
            }
        }

        gameTree = null; //all checked and move made - tree is not needed anymore
        endMove();

        for(Pawn pawn1 : whitePawn) {
            Log.v("Updated white", ""+pawn1.getPosition());
        }

        for(Pawn pawn1 : brownPawn) {
            Log.v("Updated brown", ""+pawn1.getPosition());
        }
        Log.v("White size", ""+whitePawn.size());
        Log.v("Brown size", ""+brownPawn.size());
    }

    public void searchForNodes(ArrayList<Pawn> pawnColor, GameTree gameTree) {
        ArrayList<Integer> moveList = new ArrayList<>(); //combination of moves to get to a specific node
        for (Pawn pawn : pawnColor) {
            if (pawn.getPawnTree() != null) { //scenario when brown can take a pawn or more
                chosenPawn = pawn;
                for(ArrayList<Integer> branch : chosenPawn.getPawnTree().treeBranch) { //in many ways
                    for(int i=0; i<branch.size(); i++) { //and many pawns in each way
                        moveList.add(branch.get(i));
                    }
                    gameTree.gameNodeList.add(new GameNode(/*gameTree.gameNodeList.size(),
                            gameTree.getCurrentNode().getId(), nodeLevel,*/
                            pawn, createMoveListDeepCopy(moveList), true
                    ));
                    moveList.clear();
                }
            }

            else { //scenario when brown can move pawn
                chosenPawn = pawn;
                checkPossibleMoves(pawn);
                if(possibleMove.size() > 0) { //there are moves for this particular pawn
                    for(int i=1; i<=possibleMove.size(); i++) {
                        moveList.add(possibleMove.get(possibleMove.size() - i));
                        gameTree.gameNodeList.add(new GameNode(/*gameTree.gameNodeList.size(),
                                gameTree.getCurrentNode().getId(), nodeLevel,*/
                                pawn, createMoveListDeepCopy(moveList), false
                        ));
                        moveList.clear();
                    }
                    possibleMove.clear();
                } //possible move checked, there will be different ones for another board situation
            }
        }
    }

    public void checkGameNodes() {
        for(GameNode node : gameTree.gameNodeList) { //check created scenarios (nodes)
            gameTree.setCurrentNode(node);
            GameNode chosenNode = gameTree.getCurrentNode();

            if(!chosenNode.getIsThereTaking()) { //normal move in this node
                possibleMove.add(chosenNode.moveList.get(0));
                chosenPawn = chosenNode.getPawn();
                Log.v("Pawn moved", ""+chosenPawn.getPosition());
                Log.v("Move done", ""+chosenNode.moveList.get(0));
                makeMove(chosenNode.moveList.get(0));
                possibleMove.clear();
            }
            else { //taking in this move
                for(Integer move : chosenNode.moveList) {
                    possibleMove.add(move);
                    chosenPawn = chosenNode.getPawn();
                    Log.v("Pawn moved", ""+chosenPawn.getPosition());
                    Log.v("Move done", ""+move);
                    makeMove(move); //consider one as brown
                    possibleMove.clear();
                }
            }
            endMove();
            checkForMoves(); //check if white can take after just considered move
            if(mandatoryPawn) gameTree.getCurrentNode().setCanWhiteTakeAfter(true);
            else gameTree.getCurrentNode().setCanWhiteTakeAfter(false);
            Log.v("Is there taking", ""+gameTree.getCurrentNode().getCanWhiteTakeAfter());
            Log.v("How long", ""+gameTree.getCurrentNode().getLengthOfWhiteTaking());
            resetBoardState();
            gameTree.pawnToRestore.clear(); //new node will be analyzed, previously taken pawns are irrelevant
        }
    }

    public void chooseBestGameNode() {
        for(GameNode node : gameTree.gameNodeList) {
            if(!node.getCanWhiteTakeAfter()) gameTree.bestNodeList.add(node);
        }

        if(gameTree.bestNodeList.size() == 0) { //every brown move leads to losing pawn
            int pawnsTaken = 21; //how many;
            for(GameNode node : gameTree.gameNodeList) {
                if(node.getLengthOfWhiteTaking() <= pawnsTaken)
                    pawnsTaken = node.getLengthOfWhiteTaking();
            }
            for(GameNode node : gameTree.gameNodeList) {
                if(node.getLengthOfWhiteTaking() <= pawnsTaken)
                    gameTree.bestNodeList.add(node);
            }

        }
    }
    public void resetBoardState() { //go back with brown pawns and restore white (if any have been taken)
        whiteMove = false;
        for(int i=0; i<playableTile.length; i++) { //get deep copy - current board state
            playableTile[i].setIsTaken(gameTree.getBoardState()[i].getIsTaken());
        }

        if(gameTree.whiteState.size() != whitePawn.size()) {
            for(int i=0; i<gameTree.pawnToRestore.size(); i++) {
                if(whitePawn.size()-1 < gameTree.restoringPawnIndex.get(i)) {
                    whitePawn.add(gameTree.pawnToRestore.get(i));
                }
                else
                    whitePawn.add(gameTree.restoringPawnIndex.get(i), gameTree.pawnToRestore.get(i));
                Log.v("Pawn restored", "" + gameTree.pawnToRestore.get(i).getPosition());
            }
        }

        for(int i=0; i<gameTree.brownState.size(); i++) {
            if(gameTree.brownState.size() == brownPawn.size()) { //no need to restore pawns then;
                if(brownPawn.get(i).getPosition() != gameTree.brownState.get(i).getPosition()) { //just replace them
                    brownPawn.get(i).setPosition(gameTree.brownState.get(i).getPosition());
                    brownPawn.get(i).setIsQueen(gameTree.brownState.get(i).getIsQueen());
                    brownPawn.get(i).setFirstDiagonal(gameTree.brownState.get(i).getFirstDiagonal());
                    brownPawn.get(i).setSecondDiagonal(gameTree.brownState.get(i).getSecondDiagonal());
                }
            }
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
                if (consideredPawn.getIsWhite()) checkMandatoryMove(thisTree.getCurrentNode().getPosition(), -1);
                else checkMandatoryMove(thisTree.getCurrentNode().getPosition(), 1);
            }
        }
        else {
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
                    markPawn(wPawn, view.getId());
                    if(mandatoryPawn) {
                        if(wPawn.getPawnTree() != null) markPossibleMove();
                    }
                    else markPossibleMove(); //there are no mandatory moves
                }
            }
            else makeMove(view.getId()); //white pawn was chosen before - this is setting his destination
            //TO DO: do not send whole object - just int with destination will do
        }
        else {
            if (playableTile[view.getId() - 1].getIsTaken() < 0) {
                possibleMove.clear();
                for (Pawn bPawn : brownPawn) {
                    markPawn(bPawn, view.getId());
                    if(mandatoryPawn) {
                        if(bPawn.getPawnTree() != null) markPossibleMove();
                    }
                    markPossibleMove();
                }
            }
            else makeMove(view.getId());
        }
    }


    public void markPawn(Pawn wPawn, int position) {
        playableTileView[wPawn.getPosition() - 1].getBackground().setAlpha(255);
        if (wPawn.getPosition() == position) {
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

    public void makeMove(int destination) {
        boolean validMove = false;
        if(chosenPawn != null) { //a pawn has been clicked - so it can be moved (or not - if it has no possible moves)
            for (Integer move : possibleMove) {
                if (destination == move) { //chosen tile is a valid move
                    playableTile[chosenPawn.getPosition() - 1].setIsTaken(0); //free previous position
                    if(!chosenPawn.getIsQueen()) {
                        if(whiteMove) playableTile[destination - 1].setIsTaken(1); //set pawn on new position
                        else playableTile[destination - 1].setIsTaken(-1);
                    }
                    else {
                        if(whiteMove) playableTile[destination - 1].setIsTaken(2);
                        else playableTile[destination - 1].setIsTaken(-2);
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
                    addMoveToDatabase(destination); //but to a global variable first
                    chosenPawn.setPosition(destination);
                    validMove = true;
                    break;
                }
            }

            if(gameTree == null && validMove && whiteMove) { //multiple taking for player; for cpu makeMove()
                if(mandatoryPawn) {                          // is called multiple times in checkForMoves -
                    possibleMove.clear();                   // - for every GameTree node respectively
                    takeNumber++;
                    checkPossibleMoves(chosenPawn); //there might be multiple taking
                    if(possibleMove.size() != 0) {
                        markPawn(chosenPawn, destination);
                        markPossibleMove();
                    }
                    else endMove();
                }
                else endMove();
            }
        }
    }

    public void addMoveToDatabase(int destination) { //but first to global variable
        if(gameTree == null || gameTree.getAllNodesFound()) { //it's either player's move or final decision for cpu
            String move = "";
            if(takeNumber == 0) move += chosenPawn.getPosition(); //starting point in notation
            if(chosenPawn.getIsQueen() && takeNumber == 0) move += "(D)"; //notation sign for queen
            if(mandatoryPawn) move += "x"; //notation sign for taking
            else move += "-"; //notation sign for regular move
            move += destination; //ending (or mid) point

            if(whiteMove) whiteMoves += move;
            else brownMoves += move;
        }
    }

    public void endMove() {
        if(chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 0) { //pawn in the last row
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(2); //white pawn promoted;
            chosenPawn.setIsQueen(true); //only if he finished his moves/takes
            whiteMoves += "=D"; //notation sign for pawn promotion
        }
        else if(!chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 9) {
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(-2);
            chosenPawn.setIsQueen(true);
            if(gameTree == null) brownMoves += "=D";
        }
        mandatoryPawn = false;
        takeNumber = 0;
        if(gameTree == null) { //making move
            for(PlayableTile tile : playableTile) {
                boardStates += tile.getIsTaken();
            }
            boardStates += "#"; //end of one state to know where to read it from database
            if(whiteMove) whiteMoves += "#";
            else brownMoves += "#";

            chosenPawn = null;
            possibleMove.clear();
            whiteMove = !whiteMove; //time for next move
            drawPawns(); //display current state of the board
        }
        else { //next level for cpu analysis - no need for drawing changes
            chosenPawn = null;
            possibleMove.clear();
            whiteMove = !whiteMove;
        }

        Log.v("White database", whiteMoves);
        Log.v("Brown database", brownMoves);
        Log.v("Board database", boardStates);

    }

    public void takePawn(int pos) { //one array list if(whiteMove) list = whitePawn;
        playableTile[pos - 1].setIsTaken(0);
        if(whiteMove) {
            //for (Pawn bPawn : brownPawn) {
            for(int i=0; i<brownPawn.size(); i++) {
                if (brownPawn.get(i).getPosition() == pos) {
                    if(gameTree != null) {
                        if (!gameTree.getAllNodesFound()) {
                            gameTree.pawnToRestore.add(brownPawn.get(i));
                            gameTree.restoringPawnIndex.add(i);
                            Log.v("Pawn taken", "Pos: " + gameTree.pawnToRestore.get(0).getPosition() + " Index: " + gameTree.restoringPawnIndex.get(0));
                        }
                    }
                    brownPawn.remove(brownPawn.get(i));
                    break;
                }
            }
        }
        else {
            //for (Pawn wPawn : whitePawn) {
            for(int i=0; i<whitePawn.size(); i++) {
                if (whitePawn.get(i).getPosition() == pos) {
                    if(gameTree!=null) {
                        if (!gameTree.getAllNodesFound()) {
                            gameTree.pawnToRestore.add(whitePawn.get(i));
                            gameTree.restoringPawnIndex.add(i);
                            Log.v("Pawn taken", "Pos: " + gameTree.pawnToRestore.get(0).getPosition() + " Index: " + gameTree.restoringPawnIndex.get(0));
                        }
                    }
                    whitePawn.remove(whitePawn.get(i));
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

