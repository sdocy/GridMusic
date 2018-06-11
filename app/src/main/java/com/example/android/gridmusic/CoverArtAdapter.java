package com.example.android.gridmusic;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

// view adapter for the list of albums
public class CoverArtAdapter extends RecyclerView.Adapter<CoverArtAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<DownloadedCoverArt> {

    private Context myContext;
    private List<CoverArt> albums;
    private NetworkWorker netWork;            // worker class for network tasks
    private LoaderManager lowdaMgr;         // async loader to perform network tasks

    private int numDownloaded = 0;          // number of covers downloaded

    private CoverArt albumToRetrieve;       // current album we are retrieving art for
    private String albumGetInfoRequest;  // holds URL for a cover art request

    private boolean loadingNow = false;             // is there a lowda currently running, it must finish before
    // we start a new one

    // find all         findingAll=true, findingUnknown=false
    // find unknown     findingAll=true, findingUnknown=true
    private boolean findingAll = false;
    private boolean findingUnknown = false;
    private int findingAllIndex;            // index of the album we are findin art for when finding ALL

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        TextView albumName;
        TextView findArt;
        TextView noArt;
        TextView noNet;
        ProgressBar progress;
        ImageView downloadArt;
        ImageView deviceArt;
        ImageView deviceArtCheck;
        ImageView downloadArtCheck;

