package com.example.android.gridmusic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Random;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;



// contains all code for playing a Grid
public class PlayGridActivity extends AppCompatActivity implements OnClickListener {

    Song currentSong;

    // stats for hardcoded grid for prototype
    private int numGridCols = 6;                // how many columns widde is the Grid?
    private int numGridElems;                   // total elements in the grid, including blank grids
    private int numPlayable = 33;               // number of non-blank grids
    private int numNotPlayed = numPlayable;     // how many grids have not been played yet?

    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnCompletionListener songDoneListener;

    private GridAdapter gridAdapter;        // view adapter for the Grid
    private GridView gridView;
    private int prevGridIndex = -1;         // grid we played last time
    private int currGridIndex = -1;         // grid we are playing now, or have just chosen to play
    private int nextGridIndex = -1;         // grid to play next
    private int playEntireGridIndex = -1;              // when playing entire grids, this stores the track we are currently playing

    // music control and display
    private boolean playingMusic = false;   // has user pressed play?
    private ImageView controlPlay;
    private ImageView controlSkipRewind;
    private ImageView controlRewind;
    private ImageView controlFastForward;
    private ImageView controlSkipFastForward;
    private TextView playMusicText;                 // shows playing/paused and artist info
    private String playingMusicString;
    private TextView playSongText;                  // shows title of song being played

    private ImageView backArrowButton;              // go back to main menu
    private ImageView settingsButton;               // open setting layout
    private LinearLayout settingsLayout;            // expandable layout to expose settings
    private TextView resetPlayedGridsText;              // set all grids back to `not played`
    private TextView songsPerGridText;                  // how many songs to play per grid
    private boolean playEntireGrid = false;         // play all songs on a grid before choosing next grid?
    private TextView editGridText;                  // enter Grid edit mode
    private boolean showingSettings = false;        // is the settings layout expanded?
    // expand/collapse settings layout
    private LinearLayout.LayoutParams closedParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
    private LinearLayout.LayoutParams openParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private Random RNG = new Random();
    private GeneralTools myTools;           // class with useful tools

    // the Grid
    private ArrayList<GridElement> theGrid = new ArrayList<>();

