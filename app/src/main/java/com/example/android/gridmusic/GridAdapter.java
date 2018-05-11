package com.example.android.gridmusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

// view adapter for our grid view
public class GridAdapter extends ArrayAdapter<GridElement> {

    private Context myContext;
    private List<GridElement> gridList;

    GridAdapter(Context context, ArrayList<GridElement> list) {
        super (context, 0, list);
        myContext = context;
        gridList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(myContext).inflate(R.layout.grid_element, parent, false);
        }

        GridElement currentElem = getItem(position);
        if (currentElem == null) {
            throw new AssertionError("GridAdapter.getView() : null currentElem");
        }

        ImageView gridImage = listItem.findViewById(R.id.gridImage);
        if (currentElem.imageResourceId != -1) {
            gridImage.setImageResource(currentElem.imageResourceId);
            gridImage.setColorFilter(myContext.getResources().getColor(currentElem.filterColor));
        } else {
            gridImage.setImageDrawable(null);
        }
        gridImage.setBackgroundColor(myContext.getResources().getColor(currentElem.bgColor));

        return listItem;
    }
}
