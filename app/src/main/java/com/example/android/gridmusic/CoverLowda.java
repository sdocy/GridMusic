package com.example.android.gridmusic;

import android.content.AsyncTaskLoader;
import android.content.Context;


public class CoverLowda extends AsyncTaskLoader<DownloadedCoverArt> {

    private String musicBrainzFirstURL;

    CoverLowda(Context context, String requestUrl) {
        super(context);

        musicBrainzFirstURL = requestUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public DownloadedCoverArt loadInBackground() {
        //Log.d("NETWORK", "retrieving " + musicBrainzFirstURL);

        return NetworkWorker.retrieveAlbumInfo(musicBrainzFirstURL);
    }
}
