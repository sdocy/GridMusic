package com.example.android.gridmusic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.os.Handler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// contains all code for playing a Grid
public class PlayGridActivity extends AppCompatActivity implements TheGridClicks {

    private final int CURR_STATE_COLOR = -1;

    private Song currentSong;

    private int numGridCols;             // how many columns widde is the Grid?
    private int numGridElems;            // total elements in the grid, including blank grids
    private int numPlayable;             // number of non-blank grids
    private int numNotPlayed;            // how many grids have not been played yet?

    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnCompletionListener songDoneListener;

    private AudioManager audioMgr;
    private AudioManager.OnAudioFocusChangeListener audioFocusListener = null;

    private GridAdapter gridAdapter;        // view adapter for the Grid
    private RecyclerView theGridView;

    private int prevGridIndex = -1;         // grid we played last time
    private int currGridIndex = -1;         // grid we are playing now, or have just chosen to play
    private int nextGridIndex = -1;         // grid to play next
    private int playEntireGridIndex = -1;   // when playing entire grids, this stores the track we are currently playing

    // music control and display
    private boolean playingMusic = false;   // has user pressed play?
    private ImageView controlPlay;
    private ImageView controlStop;
    private ImageView controlSkipRewind;
    private ImageView controlSkipFastForward;
    private TextView playMusicText;                 // shows playing/paused and artist info
    private String playingMusicString;
    private TextView playSongText;                  // shows title of song being played

    private ImageView backArrowButton;              // go back to main menu
    private ImageView optionsButton;               // open options layout
    private NavigationView optionsDrawer;
    private DrawerLayout optionsDrawerLayout;
    private boolean playEntireGrid = false;         // play all songs on a grid before choosing next grid?
    private ImageView infoButton;

    private Random RNG = new Random();

