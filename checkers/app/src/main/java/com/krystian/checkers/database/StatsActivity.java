package com.krystian.checkers.database;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.krystian.checkers.R;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TO DO: get numbers from database
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView gamesPlayed = (TextView) findViewById(R.id.played_games);
        TextView gamesWon = (TextView) findViewById(R.id.won_games);
        TextView percent = (TextView) findViewById(R.id.percent);

        gamesPlayed.setText(getResources().getString(R.string.stats_number, 10));
        gamesWon.setText(getResources().getString(R.string.stats_number, 10));
        percent.setText(getResources().getString(R.string.stats_percent, 10));
    }
}