        ViewHolder(View listItem) {
            super(listItem);

            artistName = listItem.findViewById(R.id.cover_art_artist_name);
            albumName = listItem.findViewById(R.id.cover_art_album_name);
            findArt = listItem.findViewById(R.id.cover_art_find_art);
            noArt = listItem.findViewById(R.id.cover_art_no_art);
            noNet = listItem.findViewById(R.id.cover_art_no_internet);
            progress = listItem.findViewById(R.id.cover_art_progress_bar);
            downloadArt = listItem.findViewById(R.id.cover_art_download_art);
            deviceArt = listItem.findViewById(R.id.cover_art_device_art);
            deviceArtCheck = listItem.findViewById(R.id.cover_art_device_art_checkmark);
            downloadArtCheck = listItem.findViewById(R.id.cover_art_download_art_checkmark);
        }
    }

    CoverArtAdapter(@NonNull Context context, @NonNull List<CoverArt> objs, LoaderManager lowdaManager) {
        myContext = context;
        albums = objs;

        netWork = new NetworkWorker(myContext);

        lowdaMgr = lowdaManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cover_art_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        CoverArt currentInfo = albums.get(position);

        holder.artistName.setText(currentInfo.artistName);
        holder.albumName.setText(currentInfo.albumName);

        // show number downloaded iff we have downloaded some art
        if (numDownloaded > 0) {
            ((DownloadArtActivity) myContext).numDownloadedView.setText(myContext.getString(R.string.coversDownloaded, numDownloaded));
        } else {
            ((DownloadArtActivity) myContext).numDownloadedView.setText("");
        }

        // show device art or "unknown" if there is no device art
        if (currentInfo.hasDeviceArt()) {
            Bitmap bm = BitmapFactory.decodeFile(currentInfo.deviceArtPath);
            holder.deviceArt.setImageBitmap(bm);
        } else {
            // this is used for unknown album art
            holder.deviceArt.setImageResource(R.drawable.unknown);
        }

        // show downloaded art if there is one
        if (currentInfo.hasDownloadedArt()) {
            holder.downloadArt.setImageBitmap(currentInfo.downloadArt.coverArt);
        }

        // got this trick for having clickable elements inside a listview from
        // https://stackoverflow.com/questions/20541821/get-listview-item-position-on-button-click
        holder.deviceArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CoverArt currElem = albums.get(holder.getAdapterPosition());
                if (currElem == null) {
                    return;
                }

                GeneralTools.vibrate(myContext, GeneralTools.touchVibDelay);

                if (!currElem.useDownloadArt) {
                    // second click will open a google search for the artist / album
                    String googleSearch = "https://www.google.com/search?tbm=isch&q=";
                    try {
                        googleSearch += URLEncoder.encode(currElem.artistName, "UTF-8");
                        googleSearch += "+" + URLEncoder.encode(currElem.albumName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("ERROR", "CoverArtAdapter:turnOnLoaded:onClick(): Problem formatting URL.", e);
                        return;
                    }

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleSearch));
                    myContext.startActivity(browserIntent);
                } else {
                    // first click switches chosen art
                    currElem.useDownloadArt = false;
                    notifyDataSetChanged();
                }
            }
        });

        // show / hide certain views based on item state
        switch (currentInfo.artState) {
            case find :         turnOnFind(holder);
                break;
            case loading :      turnOnLoading(holder);
                break;
            case loaded :       turnOnLoaded(position, holder);
                break;
            case noart :        turnOnNoArt(holder);
                break;
            case nointernet :   turnOnNoNet(holder);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    // user has not tried to find art for this album yet, find art button is visible
    private void turnOnFind(final ViewHolder holder) {
        holder.findArt.setVisibility(View.VISIBLE);
        holder.noArt.setVisibility(View.INVISIBLE);
        holder.noNet.setVisibility(View.INVISIBLE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.downloadArt.setVisibility(View.INVISIBLE);

        // got this trick for having clickable elements inside a listview from
        // https://stackoverflow.com/questions/20541821/get-listview-item-position-on-button-click
        holder.findArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (loadingNow) {
                    // can only do one load at a time
                    return;
                }

                GeneralTools.vibrate(myContext, GeneralTools.touchVibDelay);

                loadArt(holder.getAdapterPosition());
            }
        });
    }

    // we are in the process of downloading art for this item, progressbar is visible
    private void turnOnLoading(ViewHolder holder) {
        holder.findArt.setVisibility(View.INVISIBLE);
        holder.noArt.setVisibility(View.INVISIBLE);
        holder.noNet.setVisibility(View.INVISIBLE);
        holder.progress.setVisibility(View.VISIBLE);
        holder.downloadArt.setVisibility(View.INVISIBLE);
    }

    // we have successfully downloaded cover art for this item, downloaded art image is visible
    private void turnOnLoaded(int pos, final ViewHolder holder) {
        holder.findArt.setVisibility(View.INVISIBLE);
        holder.noArt.setVisibility(View.INVISIBLE);
        holder.noNet.setVisibility(View.INVISIBLE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.downloadArt.setVisibility(View.VISIBLE);

        CoverArt currElem = albums.get(pos);
        if (currElem == null) {
            return;
        }
        if (currElem.useDownloadArt) {
            // downloaded art is chosen
            holder.deviceArtCheck.setVisibility(View.INVISIBLE);
            holder.downloadArtCheck.setVisibility(View.VISIBLE);
            holder.deviceArt.setColorFilter(myContext.getResources().getColor(R.color.filterPlayed));
            holder.downloadArt.setColorFilter(myContext.getResources().getColor(R.color.filterNotPlayed));
        } else {
            // device art is chosen
            holder.deviceArtCheck.setVisibility(View.VISIBLE);
            holder.downloadArtCheck.setVisibility(View.INVISIBLE);
            holder.deviceArt.setColorFilter(myContext.getResources().getColor(R.color.filterNotPlayed));
            holder.downloadArt.setColorFilter(myContext.getResources().getColor(R.color.filterPlayed));
        }

        // got this trick for having clickable elements inside a listview from
        // https://stackoverflow.com/questions/20541821/get-listview-item-position-on-button-click
        holder.downloadArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                CoverArt currElem = albums.get(holder.getAdapterPosition());
                if (currElem == null) {
                    return;
                }

                GeneralTools.vibrate(myContext, GeneralTools.touchVibDelay);

                if (currElem.useDownloadArt) {
                    // user already chose download art, we open image URL on second click
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currElem.downloadArt.coverArtURL));
                    myContext.startActivity(browserIntent);
                } else {
                    // first click switches chosen art
                    currElem.useDownloadArt = true;
                    notifyDataSetChanged();
                }
            }
        });
    }

    // we did not find any cover art for this item, "no art" message is visible
    private void turnOnNoArt(ViewHolder holder) {
        holder.findArt.setVisibility(View.INVISIBLE);
        holder.noArt.setVisibility(View.VISIBLE);
        holder.noNet.setVisibility(View.INVISIBLE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.downloadArt.setVisibility(View.INVISIBLE);
    }

    // we have no internet connect, "no internet" message is visible
    private void turnOnNoNet(ViewHolder holder) {
        holder.findArt.setVisibility(View.INVISIBLE);
        holder.noArt.setVisibility(View.INVISIBLE);
        holder.noNet.setVisibility(View.VISIBLE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.downloadArt.setVisibility(View.INVISIBLE);
    }

    // find cover art for the specified item
    private void loadArt(int position) {
        if ((position < 0) || (position > getItemCount())) {
            return;
        }

        CoverArt currElem = albums.get(position);
        if (currElem == null) {
            return;
        }

        // check for internet connection
        if (!netWork.checkConnectivity()) {
            currElem.artState = CoverArt.state.nointernet;
            notifyDataSetChanged();

            return;
        } else {
            currElem.artState = CoverArt.state.loading;
            notifyDataSetChanged();
        }

        // form album.getinfo URL query
        albumGetInfoRequest = netWork.formatMusicBrainzAlbumGetinfoRequest(currElem);
        if (albumGetInfoRequest == null) {
            return;
        }

        // save ref to the album we are getting art for so we can update it in onLoadFinished()
        albumToRetrieve = currElem;

        // disable other loads until this one is done
        loadingNow = true;

        lowdaMgr.restartLoader(1, null, CoverArtAdapter.this);
    }

    @Override
    // create a lowda for downloading cover art
    public Loader<DownloadedCoverArt> onCreateLoader(int id, Bundle args) {
        return new CoverLowda(myContext, albumGetInfoRequest);
    }

    @Override
    // loading of art has completed
    public void onLoadFinished(Loader<DownloadedCoverArt> lowda, DownloadedCoverArt data) {
        if (data == null) {
            albumToRetrieve.artState = CoverArt.state.noart;
        } else {
            albumToRetrieve.artState = CoverArt.state.loaded;
            albumToRetrieve.downloadArt = data;
            numDownloaded++;
        }

        // we are done loading, user may start a new load
        loadingNow = false;

        notifyDataSetChanged();

        // if we are doing auto-find, get the next album
        if (findingAll) {
            int toFind = findAlbumToFind(findingAllIndex + 1, findingUnknown);
            if (toFind >= 0) {
                loadArt(toFind);
            } else {
                // we are done
                findingAll = false;
                ((DownloadArtActivity) myContext).enableAutoFind();
            }
        }
    }

    @Override
    // lowda has been reset
    public void onLoaderReset(Loader<DownloadedCoverArt> lowda) {

    }

    // kick off an auto-find by finding the first album to find art for
    public void findAllArt(boolean unknownOnly) {
        GeneralTools.vibrate(myContext, GeneralTools.touchVibDelay);

        findingAll = true;
        findingUnknown = unknownOnly;

        int toFind = findAlbumToFind(0, findingUnknown);
        if (toFind >= 0) {
            loadArt(toFind);
        } else {
            findingAll = false;
        }
    }

    // find an album that we need to get cover art for, return the album's list position
    private int findAlbumToFind(int startingPos, boolean unknownOnly) {
        // look for an album without device art
        for (findingAllIndex = startingPos; findingAllIndex < getItemCount(); findingAllIndex++) {
            CoverArt alb = albums.get(findingAllIndex);

            // have we downloaded art for this album yet?
            if (!alb.hasDownloadedArt()) {
                // is there device art for this album?
                if ((!unknownOnly) || (!alb.hasDeviceArt())) {
                    break;
                }
            }
        }

        if (findingAllIndex < getItemCount()) {
            return findingAllIndex;
        } else {
            return -1;
        }
    }
}
