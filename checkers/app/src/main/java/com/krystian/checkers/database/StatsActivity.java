package com.krystian.checkers.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.krystian.checkers.R;


public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView gamesPlayed = (TextView) findViewById(R.id.played_games);
        TextView gamesWon = (TextView) findViewById(R.id.won_games);
        TextView percent = (TextView) findViewById(R.id.percent);

        int[] stats = getDatabaseStats();
        gamesPlayed.setText(getResources().getString(R.string.games_number, stats[0]));
        gamesWon.setText(getResources().getString(R.string.stats_number, stats[1], stats[2], stats[3]));

        if(stats[0] != 0)
        {
            float wonPercent = ((float)stats[1]/(float)stats[0])*100;
            float drawnPercent = ((float)stats[2]/(float)stats[0])*100;
            float lostPercent = ((float)stats[3]/(float)stats[0])*100;
            percent.setText(getResources().getString(R.string.stats_percent, wonPercent, drawnPercent, lostPercent));
        }
        else  percent.setText("-");
    }

    public int[] getDatabaseStats() {
        int[] gamesStats = new int[4];
        try {
            GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("STATS", new String[] {"PLAYED", "WON", "DRAWN", "LOST"},
                    null, null, null, null, null);

            if(cursor.moveToFirst()) {
                gamesStats[0] = cursor.getInt(0);
                gamesStats[1] = cursor.getInt(1);
                gamesStats[2] = cursor.getInt(2);
                gamesStats[3] = cursor.getInt(3);
            }

            cursor.close();
            db.close();

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, R.string.database_unavailable, Toast.LENGTH_SHORT);
            toast.show();
        }

        return gamesStats;
    }
}
