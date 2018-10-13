package com.krystian.checkers.database;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.krystian.checkers.R;

import java.util.ArrayList;
import java.util.List;

public class GamesListActivity extends ListActivity {

    public int numberOfGames = 0; //in database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TO DO: get names and quantity of them from database
        ArrayList<String> gameNames = new ArrayList<>();
        gameNames.add("#5");
        gameNames.add("#4");
        gameNames.add("#3");
        gameNames.add("#2");
        gameNames.add("#1");
        numberOfGames = gameNames.size();
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<String>(
                this, R.layout.listview_item, gameNames);

        ListView listView = getListView();
        listView.setAdapter(gameAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(this, GameReviewActivity.class);
        intent.putExtra("Game Number", numberOfGames - position); //they are listed from the newest ones
        startActivity(intent);
    }
}
