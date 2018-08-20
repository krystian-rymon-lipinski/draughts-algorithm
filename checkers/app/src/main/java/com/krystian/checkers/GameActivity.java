package com.krystian.checkers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;

import static android.R.color.white;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int NUMBER_OF_PAWNS = 20; //both white and brown
    public final static int NUMBER_OF_TILES = 100;
    public final static int NUMBER_OF_PLAYABLE_TILES = 50;

    GridLayout board;
    View[] playableTileView = new View[NUMBER_OF_PLAYABLE_TILES];
    PlayableTile[] playableTile = new PlayableTile[NUMBER_OF_PLAYABLE_TILES];
    ArrayList<Pawn> whitePawn = new ArrayList<>();
    ArrayList<Pawn> brownPawn = new ArrayList<>();
    ArrayList<Integer> possibleMove = new ArrayList<>();
    boolean whiteMove = true;
    Pawn chosenPawn; //to set new position for a specific pawn

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

    */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = (GridLayout) findViewById(R.id.board);

        measureBoard(); //and draw it with pawns after that
    }

    public void onClick(View view) {
        if(whiteMove) {
            if (playableTile[view.getId() - 1].getIsTaken() == 1) {
                possibleMove.clear();
                for (Pawn wPawn : whitePawn) {
                    markPawn(wPawn, view);
                }
                markPossibleMove();
            }
            else makeMove(view);
        }

        else {
            if (playableTile[view.getId() - 1].getIsTaken() == -1) {
                possibleMove.clear();
                for (Pawn wPawn : brownPawn) {
                    markPawn(wPawn, view);
                }
                markPossibleMove();
            }
            else makeMove(view);
        }
    }

    public void markPawn(Pawn wPawn, View view) {
        playableTileView[wPawn.getPosition() - 1].getBackground().setAlpha(255);
        if (wPawn.getPosition() == view.getId()) {
            playableTileView[wPawn.getPosition() - 1].getBackground().setAlpha(70);
            chosenPawn = wPawn;
            checkPossibleMoves(wPawn);
        }
    }

    public void markPossibleMove() {
        for(View tile : playableTileView) {  //mark legal moves
            if (playableTile[tile.getId()-1].getIsTaken() == 0)
                tile.setBackgroundColor(getResources().getColor(R.color.brownTile)); //un-mark if just switching pawn
            for (Integer move : possibleMove)
                if (tile.getId() == move)
                    tile.setBackgroundColor(getResources().getColor(R.color.possibleMove));
        }
    }

    public void makeMove(View view) {
        if(chosenPawn != null) { //there is a pawn to move
            for (Integer move : possibleMove) {
                if (view.getId() == move) { //chosen tile is a valid move
                    playableTile[chosenPawn.getPosition() - 1].setIsTaken(0);
                    playableTileView[chosenPawn.getPosition() - 1].getBackground().setAlpha(255);
                    if(whiteMove) playableTile[view.getId() - 1].setIsTaken(1);
                    else playableTile[view.getId() - 1].setIsTaken(-1);

                    if(!chosenPawn.getIsQueen() && Math.abs(chosenPawn.getPosition() - view.getId()) > 6) { //pawn took another pawn
                        int rowImpact = (chosenPawn.getPosition()-1)/5%2; //finding taken pawn differs in odd and even rows
                        int upTaking = chosenPawn.getPosition() - ( (chosenPawn.getPosition() - view.getId()) / 2 + rowImpact);
                        int downTaking = chosenPawn.getPosition() + ((view.getId() - chosenPawn.getPosition()) / 2 + 1 - rowImpact);
                        //upTaking - from higher tile values to smaller; white forward or brown backward
                        //downTaking - reverse

                        for(PlayableTile tile : playableTile) {
                            if(chosenPawn.getPosition() > view.getId() && tile.getValue() == upTaking) {
                                if(tile.getIsTaken() == -1) takePawn(true, tile.getValue()); //white forward taking
                                else if(tile.getIsTaken() == 1) takePawn(false, tile.getValue()); //brown backward taking

                            }
                            else if (chosenPawn.getPosition() < view.getId() && tile.getValue() == downTaking ) {
                                if(tile.getIsTaken() == -1) takePawn(true, tile.getValue()); //white backward taking
                                else if(tile.getIsTaken() == 1) takePawn(false, tile.getValue()); //black forward taking
                            }
                        }
                    }
                    chosenPawn.setPosition(view.getId());
                    chosenPawn = null;
                    whiteMove = !whiteMove;
                    drawPawns();
                }
            }
        }
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
        for(int i=0; i < tile.length; i++) {
            tile[i] = new View(this);
            tile[i].setLayoutParams(new LinearLayout.LayoutParams(width/10, height/10)); //10 x 10 tiles board
            if( (i%2 == 0 && (i/10)%2 == 0) || (i%2 != 0 && (i/10)%2 != 0) )  //which tiles should be white
                tile[i].setBackgroundColor(getResources().getColor(R.color.whiteTile));
            else {
                playableTileView[brownTileCounter] = tile[i];
                playableTileView[brownTileCounter].setId(brownTileCounter+1);
                playableTileView[brownTileCounter].setOnClickListener(this);
                brownTileCounter++;
            }

            board.addView(tile[i]);
        }

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
            else playableTileView[i].setBackgroundResource(0);
        }
    }

    public void checkPossibleMoves(Pawn pawn) {
        int pos = pawn.getPosition(); //it's too long to write it in every condition n times
        int rowImpact = (pos-1)/5%2; //is row even or odd? 0-9 range; pawns pos: 0-4, 5-9... 45-49; helps with modulo

        if (pawn.getIsWhite()) {
            if (!pawn.getIsQueen()) {
                if ( (rowImpact != 0 || pos%5 != 0) && playableTile[pos-1-4-rowImpact].getIsTaken() == 0) //check if pawn is at the side and if not - check if tile is free to go
                    possibleMove.add(pos-4-rowImpact); //rightMove
                if ( (rowImpact != 1 || (pos-1)%5 != 0) && playableTile[pos-1-5-rowImpact].getIsTaken() == 0)
                    possibleMove.add(pos-5-rowImpact); //leftMove
                if( pos > 10 ) {
                    if (pos % 5 != 0 && playableTile[pos - 1 - 4 - rowImpact].getIsTaken() == -1 &&
                            playableTile[pos - 1 - 9].getIsTaken() == 0)
                        possibleMove.add(pos - 9); //rightForwardTake
                    if ((pos - 1) % 5 != 0 && playableTile[pos - 1 - 5 - rowImpact].getIsTaken() == -1 &&
                            playableTile[pos - 1 - 11].getIsTaken() == 0)
                        possibleMove.add(pos - 11); //leftForwardTake
                }
                if(pos <= 40) {
                    if( (pos-1)%5 != 0 && playableTile[pos-1+5-rowImpact].getIsTaken() == -1 &&
                            playableTile[pos-1+9].getIsTaken() == 0)
                        possibleMove.add(pos+9); //leftBackwardTake
                    if( pos%5 != 0 && playableTile[pos-1+6-rowImpact].getIsTaken() == -1 &&
                            playableTile[pos-1+11].getIsTaken() == 0)
                        possibleMove.add(pos+11); //rightBackwardTake
                }
            }
        }
        else {
            if (!pawn.getIsQueen()) {
                if( (rowImpact != 1 || (pos-1)%5 != 0) && playableTile[pos-1+5-rowImpact].getIsTaken() == 0 )
                    possibleMove.add(pos+5-rowImpact); //rightMove; brown perspective - in every brown move
                if( (rowImpact != 0 || pos%5 != 0) && playableTile[pos-1+6-rowImpact].getIsTaken() == 0 )
                    possibleMove.add(pos+6-rowImpact); //leftMove
                if(pos <= 40) {
                    if( (pos-1)%5 != 0 && playableTile[pos-1+5-rowImpact].getIsTaken() == 1 &&
                            playableTile[pos-1+9].getIsTaken() == 0)
                        possibleMove.add(pos+9); //rightForwardTake
                    if( pos%5 != 0 && playableTile[pos-1+6-rowImpact].getIsTaken() == 1 &&
                            playableTile[pos-1+11].getIsTaken() == 0)
                        possibleMove.add(pos+11); //leftForwardTake
                }
                if( pos > 10 ) {
                    if (pos % 5 != 0 && playableTile[pos - 1 - 4 - rowImpact].getIsTaken() == 1 &&
                            playableTile[pos - 1 - 9].getIsTaken() == 0)
                        possibleMove.add(pos - 9); //leftBackwardTake
                    if ((pos - 1) % 5 != 0 && playableTile[pos - 1 - 5 - rowImpact].getIsTaken() == 1 &&
                            playableTile[pos - 1 - 11].getIsTaken() == 0)
                        possibleMove.add(pos - 11); //rightBackwardTake
                }
            }
        }
    }
}

