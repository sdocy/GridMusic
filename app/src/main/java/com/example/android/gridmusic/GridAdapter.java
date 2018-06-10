package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import java.util.List;

// view adapter for our grid view
public class GridAdapter extends ArrayAdapter<GridElement> {

    private Context myContext;
    private List<GridElement> gridList;

    // viewholder for listview item
    private static class ViewHolder {
        private ImageView gridImage;
    }

    GridAdapter(Context context, List<GridElement> list) {
        super (context, 0, list);
        myContext = context;
        gridList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_element, parent, false);

            holder = new  ViewHolder();
            holder.gridImage = listItem.findViewById(R.id.gridElement_GridImage);

            listItem.setTag(holder);
        } else {
            // recycled view, get existing ViewHolder
            holder = (ViewHolder) listItem.getTag();
        }

        GridElement currentElem = getItem(position);
        if (currentElem == null) {
            throw new AssertionError("GridAdapter.getView() : null currentElem");
        }

        if (currentElem.imageResourceId == -1) {
            // blank
            holder.gridImage.setImageDrawable(null);
        } else if (currentElem.imageResourceId == -2) {
            // empty grid outline
            holder.gridImage.setImageResource(R.drawable.emptygrid);
        } else {
            // filled grid
            if (currentElem.albumArtPath != null) {
                // this is how we do it in CreateGrid, and eventually PlayGrid
                Bitmap bm = BitmapFactory.decodeFile(currentElem.albumArtPath);
                holder.gridImage.setImageBitmap(bm);
            } else {
                // this is how we currently do it in PlayGrid
                // we also use this for unknown album art, and will continue to do so
                holder.gridImage.setImageResource(currentElem.imageResourceId);
            }
            holder.gridImage.setColorFilter(myContext.getResources().getColor(currentElem.filterColor));
        }
        holder.gridImage.setBackgroundColor(myContext.getResources().getColor(currentElem.bgColor));

        return listItem;
    }
}
