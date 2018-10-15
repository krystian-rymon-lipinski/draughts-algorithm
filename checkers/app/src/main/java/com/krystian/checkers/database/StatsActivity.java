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
        gamesPlayed.setText(getResources().getString(R.string.stats_number, stats[0]));
        gamesWon.setText(getResources().getString(R.string.stats_number, stats[1]));

        if(stats[0] != 0)
        {
            float percentText = ((float)stats[1]/(float)stats[0])*100;
            percent.setText(getResources().getString(R.string.stats_percent, percentText));
        }
        else  percent.setText("-");
    }

    public int[] getDatabaseStats() {
        int[] gamesStats = new int[2];
        try {
            GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("STATS", new String[] {"PLAYED", "WON"}, null, null, null, null, null);

            if(cursor.moveToFirst()) {
                gamesStats[0] = cursor.getInt(0);
                gamesStats[1] = cursor.getInt(1);
            }

            cursor.close();
            db.close();

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych niedostępna", Toast.LENGTH_SHORT);
            toast.show();
        }

        return gamesStats;
    }
}
