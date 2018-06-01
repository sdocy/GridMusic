package com.example.android.gridmusic;

// stores song information
public class Song {
    String songName;        // song name
    String artistName;      // artist name
    String albumName;       // album name
    boolean played;         // has this song been played?
    boolean errored;        // did an error occur while trying to play the song?
    String filePath;        // path to the music file
    int trackNumber;

    // String song - song name
    // String artist - artist name
    // int audioR - audio resource id
    Song(String song, String artist, String album, String path, int track) {
        songName = song;
        artistName = artist;
        albumName = album;
        filePath = path;
        played = false;
        errored = false;
        trackNumber = track;
    }
}
