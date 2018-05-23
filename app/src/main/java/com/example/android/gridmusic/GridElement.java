package com.example.android.gridmusic;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;


// data and methods for an individual cell in the Grid (generally referred to as a grid, with lower-case `g`)
public class GridElement {

    int imageResourceId;            // grid image
    boolean played = false;         // has this grid been played?
    int bgColor;                    // provides a border around the grid image
    int filterColor;                // filter color for different grid states

    boolean hasSongError;

    private ArrayList<Song> songList;       // list of songs
    private int numSongs = 0;               // how many songs in this grid
    private int numSongsNotPlayed = 0;      // how many songs in this grid have not been played yet

    private Random songRNG;             // for returning a random song from this grid

    // int imageR - image resource id
    GridElement(int imageR) {
        imageResourceId = imageR;
        hasSongError = false;
        songList = new ArrayList<>();
        songRNG = new Random();

        if (imageR == -1) {
            bgColor = R.color.borderEmptyGrid;
        } else {
            bgColor = R.color.borderNotPlayed;
            filterColor = R.color.filterNotPlayed;
        }
    }

    // add a song to this grid
    public void addSong(String song, String artist, String path) {
        Song newSong = new Song(song, artist, path);

        songList.add(newSong);
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

        // find arraylist index for the randomly chosen playable grid element
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
        return ((!isBlank()) && (!played));
    }

    // see if this is a blank grid
    public boolean isBlank() {
        return (imageResourceId == -1);
    }
}
