package com.example.android.gridmusic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadArtActivity extends AppCompatActivity {

    List<CoverArt> albumList;
    private RecyclerView albumListView;
    private TextView emptyListView;

    private CoverArtAdapter adapter = null;

    private ImageView backArrowButton;          // go back to main menu
    private TextView findAllArtView;
    private TextView findUnknownArtView;
    private boolean findingArt = false;

    // this are updated directly by our CoverArtAdapter
    TextView numDownloadedView;

    // for sorting the gridList
    //sort by artist name -> album name -> track number
    public class AlbumComparator implements Comparator<CoverArt>
    {
        public int compare(CoverArt left, CoverArt right) {
            if (left.artistName.equals(right.artistName)) {
                return left.albumName.compareTo(right.albumName);
            } else {
                return left.artistName.compareTo(right.artistName);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_art);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        getAlbumList();

        if (albumList.isEmpty()) {
            // display empty list view
            emptyListView.setVisibility(View.VISIBLE);
            albumListView.setVisibility(View.GONE);

            // grey out auto-find buttons
            findAllArtView.setTextColor(getResources().getColor(R.color.filterPlayed));
            findUnknownArtView.setTextColor(getResources().getColor(R.color.filterPlayed));
            backArrowButton.setVisibility(View.INVISIBLE);
        } else {
            initAdapters();

            initListeners();
        }
    }

    @Override
    public void onBackPressed() {
        // XXX not sure if this is the correct way to let PlayGrid continue when the back button
        // is pressed but it seems to work fairly well
        goBackToMainMenu();

        //super.onBackPressed();
    }

    private void initViews() {
        albumListView = findViewById(R.id.download_art_song_list);
        emptyListView = findViewById(R.id.download_art_empty_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        albumListView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager albumListLayoutManager = new LinearLayoutManager(this);
        albumListView.setLayoutManager(albumListLayoutManager);

        findAllArtView = findViewById(R.id.download_art_find_all_art);
        findUnknownArtView = findViewById(R.id.download_art_find_unknown_art);
        numDownloadedView = findViewById(R.id.download_art_num_downloaded);

        backArrowButton = findViewById(R.id.download_art_BackArrow);
    }

    // user pressed back arrow or back button, load MainActivity
    private void goBackToMainMenu() {
        // Create a new intent to open the activity
        Intent mainMenuIntent = new Intent(DownloadArtActivity.this, MainActivity.class);

        GeneralTools.vibrate(this, GeneralTools.touchVibDelay);

        startActivity(mainMenuIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // initialize music list
    private void getAlbumList() {
        albumList = getMusicList();

        // sort the songList
        Collections.sort(albumList, new AlbumComparator());

        // requires list to be sorted by artist and album
        removeAlbumDuplicates();
    }

    private void initAdapters() {
        adapter = new CoverArtAdapter(this, albumList, getLoaderManager());

        albumListView.setAdapter(adapter);
    }

    private void initListeners() {
        // listener for main menu navigation buttons
        OnClickListener downLoadArtListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to open the activity
                switch (view.getId()) {
                    case R.id.download_art_BackArrow:   goBackToMainMenu();
                                                        break;
                }
            }
        };

        backArrowButton.setOnClickListener(downLoadArtListener);
    }

    // intiate auto-find for all albums
    public void findAllArt(View v) {
        if (findingArt) {
            // let current auto-find complete
            return;
        }

        findingArt = true;
        findAllArtView.setTextColor(getResources().getColor(R.color.filterPlayed));
        findUnknownArtView.setTextColor(getResources().getColor(R.color.filterPlayed));

        if (adapter != null) {
            adapter.findAllArt(false);
        }
    }

    // intiate auto-find for albums with no cover art on this device
    public void findUnknownArt(View v) {
        if (findingArt) {
            // let current auto-find complete
            return;
        }

        findingArt = true;
        findAllArtView.setTextColor(getResources().getColor(R.color.filterPlayed));
        findUnknownArtView.setTextColor(getResources().getColor(R.color.filterPlayed));

        if (adapter != null) {
            adapter.findAllArt(true);
        }
    }

    // current auto-find is complete, re-enable find buttons
    public void enableAutoFind() {
        findingArt = false;

        findAllArtView.setTextColor(getResources().getColor(R.color.MainMenuTextColor));
        findUnknownArtView.setTextColor(getResources().getColor(R.color.MainMenuTextColor));
    }

    // remove songs that are on the same album, requires list to be sorted based on artist
    // and album so that duplicates will be adjacent
    private void removeAlbumDuplicates() {
        List<CoverArt> toRemove = new ArrayList<>();

        for (int i = 0; i < albumList.size() - 1; i++) {
            CoverArt s1 = albumList.get(i);
            CoverArt s2 = albumList.get(i + 1);

            if (dupeAlbum(s1, s2)) {
                toRemove.add(s1);
            }
        }

        for (CoverArt s : toRemove) {
            albumList.remove(s);
        }
    }

    // are two songs on the same album...identical artist name and album name?
    private boolean dupeAlbum(CoverArt a, CoverArt b) {
        if (!a.artistName.equals(b.artistName)) {
            return false;
        }

        return (a.albumName.equals(b.albumName));
    }

    // this code retrieved from https://gist.github.com/novoda/374533
    // it uses MediaStore to find all music files and related cover art on this device
    private List<CoverArt> getMusicList() {
        Cursor cursor;
        List<CoverArt> grids = new ArrayList<>();

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

        // deprecated, should use CursorLoader
        cursor = this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);

        while (cursor.moveToNext()) {
            //Log.e("GET_SONGS", cursor.getString(0) + "||" + cursor.getString(1) + "||" + cursor.getString(2)
            //        + "||" +  cursor.getString(3) + "||" + cursor.getString(4) + "||" + cursor.getString(5));

            grids.add(new CoverArt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))));
            CoverArt s = grids.get(grids.size() - 1);
            // songName, artistName, albumName, filePath, trackNumber
            /*s.addSong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));*/

            Cursor cursorArt = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))}, null);
            if (cursorArt != null) {
                if (cursorArt.moveToFirst()) {
                    String path = cursorArt.getString(cursorArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    if (path != null) {
                        //Log.e("GET_ART", path);
                        s.deviceArtPath = path;
                    }
                }

                cursorArt.close();
            }
        }

        return grids;
    }
}
