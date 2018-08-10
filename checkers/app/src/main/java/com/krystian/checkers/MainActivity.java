package com.krystian.checkers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play = (Button) findViewById(R.id.play);
        Button stats = (Button) findViewById(R.id.stats);
        Button history = (Button) findViewById(R.id.history);
        play.setOnClickListener(this);
        stats.setOnClickListener(this);
        history.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play:
                startActivity(new Intent(this, GameActivity.class));
                break;
            case R.id.stats:
                //startActivity(new Intent(this, GameActivity.class));
                break;
            case R.id.history:
                //startActivity(new Intent(this, GameActivity.class));
                break;
            default: break;
        }
    }
}
