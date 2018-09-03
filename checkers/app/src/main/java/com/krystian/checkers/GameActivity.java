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
    int[][] diagonal = new int[19][];
    ArrayList<Pawn> whitePawn = new ArrayList<>();
    ArrayList<Pawn> brownPawn = new ArrayList<>();
    ArrayList<Integer> possibleMove = new ArrayList<>();
    ArrayList<Integer> mandatoryPawn = new ArrayList<>(); //position of a pawn that can take another one; there might be more than 1
    ArrayList<ArrayList<Integer>> mandatoryMove = new ArrayList<ArrayList<Integer>>(); //there might be more possible multiple takings
    boolean whiteMove = true;
    Pawn chosenPawn; //to set new position for a specific pawn
    int pawnToTake; //pawn taken by queen


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = (GridLayout) findViewById(R.id.board);
        setDiagonals();
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

        checkMandatoryMove();
    }

    public void onClick(View view) {
        if(whiteMove) {
            if (playableTile[view.getId() - 1].getIsTaken() > 0) {
                //white pawn (or queen) has just been clicked
                possibleMove.clear();
                for (Pawn wPawn : whitePawn) {
                    markPawn(wPawn, view);
                    if(mandatoryPawn.size() != 0) {
                        for(Integer mPawn : mandatoryPawn) {
                            if(wPawn.getPosition() == mPawn) markPossibleMove();
                        }
                    }
                    else markPossibleMove(); //there are no mandatory moves
                }
            }
            else
                makeMove(view); //white pawn was chosen before - this is setting his destination
            //TO DO: do not send whole object - just int with destination will do
        }
        else {
            if (playableTile[view.getId() - 1].getIsTaken() < 0) {
                possibleMove.clear();
                for (Pawn bPawn : brownPawn) {
                    markPawn(bPawn, view);
                    if(mandatoryPawn.size() != 0) {
                        for(Integer mPawn : mandatoryPawn) {
                            if(bPawn.getPosition() == mPawn) markPossibleMove();
                        }
                    }
                }
                markPossibleMove();
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
        if(chosenPawn != null) { //there is a pawn to move
            for (Integer move : possibleMove) {
                if (view.getId() == move) { //chosen tile is a valid move
                    playableTile[chosenPawn.getPosition() - 1].setIsTaken(0);
                    playableTileView[chosenPawn.getPosition() - 1].getBackground().setAlpha(255);
                    if(!chosenPawn.getIsQueen()) {
                        if(whiteMove) playableTile[view.getId() - 1].setIsTaken(1);
                        else playableTile[view.getId() - 1].setIsTaken(-1);
                    }
                    else {
                        if(whiteMove) playableTile[view.getId() - 1].setIsTaken(2);
                        else playableTile[view.getId() - 1].setIsTaken(-2);
                    }


                    if(!chosenPawn.getIsQueen() && Math.abs(chosenPawn.getPosition() - view.getId()) > 6) { //pawn took another pawn
                        int rowImpact = (chosenPawn.getPosition()-1)/5%2; //finding taken pawn differs in odd and even rows
                        int upTaking = chosenPawn.getPosition() - ( (chosenPawn.getPosition() - view.getId()) / 2 + rowImpact);
                        int downTaking = chosenPawn.getPosition() + ((view.getId() - chosenPawn.getPosition()) / 2 + 1 - rowImpact);
                        //upTaking - from higher tile values to smaller; white forward or brown backward
                        //downTaking - reverse

                        for(PlayableTile tile : playableTile) {
                            if(chosenPawn.getPosition() > view.getId() && tile.getValue() == upTaking) {
                                if(tile.getIsTaken() < 0 ) takePawn(true, tile.getValue()); //white forward taking
                                else if(tile.getIsTaken() > 0) takePawn(false, tile.getValue()); //brown backward taking
                            }
                            else if (chosenPawn.getPosition() < view.getId() && tile.getValue() == downTaking ) {
                                if(tile.getIsTaken() < 0) takePawn(true, tile.getValue()); //white backward taking
                                else if(tile.getIsTaken() > 0) takePawn(false, tile.getValue()); //black forward taking
                            }
                        }
                    }
                    else if(chosenPawn.getIsQueen()) {
                        for(PlayableTile tile : playableTile) {
                            if(tile.getValue() == pawnToTake) {
                                if(tile.getIsTaken() < 0) takePawn(true, tile.getValue());
                                else takePawn(false, tile.getValue());
                            }
                        }
                    }
                    chosenPawn.setPosition(view.getId());
                    validMove = true;
                    break;
                }
            }

            if(validMove) {
                if(mandatoryPawn.size() != 0) {
                    pawnToTake = 0;
                    possibleMove.clear();
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
        if(chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 0) { //white pawn promoted
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(2);
            chosenPawn.setIsQueen(true);
        }//pawn in the last row
        else if(!chosenPawn.getIsWhite() && !chosenPawn.getIsQueen() && (chosenPawn.getPosition()-1)/5 == 9) {
            playableTile[chosenPawn.getPosition() - 1].setIsTaken(-2);
            chosenPawn.setIsQueen(true);
        }
        chosenPawn = null;
        whiteMove = !whiteMove;
        mandatoryPawn.clear();
        drawPawns();
    }

    public void takePawn(boolean isWhite, int pos) { //isWhite == true means white just took a brown pawn
        playableTile[pos - 1].setIsTaken(0);
        if(isWhite) {
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

        if (!pawn.getIsQueen()) {
            if (pawn.getIsWhite()) {
                if(mandatoryPawn.size() == 0) checkWhiteMove(pos, rowImpact); //left/right; there are no mandatory takes
                else {
                    checkUpTaking(pawn, rowImpact, -1); //forward take
                    checkUpTaking(pawn, rowImpact, -2); //forward take
                    checkDownTaking(pawn, rowImpact, -1); //backward take
                    checkDownTaking(pawn, rowImpact, -2); //backward take
                }
            }
            else {
                if(mandatoryPawn.size() == 0) checkBrownMove(pos, rowImpact); //left/right
                else {
                    checkUpTaking(pawn, rowImpact, 1); //backward take
                    checkUpTaking(pawn, rowImpact, 2); //backward take
                    checkDownTaking(pawn, rowImpact, 1); //forward take
                    checkDownTaking(pawn, rowImpact, 2); //forward take
                }
            }
        }
        else {
            checkDiagonals(pawn);
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
            possibleMove.add(pos + 5 - rowImpact); //rightMove; brown perspective - in every brown move
        if ((rowImpact != 0 || pos % 5 != 0) && playableTile[pos - 1 + 6 - rowImpact].getIsTaken() == 0)
            possibleMove.add(pos + 6 - rowImpact); //leftMove
    }

    public void checkUpTaking(Pawn pawn, int rowImpact, int takenPawn) { //taken pawn: -1 = brown, 1 = white
        if (pawn.getPosition() > 10) { //if you'll try to take from the last row - array out of bounds
            if (pawn.getPosition() % 5 != 0 && playableTile[pawn.getPosition() - 1 - 4 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[pawn.getPosition() - 1 - 9].getIsTaken() == 0) {
                if (chosenPawn != null) possibleMove.add(pawn.getPosition() - 9); //checking mandatory taking... or showing possible moves
                else //checking possible mandatory moves before clicking pawn
                    if(pawn.getOldPosition() != pawn.getPosition() - 9) //no take-reverse take possibilities
                        //checkLongestTake(pawn, pawn.getPosition() - 9);
                     mandatoryPawn.add(pawn.getPosition());
            }
            if ((pawn.getPosition() - 1) % 5 != 0 && playableTile[pawn.getPosition() - 1 - 5 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[pawn.getPosition() - 1 - 11].getIsTaken() == 0) {
                if (chosenPawn != null) possibleMove.add(pawn.getPosition() - 11);
                else
                    if(pawn.getOldPosition() != pawn.getPosition() - 11) //no take-reverse take possibilities
                        //checkLongestTake(pawn, pawn.getPosition() - 11);
                     mandatoryPawn.add(pawn.getPosition());

            }
        }
    }

    public void checkDownTaking(Pawn pawn, int rowImpact, int takenPawn) {
        if (pawn.getPosition() <= 40) {
            if ((pawn.getPosition() - 1) % 5 != 0 && playableTile[pawn.getPosition() - 1 + 5 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[pawn.getPosition() - 1 + 9].getIsTaken() == 0) {
                if (chosenPawn != null) possibleMove.add(pawn.getPosition() + 9);
                else
                    if(pawn.getOldPosition() != pawn.getPosition() - 9) //no take-reverse take possibilities
                        //checkLongestTake(pawn, pawn.getPosition() - 9);
                     mandatoryPawn.add(pawn.getPosition());
            }
            if (pawn.getPosition() % 5 != 0 && playableTile[pawn.getPosition() - 1 + 6 - rowImpact].getIsTaken() == takenPawn &&
                    playableTile[pawn.getPosition() - 1 + 11].getIsTaken() == 0) {
                if (chosenPawn != null) possibleMove.add(pawn.getPosition() + 11);
                else
                    if(pawn.getOldPosition() != pawn.getPosition() - 9) //no take-reverse take possibilities
                        //checkLongestTake(pawn, pawn.getPosition() - 9);
                     mandatoryPawn.add(pawn.getPosition());
            }
        }
    }

    public void checkMandatoryMove() {
        if(whiteMove) {
            for(Pawn wPawn : whitePawn) {
                if(!wPawn.getIsQueen()) {
                    int rowImpact = (wPawn.getPosition()-1)/5%2;
                    checkUpTaking(wPawn, rowImpact, -1);
                    checkUpTaking(wPawn, rowImpact, -2);
                    checkDownTaking(wPawn, rowImpact, -1);
                    checkDownTaking(wPawn, rowImpact, -2);
                }
                else checkDiagonals(wPawn);
            }
        }
        else {
            for(Pawn bPawn : brownPawn) {
                if(!bPawn.getIsQueen()) {
                    int rowImpact = (bPawn.getPosition()-1)/5%2;
                    checkUpTaking(bPawn, rowImpact, 1);
                    checkUpTaking(bPawn, rowImpact, 2);
                    checkDownTaking(bPawn, rowImpact, 1);
                    checkDownTaking(bPawn, rowImpact, 2);
                }
                else checkDiagonals(bPawn);
            }
        }
    }
/*
    public void checkLongestTake(Pawn pawn, int pos) {
        ArrayList<Integer> combination = new ArrayList<>();
        int rowImpact = (pos-1)/5%2; //possible position of a pawn during multiple taking is changing
        if(pawn.getTakeLength() == 0) {
            mandatoryMove.add(combination);
            combination.add(pos);
        }
        else pawn.setOldPosition(pos);
        pawn.setTakeLength(pawn.getTakeLength() + 1);
        pawn.setPosition(pos);
        //pawn.setPossiblePosition(pos);
        checkDownTaking(pawn, rowImpact, -1);
        checkUpTaking(pawn, rowImpact, -1);
    }*/

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

    public void checkDiagonals(Pawn pawn) {
        int firstDiagonalIndex = 0;
        int secondDiagonalIndex = 0;
        for (int i = 0; i < diagonal.length; i++) {
            for (int j = 0; j < diagonal[i].length; j++) {
                if (i < 9 && diagonal[i][j] == pawn.getPosition()) {
                    pawn.setFirstDiagonal(diagonal[i]);
                    firstDiagonalIndex = j;
                } else if (i >= 9 && diagonal[i][j] == pawn.getPosition()) {
                    pawn.setSecondDiagonal(diagonal[i]);
                    secondDiagonalIndex = j;
                }
            }
        }

        Log.v("First Index", ""+firstDiagonalIndex);
        Log.v("Second Index", ""+secondDiagonalIndex);

        if(chosenPawn == null) checkQueenTakes(pawn, firstDiagonalIndex, secondDiagonalIndex);
        else {
            if(mandatoryPawn.size() == 0) checkQueenMoves(pawn, firstDiagonalIndex, secondDiagonalIndex);
            else
                if(pawnToTake != 0) checkQueenFinish(pawn, pawnToTake); //which position is available for queen after taking
                //pawnToTake == 0 means there is no pawn to take for queen
        }

    }

    public void checkQueenTakes(Pawn pawn, int firstDiagonalIndex, int secondDiagonalIndex) {
        if(whiteMove) {
            while (firstDiagonalIndex > 1) {
                //Log.v("First", ""+firstDiagonalIndex);
                if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() < 0) {
                    if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break; //two pawns in a row; queen blocked
                    else {
                        mandatoryPawn.add(pawn.getPosition());
                        pawnToTake = pawn.getFirstDiagonal()[firstDiagonalIndex - 1];
                        break;
                    }
                }
                else if(playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() == 0) firstDiagonalIndex--;
                else break; //white pawn; queen blocked
            }
            Log.v("First", ""+firstDiagonalIndex);
            while (secondDiagonalIndex > 1) {
                if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() < 0) {
                    if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex - 2] - 1].getIsTaken() != 0)
                        break;
                    else {
                        mandatoryPawn.add(pawn.getPosition());
                        pawnToTake = pawn.getSecondDiagonal()[secondDiagonalIndex - 1];
                        break;
                    }
                }
                else if(playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() == 0) secondDiagonalIndex--;
                else break;
            }
            Log.v("Second", ""+secondDiagonalIndex);
            while (firstDiagonalIndex < pawn.getFirstDiagonal().length - 2) { //there's a mistake somewhere here if there's taking
                //Log.v("First", ""+firstDiagonalIndex);
                if (pawn.getFirstDiagonal()[firstDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                    if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() < 0) {
                        if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex + 2] - 1].getIsTaken() != 0)
                            break;
                        else {
                            mandatoryPawn.add(pawn.getPosition());
                            pawnToTake = pawn.getFirstDiagonal()[firstDiagonalIndex + 1];
                            break;
                        }
                    }
                    else if(playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() == 0) firstDiagonalIndex++;
                    else break;
                }
                else firstDiagonalIndex++;
            }
            Log.v("First", ""+firstDiagonalIndex);
            while (secondDiagonalIndex < pawn.getSecondDiagonal().length - 2) {
                if (pawn.getSecondDiagonal()[secondDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                    if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() < 0) {
                        if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex + 2] - 1].getIsTaken() != 0)
                            break;
                        else {
                            mandatoryPawn.add(pawn.getPosition());
                            pawnToTake = pawn.getSecondDiagonal()[secondDiagonalIndex + 1];
                            break;
                        }
                    }
                    else if(playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() == 0) secondDiagonalIndex++;
                    else break;
                }
                else secondDiagonalIndex++;
            }
            Log.v("Second", ""+secondDiagonalIndex);
        }
        else {  //TO DO: brown queen taking is old; white is ok
            while (firstDiagonalIndex > 1) {
                if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 1].getIsTaken() > 0) {
                    if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex - 1] - 2].getIsTaken() != 0)
                        break; //two pawns in a row; queen blocked
                    else {
                        mandatoryPawn.add(pawn.getFirstDiagonal()[firstDiagonalIndex]);
                        pawnToTake = pawn.getFirstDiagonal()[firstDiagonalIndex - 1];
                    }
                } else firstDiagonalIndex--;

            }
            while (secondDiagonalIndex > 1) {
                if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 1].getIsTaken() > 0) {
                    if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex - 1] - 2].getIsTaken() != 0)
                        break;
                    else {
                        mandatoryPawn.add(pawn.getSecondDiagonal()[secondDiagonalIndex]);
                        pawnToTake = pawn.getSecondDiagonal()[secondDiagonalIndex - 1];
                    }
                }
                secondDiagonalIndex--;
            }

            while (firstDiagonalIndex < pawn.getFirstDiagonal().length - 2) {
                if (pawn.getFirstDiagonal()[firstDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                    if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex + 1] - 1].getIsTaken() > 0) {
                        if (playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex + 2] - 1].getIsTaken() != 0)
                            break;
                        else {
                            mandatoryPawn.add(pawn.getFirstDiagonal()[firstDiagonalIndex]);
                            pawnToTake = pawn.getFirstDiagonal()[firstDiagonalIndex + 1];
                        }
                    }
                }
                firstDiagonalIndex++;
            }

            while (secondDiagonalIndex < pawn.getSecondDiagonal().length - 2) {
                if (pawn.getSecondDiagonal()[secondDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                    if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex + 1] - 1].getIsTaken() > 0) {
                        if (playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex + 2] - 1].getIsTaken() != 0)
                            break;
                        else {
                            mandatoryPawn.add(pawn.getSecondDiagonal()[secondDiagonalIndex]);
                            pawnToTake = pawn.getSecondDiagonal()[secondDiagonalIndex + 1];
                        }
                    }
                }
                secondDiagonalIndex++;
            }
        }

        Log.v("Pawn to take:", ""+pawnToTake);
        for(int mPawn : mandatoryPawn) Log.v("Mandatory pawn", ""+mPawn);
    }

    public void checkQueenFinish(Pawn pawn, int pawnToTake) {
        Log.v("Queen finishes", "Yeeeeeah!");
        int pawnToTakeFirstIndex = 0;
        int pawnToTakeSecondIndex = 0;
        for(int i=0; i<pawn.getFirstDiagonal().length; i++) {
            if(pawnToTake == pawn.getFirstDiagonal()[i]) pawnToTakeFirstIndex = i;
        }
        for(int i=0; i<pawn.getSecondDiagonal().length; i++) {
            if(pawnToTake == pawn.getSecondDiagonal()[i]) pawnToTakeSecondIndex = i;
        }

        Log.v("Taken pawn 1 index", ""+pawnToTakeFirstIndex);
        Log.v("Taken pawn 2 index", ""+pawnToTakeSecondIndex);

        if(pawn.getPosition() > pawnToTake) {
            while(pawnToTakeFirstIndex > 0) {
                if(playableTile[pawn.getFirstDiagonal()[pawnToTakeFirstIndex-1] - 1].getIsTaken() == 0) //tile behind a pawn is free and can be taken by queen
                    possibleMove.add(pawn.getFirstDiagonal()[pawnToTakeFirstIndex - 1]);
                else break;
                pawnToTakeFirstIndex--;
            }
            while(pawnToTakeSecondIndex > 0) {
                if(playableTile[pawn.getSecondDiagonal()[pawnToTakeSecondIndex-1] - 1].getIsTaken() == 0) //tile behind a pawn is free and can be taken by queen
                    possibleMove.add(pawn.getSecondDiagonal()[pawnToTakeSecondIndex - 1]);
                else break;
                pawnToTakeSecondIndex--;
            }
        }

        for(int i=0; i<pawn.getFirstDiagonal().length; i++) {
            if(pawnToTake == pawn.getFirstDiagonal()[i]) pawnToTakeFirstIndex = i;
        }
        for(int i=0; i<pawn.getSecondDiagonal().length; i++) {
            if(pawnToTake == pawn.getSecondDiagonal()[i]) pawnToTakeSecondIndex = i;
        }

        if(pawn.getPosition() < pawnToTake) {
            while(pawnToTakeFirstIndex < pawn.getFirstDiagonal().length - 1) {
                if(playableTile[pawn.getFirstDiagonal()[pawnToTakeFirstIndex+1] - 1].getIsTaken() == 0) //tile behind a pawn is free and can be taken by queen
                    possibleMove.add(pawn.getFirstDiagonal()[pawnToTakeFirstIndex + 1]);
                else break;
                pawnToTakeFirstIndex++;
            }
            while(pawnToTakeSecondIndex < pawn.getSecondDiagonal().length - 1) {
                if(playableTile[pawn.getSecondDiagonal()[pawnToTakeSecondIndex+1] - 1].getIsTaken() == 0) //tile behind a pawn is free and can be taken by queen
                    possibleMove.add(pawn.getSecondDiagonal()[pawnToTakeSecondIndex + 1]);
                else break;
                pawnToTakeSecondIndex++;
            }
        }
        for(int poss : possibleMove) {
            Log.v("Move", ""+poss);
        }
    }

    public void checkQueenMoves(Pawn pawn, int firstDiagonalIndex, int secondDiagonalIndex) {

        while(firstDiagonalIndex != 0) {
            if(playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex-1] - 1].getIsTaken() == 0)
                possibleMove.add(pawn.getFirstDiagonal()[firstDiagonalIndex-1]);
            else break;
            firstDiagonalIndex--;

        }
        while(secondDiagonalIndex != 0) {
            if(playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex-1] - 1].getIsTaken() == 0)
                possibleMove.add(pawn.getSecondDiagonal()[secondDiagonalIndex-1]);
            else break;
            secondDiagonalIndex--;
        }
        while(firstDiagonalIndex != pawn.getFirstDiagonal().length - 1) {
            if(pawn.getFirstDiagonal()[firstDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                if(playableTile[pawn.getFirstDiagonal()[firstDiagonalIndex+1] - 1].getIsTaken() == 0)
                    possibleMove.add(pawn.getFirstDiagonal()[firstDiagonalIndex+1]);
                else break;
            }
            firstDiagonalIndex++;
        }
        while(secondDiagonalIndex != pawn.getSecondDiagonal().length - 1) {
            if(pawn.getSecondDiagonal()[secondDiagonalIndex] >= pawn.getPosition()) { //restoring index after decrementing it
                if(playableTile[pawn.getSecondDiagonal()[secondDiagonalIndex+1] - 1].getIsTaken() == 0)
                    possibleMove.add(pawn.getSecondDiagonal()[secondDiagonalIndex+1]);
                else break;
            }
            secondDiagonalIndex++;
        }
    }
}

