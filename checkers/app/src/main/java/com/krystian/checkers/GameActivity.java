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
    View[] tile = new View[NUMBER_OF_TILES];
    View[] playableTile = new View[NUMBER_OF_PLAYABLE_TILES];
    ArrayList<Pawn> whitePawn = new ArrayList<>();
    ArrayList<Pawn> brownPawn = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = (GridLayout) findViewById(R.id.board);
        measureBoard();
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case 0: case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12:
            case 1:
            case 2:
                view.getBackground().setAlpha(70);
                break;
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
        int brownTileCounter = 0; //to set value for brown tiles which will be needed for game mechanics (for example possible moves)
        for(int i=0; i < tile.length; i++) {
            tile[i] = new View(this);
            tile[i].setLayoutParams(new LinearLayout.LayoutParams(width/10, height/10)); //10 tiles x 10 tiles board
            if( (i%2 == 0 && (i/10)%2 == 0) || (i%2 != 0 && (i/10)%2 != 0) )  //which tiles should be white
                tile[i].setBackgroundColor(getResources().getColor(R.color.whiteTile));
            else {
                playableTile[brownTileCounter] = tile[i];
                playableTile[brownTileCounter].setId(brownTileCounter);
                playableTile[brownTileCounter].setOnClickListener(this);
                brownTileCounter++;
            }

            board.addView(tile[i]);
        }

        for(int i=0; i<NUMBER_OF_PAWNS; i++) {
            whitePawn.add(new Pawn( NUMBER_OF_PLAYABLE_TILES-i, true, false));
            brownPawn.add(new Pawn( i+1, false, false));
        }
        drawPawns();
    }

    public void drawPawns() { //will be useful after every move
        for(int i=0; i<NUMBER_OF_PLAYABLE_TILES; i++) {
            for (int j = 0; j < whitePawn.size(); j++) {
                if (whitePawn.get(j).getPosition()-1 == i) {
                    playableTile[i].setBackgroundResource(R.drawable.white_pawn);
                }
            }

            for(int j=0; j<brownPawn.size(); j++) {
                if (brownPawn.get(j).getPosition()-1 == i) {
                    playableTile[i].setBackgroundResource(R.drawable.brown_pawn);
                }
            }
        }
    }
}

