package com.example.alex.first_attempt;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class TouchTest extends ActionBarActivity {

    private final String TAG = "TKT";

    private LinearLayout background;
    private Button turn_green;
    private Button turn_red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_test);
        Log.d(TAG,"onCreate");

        background = (LinearLayout) findViewById(R.id.background);
        turn_green = (Button) findViewById(R.id.green);
        turn_red = (Button) findViewById(R.id.red);

        turn_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                background.setBackgroundColor(Color.parseColor("#00FF00"));
            }
        });

        turn_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                background.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG,"onStop");
    }

}
