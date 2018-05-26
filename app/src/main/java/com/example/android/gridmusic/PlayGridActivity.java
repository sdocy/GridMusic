package com.example.android.gridmusic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.List;
import java.util.Random;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;



// contains all code for playing a Grid
public class PlayGridActivity extends AppCompatActivity implements OnClickListener {

    private final int CURR_STATE_COLOR = -1;

    private Song currentSong;

    // stats for hardcoded grid for prototype
    private int numGridCols = 6;                // how many columns widde is the Grid?
    private int numGridElems;                   // total elements in the grid, including blank grids
    private int numPlayable = 33;               // number of non-blank grids
    private int numNotPlayed = numPlayable;     // how many grids have not been played yet?

    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnCompletionListener songDoneListener;

    private AudioManager audioMgr;
    private AudioManager.OnAudioFocusChangeListener audioFocusListener = null;

    private GridAdapter gridAdapter;        // view adapter for the Grid
    private GridView gridView;
    private int prevGridIndex = -1;         // grid we played last time
    private int currGridIndex = -1;         // grid we are playing now, or have just chosen to play
    private int nextGridIndex = -1;         // grid to play next
    private int playEntireGridIndex = -1;              // when playing entire grids, this stores the track we are currently playing

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
        grid.addSong("I'm On Fire", "Bruce Springsteen", "/Music/im_on_fire.m4a");
        grid.addSong("Bobby Jean", "Bruce Springsteen", "/Music/bobby_jean.m4a");
        grid.addSong("Glory Days", "Bruce Springsteen", "/Music/glory_days.m4a");

        theGrid.add(new GridElement(R.drawable.very_best_of_the_eagles));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Desperado", "Eagles", "/Music/desperado.m4a");
        grid.addSong("Tequila Sunrise", "Eagles", "/Music/tequila_sunrise.m4a");
        grid.addSong("Hotel California", "Eagles", "/Music/hotel_california.m4a");

        theGrid.add(new GridElement(-1));


        // row 1
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.elton_john_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Daniel", "Elton John", "/Music/daniel.m4a");
        grid.addSong("Rocket Man", "Elton John", "/Music/rocket_man.m4a");
        grid.addSong("Candle In The Wind", "Elton John", "/Music/candle_in_the_wind.m4a");

        theGrid.add(new GridElement(R.drawable.abbey_road));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Maxwell's Silver Hammer", "Beatles", "/Music/maxwells_silver_hammer.m4a");
        grid.addSong("You Never Give Me Your Money", "Beatles", "/Music/you_never_give_me_your_money.m4a");
        grid.addSong("She Came In Through The Bathroom Window", "Beatles", "/Music/she_came_in_through_the_bathroom.m4a");


        // row 2
        theGrid.add(new GridElement(R.drawable.fifty_one_fifty));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dreams", "Van Halen", "/Music/dreams.m4a");
        grid.addSong("Summer Nights", "Van Halen", "/Music/summer_nights.m4a");
        grid.addSong("Inside", "Van Halen", "/Music/inside.m4a");

        theGrid.add(new GridElement(R.drawable.van_halen));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Runnin' With The Devil", "Van Halen", "/Music/runnin_with_the_devil.m4a");
        grid.addSong("Jamie's Cryin'", "Van Halen", "/Music/jamies_cryin.m4a");
        grid.addSong("Ice Cream Man", "Van Halen", "/Music/ice_cream_man.m4a");

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.fleetwood_mac_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Go Your Own Way", "Fleetwood Mac", "/Music/go_your_own_way.mp3");

        theGrid.add(new GridElement(R.drawable.best_of_david_bowie));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Space Oddity", "David Bowie", "/Music/space_oddity.m4a");
        grid.addSong("Ziggy Stardust", "David Bowie", "/Music/ziggy_stardust.m4a");
        grid.addSong("Life On Mars", "David Bowie", "/Music/life_on_mars.m4a");

        theGrid.add(new GridElement(-1));


