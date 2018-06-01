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

    GridDetailsAdapter(Context context, List<Song> list) {
        super (context, 0, list);

        myContext = context;
        songList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_details_item, parent, false);
        }

        TextView artistName = listItem.findViewById(R.id.gridDetails_ArtistName);
        TextView albumName = listItem.findViewById(R.id.gridDetails_AlbumName);
        TextView songName = listItem.findViewById(R.id.gridDetails_SongName);
        TextView seperator1 = listItem.findViewById(R.id.gridDetails_Sep1);
        TextView seperator2 = listItem.findViewById(R.id.gridDetails_Sep2);

        if (showDetails) {
            Song currentElem = songList.get(position);
            if (currentElem == null) {
                throw new AssertionError("SongListAdapter.getView() : null currentElem");
            }

            artistName.setText(currentElem.artistName);
            albumName.setText(currentElem.albumName);
            songName.setText(currentElem.songName);
            seperator1.setText(" | ");
            seperator2.setText(" | ");
        } else {
            artistName.setText("");
            albumName.setText("");
            songName.setText("");
            seperator1.setText("");
            seperator2.setText("");
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
