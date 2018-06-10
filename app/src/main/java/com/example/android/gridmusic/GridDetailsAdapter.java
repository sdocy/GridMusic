package com.example.android.gridmusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

// view adapter for our grid view
public class GridDetailsAdapter extends ArrayAdapter<Song> {

    private Context myContext;
    private List<Song> songList;

    private boolean showDetails = false;

    // viewholder for listview item
    private static class ViewHolder {
        private TextView artistName;
        private TextView albumName;
        private TextView songName;
        private TextView seperator1;
        private TextView seperator2;
    }

    GridDetailsAdapter(Context context, List<Song> list) {
        super (context, 0, list);

        myContext = context;
        songList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_details_item, parent, false);

            holder = new ViewHolder();
            holder.artistName = listItem.findViewById(R.id.gridDetails_ArtistName);
            holder.albumName = listItem.findViewById(R.id.gridDetails_AlbumName);
            holder.songName = listItem.findViewById(R.id.gridDetails_SongName);
            holder.seperator1 = listItem.findViewById(R.id.gridDetails_Sep1);
            holder.seperator2 = listItem.findViewById(R.id.gridDetails_Sep2);

            listItem.setTag(holder);
        } else {
            // recycled view, get existing ViewHolder
            holder = (ViewHolder) listItem.getTag();
        }

        if (showDetails) {
            Song currentElem = songList.get(position);
            if (currentElem == null) {
                throw new AssertionError("SongListAdapter.getView() : null currentElem");
            }

            holder.artistName.setText(currentElem.artistName);
            holder.albumName.setText(currentElem.albumName);
            holder.songName.setText(currentElem.songName);
            holder.seperator1.setText(" | ");
            holder.seperator2.setText(" | ");
        } else {
            holder.artistName.setText("");
            holder.albumName.setText("");
            holder.songName.setText("");
            holder.seperator1.setText("");
            holder.seperator2.setText("");
        }

        return listItem;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    public void newDetailsList(List<Song> list) {
        songList = list;
        showDetails = true;
        notifyDataSetChanged();
    }

    public void turnOffDetails() {
        showDetails = false;
        notifyDataSetChanged();
    }
}
