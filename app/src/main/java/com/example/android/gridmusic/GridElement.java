package com.example.android.gridmusic;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// data and methods for an individual cell in the Grid (generally referred to as a grid, with lower-case `g`)
public class GridElement {

    boolean played = false;         // has this grid been played?
    int bgColor;                    // provides a border around the grid image
    int filterColor;                // filter color for different grid states
    String albumArtPath;
    private boolean isEmptyGrid;

    // used when saving and loading grids
    public int gridRow;
    public int gridCol;

    int position = -1;              // position of the grid in the Grid
    boolean hasSongError;

    public List<Song> songList;       // list of songs
    private int numSongs = 0;               // how many songs in this grid
    private int numSongsNotPlayed = 0;      // how many songs in this grid have not been played yet

    private Random songRNG;             // for returning a random song from this grid

    // int imageR - image resource id
    GridElement(String artPath, boolean specialGrid) {
        albumArtPath = artPath;
        hasSongError = false;
        songList = new ArrayList<>();
        songRNG = new Random();

        if (specialGrid) {
            bgColor = R.color.borderEmptyGrid;
            isEmptyGrid = true;
        } else {
            bgColor = R.color.borderNotPlayed;
            filterColor = R.color.filterNotPlayed;
            isEmptyGrid = false;
        }
    }

    // add a song to this grid
    public void addSong(String song, String artist, String album, String path, int track, String id) {
        Song newSong = new Song(song, artist, album, path, track, id);

        songList.add(newSong);
        numSongs++;
        numSongsNotPlayed++;
    }

    public void addSong(Song s) {
        songList.add(s);
        numSongs++;
        numSongsNotPlayed++;
    }

    // return a random song that has not been set to played
    // if `setToPlayed` is true, set the song to played
    public Song getRandomSongNotPlayed(boolean setToPlayed) {
        if ((numSongs == 0) || (numSongsNotPlayed == 0)) {
            return null;
        }

        int randomSong = songRNG.nextInt(numSongsNotPlayed);
        int index;
        int count = -1;

        // find list index for the randomly chosen playable grid element
        for (index = 0; index < numSongs; index++) {
            // skip played songs
            if (songList.get(index).played)  {
                continue;
            }

            // found one
            count++;
            if (count == randomSong) {
                break;
            }
        }

        if (count != randomSong) {
            // we didn't find one
            Log.e("ERROR", "GridElement.getRandomSongNotPlayed() : unable to find playable song(" + randomSong + ")");
            return null;
        }

        if (setToPlayed) {
            songList.get(index).played = true;
            numSongsNotPlayed--;
        }

        return songList.get(index);
    }

    // get a song at a specific index, useful for playing grid songs in order
    public Song getNthSong(int n) {
        if (n > songList.size() - 1) {
            return null;
        } else {
            return songList.get(n);
        }
    }

    // get the index of the specified song
    public int getSongIndex(Song s) {
        return songList.indexOf(s);
    }

    // set all songs for this grid to not-played
    public void setAllSongsToNotPlayed() {
        for (Song s : songList) {
            s.played = false;
        }
        numSongsNotPlayed = numSongs;
    }

    // see if grid is playable (non-blank, non-played)
    public boolean isPlayable() {
        return ((!isEmpty()) && (!played));
    }

    // see if this is a blank or empty grid
    public boolean isEmpty() {
        return (isEmptyGrid);
    }

    public int numSongs() {
        return songList.size();
    }

    public boolean hasSong(String songName) {
        for (Song s : songList) {
            if (songName.equals(s.songName)) {
                return true;
            }
        }

        return false;
    }
}
