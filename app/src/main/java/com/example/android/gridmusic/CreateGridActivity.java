package com.example.android.gridmusic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CreateGridActivity extends AppCompatActivity implements View.OnClickListener{

    // for sorting the gridList
    //sort by artist name -> album name -> track number
    public class GridComparator implements Comparator<GridElement>
    {
        public int compare(GridElement left, GridElement right) {
            Song s1 = left.getNthSong(0);
            Song s2 = right.getNthSong(0);

            if (s1.artistName.equals(s2.artistName)) {
                if (s1.albumName.equals(s2.albumName)) {
                    return s1.trackNumber - s2.trackNumber;
                } else {
                    return s1.albumName.compareTo(s2.albumName);
                }
            } else {
                return s1.artistName.compareTo(s2.artistName);
            }
        }
    }

    // used for combining grids
    enum combineType {artist, album}

    // Grid size
    private final int GRID_CREATE_NUM_COLS = 20;
    private final int GRID_CREATE_NUM_ROWS = 20;

    // list of grids to place
    private List<GridElement> gridList = new ArrayList<>();
    GridListAdapter gridListAdapter;
    private ListView gridListView;

    // list of grids user deleted (in case they want to undelete)
    private List<GridElement> delGridList = new ArrayList<>();

    // the Grid
    private List<GridElement> theGrid = new ArrayList<>();
    private GridAdapter gridAdapter;        // view adapter for the Grid
    private GridView theGridView;

    // reference to an empty griod object
    private GridElement emptyGrid;

    // grid details list
    private GridDetailsAdapter gridDetailsAdapter;    // view adapter for the Grid

    // view refs
    private TextView infoViewText;
    private ImageView backArrowButton;          // go back to main menu
    private ImageView settingsButton;           // open setting layout
    private LinearLayout settingsLayout;        // expandable layout to expose settings
    private TextView numGridsView;              // number of songs in the gridList
    private TextView undeleteLastText;          // clickable settings textview to undelete the last song deleted
    private TextView undeleteAllText;           // clickable settings textview to undelete all deleted songs
    private TextView combineByArtistText;       // clickable settings textview to combine all grids for same artist
    private TextView combineByAlbumText;        // clickable settings textview to combine all grids for same album

    private boolean showingSettings = false;    // is the settings layout expanded?

    // expand/collapse settings layout
    private LinearLayout.LayoutParams closedParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
    private LinearLayout.LayoutParams openParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    // user-chosen grids
    int currGridListIndex = -1;

    private GeneralTools myTools;           // class with useful tools

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_grid);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // misc initialization
        initMisc();

        // init view refs that are used when creating the Grid and the gridList
        initEarlyViews();

        initSongList();

        // get list of grid elements
        initGridArray();

        initViews();

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

    // views we need refs to prior to importing music
    private void initEarlyViews() {
        // we need this view early to display loading status
        infoViewText = findViewById(R.id.createGrid_InfoView);


    }

    // import and display music
    private void initSongList() {
        infoViewText.setText(R.string.loadingSongs);

        getMusicList();

        // sort the gridList
        Collections.sort(gridList, new GridComparator());

        // remove grids with song with identical artist, album and track number
        removeDuplicates();

        infoViewText.setText("");
    }

    // create the Grid and fill it with empty grids
    private void initGridArray() {
        emptyGrid = new GridElement(-2);

        // fill in the Grid with blank grids
        for (int i = 0; i < (GRID_CREATE_NUM_COLS * GRID_CREATE_NUM_ROWS); i++) {
            theGrid.add(emptyGrid);
        }
    }

    // init view refs and adapters
    private void initViews() {
        // Find a reference to the {@link ListView} in the layout
        gridListView = findViewById(R.id.createGrid_GridList);
        if (gridListView == null) {
            throw new AssertionError("CreateGridActivity.initViews() : null gridListView");
        }
        gridListAdapter = new GridListAdapter(this, gridList);
        gridListView.setAdapter(gridListAdapter);

        theGridView = findViewById(R.id.createGrid_TheGrid);
        gridAdapter = new GridAdapter(this, theGrid);
        if (theGridView == null) {
            throw new AssertionError("CreateGridActivity.initViews() : null gridView");
        }
        theGridView.setAdapter(gridAdapter);

        ListView gridDetailsView;
        gridDetailsView = findViewById(R.id.createGrid_DetailsList);
        // We just pass in the song list for the first item in gridList to jumpstart
        // gridDetailsAdapter.  It won't actually display anything until we call showGridInfo()
        // for the first time.
        gridDetailsAdapter = new GridDetailsAdapter(this, gridList.get(0).songList);
        gridDetailsView.setAdapter(gridDetailsAdapter);

        setGridTotalWidth();
        theGridView.setNumColumns(GRID_CREATE_NUM_COLS);

        backArrowButton = findViewById(R.id.createGrid_BackArrow);
        settingsButton = findViewById(R.id.createGrid_SettingsButton);
        settingsLayout = findViewById(R.id.createGrid_SettingsLayout);
        undeleteLastText = findViewById(R.id.createGrid_Settings_UndeleteLast);
        undeleteAllText = findViewById(R.id.createGrid_Settings_UndeleteAll);
        combineByArtistText = findViewById(R.id.createGrid_Settings_CombineArtist);
        combineByAlbumText = findViewById(R.id.createGrid_Settings_CombineAlbum);

        numGridsView = findViewById(R.id.createGrid_NumGrids);
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
    }

    // misc setup
    private void initMisc() {
        myTools = new GeneralTools(this);
    }

    // display a tip for creating a Grid
    private void initTips() {
        Random RNG = new Random();

        // create resource name for a tip
        int tip = RNG.nextInt(getResources().getInteger(R.integer.numCreateTips));
        String tipName = getString(R.string.createTipName) + tip;

        myTools.showToast(getString(R.string.tip) + " "
                + getString(getResources().getIdentifier(tipName, "string", getPackageName())));
    }

    // setup onClick(), onItemClick() and onItemLongClick() listeners
    private void initListeners() {
        // this class implements its own onClick()
        backArrowButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        undeleteLastText.setOnClickListener(this);
        undeleteAllText.setOnClickListener(this);
        combineByArtistText.setOnClickListener(this);
        combineByAlbumText.setOnClickListener(this);

        // select a grid from the gridList
        gridListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                chooseListGrid(position);
            }
        });

        // long click a gridList grid to delete it
        gridListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                GridElement grid = gridList.get(position);
                // save this grid in case the user wants to undelete
                delGridList.add(grid);

                infoViewText.setText(R.string.gridDeleted);

                removeGridListItem(position);

                return true;
            }
        });

        theGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                chooseGridGrid(position);
            }
        });

        // long click a Grid grid to put it back on the gridList
        theGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                returnItemToGridList(position);
                return true;
            }
        });
    }

    // non-list and non-grid clicks
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createGrid_BackArrow :                goBackToMainMenu();
                break;

            case R.id.createGrid_SettingsButton :           openSettings();
                break;

            case R.id.createGrid_Settings_UndeleteLast :    undeleteLast();
                break;

            case R.id.createGrid_Settings_UndeleteAll :     undeleteAll();
                break;

            case R.id.createGrid_Settings_CombineArtist :   combineGrids(combineType.artist);
                break;

            case R.id.createGrid_Settings_CombineAlbum :    combineGrids(combineType.album);
                break;

            default :                                       myTools.notSupported();
        }
    }

    // user pressed back arrow or back button, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(CreateGridActivity.this, MainActivity.class);

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

    // undelete the last grid that was deleted from the gridList
    private void undeleteLast() {
        myTools.vibrate(GeneralTools.touchVibDelay);

        if (delGridList.isEmpty()) {
            myTools.showToast(this.getString(R.string.noDelete));
            return;
        }

        int index = delGridList.size() - 1;
        gridList.add(delGridList.get(index));
        delGridList.remove(index);

        // sort the gridList
        Collections.sort(gridList, new GridComparator());
        updateGridList();

        infoViewText.setText(R.string.gridRecovered);
    }

    // undelete all grids that have been deleted from the gridList
    private void undeleteAll() {
        myTools.vibrate(GeneralTools.touchVibDelay);

        if (delGridList.isEmpty()) {
            myTools.showToast(this.getString(R.string.noDelete));
            return;
        }

        infoViewText.setText(getString(R.string.allRecovered, delGridList.size()));

        gridList.addAll(delGridList);
        delGridList.clear();

        // sort the gridList
        Collections.sort(gridList, new GridComparator());
        updateGridList();
    }

    // currently : moves songs from grid1 to grid2 if the first song of both have the same artist name
    // requires a sorted list so that grids with the same artist are adjacent
    private void combineGrids(combineType combine) {
        myTools.vibrate(GeneralTools.touchVibDelay);

        List<GridElement> toRemove = new ArrayList<>();

        for (int i = 0; i < gridList.size() - 1; i++) {
            GridElement g1 = gridList.get(i);
            GridElement g2 = gridList.get(i + 1);

            Song s1 = g1.getNthSong(0);
            Song s2 = g2.getNthSong(0);

            if (!s1.artistName.equals(s2.artistName)) {
                continue;
            }

            // when combining by album, make sure both artist name and album name match
            if ((combine == combineType.album) && (!s1.albumName.equals(s2.albumName))) {
                continue;
            }

            for (Song s : g1.songList) {
                g2.addSong(s);
            }
            toRemove.add(g1);
        }

        infoViewText.setText(getString(R.string.gridCombined, toRemove.size()));

        // anything that removes items from gridList must first reset currGridListIndex
        reset_currGridListIndex();

        for (GridElement g : toRemove) {
            gridList.remove(g);
        }

        updateGridList();
    }

    // this code retrieved from https://gist.github.com/novoda/374533
    private void getMusicList() {
        Cursor cursor;

        //Retrieve a list of Music files currently listed in the Media store DB via URI.

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TRACK
        };

        cursor = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);

        while (cursor.moveToNext()) {
            //Log.e("GET_SONGS", cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2)
            //        + "||" +  cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5));

            gridList.add(new GridElement(R.drawable.emptygrid));
            GridElement grid = gridList.get(gridList.size() - 1);
            // songName, artistName, albumName, filePath, trackNumber
            grid.addSong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));

            Cursor cursorArt = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))}, null);
            if (cursorArt != null) {
                if (cursorArt.moveToFirst()) {
                    String path = cursorArt.getString(cursorArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    if (path != null) {
                        //Log.e("GET_ART", path);
                        grid.albumArtPath = path;
                    } else {
                        grid.imageResourceId = R.drawable.unknown;
                    }
                }

                cursorArt.close();
            } else {
                grid.imageResourceId = R.drawable.unknown;
            }
        }
    }

    // Remove duplicate songs.
    // We are comparing songs, so it's hard to do if there are multiple songs in a grid
    // so we only support removing dupes when we have single-song grids, such as
    // right after importing and creating the gridList.
    //
    // Duplicates are determined by equal artist name, album name and track number.
    // Requires a sorted list so that duplicates are adjacent.
    private void removeDuplicates() {
        List<GridElement> toRemove = new ArrayList<>();

        for (int i = 0; i < gridList.size() - 1; i++) {
            GridElement g1 = gridList.get(i);
            GridElement g2 = gridList.get(i + 1);

            if (g1.numSongs() != 1) {
                continue;
            }

            if (dupeGrid(g1, g2)) {
                toRemove.add(g1);
            }
        }

        Log.e("WARNING", "removing " + toRemove.size() + " duplicates");
        for (GridElement g : toRemove) {
            gridList.remove(g);
        }
    }

    // are two grids duplicates...identical artist name, album name and track number?
    private boolean dupeGrid(GridElement a, GridElement b) {
        Song s1 = a.getNthSong(0);
        Song s2 = b.getNthSong(0);

        if (!s1.artistName.equals(s2.artistName)) {
            return false;
        }

        if (!s1.albumName.equals(s2.albumName)) {
            return false;
        }

        return (s1.trackNumber == s2.trackNumber);
    }

    // expand the GridView to hold the correct number of columns
    private void setGridTotalWidth() {
        ViewGroup.LayoutParams layoutParams = theGridView.getLayoutParams();
        layoutParams.width = myTools.convertDpToPixels(GRID_CREATE_NUM_COLS * MainActivity.GRID_COLUMN_TOTALWIDTH, this);
        theGridView.setLayoutParams(layoutParams);
    }

    // player pressed a grid on the gridList, highlight it and show details unless this
    // grid was already selected, then unhighlight it and clear details
    private void chooseListGrid(int position) {
        myTools.vibrate(GeneralTools.touchVibDelay);

        if (currGridListIndex != -1) {
            gridList.get(currGridListIndex).filterColor = R.color.filterNotPlayed;
        }

        if (currGridListIndex == position) {
            // user re-clicked the same grid, deselect it
            currGridListIndex = -1;
            gridListAdapter.notifyDataSetChanged();

            gridDetailsAdapter.turnOffDetails();

            infoViewText.setText("");

            return;
        }

        currGridListIndex = position;
        GridElement clickedGrid = gridList.get(position);
        gridListAdapter.notifyDataSetChanged();
        clickedGrid.filterColor = R.color.filterNextToPlay;
        showGridInfo(clickedGrid);
        infoViewText.setText(getString(R.string.numSongs, clickedGrid.songList.size()));
    }

    // user chose a grid from the gridList,
    // give gridDetailsAdapter the song list for the selected grid
    private void showGridInfo(GridElement grid) {
        gridDetailsAdapter.newDetailsList(grid.songList);
    }

    // user pressed a grid on the Grid
    // if the grid they pressed is not empty
    //      if there is a gridList grid selected, move all songs to the pressed grid
    //      else show info for the pressed grid
    // else
    //      if there is a gridList grid selected, move that grid to the pressed grid
    private void chooseGridGrid(int position) {
        GridElement clickedGrid = theGrid.get(position);

        if (!clickedGrid.isEmpty()) {
            myTools.vibrate(GeneralTools.touchVibDelay);

            if (currGridListIndex != -1) {
                infoViewText.setText(getString(R.string.addedSongs, gridList.get(currGridListIndex).songList.size()));
                // move all songs from the selected gridList grid to this Grid grid
                for (Song s : gridList.get(currGridListIndex).songList) {
                    clickedGrid.addSong(s);
                }

                removeGridListItem(currGridListIndex);
            } else {
                // if no gridList grid is selected, just show info for this Grid grid
                infoViewText.setText(getString(R.string.numSongs, clickedGrid.songList.size()));
                showGridInfo(clickedGrid);
            }

            return;
        }

        if (currGridListIndex == -1) {
            // no gridList grid selected, do nothing
            return;
        }

        theGrid.set(position, gridList.get(currGridListIndex));
        theGrid.get(position).filterColor = R.color.filterNotPlayed;
        gridAdapter.notifyDataSetChanged();

        removeGridListItem(currGridListIndex);

        infoViewText.setText(R.string.gridAdded);
    }

    // When we remove an item from the gridList, the indices will change, so we need to reset
    // currGridListIndex.  Anything that removes items from gridList must first call this
    // method to reset currGridListIndex.
    private void reset_currGridListIndex() {
        if (currGridListIndex != -1) {
            gridList.get(currGridListIndex).filterColor = R.color.filterNotPlayed;
        }
        currGridListIndex = -1;

        gridDetailsAdapter.turnOffDetails();
    }

    // remove an item from the gridList and reset currGridListIndex
    private void removeGridListItem(int position) {
        myTools.vibrate(GeneralTools.touchVibDelay);

        // anything that removes items from gridList must first reset currGridListIndex
        reset_currGridListIndex();

        gridList.remove(position);

        updateGridList();
    }

    // user long pressed a grid on the Grid, return it to the gridList
    private void returnItemToGridList(int position) {
        GridElement clickedGrid = theGrid.get(position);

        if (clickedGrid.isEmpty()) {
            return;
        }

        myTools.vibrate(GeneralTools.touchVibDelay);

        gridList.add(clickedGrid);
        // sort the gridList
        Collections.sort(gridList, new GridComparator());
        updateGridList();

        theGrid.set(position, emptyGrid);
        gridAdapter.notifyDataSetChanged();

        infoViewText.setText(R.string.gridRemoved);
    }

    // notify gridListAdapter of data changes and update the number of grids
    private void updateGridList() {
        gridListAdapter.notifyDataSetChanged();
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
    }
}