    // list of tips, show one whenever this activity starts
    private ArrayList<String> playTips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_grid);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get list of grid elements
        initGridArray();

        // getr view refs
        initViews();

        // misc initialization
        initMisc();

        // setup OnClick listeners
        initListeners();
    }

    // setup the Grid
    // -1 is a blank (empty) grid
    // The strings here have not been put into `strings.xml` since, in the final app
    // version, they would be retrieved from a grid configuration file.
    private void initGridArray() {
        GridElement grid;

        // row 0
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.born_in_the_usa));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("I'm On Fire", "Bruce Springsteen", R.raw.im_on_fire);
        grid.addSong("Bobby Jean", "Bruce Springsteen", R.raw.bobby_jean);
        grid.addSong("Glory Days", "Bruce Springsteen", R.raw.glory_days);

        theGrid.add(new GridElement(R.drawable.very_best_of_the_eagles));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Desperado", "Eagles", R.raw.desperado);
        grid.addSong("Tequila Sunrise", "Eagles", R.raw.tequila_sunrise);
        grid.addSong("Hotel California", "Eagles", R.raw.hotel_california);

        theGrid.add(new GridElement(-1));


        // row 1
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.elton_john_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Daniel", "Elton John", R.raw.daniel);
        grid.addSong("Rocket Man", "Elton John", R.raw.rocket_man);
        grid.addSong("Candle In The Wind", "Elton John", R.raw.candle_in_the_wind);

        theGrid.add(new GridElement(R.drawable.abbey_road));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Maxwell's Silver Hammer", "Beatles", R.raw.maxwells_silver_hammer);
        grid.addSong("You Never Give Me Your Money", "Beatles", R.raw.you_never_give_me_your_money);
        grid.addSong("She Came In Through The Bathroom Window", "Beatles", R.raw.she_came_in_through_the_bathroom);


        // row 2
        theGrid.add(new GridElement(R.drawable.fifty_one_fifty));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dreams", "Van Halen", R.raw.dreams);
        grid.addSong("Summer Nights", "Van Halen", R.raw.summer_nights);
        grid.addSong("Inside", "Van Halen", R.raw.inside);

        theGrid.add(new GridElement(R.drawable.van_halen));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Runnin' With The Devil", "Van Halen", R.raw.runnin_with_the_devil);
        grid.addSong("Jamie's Cryin'", "Van Halen", R.raw.jamies_cryin);
        grid.addSong("Ice Cream Man", "Van Halen", R.raw.ice_cream_man);

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.fleetwood_mac_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Go Your Own Way", "Fleetwood Mac", R.raw.go_your_own_way);

        theGrid.add(new GridElement(R.drawable.best_of_david_bowie));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Space Oddity", "David Bowie", R.raw.space_oddity);
        grid.addSong("Ziggy Stardust", "David Bowie", R.raw.ziggy_stardust);
        grid.addSong("Life On Mars", "David Bowie", R.raw.life_on_mars);

        theGrid.add(new GridElement(-1));


        // row 3
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.synchronicity));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Synchronicity II", "Police", R.raw.synchronicity_ii);
        grid.addSong("Every Breath You Take", "Police", R.raw.every_breath_you_take);
        grid.addSong("King Of Pain", "Police", R.raw.king_of_pain);

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.point_of_know_return));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Point Of Know Return", "Kansas", R.raw.point_of_know_return);
        grid.addSong("Dust In The Wind", "Kansas", R.raw.dust_in_the_wind);
        grid.addSong("Nobody's Home", "Kansas", R.raw.nobodys_home);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 4
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.genesis));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Mama", "Genesis", R.raw.mama);
        grid.addSong("Home By The Sea", "Genesis", R.raw.home_by_the_sea);
        grid.addSong("Second Home By The Sea", "Genesis", R.raw.second_home_by_the_sea);

        theGrid.add(new GridElement(R.drawable.selling_england_by_the_pound));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dancing With The Moonlight Knight", "Genesis", R.raw.dancing_with_the_moonlight_knight);
        grid.addSong("More Fool Me", "Genesis", R.raw.more_fool_me);
        grid.addSong("The Cinema Show", "Genesis", R.raw.the_cinema_show);

        theGrid.add(new GridElement(R.drawable.best_of_emerson_lake_and_palmer));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Fanfare For The Common Man", "Emerson, Lake and Palmer", R.raw.fanfare_for_the_common_man);
        grid.addSong("C'est La Vie", "Emerson, Lake and Palmer", R.raw.cest_la_vie);
        grid.addSong("Lucky Man", "Emerson, Lake and Palmer", R.raw.lucky_man);

        theGrid.add(new GridElement(R.drawable.chronicles));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Tom Sawyer", "Rush", R.raw.tom_sawyer);
        grid.addSong("Subdivisions", "Rush", R.raw.subdivisions);
        grid.addSong("Time Stands Still", "Rush", R.raw.time_stand_still);

        theGrid.add(new GridElement(R.drawable.the_who_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Love Reign O'er Me", "The Who", R.raw.love_reign_oer_me);
        grid.addSong("Eminence Front", "The Who", R.raw.eminence_front);


        // row 5
        theGrid.add(new GridElement(R.drawable.led_zeppelin_iii));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Since I've Been Loving You", "Led Zeppelin", R.raw.since_ive_been_loving_you);
        grid.addSong("Gallows Pole", "Led Zeppelin", R.raw.gallows_pole);
        grid.addSong("That's The Way", "Led Zeppelin", R.raw.thats_the_way);

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.a_trick_of_the_tail));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Mad Man Moon", "Genesis", R.raw.mad_man_moon);
        grid.addSong("Ripples", "Genesis", R.raw.ripples);
        grid.addSong("A Trick Of The Tail", "Genesis", R.raw.a_trick_of_the_tail);

        theGrid.add(new GridElement(R.drawable.highlights));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Starship Trooper", "Yes", R.raw.starship_trooper);
        grid.addSong("I've Seen All Good People", "Yes", R.raw.ive_seen_all_good_people);
        grid.addSong("Wondrous Stories", "Yes", R.raw.wondrous_stories);

        theGrid.add(new GridElement(R.drawable.boston));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("More Than A Feeling", "Boston", R.raw.more_than_a_feeling);
        grid.addSong("Peace Of Mind", "Boston", R.raw.peace_of_mind);
        grid.addSong("Hitch A Ride", "Boston", R.raw.hitch_a_ride);

        theGrid.add(new GridElement(-1));


        // row 6
        theGrid.add(new GridElement(R.drawable.led_zeppelin_iv));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("StairWay To Heaven", "Led Zeppelin", R.raw.stairway_to_heaven);
        grid.addSong("Misty Mountain Hop", "Led Zeppelin", R.raw.misty_mountain_hop);
        grid.addSong("When The Levee Breaks", "Led Zeppelin", R.raw.when_the_levee_breaks);

        theGrid.add(new GridElement(R.drawable.dark_side_of_the_moon));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Us And Them", "Pink Floyd", R.raw.us_and_them);
        grid.addSong("Any Colour You Like", "Pink Floyd", R.raw.any_colour_you_like);
        grid.addSong("Brain Damage", "Pink Floyd", R.raw.brain_damage);

        theGrid.add(new GridElement(R.drawable.wish_you_were_here));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Shine On You Crazy Diamond", "Pink Floyd", R.raw.shine_on_you_crazy_diamond);
        grid.addSong("Have A Cigar", "Pink Floyd", R.raw.have_a_cigar);
        grid.addSong("Wish You Were Here", "Pink Floyd", R.raw.wish_you_were_here);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 7
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.rock_and_soul_part_one));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Sara Smile", "Hall and Oates", R.raw.sara_smile);
        grid.addSong("She's Gone", "Hall and Oates", R.raw.shes_gone);
        grid.addSong("Maneater", "Hall and Oates", R.raw.maneater);

        theGrid.add(new GridElement(R.drawable.bill_withers_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Ain't No Sunshine", "Bill Withers", R.raw.aint_no_sunshine);

        theGrid.add(new GridElement(R.drawable.leave_your_hat_on));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("I'd Rather Go Blind", "Michael Grimm", R.raw.id_rather_go_blind);

        theGrid.add(new GridElement(-1));


        // row 8
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.eric_clapton_unplugged));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Before You Accuse Me", "Eric Clapton", R.raw.before_you_accuse_me);
        grid.addSong("Layla", "Eric Clapton", R.raw.layla);
        grid.addSong("Running On Faith", "Eric Clapton", R.raw.running_on_faith);

        theGrid.add(new GridElement(R.drawable.twenty_four_nights));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Old Love", "Eric Clapton", R.raw.old_love);
        grid.addSong("Wonderful Tonight", "Eric Clapton", R.raw.wonderful_tonight);
        grid.addSong("Hard Times", "Eric Clapton", R.raw.hard_times);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 9
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.best_of_hooker_and_heat));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("You Talk Too Much", "Hooker and Heat", R.raw.you_talk_too_much);
        grid.addSong("I Got My Eyes On You", "Hooker and Heat", R.raw.i_got_my_eyes_on_you);
        grid.addSong("Wiskey And Wimmen'", "Hooker and Heat", R.raw.wiskey_and_wimmen);

        theGrid.add(new GridElement(R.drawable.best_of_muddy_waters));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("I Just Want To Make Love To You", "Muddy Waters", R.raw.i_just_want_to_make_love_to_you);
        grid.addSong("Honey Bee", "Muddy Waters", R.raw.honey_bee);
        grid.addSong("Hoochie Coochie Man", "Muddy Waters", R.raw.hoochie_coochie_man);

        theGrid.add(new GridElement(R.drawable.live_at_carnegie_hall));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dirty Pool", "Stevie Ray Vaughan", R.raw.dirty_pool);
        grid.addSong("The Things That I Used To Do", "Stevie Ray Vaughan", R.raw.the_things_that_i_used_to_do);
        grid.addSong("Lenny", "Stevie Ray Vaughan",  R.raw.lenny);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 10
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.gang_related));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Staring Through My Rearview", "2Pac", R.raw.staring_through_my_rearview);

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.sky_is_crying));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("The Sky Is Crying", "Stevie Ray Vaughan", R.raw.the_sky_is_crying);
        grid.addSong("Little Wing", "Stevie Ray Vaughan", R.raw.little_wing);
        grid.addSong("Life By The Drop", "Stevie Ray Vaughan", R.raw.life_by_the_drop);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 11
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.recovery));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Love The Way You Lie", "Eminim", R.raw.love_the_way_you_lie);

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        numGridElems = theGrid.size();
    }

    // get view references and setup GridView adapter
    private void initViews() {
        gridAdapter = new GridAdapter(this, theGrid);
        gridView = findViewById(R.id.gridView);
        if (gridView == null) {
            throw new AssertionError("PlayGridActivity.initView() : null gridView");
        }
        gridView.setAdapter(gridAdapter);

        setGridTotalWidth();
        gridView.setNumColumns(numGridCols);

        // views for music control and display
        controlPlay = findViewById(R.id.control_play);
        controlSkipRewind = findViewById(R.id.control_skip_rewind);
        controlRewind = findViewById(R.id.control_rewind);
        controlFastForward = findViewById(R.id.control_fastforward);
        controlSkipFastForward = findViewById(R.id.control_skip_fastforward);
        playMusicText = findViewById(R.id.playMusicText);
        playSongText = findViewById(R.id.playSongText);

        backArrowButton = findViewById(R.id.backArrow);
        settingsButton = findViewById(R.id.settingsButton);
        settingsLayout = findViewById(R.id.settingsLayout);
        resetPlayedGridsText = findViewById(R.id.resetPlayedGrids);
        songsPerGridText = findViewById(R.id.songsPerGrid);
        editGridText = findViewById(R.id.editGrid);
    }

    // misc setup
    private void initMisc() {
        myTools = new GeneralTools(this);

        initTips();
    }

    // initialize and display a usage tip
    private void initTips() {
        playTips.add(this.getString(R.string.playTip1));
        playTips.add(this.getString(R.string.playTip2));
        playTips.add(this.getString(R.string.playTip3));
        playTips.add(this.getString(R.string.playTip4));
        playTips.add(this.getString(R.string.playTip5));

        int tip = RNG.nextInt(playTips.size());
        myTools.showToast(this.getString(R.string.tip) + " " + playTips.get(tip));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_play :    playPause();
                                        break;

            case R.id.control_skip_fastforward:     skipFastForward();
                                                    break;

            case R.id.backArrow :       goBackToMainMenu();
                                        break;

            case R.id.settingsButton :  openSettings();
                                        break;

            case R.id.resetPlayedGrids :// These are not inside resetGrids() so they don't happen
                                        // when the auto-reset occurs after all grids are played.
                                        myTools.vibrate(50);
                                        myTools.showToast(PlayGridActivity.this.getString(R.string.resettingPlayed));

                                        resetGrids();
                                        break;

            case R.id.songsPerGrid :    changeSongsPerGrid();
                                        break;

            default :                   myTools.notSupported();
        }
    }

    // init OnClickListeners
    private void initListeners() {

        controlPlay.setOnClickListener(this);
        backArrowButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        resetPlayedGridsText.setOnClickListener(this);
        songsPerGridText.setOnClickListener(this);

        // features currently not implemented
        controlSkipRewind.setOnClickListener(this);
        controlRewind.setOnClickListener(this);
        controlFastForward.setOnClickListener(this);
        controlSkipFastForward.setOnClickListener(this);
        editGridText.setOnClickListener(this);


        // We can implement these like we did for OnClick() if we do more with item clicks
        // listen for for grid element press
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                userChoseNextGrid(position);
            }
        });

        // listen for for grid element long press
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                return userToggledPlayState(position);
            }
        });

        songDoneListener = (new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                if (playEntireGrid) {
                    playAllSongs(theGrid.get(currGridIndex));
                } else {
                    pickNextGrid();
                }
            }
        });
    }

    // ***************************
    // Methods called by listeners
    // ***************************

    // user pressed play / pause, start or stop music play
    private void playPause() {
        myTools.vibrate(50);

        // flashy flash the `play` button when you press it
        controlPlay.setColorFilter(Color.WHITE);
        handler.postDelayed(turnOffFilter(controlPlay), 100);

        if (!playingMusic) {
            // music not yet started or paused
            playingMusic = true;
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
            playMusicText.setText(R.string.paused);
            mediaPlayer.pause();
        }
    }

    // user pressed skip-fastforward
    // stop song and call completion listener
    private void skipFastForward() {
        if (mediaPlayer != null) {
            // we might be paused, transition back to playing
            playingMusic = true;

            // flashy flash the `skip-fastfforward` button when you press it
            controlSkipFastForward.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlSkipFastForward), 100);

            mediaPlayer.stop();
            songDoneListener.onCompletion(mediaPlayer);
        }
    }

    // user clicked a grid, choose it to play next
    private void userChoseNextGrid(int position) {
        GridElement g = theGrid.get(position);

        // ignore presses on blank grids
        if (g.isBlank()) {
            return;
        }

        myTools.vibrate(50);

        if (nextGridIndex != -1) {
            GridElement currNextGrid = theGrid.get(nextGridIndex);
            // player chose a second grid to play reset the previous choice
            resetGridColor(currNextGrid);
        }

        nextGridIndex = position;
        setGridState(position, R.color.filterNextToPlay, false);
    }

    // user long-pressed a grid, switch its play state
    private boolean userToggledPlayState(int position) {
        GridElement g = theGrid.get(position);

        // ignore presses on blank grids
        if (g.isBlank() || (position == currGridIndex)) {
            return false;
        }

        myTools.vibrate(50);

        if (g.played) {
            numNotPlayed++;
        } else {
            numNotPlayed--;
        }

        g.played = !g.played;
        resetGridColor(g);
        gridAdapter.notifyDataSetChanged();

        return true;
    }

    // user pressed back arrow, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(PlayGridActivity.this, MainActivity.class);

        myTools.vibrate(50);

        startActivity(mainMenuIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // user pressed settings icon, open the settings layout
    private void openSettings() {
        myTools.vibrate(50);

        if (showingSettings) {
            // settings are currently visible, hide them
            settingsLayout.setLayoutParams(closedParams);
        } else {
            // settings are currently hidden, show them
            settingsLayout.setLayoutParams(openParams);
        }

        showingSettings = !showingSettings;
    }

    // set all played grids to not-played
    // All grids are reset if we are not currently playing music.
    // If we are playing music, then the currently playing grid is not reset.
    private void resetGrids() {
        // find arraylist index for the randomly chosen playable grid element
        for (int index = 0; index < numGridElems; index++) {
            GridElement g = theGrid.get(index);

            // skip empty and already played grid elements
            if (g.isBlank())  {
                continue;
            }

            if ((index == currGridIndex) && (playingMusic)) {
                // don't reset the grid that is currently playing
                continue;
            }

            if (g.played) {
                g.played = false;
                resetGridColor(g);
                g.setAllSongsToNotPlayed();
            }
        }

        // minus one to account for the song currently being played
        numNotPlayed = numPlayable;
        if (playingMusic) {
            numNotPlayed--;
        }

        // tell adapter to update the view
        gridAdapter.notifyDataSetChanged();
    }

    // user pressed setting to change the songs played per grid
    private void changeSongsPerGrid() {
        myTools.vibrate(50);

        if (playEntireGrid) {
            songsPerGridText.setText(R.string.oneSong);
        } else {
            songsPerGridText.setText(R.string.allSongs);
        }

        playEntireGrid = !playEntireGrid;

        if (currGridIndex != -1) {
            // if we have a grid we are playing, we should start playEntireGrid with
            // the next song from this grid
            playEntireGridIndex = theGrid.get(currGridIndex).getSongIndex(currentSong);
        }

    }



    // expand the GridView to hold the correct number of columns
    // I got this code from stackoverflow while back investigating horizontally scrolling GridViews.
    // https://stackoverflow.com/questions/16299633/android-gridview-with-both-horizontal-and-vertical-scrolbars-at-the-same-time
    private void setGridTotalWidth() {
        ViewGroup.LayoutParams layoutParams = gridView.getLayoutParams();
        layoutParams.width = convertDpToPixels(numGridCols * MainActivity.GRID_COLUMN_TOTALWIDTH, this);
        gridView.setLayoutParams(layoutParams);
    }

    // I got this code from stackoverflow while back investigating horizontally scrolling GridViews.
    // https://stackoverflow.com/questions/16299633/android-gridview-with-both-horizontal-and-vertical-scrolbars-at-the-same-time
    private int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
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
            resetGrids();
            currGridIndex = -1;
            myTools.vibrate(500);
            return;
        }

        if (nextGridIndex != -1) {
            // we have a grid assigned to play next
            playGrid(nextGridIndex);
            nextGridIndex = -1;
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
                playGrid(nextGridIndex);
                nextGridIndex = -1;
            }
        }


    // Return index of a random playable grid element.
    // Throw assertion if we can't find a playable grid element.
    private int getRandomNotPlayed() {
        int randChoice = RNG.nextInt(numNotPlayed);
        int index;
        int count = -1;

        // find arraylist index for the randomly chosen playable grid element
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
        ArrayList<Integer> possibleDirs = new ArrayList<>();

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
        if ((currGridIndex + numGridCols) <= numGridElems) {
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
        GridElement elem = theGrid.get(currGridIndex);
        if (!elem.played) {
            numNotPlayed--;
        }

        // if we had a prev grid, grey it out
        if (prevGridIndex != -1) {
            setGridState(prevGridIndex, R.color.filterPlayed, true);
        }

        // Highlight the grid we are now playing and set it to played.
        // We must set it to played when we start playing it so we can't
        // choose it as our next grid
        setGridState(currGridIndex, R.color.filterPlaying, true);

        if (!playEntireGrid) {
            getSongToPlay(elem);
        } else {
            playAllSongs(elem);
        }

    }

    // get a random song from the specified grid to play
    private void getSongToPlay(GridElement grid) {
        currentSong = grid.getRandomSongNotPlayed(true);

        if (currentSong != null) {
            displaySongInfo(currentSong.songName, currentSong.artistName);

            // start song playback
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = MediaPlayer.create(this, currentSong.audioResource);
            setCompletionListener();

            Log.e("SONG_LOG", currentSong.songName);
            mediaPlayer.start();
        } else {
            displaySongInfo(this.getString(R.string.nullval), this.getString(R.string.nullval));
        }
    }

    // show the title and artist info for the song being played
    private void displaySongInfo(String song, String artist) {
        playSongText.setText(song);
        playingMusicString = String.format(this.getString(R.string.playing), artist);
        playMusicText.setText(playingMusicString);
    }

    // recursively play all songs in this grid
    private void playAllSongs(GridElement elem) {
        playEntireGridIndex++;

        currentSong = elem.getNthSong(playEntireGridIndex);
        if (currentSong != null) {
            displaySongInfo(currentSong.songName, currentSong.artistName);

            // start song playback
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = MediaPlayer.create(this, currentSong.audioResource);
            setCompletionListener();

            Log.e("SONG_LOG", currentSong.songName);
            mediaPlayer.start();
        } else {
            playEntireGridIndex = -1;
            pickNextGrid();
        }
    }

    void setCompletionListener() {


        mediaPlayer.setOnCompletionListener(songDoneListener);
    }

    // Set grid color to correct filter based on played state.
    private void resetGridColor(GridElement g) {
        if (g.played) {
            g.bgColor = R.color.filterPlayed;
            g.filterColor = R.color.filterPlayed;
        } else {
            g.bgColor = R.color.borderNotPlayed;
            g.filterColor = R.color.filterNotPlayed;
        }
    }

    // Update image state so it will show correctly if the view
    // is scrolled off screen then back on screen.
    // Set played to true as soon as a grid starts playing
    private void setGridState(int index, int color, boolean setToPlayed) {
        GridElement elem = theGrid.get(index);

        elem.bgColor = color;
        elem.filterColor = color;
        if (setToPlayed) {
            elem.played = true;
        }

        gridAdapter.notifyDataSetChanged();
    }

    // turn off button flash, and change between play / pause graphic
    private Runnable turnOffFilter(final ImageView v) {
        return new Runnable() {
            public void run() {
                v.setColorFilter(null);
                if (playingMusic) {
                    controlPlay.setImageResource(R.drawable.ctrlpause);
                } else {
                    controlPlay.setImageResource(R.drawable.ctrlplay);
                }
            }
        };
    }
}
