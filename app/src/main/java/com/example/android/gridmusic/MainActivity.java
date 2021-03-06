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
    // main menu navigation buttons
    private TextView playGridText;
    private TextView createGridText;
    private TextView downloadArtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        initListeners();
    }

    @Override
    public void onBackPressed() {
        // XXX not sure if this is the correct way to let PlayGrid continue when the back button
        // is pressed but it seems to work fairly well
        moveTaskToBack(true);

        //super.onBackPressed();
    }

    // get refs to activity views
    private void initViews() {
        playGridText = findViewById(R.id.mainMenu_PlayGrid);
        createGridText = findViewById(R.id.mainMenu_CreateGrid);
        downloadArtText = findViewById(R.id.mainMenu_DownloadArt);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/good-times.ttf");
        playGridText.setTypeface(custom_font);
        createGridText.setTypeface(custom_font);
        downloadArtText.setTypeface(custom_font);
    }

    // setup listeners for main menu buttons
    private void initListeners() {

        // listener for main menu navigation buttons
        OnClickListener mainMenuListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buttonIntent = null;

                // Create a new intent to open the activity
                switch (view.getId()) {
                    case R.id.mainMenu_PlayGrid :       buttonIntent = new Intent(MainActivity.this, PlayGridActivity.class);
                                                        break;
                    case R.id.mainMenu_CreateGrid :     buttonIntent = new Intent(MainActivity.this, CreateGridActivity.class);
                                                        break;
                    case R.id.mainMenu_DownloadArt :    buttonIntent = new Intent(MainActivity.this, DownloadArtActivity.class);
                }

                if (buttonIntent != null) {
                    GeneralTools.vibrate(MainActivity.this, GeneralTools.touchVibDelay);
                    GeneralTools.flashText(MainActivity.this, (TextView) view, R.color.highlightBlue, R.color.MainMenuTextColor, 75);

                    buttonIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityIfNeeded(buttonIntent, 0);

                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        };

        playGridText.setOnClickListener(mainMenuListener);
        createGridText.setOnClickListener(mainMenuListener);
        downloadArtText.setOnClickListener(mainMenuListener);
    }
}