    // the Grid
    private List<GridElement> theGrid = new ArrayList<>();
    private String saveFileCurrentlyLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_grid);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        initMisc();

        initGrid();

        initAdapters();

        initListeners();

        initTips();
    }

    @Override
    public void onBackPressed() {
        // XXX not sure if this is the correct way to let PlayGrid continue when the back button
        // is pressed but it seems to work fairly well
        goBackToMainMenu();

        //super.onBackPressed();
    }

    // setup the Grid
    private void initGrid() {
        String filename = getFirstSaveFile();
        loadGridArray(SaveFileLoader.loadGrid(this, filename));
        saveFileCurrentlyLoaded = filename;
    }

    // get view references and setup GridView adapter
    private void initViews() {
        theGridView = findViewById(R.id.playGrid_TheGrid);

        // views for music control and display
        controlPlay = findViewById(R.id.playGrid_Control_Play);
        controlStop = findViewById(R.id.playGrid_Control_Stop);
        controlSkipRewind = findViewById(R.id.playGrid_Control_SkipRewind);
        controlSkipFastForward = findViewById(R.id.playGrid_Control_SkipFastforward);
        playMusicText = findViewById(R.id.playGrid_MusicText);
        playSongText = findViewById(R.id.playGrid_SongText);

        backArrowButton = findViewById(R.id.playGrid_BackArrow);
        optionsButton = findViewById(R.id.playGrid_SettingsButton);
        optionsDrawer = findViewById(R.id.playGrid_OptionsDrawer);
        optionsDrawerLayout = findViewById(R.id.playGrid_DrawerLayout);
        infoButton = findViewById(R.id.playGrid_InfoButton);
    }

    // misc setup
    private void initMisc() {
        audioMgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    // set up view adapters and layout managers
    private void initAdapters() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        theGridView.setHasFixedSize(true);

        // use a grid layout manager
        theGridView.setLayoutManager(new GridLayoutManager(this, numGridCols));

        gridAdapter = new GridAdapter(this, theGrid);
        theGridView.setAdapter(gridAdapter);
    }

    // init OnClickListeners
    private void initListeners() {
        View.OnClickListener UIClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.playGrid_Control_Play :               playPause(true);
                        break;

                    case R.id.playGrid_Control_Stop :               stopMusic(true);
                        break;

                    case R.id.playGrid_Control_SkipFastforward :    skipFastForward();
                        break;

                    case R.id.playGrid_BackArrow :                  goBackToMainMenu();
                        break;

                    case R.id.playGrid_SettingsButton :             openSettings(Gravity.START);
                        break;

                    case R.id.playGrid_InfoButton :                 openSettings(Gravity.END);
                        break;

                    default :                                       GeneralTools.notSupported(PlayGridActivity.this);
                }
            }
        };

        controlPlay.setOnClickListener(UIClickListener);
        controlStop.setOnClickListener(UIClickListener);
        controlSkipFastForward.setOnClickListener(UIClickListener);
        backArrowButton.setOnClickListener(UIClickListener);
        optionsButton.setOnClickListener(UIClickListener);
        infoButton.setOnClickListener(UIClickListener);

        // features currently not implemented
        controlSkipRewind.setOnClickListener(UIClickListener);

        songDoneListener = (new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                if (playEntireGrid) {
                    //noinspection StatementWithEmptyBody
                    while (playAllSongs(theGrid.get(currGridIndex)) != 0);
                } else {
                    pickNextGrid();
                }
            }
        });

        audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    // Pause playback because your Audio Focus was
                    // temporarily stolen, but will be back soon.
                    // i.e. for a phone call
                    if (mediaPlayer != null) {
                        playPause(false);
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // Stop playback, because you lost the Audio Focus.
                    // i.e. the user started some other playback app
                    // Remember to unregister your controls/buttons here.
                    // And release the kra — Audio Focus!
                    // You’re done.
                    stopMusic(false);
                } else if (focusChange ==
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    // Lower the volume, because something else is also
                    // playing audio over you.
                    // i.e. for notifications or navigation directions
                    // Depending on your audio playback, you may prefer to
                    // pause playback here instead. You do you.
                    if (mediaPlayer != null) {
                        playPause(false);
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Resume playback, because you hold the Audio Focus
                    // again!
                    // i.e. the phone call ended or the nav directions
                    // are finished
                    // If you implement ducking and lower the volume, be
                    // sure to return it to normal here, as well.
                    if (mediaPlayer != null) {
                        playPause(false);
                    }
                }
            }
        };

        optionsDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case (R.id.playGridDrawer_OneSongPerGrid) :     menuItem.setChecked(true);
                                                                            changeSongsPerGrid(false);
                                                                            break;

                            case (R.id.playGridDrawer_AllSongsPerGrid) :    menuItem.setChecked(true);
                                                                            changeSongsPerGrid(true);
                                                                            break;

                            case (R.id.playGridDrawer_ResetPlayedGrids) :   resetGrids(true);
                                                                            break;

                            case (R.id.playGridDrawer_LoadGrid) :           showLoadMenu();
                                                                            break;

                            default :                                       GeneralTools.notSupported(PlayGridActivity.this);
                        }

                        // close drawer when item is tapped
                        optionsDrawerLayout.closeDrawers();

                        return true;
                    }
                });
    }

    // display a tip for playing a Grid
    private void initTips() {
        // create resource name for a tip
        int tip = RNG.nextInt(getResources().getInteger(R.integer.numPlayTips));
        String tipName = getString(R.string.playTipName) + tip;

        GeneralTools.showToast(this, getString(R.string.tip) + " "
                + getString(getResources().getIdentifier(tipName, "string", getPackageName())));
    }

    // method for GridAdapter to call for onClick()
    public void theGridOnClick(int position) {
        userChoseNextGrid(position);
    }

    // method for GridAdapter to call for onLongClick()
    public void theGridOnLongClick(int position) {
        userToggledPlayState(position);
    }

    // ***************************
    // Methods called by listeners
    // ***************************

    // user pressed play / pause or we lost / gained audio focus transiently, start or stop music play
    private void playPause(boolean fromUser) {
        if (fromUser) {
            // this call is from a user click, provide tactile feedback
            GeneralTools.vibrate(this, GeneralTools.touchVibDelay);



            if (!playingMusic) {
                // user wants to play, request audio focus
                if (audioMgr.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.e("ERROR", "Request for audio focus was denied.");
                    return;
                }
            } else {
                // user is pausing, release audio focus
                audioMgr.abandonAudioFocus(audioFocusListener);
            }
        }

        if (!playingMusic) {
            // music not yet started or paused
            playingMusic = true;

            // flashy flash the `play` button when you press it
            controlPlay.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlPlay), 100);

            if (currGridIndex == -1) {
                // we are just starting out, pick a starting grid
                pickStartingGrid();
            } else {
                // we were paused, continue where we left off
                playMusicText.setText(playingMusicString);
                mediaPlayer.start();
            }
        } else {
            // music currently playing, pause it
            playingMusic = false;

            // flashy flash the `play` button when you press it
            controlPlay.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlPlay), 100);

            playMusicText.setText(R.string.paused);
            mediaPlayer.pause();
        }
    }

    // user pressed stop or we lost audio focus
    private void stopMusic(boolean fromUser) {
        if (mediaPlayer != null) {
            if (fromUser) {
                // this call is from a user click, provide tactile feedback
                GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

                // flashy flash the `stop` button when you press it
                controlStop.setColorFilter(Color.WHITE);
                handler.postDelayed(turnOffFilter(controlStop), 100);
            }

            playingMusic = false;

            GridElement grid = theGrid.get(currGridIndex);
            grid.played = true;
            setGridColor(grid, CURR_STATE_COLOR, true);

            currGridIndex = -1;
            currentSong = null;
            playEntireGridIndex = -1;

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            // release audio focus
            audioMgr.abandonAudioFocus(audioFocusListener);
        }
    }

    // user pressed skip-fastforward
    // stop song and call completion listener to trigger picking of next song
    private void skipFastForward() {
        if (mediaPlayer != null) {
            GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

            // we might be paused, transition back to playing
            playingMusic = true;

            // flashy flash the `skip-fastfforward` button when you press it
            controlSkipFastForward.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlSkipFastForward), 100);

            mediaPlayer.stop();

            // trigger song completion so the app will pick the next song
            songDoneListener.onCompletion(mediaPlayer);
        }
    }

    // user clicked a grid, choose it to play next
    private void userChoseNextGrid(int position) {
        GridElement grid = theGrid.get(position);

        // ignore presses on blank grids
        if (grid.isEmpty()) {
            return;
        }

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        if (nextGridIndex != -1) {
            GridElement currNextGrid = theGrid.get(nextGridIndex);
            // player chose a second grid to play reset the previous choice
            setGridColor(currNextGrid, CURR_STATE_COLOR, true);
        }

        if (nextGridIndex == position) {
            // user clicked an already chosen grid, de-select it
            nextGridIndex = -1;
            return;
        }

        nextGridIndex = position;

        if (!playingMusic && (currGridIndex == -1)) {
            // we are not playing anything, so go ahead and start playing with user-chosen grid
            playPause(false);
        } else {
            // we are currently playiong something or paused, so choose this grid as next play
            setGridColor(grid, R.color.filterNextToPlay, true);
        }
    }

    // user long-pressed a grid, switch its play state
    private void userToggledPlayState(int position) {
        GridElement g = theGrid.get(position);

        // ignore presses on blank grids and the currently playing grid
        if (g.isEmpty() || (position == currGridIndex)) {
            return;
        }

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        if (g.played) {
            numNotPlayed++;
        } else {
            numNotPlayed--;
        }

        g.played = !g.played;
        setGridColor(g, CURR_STATE_COLOR, true);
    }

    // user pressed back arrow or back button, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(PlayGridActivity.this, MainActivity.class);

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        startActivity(mainMenuIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // user pressed settings/ info icon, open the appropriate drawer
    private void openSettings(int gravity) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        optionsDrawerLayout.openDrawer(gravity);
    }

    // set all played grids to not-played
    // All grids are reset if we are not currently playing music.
    // If we are playing music, then the currently playing grid is not reset.
    private void resetGrids(boolean fromUser) {
        if (fromUser) {
            GeneralTools.vibrate(this, GeneralTools.touchVibDelay);
            GeneralTools.showToast(this, PlayGridActivity.this.getString(R.string.resettingPlayed));
        }

        // find list index for the randomly chosen playable grid element
        for (int index = 0; index < numGridElems; index++) {
            GridElement g = theGrid.get(index);

            // skip empty and already played grid elements
            if (g.isEmpty())  {
                continue;
            }

            if ((index == currGridIndex) && (playingMusic)) {
                // don't reset the grid that is currently playing
                continue;
            }

            if (g.played) {
                g.played = false;
                setGridColor(g, CURR_STATE_COLOR, false);
                g.setAllSongsToNotPlayed();
            }
        }

        // minus one to account for the song currently being played
        numNotPlayed = numPlayable;
        if (playingMusic) {
            numNotPlayed--;
        }

        // tell adapter to update the view
        // we do it here once instead of in every call to setGriodColor()
        gridAdapter.notifyDataSetChanged();
    }

    // user pressed setting to change the songs played per grid
    private void changeSongsPerGrid(boolean playAll) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        playEntireGrid = playAll;

        if (currGridIndex != -1) {
            // if we have a grid we are playing, we should start playEntireGrid with
            // the next song from this grid
            playEntireGridIndex = theGrid.get(currGridIndex).getSongIndex(currentSong);
        }

    }

    // pick a grid to roam from
    //      return 0 if we find a grid to play
    //      return 1 if there are no more unplayed grids left
    private void pickStartingGrid() {
        // make sure there is a grid element that has not been played
        if (numNotPlayed <= 0) {
            playMusicText.setText(R.string.done);
            playSongText.setText("");
            playingMusic = false;

            controlPlay.setImageResource(R.drawable.ctrlplay);
            resetGrids(false);
            currGridIndex = -1;
            currentSong = null;
            playEntireGridIndex = -1;

            // release audio focus
            audioMgr.abandonAudioFocus(audioFocusListener);

            GeneralTools.vibrate(this, 500);

            return;
        }

        if (nextGridIndex != -1) {
            // we have a grid assigned to play next
            int toPlay = nextGridIndex;
            nextGridIndex = -1;
            playGrid(toPlay);
        } else {
            // no grid assigned, find one
            playGrid(getRandomNotPlayed());
        }
    }

    // find the next grid to play, either one the user has chosen,
    // or an adjacent, non-blank, non-played grid
    private void pickNextGrid() {
            if (nextGridIndex == -1) {
                // user has not set a next grid to play, find one
                nextGridIndex = getRandomDirIndex();
            }

            if (nextGridIndex == -1) {
                // User has not set next grid to play and
                // we could not find an adjacent grid to play
                // so pick a new starting grid.
                prevGridIndex = currGridIndex;
                pickStartingGrid();
            } else {
                // we have a next grid to play
                int toPlay = nextGridIndex;
                // need to reset nextGridIndex here in case there are errors playing
                // the album and we have to pick a new grid before returning here
                nextGridIndex = -1;
                playGrid(toPlay);
            }
        }


    // Return index of a random playable grid element.
    // Throw assertion if we can't find a playable grid element.
    private int getRandomNotPlayed() {
        int randChoice = RNG.nextInt(numNotPlayed);
        int index;
        int count = -1;

        // find list index for the randomly chosen playable grid element
        for (index = 0; index < numGridElems; index++) {
            GridElement g = theGrid.get(index);
            // skip empty and already played grid elements
            if (!g.isPlayable())  {
                continue;
            }

            // found one
            count++;
            if (count == randChoice) {
                break;
            }
        }

        if (count != randChoice) {
            // we didn't find one, must be a bug
            throw new AssertionError("PlayGridActivity.getRandomNotPlayed() : unable to find playable grid(" + randChoice + ")");
        }

        return index;
    }

    // Return index of a random, adjacent, playable grid element,
    // based on currGridIndex value.
    //      return -1 if none exist
    private int getRandomDirIndex() {
        List<Integer> possibleDirs = new ArrayList<>();

        Log.e("ERROR", "curr " + currGridIndex + " numCols " + numGridCols + " numelems " + numGridElems);

        // look left
        if ((currGridIndex % numGridCols) != 0) {
            GridElement g = theGrid.get(currGridIndex - 1);
            if (g.isPlayable()) {
                possibleDirs.add(currGridIndex - 1);
            }
        }

        // look right
        if ((currGridIndex % numGridCols) != (numGridCols - 1)) {
            GridElement g = theGrid.get(currGridIndex + 1);
            if (g.isPlayable()) {
                possibleDirs.add(currGridIndex + 1);
            }
        }

        // look up
        if ((currGridIndex - numGridCols) >= 0) {
            GridElement g = theGrid.get(currGridIndex - numGridCols);
            if (g.isPlayable()) {
                possibleDirs.add(currGridIndex - numGridCols);
            }
        }

        // look down
        if ((currGridIndex + numGridCols) < numGridElems) {
            GridElement g = theGrid.get(currGridIndex + numGridCols);
            if (g.isPlayable()) {
                possibleDirs.add(currGridIndex + numGridCols);
            }
        }

        if (possibleDirs.size() == 0) {
            // didn't find any adjacent, playable grids
            return -1;
        } else {
            // randomly pick one of our adjacent, playable grids
            return possibleDirs.get(RNG.nextInt(possibleDirs.size()));
        }
    }

    // play the grid element at the specified grid index
    private void playGrid(int gridIndex) {
        prevGridIndex = currGridIndex;
        currGridIndex = gridIndex;

        // check if this grid has already been played, if
        // not, subtract one from numNotPlayed
        GridElement grid = theGrid.get(currGridIndex);
        if (!grid.played) {
            numNotPlayed--;
        }

        // if we had a prev grid, grey it out
        if (prevGridIndex != -1) {
            GridElement prevGrid = theGrid.get(prevGridIndex);
            setGridColor(prevGrid, CURR_STATE_COLOR, true);
        }

        // Highlight the grid we are now playing and set it to played.
        // We must set it to played when we start playing it so we can't
        // choose it as our next grid
        grid.played = true;
        setGridColor( grid, R.color.filterPlaying, true);

        if (!playEntireGrid) {
            //noinspection StatementWithEmptyBody
            while (getSongToPlay(grid) != 0);
        } else {
            //noinspection StatementWithEmptyBody
            while (playAllSongs(grid) != 0);
        }
    }

    // get a random song from the specified grid to play
    private int getSongToPlay(GridElement grid) {
        currentSong = grid.getRandomSongNotPlayed(true);

        if (currentSong != null) {
            displaySongInfo(currentSong.songName, currentSong.artistName);

            // start song playback
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            //mediaPlayer = MediaPlayer.create(this, currentSong.audioResource);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ currentSong.filePath));

            if (mediaPlayer == null) {
                Log.e("ERROR", "COULD NOT PLAY " + currentSong.filePath);
                GeneralTools.showToast(this, "COULD NOT PLAY " + currentSong.songName);

                currentSong.errored = true;
                grid.hasSongError = true;
                setGridColor(grid, CURR_STATE_COLOR, true);
                return -1;
            }

            setCompletionListener();

            Log.e("SONG_LOG", currentSong.songName);
            mediaPlayer.start();
        } else {
            pickNextGrid();
        }

        return 0;
    }

    // show the title and artist info for the song being played
    private void displaySongInfo(String song, String artist) {
        playSongText.setText(song);
        playingMusicString = String.format(this.getString(R.string.playing), artist);
        playMusicText.setText(playingMusicString);
    }

    // recursively play all songs in this grid
    private int playAllSongs(GridElement grid) {
        playEntireGridIndex++;

        currentSong = grid.getNthSong(playEntireGridIndex);
        if (currentSong != null) {
            displaySongInfo(currentSong.songName, currentSong.artistName);

            // start song playback
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            //mediaPlayer = MediaPlayer.create(this, currentSong.audioResource);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ currentSong.filePath));

            if (mediaPlayer == null) {
                Log.e("ERROR", "COULD NOT PLAY " + currentSong.filePath);
                GeneralTools.showToast(this, "COULD NOT PLAY " + currentSong.songName);

                currentSong.errored = true;
                grid.hasSongError = true;
                setGridColor(grid, CURR_STATE_COLOR, true);
                return -1;
            }

            setCompletionListener();

            Log.e("SONG_LOG", currentSong.songName);
            mediaPlayer.start();
        } else {
            playEntireGridIndex = -1;
            pickNextGrid();
        }

        return 0;
    }

    void setCompletionListener() {
        mediaPlayer.setOnCompletionListener(songDoneListener);
    }

    // Set grid color to correct filter based on played / errored state.
    // boolean notify allows us to suppress per-grid adapter notifications in
    // case we are updating a lot of grids, i.e. resetGrids()
    private void setGridColor(GridElement grid, int color, boolean notify) {
        int position = theGrid.indexOf(grid);

        // if a color was specified, set grid to that color
        if (color != CURR_STATE_COLOR) {
            grid.bgColor = color;
            grid.filterColor = color;

            if (notify) {
                gridAdapter.notifyItemChanged(position);
            }

            return;
        }

        // no color specified, set grid to correct color by played / errored state
        if (grid.hasSongError) {
            grid.bgColor = R.color.filterErrorOnPlay;
            grid.filterColor = R.color.filterErrorOnPlay;
        } else if (grid.played) {
            grid.bgColor = R.color.filterPlayed;
            grid.filterColor = R.color.filterPlayed;
        } else {
            grid.bgColor = R.color.borderNotPlayed;
            grid.filterColor = R.color.filterNotPlayed;
        }

        if (notify) {
            gridAdapter.notifyItemChanged(position);
        }
    }

    // turn off button flash, and change between play / pause graphic
    private Runnable turnOffFilter(final ImageView v) {
        return new Runnable() {
            public void run() {
                v.setColorFilter(null);

                // we change the icon here so that the highlighted icon is the one shown when
                // the user presses, we change it to the new one after we are done highlighting
                if (playingMusic) {
                    controlPlay.setImageResource(R.drawable.ctrlpause);
                } else {
                    controlPlay.setImageResource(R.drawable.ctrlplay);
                }
            }
        };
    }

    private void showLoadMenu() {
        PopupMenu loadMenu = new PopupMenu(this, controlStop);

        for (String item : SaveFileLoader.listSaveFiles(this)) {
            loadMenu.getMenu().add(item);
            if (item.equals(saveFileCurrentlyLoaded)) {
                // set checked if this is the item we currently have loaded
                int lastItemPos = loadMenu.getMenu().size() - 1;
                MenuItem newItem = loadMenu.getMenu().getItem(lastItemPos);
                newItem.setCheckable(true);
                newItem.setChecked(true);
            }
        }

        loadMenu.show();

        loadMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                stopMusic(true);
                playMusicText.setText("");
                playSongText.setText("");

                theGrid.clear();
                loadGridArray(SaveFileLoader.loadGrid(PlayGridActivity.this, item.getTitle().toString()));
                saveFileCurrentlyLoaded = item.getTitle().toString();
                gridAdapter.notifyDataSetChanged();

                playMusicText.setText(getResources().getString(R.string.gridLoaded));

                return true;
            }
        });
    }

    private String getFirstSaveFile() {
        String[] files = SaveFileLoader.listSaveFiles(this);

        if (files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }


    private void loadGridArray(String gridData) {
        GridElement grid;
        GridElement blankGrid = new GridElement(getResources().getString(R.string.gridBlank), true);
        JSONObject gridInput;

        try {
            gridInput = new JSONObject(gridData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        int rows;

        try {
            rows = gridInput.getInt(getResources().getString(R.string.savelabelRows));
            numGridCols = gridInput.getInt(getResources().getString(R.string.savelabelColumns));
            theGridView.setLayoutManager(new GridLayoutManager(this, numGridCols));

            JSONArray gridArray = gridInput.getJSONArray(getResources().getString(R.string.savelabelGrids));

            if (gridArray.length() == 0) {
                Log.e("ERROR", "PlayGridActivity:initGridArray() json grid array length is 0");
                return;
            }

            numNotPlayed = numPlayable = gridArray.length();

            int gridArrayIndex = 0;
            JSONObject jsonGrid = gridArray.getJSONObject(gridArrayIndex);
            int gRow = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsRow));
            int gCol = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsColumn));

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < numGridCols; j++) {
                    if ((i != gRow) || (j != gCol)) {
                        theGrid.add(blankGrid);
                    } else {
                        theGrid.add(new GridElement(jsonGrid.getString(getResources().getString(R.string.savelabelGridsCoverart)), false));
                        grid = theGrid.get(theGrid.size() - 1);

                        JSONArray songArray = jsonGrid.getJSONArray(getResources().getString(R.string.savelabelGridsSongs));
                        for (int s = 0; s < songArray.length(); s++) {
                            JSONObject jsonSong = songArray.getJSONObject(s);

                            // TODO hack to skip over storage path which seems to prevent playing of the music file
                            String path = jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsFilepath));
                            String skip = "/storage/emulated/0";
                            path = path.substring(skip.length(), path.length());

                            grid.addSong(jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsName)),
                                    jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsArtist)),
                                    jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsAlbum)),
                                    path,
                                    jsonSong.getInt(getResources().getString(R.string.savelabelGridsSongsTrack)),
                                    jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsAlbumID)));
                        }

                        gridArrayIndex++;
                        if (gridArrayIndex < gridArray.length()) {
                            jsonGrid = gridArray.getJSONObject(gridArrayIndex);
                            gRow = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsRow));
                            gCol = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsColumn));
                        } else {
                            gRow = gCol = -1;
                        }
                    }
                }
            }

        } catch (JSONException e) {
            Log.e("ERROR", "JSON EXCEPTION");
            e.printStackTrace();
            return;
        }

        numGridElems = theGrid.size();
    }

}
