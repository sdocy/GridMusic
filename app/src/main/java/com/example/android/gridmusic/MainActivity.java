package com.example.android.gridmusic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


// GoodTimes font downloaded from 1001fonts.com
// http://www.1001fonts.com/good-times-font.html
//
// All album cover art was downloaded from Itunes for albums / songs which I own

public class MainActivity extends AppCompatActivity {

    // these define the current default grid layout
    private final static int GRID_COLUMN_WIDTH = 50 + 10;
    private final static int GRID_COLUMN_PADDING = 4;
    final static int GRID_COLUMN_TOTALWIDTH = GRID_COLUMN_WIDTH + GRID_COLUMN_PADDING;

    // main menu navigation buttons
    private TextView playGridText;
    private TextView createGridText;
    private TextView editGridText;

    // provides useful tools
    private GeneralTools myTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        initListeners();

        initMisc();
    }

    // get refs to activity views
    private void initViews() {
        playGridText = findViewById(R.id.playGrid);
        createGridText = findViewById(R.id.createGrid);
        editGridText = findViewById(R.id.editGrid);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/good-times.ttf");
        playGridText.setTypeface(custom_font);
        createGridText.setTypeface(custom_font);
        editGridText.setTypeface(custom_font);
    }

    // setup listeners for main menu buttons
    private void initListeners() {

        // listen for `Play Grid` press
        playGridText.setOnClickListener(new OnClickListener() {
            // The code in this method will be executed when the numbers category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the activity
                Intent playGridIntent = new Intent(MainActivity.this, PlayGridActivity.class);

                myTools.vibrate(GeneralTools.touchVibDelay);

                // highlight view text when pressed
                myTools.flashText((TextView) view, R.color.highlightBlue, R.color.MainMenuTextColor, 75);

                // I noticed when going back to the main menu that the playGrid activity
                // continued to run (I saw the toast popup when it completed), so I
                // looked up this code to resume the playGrid activity instead of
                // starting a new one.
                // https://stackoverflow.com/questions/12408719/resume-activity-in-android
                playGridIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(playGridIntent, 0);

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        // for things I haven't implemented
        OnClickListener notImplemented = new OnClickListener() {
            // The code in this method will be executed when the numbers category is clicked on.
            @Override
            public void onClick(View view) {
                myTools.notSupported();
            }
        };

        // only `play / pause` currently work
        createGridText.setOnClickListener(notImplemented);
        editGridText.setOnClickListener(notImplemented);
    }

    // misc setup
    private void initMisc() {
        myTools = new GeneralTools(this);
    }
}
