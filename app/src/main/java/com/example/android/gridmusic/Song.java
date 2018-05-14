package com.example.android.gridmusic;

// stores song information
public class Song {
    String songName;        // song name
    String artistName;      // artist name
    boolean played;         // has this song been played?
    int audioResource;      // audio resource id

    // String song - song name
    // String artist - artist name
    // int audioR - audio resource id
    Song(String song, String artist, int audioR) {
        songName = song;
        artistName = artist;
        audioResource = audioR;
        played = false;
    }
}
