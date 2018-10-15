package com.krystian.checkers.database;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.krystian.checkers.R;

import java.util.ArrayList;
import java.util.List;

public class GamesListActivity extends ListActivity {

    SQLiteDatabase db;
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListFromDatabase();
    }

    public void getListFromDatabase() {
        ListView gamesList = getListView();
        try {
            GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
            db = dbHelper.getReadableDatabase();
            cursor = db.query("GAMES", new String[]{"_id", "NAME"}, null, null, null, null, "NAME DESC");
            CursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.listview_item,
                    cursor, new String[]{"NAME"}, new int[]{R.id.list_text}, 0);
            Log.v("List size", ""+listAdapter.getCount());
            gamesList.setAdapter(listAdapter);
        } catch(SQLiteException e) {
            Toast.makeText(this, "Nie udało się nawiązać połączenia z bazą danych", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        long tableSize = DatabaseUtils.queryNumEntries(db, "GAMES");
        Intent intent = new Intent(this, GameReviewActivity.class);
        intent.putExtra("GameNumber", tableSize - position); //they are listed from the newest ones (with bigger index)
        startActivity(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        if(cursor != null) cursor.close();
        db.close();
    }
}
