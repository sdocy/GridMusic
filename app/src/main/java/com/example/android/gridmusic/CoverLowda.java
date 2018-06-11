package com.example.android.gridmusic;

import android.content.AsyncTaskLoader;
import android.content.Context;

// Cover lowda.....sasake?
public class CoverLowda extends AsyncTaskLoader<DownloadedCoverArt> {

    private String musicURL;

    CoverLowda(Context context, String requestUrl) {
        super(context);

        musicURL = requestUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public DownloadedCoverArt loadInBackground() {
        //Log.d("NETWORK", "retrieving " + musicURL);

        return NetworkWorker.retrieveAlbumInfo(musicURL);
    }
}
