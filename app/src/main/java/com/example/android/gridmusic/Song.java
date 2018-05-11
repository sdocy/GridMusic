package com.example.android.gridmusic;

// stores song information
public class Song {
    String songName;
    String artistName;
    boolean played;

    Song(String song, String artist) {
        songName = song;
        artistName = artist;
        played = false;
    }
}
