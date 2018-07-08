package com.example.android.gridmusic;


// class to hold cover art data
public class CoverArt {
    String artistName;
    String albumName;
    String albumID;
    String deviceArtPath;       // string to cover art located on the device
    state artState;
    DownloadedCoverArt downloadArt;
    boolean useDownloadArt;     // user wants to use the downloaded art

    enum state {find, loading, loaded, noart, nointernet}       // list item states for each album

    CoverArt(String artist, String album, String id) {
        artistName = artist;
        albumName = album;
        albumID = id;
        artState = CoverArt.state.find;
        downloadArt = null;
        useDownloadArt = false;
    }

    // was cover art for this album on the device?
    public boolean hasDeviceArt() {
        return (deviceArtPath != null);
    }

    // did we download cover art for this album from the internet?
    public boolean hasDownloadedArt() {
        return (downloadArt != null);
    }
}
