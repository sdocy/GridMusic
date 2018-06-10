package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

// view adapter for our grid view
public class GridListAdapter extends RecyclerView.Adapter<GridListAdapter.ViewHolder> {

    private Context myContext;
    private List<GridElement> albumList;
    CreateGridActivity parent;

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        TextView songName;
        ImageView coverArt;

        ViewHolder(View listItem) {
            super(listItem);

            coverArt = listItem.findViewById(R.id.gridlist_CoverArt);
            songName = listItem.findViewById(R.id.gridList_SongName);
            artistName = listItem.findViewById(R.id.gridList_ArtistName);

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.chooseListGrid(getAdapterPosition());
                }
            });

            listItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    parent.userDeletesGridListItem(getAdapterPosition());
                    return true;
                }
            });
        }
    }

    GridListAdapter(Context context, List<GridElement> list, CreateGridActivity p) {
        myContext = context;
        albumList = list;
        parent = p;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridElement currentElem = albumList.get(position);

        if (currentElem.albumArtPath != null) {
            Bitmap bm = BitmapFactory.decodeFile(currentElem.albumArtPath);
            holder.coverArt.setImageBitmap(bm);
        } else {
            // this is used for unknown album art
            holder.coverArt.setImageResource(currentElem.imageResourceId);
        }

        holder.coverArt.setColorFilter(myContext.getResources().getColor(currentElem.filterColor));

        if (currentElem.numSongs() != 0) {
            Song s = currentElem.getNthSong(0);

            holder.songName.setText(s.songName);
            holder.artistName.setText(s.artistName);
        } else {
            Log.e("ERROR", "SongListAdapter:getView empty song list for grid[" + position + "]");
        }
/*
        holder.coverArt.setTag(position);
        holder.coverArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int pos = (Integer) arg0.getTag();

                Log.e("ERROR", "CLICK ON POS " + pos);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    /*
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_list_item, parent, false);

            holder = new ViewHolder();
            holder.coverArt = listItem.findViewById(R.id.gridlist_CoverArt);
            holder.songName = listItem.findViewById(R.id.gridList_SongName);
            holder.artistName = listItem.findViewById(R.id.gridList_ArtistName);

            listItem.setTag(holder);
        } else {
            // recycled view, get existing ViewHolder
            holder = (ViewHolder) listItem.getTag();
        }

        GridElement currentElem = getItem(position);
        if (currentElem == null) {
            throw new AssertionError("SongListAdapter.getView() : null currentElem");
        }

        if (currentElem.albumArtPath != null) {
            Bitmap bm = BitmapFactory.decodeFile(currentElem.albumArtPath);
            holder.coverArt.setImageBitmap(bm);
        } else {
            // this is usedd for unknown album art
            holder.coverArt.setImageResource(currentElem.imageResourceId);
        }

        holder.coverArt.setColorFilter(myContext.getResources().getColor(currentElem.filterColor));

        if (currentElem.numSongs() != 0) {
            Song s = currentElem.getNthSong(0);

            holder.songName.setText(s.songName);
            holder.artistName.setText(s.artistName);
        } else {
            Log.e("ERROR", "SongListAdapter:getView empty song list for grid[" + position + "]");
        }

        return listItem;
    }
    */
}
