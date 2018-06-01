package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

// view adapter for our grid view
public class GridListAdapter extends ArrayAdapter<GridElement> {

    private Context myContext;
    private List<GridElement> songList;

    GridListAdapter(Context context, List<GridElement> list) {
        super (context, 0, list);
        myContext = context;
        songList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_list_item, parent, false);
        }

        GridElement currentElem = getItem(position);
        if (currentElem == null) {
            throw new AssertionError("SongListAdapter.getView() : null currentElem");
        }

        ImageView coverArt = listItem.findViewById(R.id.gridlist_CoverArt);
        if (currentElem.albumArtPath != null) {
            Bitmap bm = BitmapFactory.decodeFile(currentElem.albumArtPath);
            coverArt.setImageBitmap(bm);
        } else {
            // this is usedd for unknown album art
            coverArt.setImageResource(currentElem.imageResourceId);
        }

        coverArt.setColorFilter(myContext.getResources().getColor(currentElem.filterColor));

        if (currentElem.numSongs() != 0) {
            Song s = currentElem.getNthSong(0);

            TextView songName = listItem.findViewById(R.id.gridList_SongName);
            songName.setText(s.songName);

            TextView artistName = listItem.findViewById(R.id.gridList_ArtistName);
            artistName.setText(s.artistName);
        } else {
            Log.e("ERROR", "SongListAdapter:getView empty song list for grid[" + position + "]");
        }

        return listItem;
    }
}
