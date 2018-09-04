package com.example.android.gridmusic;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CreateGridActivity extends AppCompatActivity implements TheGridClicks, LoaderManager.LoaderCallbacks<Cursor> {

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
    private final static int GRID_CREATE_NUM_COLS = 20;
    private final static int GRID_CREATE_NUM_ROWS = 20;

    // list of grids to place
    private List<GridElement> gridList = new ArrayList<>();
    GridListAdapter gridListAdapter;
    private RecyclerView gridListView;

    // list of grids user deleted (in case they want to undelete)
    private List<GridElement> delGridList = new ArrayList<>();

    // the Grid
    private List<GridElement> theGrid = new ArrayList<>();
    private GridAdapter gridAdapter;        // view adapter for the Grid
    private RecyclerView theGridView;
    private TextView emptyListView;

    // view for list of songs on a grid
    ListView gridDetailsView;

    // reference to an empty grid object
    private GridElement emptyGrid;

    // grid details list
    private GridDetailsAdapter gridDetailsAdapter;    // view adapter for the Grid

    private int saveGridNumRows;
    private int saveGridNumCols;
    private LinearLayout saveMenuView;
    SaveListAdapter saveListAdapter;
    private RecyclerView saveListView;
    private EditText saveInput;
    private String saveFileCurrentlyLoaded = null;
    private boolean loadingSaveFileToEdit = false;          // are we loading music to edit a Grid?
                                                            // triggers Grid load after gridList load
    private String loadedSaveFileData;                      // data read in from save file for editing

    // view refs
    private TextView infoViewText;
    private ImageView backArrowButton;          // go back to main menu
    private ImageView settingsButton;           // open setting layout
    private NavigationView optionsDrawer;
    private DrawerLayout optionsDrawerLayout;
    private ImageView infoButton;
    private LinearLayout moveGridView;
    private ImageView moveArrowUp;
    private ImageView moveArrowDown;
    private ImageView moveArrowLeft;
    private ImageView moveArrowRight;

    // determine if we can move Grid
    private int numGridsUp = 0;           // number of grids on upper border
    private int numGridsDown = 0;         // number of grids on lower border
    private int numGridsLeft = 0;         // number of grids on left border
    private int numGridsRight = 0;        // number of grids on right border

    private TextView numGridsView;              // number of songs in the gridList

    // user-chosen grids
    int currGridListIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_grid);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        // kick off cursorLoader to get music
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onBackPressed() {
        // XXX not sure if this is the correct way to let PlayGrid continue when the back button
        // is pressed but it seems to work fairly well
        goBackToMainMenu();

        //super.onBackPressed();
    }

    // init view refs and adapters
    private void initViews() {
        infoViewText = findViewById(R.id.createGrid_InfoView);

        // list of grids to add
        gridListView = findViewById(R.id.createGrid_GridList);

        // the Grid
        theGridView = findViewById(R.id.createGrid_TheGrid);
        emptyListView = findViewById(R.id.createGrid_empty_list);

        // grid details - songs in a grid
        gridDetailsView = findViewById(R.id.createGrid_DetailsList);

        backArrowButton = findViewById(R.id.createGrid_BackArrow);
        settingsButton = findViewById(R.id.createGrid_SettingsButton);
        optionsDrawer = findViewById(R.id.createGrid_OptionsDrawer);
        optionsDrawerLayout = findViewById(R.id.createGrid_DrawerLayout);
        infoButton = findViewById(R.id.createGrid_InfoButton);

        numGridsView = findViewById(R.id.createGrid_NumGrids);

        saveMenuView = findViewById(R.id.createGrid_SaveMenu);
        saveListView = findViewById(R.id.createGrid_SaveList);
        saveInput = findViewById(R.id.createGrid_SaveInput);

        moveGridView = findViewById(R.id.createGrid_MoveGrid);
        moveArrowUp = findViewById(R.id.createGrid_MoveUp);
        moveArrowDown = findViewById(R.id.createGrid_MoveDown);
        moveArrowLeft = findViewById(R.id.createGrid_MoveLeft);
        moveArrowRight = findViewById(R.id.createGrid_MoveRight);
    }

    // import and display music
    private void cleanGridList() {
        if (gridList.isEmpty())  {
            return;
        }

        // sort the gridList
        Collections.sort(gridList, new GridComparator());

        // remove grids with song with identical artist, album and track number
        removeDuplicates();

        infoViewText.setText("");
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
    }

    // create the Grid and fill it with empty grids
    private void initGridArray() {
        emptyGrid = new GridElement(getResources().getString(R.string.gridEmpty), true);

        // fill in the Grid with blank grids
        for (int i = 0; i < (GRID_CREATE_NUM_COLS * GRID_CREATE_NUM_ROWS); i++) {
            theGrid.add(emptyGrid);
        }
    }


    // init view adapters and layout managers
    private void initAdapters() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        gridListView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager gridListLayoutManager = new LinearLayoutManager(this);
        gridListView.setLayoutManager(gridListLayoutManager);
        gridListAdapter = new GridListAdapter(this, gridList);
        gridListView.setAdapter(gridListAdapter);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        theGridView.setHasFixedSize(true);

        // use a grid layout manager
        RecyclerView.LayoutManager theGridLayoutManager = new GridLayoutManager(this, GRID_CREATE_NUM_COLS);
        theGridView.setLayoutManager(theGridLayoutManager);

        gridAdapter = new GridAdapter(this, theGrid);
        theGridView.setAdapter(gridAdapter);

        // We just pass in the song list for the first item in gridList to jumpstart
        // gridDetailsAdapter.  It won't actually display anything until we call showGridInfo()
        // for the first time.
        gridDetailsAdapter = new GridDetailsAdapter(this, gridList.get(0).songList);
        gridDetailsView.setAdapter(gridDetailsAdapter);
    }

    // setup onClick(), onItemClick() and onItemLongClick() listeners
    private void initListeners() {
        View.OnClickListener UIClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.createGrid_BackArrow :        goBackToMainMenu();
                        break;

                    case R.id.createGrid_SettingsButton :   openSettings(Gravity.START);
                        break;

                    case R.id.createGrid_InfoButton :       openSettings(Gravity.END);
                        break;

                    default:
                        GeneralTools.notSupported(CreateGridActivity.this);
                }
            }
        };

        // this class implements its own onClick()
        backArrowButton.setOnClickListener(UIClickListener);
        settingsButton.setOnClickListener(UIClickListener);
        infoButton.setOnClickListener(UIClickListener);

        optionsDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.createGridDrawer_UndeleteLast :           undeleteLast();
                                break;

                            case R.id.createGridDrawer_UndeleteAll :            undeleteAll();
                                break;

                            case R.id.createGridDrawer_CombineGridsByArtist :   combineGrids(combineType.artist);
                                break;

                            case R.id.createGridDrawer_CombineGridsByAlbum :    combineGrids(combineType.album);
                                break;

                            case R.id.createGridDrawer_SaveGrid :               showSaveMenu();
                                break;

                            case R.id.createGridDrawer_EditGrid :               showEditMenu();
                                break;

                            case R.id.createGridDrawer_MoveGrid :               showMoveArrows();
                                break;

                            default :                                           GeneralTools.notSupported(CreateGridActivity.this);
                        }

                        // close drawer when item is tapped
                        optionsDrawerLayout.closeDrawers();

                        return true;
                    }
                });
    }

    public void setSaveEditText(String name) {
        saveInput.setText(name);
    }

    // display a tip for creating a Grid
    private void initTips() {
        Random RNG = new Random();

        // create resource name for a tip
        int tip = RNG.nextInt(getResources().getInteger(R.integer.numCreateTips));
        String tipName = getString(R.string.createTipName) + tip;

        GeneralTools.showToast(this, getString(R.string.tip) + " "
                + getString(getResources().getIdentifier(tipName, "string", getPackageName())));
    }

    // method for GridAdapter to call for onClick()
    public void theGridOnClick(int position) {
        chooseGridGrid(position);
    }

    // method for GridAdapter to call for onLongClick()
    public void theGridOnLongClick(int position) {
        returnItemToGridList(position);
    }

    public void userDeletesGridListItem(int position) {
        GridElement grid = gridList.get(position);
        // save this grid in case the user wants to undelete
        delGridList.add(grid);

        infoViewText.setText(R.string.gridDeleted);

        removeGridListItem(position);
    }

    // user pressed back arrow or back button, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(CreateGridActivity.this, MainActivity.class);

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        startActivity(mainMenuIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // user pressed settings/ info icon, open the appropriate drawer
    private void openSettings(int gravity) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        optionsDrawerLayout.openDrawer(gravity);
    }

    // undelete the last grid that was deleted from the gridList
    private void undeleteLast() {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        if (delGridList.isEmpty()) {
            GeneralTools.showToast(this, this.getString(R.string.noDelete));
            return;
        }

        int index = delGridList.size() - 1;
        insertGridListItem(delGridList.get(index));
        delGridList.remove(index);

        infoViewText.setText(R.string.gridRecovered);
    }

    // undelete all grids that have been deleted from the gridList
    private void undeleteAll() {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        if (delGridList.isEmpty()) {
            GeneralTools.showToast(this, this.getString(R.string.noDelete));
            return;
        }

        infoViewText.setText(getString(R.string.allRecovered, delGridList.size()));

        gridList.addAll(delGridList);
        delGridList.clear();

        // sort the gridList
        Collections.sort(gridList, new GridComparator());

        gridListAdapter.notifyDataSetChanged();
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
    }

    // currently : moves songs from grid1 to grid2 if the first song of both have the same artist name
    // requires a sorted list so that grids with the same artist are adjacent
    private void combineGrids(combineType combine) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

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

        // anything that adds/removes items from gridList must first reset currGridListIndex
        reset_currGridListIndex();
        for (GridElement g : toRemove) {
            gridList.remove(g);
        }

        gridListAdapter.notifyDataSetChanged();
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
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

            if (isDupeGrid(g1, g2)) {
                toRemove.add(g1);
            }
        }

        Log.e("WARNING", "removing " + toRemove.size() + " duplicates");
        for (GridElement g : toRemove) {
            gridList.remove(g);
        }
    }

    // are two grids duplicates...identical artist name, album name and track number?
    private boolean isDupeGrid(GridElement a, GridElement b) {
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

    // player pressed a grid on the gridList, highlight it and show details unless this
    // grid was already selected, then unhighlight it and clear details
    // We don't vibrate if this is called as part of loading a Grid from a save file.
    public void chooseListGrid(int position) {
        if (!loadingSaveFileToEdit) {
            GeneralTools.vibrate(this, GeneralTools.touchVibDelay);
        }

        if (currGridListIndex != -1) {
            gridList.get(currGridListIndex).filterColor = R.color.filterNotPlayed;
            gridListAdapter.notifyItemChanged(currGridListIndex);
        }

        if (currGridListIndex == position) {
            // user re-clicked the same grid, deselect it
            currGridListIndex = -1;
            gridListAdapter.notifyItemChanged(position);

            gridDetailsAdapter.turnOffDetails();

            infoViewText.setText("");

            return;
        }

        currGridListIndex = position;
        GridElement clickedGrid = gridList.get(position);
        gridListAdapter.notifyItemChanged(position);
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
    //
    // We don't vibrate if this was called as part of loading a Grid from a save file.
    private void chooseGridGrid(int position) {
        GridElement clickedGrid = theGrid.get(position);

        if (!clickedGrid.isEmpty()) {
            if (!loadingSaveFileToEdit) {
                GeneralTools.vibrate(this, GeneralTools.touchVibDelay);
            }

            if (currGridListIndex != -1) {
                // move all songs from the selected gridList grid to this Grid grid
                infoViewText.setText(getString(R.string.addedSongs, gridList.get(currGridListIndex).songList.size()));
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
        clickedGrid.filterColor = R.color.filterNotPlayed;
        clickedGrid.position = position;
        clickedGrid.gridCol = position % GRID_CREATE_NUM_COLS;
        clickedGrid.gridRow = position / GRID_CREATE_NUM_COLS;
        gridAdapter.notifyItemChanged(position);

        removeGridListItem(currGridListIndex);

        infoViewText.setText(R.string.gridAdded);

        checkGridBoundaries(position, 1);
    }

    // When we remove an item from the gridList, the indices will change, so we need to reset
    // currGridListIndex.  Anything that removes items from gridList must first call this
    // method to reset currGridListIndex.
    private void reset_currGridListIndex() {
        if (currGridListIndex != -1) {
            gridList.get(currGridListIndex).filterColor = R.color.filterNotPlayed;
            gridListAdapter.notifyItemChanged(currGridListIndex);
        }
        currGridListIndex = -1;

        gridDetailsAdapter.turnOffDetails();
    }

    // add an item from the gridList (in sorted order) and reset currGridListIndex
    private void insertGridListItem(GridElement g) {
        // anything that removes/adds items from gridList must first reset currGridListIndex
        reset_currGridListIndex();

        GridComparator gridComp = new GridComparator();
        int i;
        for (i = 0; i < gridList.size(); i++) {
            if (gridComp.compare(g, gridList.get(i)) < 0) {
                break;
            }
        }

        gridList.add(i, g);
        gridListAdapter.notifyItemInserted(i);
        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
    }

    // remove an item from the gridList and reset currGridListIndex
    private void removeGridListItem(int position) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        // anything that removes/adds items from gridList must first reset currGridListIndex
        reset_currGridListIndex();
        gridList.remove(position);

        numGridsView.setText(getString(R.string.gridsTag, gridList.size()));
        gridListAdapter.notifyItemRemoved(position);
    }

    // user long pressed a grid on the Grid, return it to the gridList
    private void returnItemToGridList(int position) {
        GridElement clickedGrid = theGrid.get(position);

        if (clickedGrid.isEmpty()) {
            return;
        }

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        insertGridListItem(clickedGrid);

        theGrid.set(position, emptyGrid);
        gridAdapter.notifyItemChanged(position);

        infoViewText.setText(R.string.gridRemoved);

        checkGridBoundaries(position, -1);
    }

    // See if we have added or subtracted a grid to/from a Grid boundary
    // in order to track how the Grid elements may be moved.
    private void checkGridBoundaries(int position, int change) {
        if ((position % GRID_CREATE_NUM_COLS) == 0) {
            numGridsLeft += change;
            if (numGridsLeft == 1) {
                moveArrowLeft.setVisibility(View.INVISIBLE);
            } else  if (numGridsLeft == 0) {
                moveArrowLeft.setVisibility(View.VISIBLE);
            }
        } else if ((position % GRID_CREATE_NUM_COLS) == (GRID_CREATE_NUM_COLS - 1)) {
            numGridsRight += change;
            if (numGridsRight == 1) {
                moveArrowRight.setVisibility(View.INVISIBLE);
            } else  if (numGridsRight == 0) {
                moveArrowRight.setVisibility(View.VISIBLE);
            }
        }

        if (position < GRID_CREATE_NUM_COLS) {
            numGridsUp += change;
            if (numGridsUp == 1) {
                moveArrowUp.setVisibility(View.INVISIBLE);
            } else  if (numGridsUp == 0) {
                moveArrowUp.setVisibility(View.VISIBLE);
            }
        } else if (position >= (GRID_CREATE_NUM_COLS * (GRID_CREATE_NUM_ROWS - 1))) {
            numGridsDown += change;
            if (numGridsDown == 1) {
                moveArrowDown.setVisibility(View.INVISIBLE);
            } else  if (numGridsDown == 0) {
                moveArrowDown.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showMoveArrows() {
        moveGridView.setVisibility(View.VISIBLE);
    }

    public void hideMoveArrows(View v) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);
        moveGridView.setVisibility(View.INVISIBLE);
    }

    public void moveGridUp(View v) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        List<GridElement> toMove = createSaveList(false);

        theGrid.clear();

        initGridArray();

        for (GridElement g : toMove) {
            g.gridRow--;
            if (g.gridRow == 0) {
                numGridsUp++;
            }
            int theGridIndex = (g.gridRow * GRID_CREATE_NUM_ROWS) + g.gridCol;
            g.position = theGridIndex;
            theGrid.set(theGridIndex, g);
        }

        gridAdapter.notifyDataSetChanged();

        //adjust number of grids on boundaries
        numGridsDown = 0;
        moveArrowDown.setVisibility(View.VISIBLE);
        if (numGridsUp >0) {
            moveArrowUp.setVisibility(View.INVISIBLE);
        }
    }

    public void moveGridDown(View v) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        List<GridElement> toMove = createSaveList(false);

        theGrid.clear();

        initGridArray();

        for (GridElement g : toMove) {
            g.gridRow++;
            if (g.gridRow == GRID_CREATE_NUM_ROWS - 1) {
                numGridsDown++;
            }
            int theGridIndex = (g.gridRow * GRID_CREATE_NUM_ROWS) + g.gridCol;
            g.position = theGridIndex;
            theGrid.set(theGridIndex, g);
        }

        gridAdapter.notifyDataSetChanged();

        //adjust number of grids on boundaries
        numGridsUp = 0;
        moveArrowUp.setVisibility(View.VISIBLE);
        if (numGridsDown >0) {
            moveArrowDown.setVisibility(View.INVISIBLE);
        }
    }

    public void moveGridLeft(View v) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        List<GridElement> toMove = createSaveList(false);

        theGrid.clear();

        initGridArray();

        for (GridElement g : toMove) {
            g.gridCol--;
            if (g.gridCol == 0) {
                numGridsLeft++;
            }
            int theGridIndex = (g.gridRow * GRID_CREATE_NUM_ROWS) + g.gridCol;
            g.position = theGridIndex;
            theGrid.set(theGridIndex, g);
        }

        gridAdapter.notifyDataSetChanged();

        //adjust number of grids on boundaries
        numGridsRight = 0;
        moveArrowRight.setVisibility(View.VISIBLE);
        if (numGridsLeft >0) {
            moveArrowLeft.setVisibility(View.INVISIBLE);
        }
    }

    public void moveGridRight(View v) {
        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        List<GridElement> toMove = createSaveList(false);

        theGrid.clear();

        initGridArray();

        for (GridElement g : toMove) {
            g.gridCol++;
            if (g.gridCol == GRID_CREATE_NUM_COLS - 1) {
                numGridsRight++;
            }
            int theGridIndex = (g.gridRow * GRID_CREATE_NUM_ROWS) + g.gridCol;
            g.position = theGridIndex;
            theGrid.set(theGridIndex, g);
        }

        gridAdapter.notifyDataSetChanged();

        //adjust number of grids on boundaries
        numGridsLeft = 0;
        moveArrowLeft.setVisibility(View.VISIBLE);
        if (numGridsRight >0) {
            moveArrowRight.setVisibility(View.INVISIBLE);
        }
    }

    private void showEditMenu() {
        PopupMenu loadMenu = new PopupMenu(this, infoViewText);

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
                //TODO see if we need to save the current Grid

                gridList.clear();
                gridListAdapter.notifyDataSetChanged();

                theGrid.clear();
                initGridArray();
                gridAdapter.notifyDataSetChanged();

                loadingSaveFileToEdit = true;

                // kick off cursorLoader to get music
                getLoaderManager().restartLoader(0, null, CreateGridActivity.this);

                loadedSaveFileData = SaveFileLoader.loadGrid(CreateGridActivity.this, item.getTitle().toString());
                saveFileCurrentlyLoaded = item.getTitle().toString();

                return true;
            }
        });
    }

    private void loadGridArray() {
        JSONObject gridInput;

        try {
            gridInput = new JSONObject(loadedSaveFileData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        try {
            JSONArray gridArray = gridInput.getJSONArray(getResources().getString(R.string.savelabelGrids));
            for (int g = 0; g < gridArray.length(); g++) {
                JSONObject jsonGrid = gridArray.getJSONObject(g);
                JSONArray songArray = jsonGrid.getJSONArray(getResources().getString(R.string.savelabelGridsSongs));
                for (int s = 0; s < songArray.length(); s++) {
                    JSONObject jsonSong = songArray.getJSONObject(s);
                    String songName = jsonSong.getString(getResources().getString(R.string.savelabelGridsSongsName));
                    int row = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsRow));
                    int col = jsonGrid.getInt(getResources().getString(R.string.savelabelGridsColumn));
                    int theGridIndex = (row * GRID_CREATE_NUM_ROWS) + col;

                    int gridListIndex = findSongInGridList(songName);
                    if (gridListIndex == -1) {
                        Log.e("ERROR", "Could not find song");
                        continue;
                    }

                    chooseListGrid(gridListIndex);
                    theGridOnClick(theGridIndex);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        gridAdapter.notifyDataSetChanged();
        loadingSaveFileToEdit = false;
    }

    // search gridList for a song name and return it's index in gridList
    private  int findSongInGridList(String songName) {
        for (int i = 0; i < gridList.size(); i++) {
            if (gridList.get(i).hasSong(songName)) {
                return i;
            }
        }

        return -1;
    }

    private void showSaveMenu() {
        // this was causing a crash
        //if (!saveList.isEmpty()) {
            //saveList.clear();
        //}
        List<String> saveList = Arrays.asList(SaveFileLoader.listSaveFiles(this));

        // save list recycler view
        RecyclerView.LayoutManager saveListLayoutManager = new LinearLayoutManager(this);
        saveListView.setLayoutManager(saveListLayoutManager);
        saveListAdapter = new SaveListAdapter(this, saveList);
        saveListView.setAdapter(saveListAdapter);

        saveMenuView.setVisibility(View.VISIBLE);
    }

    public void saveGridData(View v) {
        String filename = saveInput.getText().toString();

        // delete invalid chars for a file name
        filename = filename.replaceAll("[^a-zA-Z0-9.\\-]", "");

        if (filename.equals("")) {
            GeneralTools.showToast(this, getString(R.string.validGridName));
            return;
        }

        List<GridElement> toSave = createSaveList(true);
        if (toSave.size() == 0) {
            GeneralTools.showToast(this, getString(R.string.addMusic));

            saveInput.setText("");
            saveMenuView.setVisibility(View.GONE);

            return;
        }

        JSONObject saveData = listToJson(toSave);

        // Log.e("ERROR", saveData.toString());

        writeSaveFile(saveData, filename);

        saveInput.setText("");
        saveMenuView.setVisibility(View.GONE);

        saveFileCurrentlyLoaded = filename;
    }

    // user canceled the save, clear save edit text field and hide save menu
    public void cancelSave(View v) {
        saveInput.setText("");
        saveMenuView.setVisibility(View.GONE);
    }

    // user clicked 'Delete Saved Grid' button
    public void deleteSaveFile(View v) {
        String filename = saveInput.getText().toString();

        if (filename.equals("")) {
            GeneralTools.showToast(this, getString(R.string.chooseDelete));
            return;
        }

        final File file = getFileStreamPath(filename);
        if (!file.exists()) {
            GeneralTools.showToast(this, getString(R.string.fileNotFound));
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Save File")
                .setMessage("Do you really want to delete" + filename + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!file.delete()) {
                            GeneralTools.showToast(CreateGridActivity.this,
                                    getString(R.string.errorDeleting));
                        }

                        saveInput.setText("");
                        saveMenuView.setVisibility(View.GONE);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }



    // Find all non-empty grids and compute the bounds of the matrix to hold them,
    // only transform coords to smaller matrix if we are saving the Grid, not if we are moving it.
    private List<GridElement> createSaveList(boolean transform) {
        List<GridElement> saveGrid = new ArrayList<>();
        int maxRow = -1, minRow = GRID_CREATE_NUM_ROWS;
        int maxCol = -1, minCol = GRID_CREATE_NUM_COLS;

        for (int i = 0; i < theGrid.size(); i++) {
            GridElement g = theGrid.get(i);
            if (g.isEmpty()) {
                continue;
            }

            int row = i / GRID_CREATE_NUM_COLS;
            int col = i % GRID_CREATE_NUM_COLS;

            if (row < minRow) {
                minRow = row;
            }

            if (row > maxRow) {
                maxRow = row;
            }

            if (col < minCol) {
                minCol = col;
            }

            if (col > maxCol) {
                maxCol = col;
            }

            g.gridRow = row;
            g.gridCol = col;

            saveGrid.add(g);
        }

        saveGridNumRows = maxRow - minRow + 1;
        saveGridNumCols = maxCol - minCol + 1;

        // Transform grid coords to smaller matrix.
        // We have to do this after reading the entire grid because we
        // don't know minCol until we have read the entire grid.
        if (transform) {
            transformCoords(minRow, minCol, saveGrid);
        }

        return saveGrid;
    }

    // Transform a grid's position in the larger (20 x 20) create grid matrix, to
    // the smaller save grid matrix by subtracting out the origin position of the
    // save grid matrix.
    private void transformCoords(int x, int y, List<GridElement> dataList) {
        for (GridElement grid : dataList) {
            grid.gridRow -= x;
            grid.gridCol -= y;
        }
    }

    private JSONObject listToJson(List<GridElement> dataList) {
        JSONObject obj = new JSONObject();

        try {
            obj.put(getResources().getString(R.string.savelabelName), "savedGrid");
            obj.put(getResources().getString(R.string.savelabelRows), saveGridNumRows);
            obj.put(getResources().getString(R.string.savelabelColumns), saveGridNumCols);

            JSONArray grids = new JSONArray();
            for (GridElement grid : dataList) {
                JSONObject jsonGrid = new JSONObject();
                jsonGrid.put(getResources().getString(R.string.savelabelGridsRow), grid.gridRow);
                jsonGrid.put(getResources().getString(R.string.savelabelGridsColumn), grid.gridCol);
                jsonGrid.put(getResources().getString(R.string.savelabelGridsCoverart), grid.albumArtPath);

                JSONArray songs = new JSONArray();
                for (Song song : grid.songList) {
                    JSONObject jsonSong = new JSONObject();
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsName), song.songName);
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsArtist), song.artistName);
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsAlbum), song.albumName);
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsTrack), song.trackNumber);
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsAlbumID), song.albumID);
                    jsonSong.put(getResources().getString(R.string.savelabelGridsSongsFilepath), song.filePath);

                    songs.put(jsonSong);
                }
                jsonGrid.put(getResources().getString(R.string.savelabelGridsSongs), songs);

                grids.put(jsonGrid);
            }
            obj.put(getResources().getString(R.string.savelabelGrids), grids);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private void writeSaveFile(JSONObject obj, String filename) {
        FileOutputStream outputStream;
        String output = obj.toString();

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(output.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Retrieve a list of Music files currently listed in the Media store DB via URI.
        infoViewText.setText(R.string.loadingMusic);

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

        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            //Log.e("GET_SONGS", cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2)
            //        + "||" +  cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5));

            gridList.add(new GridElement(getResources().getString(R.string.gridUnknown), false));
            GridElement grid = gridList.get(gridList.size() - 1);

            // songName, artistName, albumName, filePath, trackNumber, albumID
            grid.addSong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
        }
        cursor.close();

        cleanGridList();

        infoViewText.setText("");

        if (gridList.isEmpty()) {
            // display empty list view
            emptyListView.setVisibility(View.VISIBLE);
            theGridView.setVisibility(View.GONE);
        } else {
            backArrowButton.setVisibility(View.VISIBLE);
            infoButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);

            initGridArray();

            initAdapters();

            initListeners();

            initTips();
        }

        // get album art
        for (int i = 0; i < gridList.size(); i++) {
            GridElement grid = gridList.get(i);

            Cursor cursorArt = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {grid.getNthSong(0).albumID}, null);

            if (cursorArt != null) {
                if (cursorArt.moveToFirst()) {
                    String path = cursorArt.getString(cursorArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    if (path != null) {
                        //Log.e("GET_ART", path);
                        grid.albumArtPath = path;

                        gridListAdapter.notifyItemChanged(i);
                    }
                }

                cursorArt.close();
            }
        }

        if (loadingSaveFileToEdit) {
            loadGridArray();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
