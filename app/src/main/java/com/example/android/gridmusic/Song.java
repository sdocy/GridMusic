package com.example.android.gridmusic;

// stores song information
public class Song {
    String songName;        // song name
    String artistName;      // artist name
    boolean played;         // has this song been played?
    boolean errored;        // did an error occur while trying to play the song?
    String filePath;        // path to the music file

    // String song - song name
    // String artist - artist name
    // int audioR - audio resource id
    Song(String song, String artist, String path) {
        songName = song;
        artistName = artist;
        filePath = path;
        played = false;
        errored = false;
    }
}
