package com.krystian.checkers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int NUMBER_OF_PAWNS = 20;

    GridLayout board;
    int counter = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = (GridLayout) findViewById(R.id.board);
        board.setOnClickListener(this);
        measureBoard();

    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.board:
                break;
        }
    }

    public void measureBoard() {
        board.post(new Runnable() {
            public void run() {
                int width = board.getWidth();
                int height = board.getHeight();
                Log.v("Wysokość: ", "" + height);
                Log.v("Szerokość: ", "" + width);
                drawBoard(width, height);
            }
        });
    }

    public void drawBoard(int width, int height) {
        View[] tile = new View[100];
        for(int i=0; i < tile.length; i++) {
            tile[i] = new TextView(this);
            tile[i].setLayoutParams(new LinearLayout.LayoutParams(width/10, height/10));
            if( (i%2 == 0 && (i/10)%2 == 0) || (i%2 != 0 && (i/10)%2 != 0) )
                tile[i].setBackgroundColor(getResources().getColor(R.color.whiteTile));
            else
                tile[i].setBackgroundColor(getResources().getColor(R.color.brownTile));

            board.addView(tile[i]);
        }

        ArrayList<Pawn> whitePawns = new ArrayList<>();
        for(int i=0; i<NUMBER_OF_PAWNS; i++) {
            whitePawns.add(new Pawn( (i*2)+1, false, true ));
        }
    }
}