        // row 3
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.synchronicity));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Synchronicity II", "Police", "/Music/synchronicity_ii.m4a");
        grid.addSong("Every Breath You Take", "Police", "/Music/every_breath_you_take.m4a");
        grid.addSong("King Of Pain", "Police", "/Music/king_of_pain.m4a");

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.point_of_know_return));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Point Of Know Return", "Kansas", "/Music/point_of_know_return.m4a");
        grid.addSong("Dust In The Wind", "Kansas", "/Music/dust_in_the_wind.m4a");
        grid.addSong("Nobody's Home", "Kansas", "/Music/nobodys_home.m4a");

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 4
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.genesis));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Mama", "Genesis", "/Music/mama.m4a");
        grid.addSong("Home By The Sea", "Genesis", "/Music/home_by_the_sea.m4a");
        grid.addSong("Second Home By The Sea", "Genesis", "/Music/second_home_by_the_sea.m4a");

        theGrid.add(new GridElement(R.drawable.selling_england_by_the_pound));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dancing With The Moonlight Knight", "Genesis", "/Music/dancing_with_the_moonlight_knight.m4a");
        grid.addSong("More Fool Me", "Genesis", "/Music/more_fool_me.m4a");
        grid.addSong("The Cinema Show", "Genesis", "/Music/the_cinema_show.m4a");

        theGrid.add(new GridElement(R.drawable.best_of_emerson_lake_and_palmer));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Fanfare For The Common Man", "Emerson, Lake and Palmer", "/Music/fanfare_for_the_common_man.m4a");
        grid.addSong("C'est La Vie", "Emerson, Lake and Palmer", "/Music/cest_la_vie.m4a");
        grid.addSong("Lucky Man", "Emerson, Lake and Palmer", "/Music/lucky_man.m4a");

        theGrid.add(new GridElement(R.drawable.chronicles));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Tom Sawyer", "Rush", "/Music/tom_sawyer.m4a");
        grid.addSong("Subdivisions", "Rush", "/Music/subdivisions.m4a");
        grid.addSong("Time Stands Still", "Rush", "/Music/time_stand_still.m4a");

        theGrid.add(new GridElement(R.drawable.the_who_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        // TODO misspelled these song names to introduce an access error
        grid.addSong("Love Reign O'er Me", "The Who", "/Music/llove_reign_oer_me.mp3");
        grid.addSong("Eminence Front", "The Who", "/Music/eeminence_front.mp3");


        // row 5
        theGrid.add(new GridElement(R.drawable.led_zeppelin_iii));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Since I've Been Loving You", "Led Zeppelin", "/Music/since_ive_been_loving_you.m4a");
        grid.addSong("Gallows Pole", "Led Zeppelin", "/Music/gallows_pole.m4a");
        grid.addSong("That's The Way", "Led Zeppelin", "/Music/thats_the_way.m4a");

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.a_trick_of_the_tail));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Mad Man Moon", "Genesis", "/Music/mad_man_moon.m4a");
        grid.addSong("Ripples", "Genesis", "/Music/ripples.m4a");
        // TODO misspelled this song name to introduce an access error
        grid.addSong("A Trick Of The Tail", "Genesis", "/Music/aa_trick_of_the_tail.m4a");

        theGrid.add(new GridElement(R.drawable.highlights));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Starship Trooper", "Yes", "/Music/starship_trooper.m4a");
        grid.addSong("I've Seen All Good People", "Yes", "/Music/ive_seen_all_good_people.m4a");
        grid.addSong("Wondrous Stories", "Yes", "/Music/wondrous_stories.m4a");

        theGrid.add(new GridElement(R.drawable.boston));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("More Than A Feeling", "Boston", "/Music/more_than_a_feeling.m4a");
        grid.addSong("Peace Of Mind", "Boston", "/Music/peace_of_mind.m4a");
        grid.addSong("Hitch A Ride", "Boston", "/Music/hitch_a_ride.m4a");

        theGrid.add(new GridElement(-1));


        // row 6
        theGrid.add(new GridElement(R.drawable.led_zeppelin_iv));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("StairWay To Heaven", "Led Zeppelin", "/Music/stairway_to_heaven.m4a");
        grid.addSong("Misty Mountain Hop", "Led Zeppelin", "/Music/misty_mountain_hop.m4a");
        grid.addSong("When The Levee Breaks", "Led Zeppelin", "/Music/when_the_levee_breaks.m4a");

        theGrid.add(new GridElement(R.drawable.dark_side_of_the_moon));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Us And Them", "Pink Floyd", "/Music/us_and_them.m4a");
        grid.addSong("Any Colour You Like", "Pink Floyd", "/Music/any_colour_you_like.m4a");
        grid.addSong("Brain Damage", "Pink Floyd", "/Music/brain_damage.m4a");

        theGrid.add(new GridElement(R.drawable.wish_you_were_here));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Shine On You Crazy Diamond", "Pink Floyd", "/Music/shine_on_you_crazy_diamond.m4a");
        grid.addSong("Have A Cigar", "Pink Floyd", "/Music/have_a_cigar.m4a");
        grid.addSong("Wish You Were Here", "Pink Floyd", "/Music/wish_you_were_here.m4a");

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 7
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.rock_and_soul_part_one));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Sara Smile", "Hall and Oates", "/Music/sara_smile.m4a");
        grid.addSong("She's Gone", "Hall and Oates", "/Music/shes_gone.m4a");
        grid.addSong("Maneater", "Hall and Oates", "/Music/maneater.m4a");

        theGrid.add(new GridElement(R.drawable.bill_withers_greatest_hits));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Ain't No Sunshine", "Bill Withers", "/Music/aint_no_sunshine.m4a");

        theGrid.add(new GridElement(R.drawable.leave_your_hat_on));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("I'd Rather Go Blind", "Michael Grimm", "/Music/id_rather_go_blind.m4a");

        theGrid.add(new GridElement(-1));


        // row 8
        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.eric_clapton_unplugged));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Before You Accuse Me", "Eric Clapton", "/Music/before_you_accuse_me.m4a");
        grid.addSong("Layla", "Eric Clapton", "/Music/layla.m4a");
        grid.addSong("Running On Faith", "Eric Clapton", "/Music/running_on_faith.m4a");

        theGrid.add(new GridElement(R.drawable.twenty_four_nights));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Old Love", "Eric Clapton", "/Music/old_love.m4a");
        grid.addSong("Wonderful Tonight", "Eric Clapton", "/Music/wonderful_tonight.m4a");
        grid.addSong("Hard Times", "Eric Clapton", "/Music/hard_times.m4a");

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 9
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.best_of_hooker_and_heat));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("You Talk Too Much", "Hooker and Heat", "/Music/you_talk_too_much.m4a");
        grid.addSong("I Got My Eyes On You", "Hooker and Heat", "/Music/i_got_my_eyes_on_you.m4a");
        grid.addSong("Wiskey And Wimmen'", "Hooker and Heat", "/Music/wiskey_and_wimmen.m4a");

        theGrid.add(new GridElement(R.drawable.best_of_muddy_waters));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("I Just Want To Make Love To You", "Muddy Waters", "/Music/i_just_want_to_make_love_to_you.m4a");
        grid.addSong("Honey Bee", "Muddy Waters", "/Music/honey_bee.m4a");
        grid.addSong("Hoochie Coochie Man", "Muddy Waters", "/Music/hoochie_coochie_man.m4a");

        theGrid.add(new GridElement(R.drawable.live_at_carnegie_hall));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("Dirty Pool", "Stevie Ray Vaughan", "/Music/dirty_pool.m4a");
        grid.addSong("The Things That I Used To Do", "Stevie Ray Vaughan", "/Music/the_things_that_i_used_to_do.m4a");
        grid.addSong("Lenny", "Stevie Ray Vaughan",  "/Music/lenny.m4a");

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 10
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.gang_related));
        grid = theGrid.get(theGrid.size() - 1);
        // TODO misspelled this song name to introduce an access error
        grid.addSong("Staring Through My Rearview", "2Pac", "/Music/sstaring_through_my_rearview.m4a");

        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.sky_is_crying));
        grid = theGrid.get(theGrid.size() - 1);
        grid.addSong("The Sky Is Crying", "Stevie Ray Vaughan", "/Music/the_sky_is_crying.m4a");
        grid.addSong("Little Wing", "Stevie Ray Vaughan", "/Music/little_wing.m4a");
        grid.addSong("Life By The Drop", "Stevie Ray Vaughan", "/Music/life_by_the_drop.m4a");

        theGrid.add(new GridElement(-1));
        theGrid.add(new GridElement(-1));


        // row 11
        theGrid.add(new GridElement(-1));

        theGrid.add(new GridElement(R.drawable.recovery));
        grid = theGrid.get(theGrid.size() - 1);
        // TODO misspelled this song name to introduce an access error
        grid.addSong("Love The Way You Lie", "Eminim", "/Music/llove_the_way_you_lie.m4a");

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
        controlStop = findViewById(R.id.control_stop);
        controlSkipRewind = findViewById(R.id.control_skip_rewind);
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

        audioMgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
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

    // init OnClickListeners
    private void initListeners() {

        controlPlay.setOnClickListener(this);
        backArrowButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        resetPlayedGridsText.setOnClickListener(this);
        songsPerGridText.setOnClickListener(this);

        // features currently not implemented
        controlSkipRewind.setOnClickListener(this);
        controlStop.setOnClickListener(this);
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
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_play :                playPause(true);
                                                    break;

            case R.id.control_stop :                stopMusic(true);
                                                    break;

            case R.id.control_skip_fastforward :    skipFastForward();
                                                    break;

            case R.id.backArrow :                   goBackToMainMenu();
                                                    break;

            case R.id.settingsButton :              openSettings();
                                                    break;

            case R.id.resetPlayedGrids :            resetGrids(true);
                                                    break;

            case R.id.songsPerGrid :                changeSongsPerGrid();
                                                    break;

            default :                               myTools.notSupported();
        }
    }

    // ***************************
    // Methods called by listeners
    // ***************************

    // user pressed play / pause or we lost / gained audio focus transiently, start or stop music play
    private void playPause(boolean fromUser) {
        if (fromUser) {
            // this call is from a user click, provide tactile feedback
            myTools.vibrate(GeneralTools.touchVibDelay);

            // flashy flash the `play` button when you press it
            controlPlay.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlPlay), 100);

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

    // user pressed stop or we lost audio focus
    private void stopMusic(boolean fromUser) {
        if (mediaPlayer != null) {
            if (fromUser) {
                // this call is from a user click, provide tactile feedback
                myTools.vibrate(GeneralTools.touchVibDelay);

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
            // we might be paused, transition back to playing
            playingMusic = true;

            // flashy flash the `skip-fastfforward` button when you press it
            controlSkipFastForward.setColorFilter(Color.WHITE);
            handler.postDelayed(turnOffFilter(controlSkipFastForward), 100);

            mediaPlayer.stop();

            // trigger song completion so the app will pick the next song
            songDoneListener.onCompletion(mediaPlayer);
        }

        //getMusicList();
    }

    // this code retrieved from https://gist.github.com/novoda/374533
    private void getMusicList() {
        Cursor cursor;

        //Retrieve a list of Music files currently listed in the Media store DB via URI.

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        List<String> songs = new ArrayList<>();
        while(cursor.moveToNext()){
            songs.add(cursor.getString(0) + "||" + cursor.getString(1) + "||" +   cursor.getString(2) + "||" +   cursor.getString(3) + "||" +  cursor.getString(4) + "||" +  cursor.getString(5));
        }

        Log.e("GETSONGS", "got " + songs.size() + " songs");
        for (String s : songs) {
            Log.e("GETSONGS", s);
        }
    }

    // user clicked a grid, choose it to play next
    private void userChoseNextGrid(int position) {
        GridElement grid = theGrid.get(position);

        // ignore presses on blank grids
        if (grid.isBlank()) {
            return;
        }

        myTools.vibrate(GeneralTools.touchVibDelay);

        if (nextGridIndex != -1) {
            GridElement currNextGrid = theGrid.get(nextGridIndex);
            // player chose a second grid to play reset the previous choice
            setGridColor(currNextGrid, CURR_STATE_COLOR, true);
        }

        nextGridIndex = position;
        setGridColor(grid, R.color.filterNextToPlay, true);
    }

    // user long-pressed a grid, switch its play state
    private boolean userToggledPlayState(int position) {
        GridElement g = theGrid.get(position);

        // ignore presses on blank grids and the currently playing grid
        if (g.isBlank() || (position == currGridIndex)) {
            return false;
        }

        myTools.vibrate(GeneralTools.touchVibDelay);

        if (g.played) {
            numNotPlayed++;
        } else {
            numNotPlayed--;
        }

        g.played = !g.played;
        setGridColor(g, CURR_STATE_COLOR, true);

        return true;
    }

    // user pressed back arrow, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(PlayGridActivity.this, MainActivity.class);

        myTools.vibrate(GeneralTools.touchVibDelay);

        startActivity(mainMenuIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // user pressed settings icon, open the settings layout
    private void openSettings() {
        myTools.vibrate(GeneralTools.touchVibDelay);

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
    private void resetGrids(boolean fromUser) {
        if (fromUser) {
            myTools.flashText(resetPlayedGridsText, R.color.highlightBlue, R.color.myBlue, 75);
            myTools.vibrate(GeneralTools.touchVibDelay);
            myTools.showToast(PlayGridActivity.this.getString(R.string.resettingPlayed));
        }

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
    private void changeSongsPerGrid() {
        myTools.flashText(songsPerGridText, R.color.highlightBlue, R.color.myBlue, 75);
        myTools.vibrate(GeneralTools.touchVibDelay);

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
    private void setGridTotalWidth() {
        ViewGroup.LayoutParams layoutParams = gridView.getLayoutParams();
        layoutParams.width = convertDpToPixels(numGridCols * MainActivity.GRID_COLUMN_TOTALWIDTH, this);
        gridView.setLayoutParams(layoutParams);
    }

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
            resetGrids(false);
            currGridIndex = -1;
            currentSong = null;
            playEntireGridIndex = -1;

            // release audio focus
            audioMgr.abandonAudioFocus(audioFocusListener);

            myTools.vibrate(500);

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
        // if a color was specified, set grid to that color
        if (color != CURR_STATE_COLOR) {
            grid.bgColor = color;
            grid.filterColor = color;

            if (notify) {
                gridAdapter.notifyDataSetChanged();
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
            gridAdapter.notifyDataSetChanged();
        }
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
