package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;

// view adapter for our grid view
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private Context myContext;
    private List<GridElement> gridList;

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gridImage;

        ViewHolder(View listItem) {
            super(listItem);

            gridImage = listItem.findViewById(R.id.gridElement_GridImage);

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TheGridClicks) myContext).theGridOnClick(getAdapterPosition());
                }
            });

            listItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((TheGridClicks) myContext).theGridOnLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }

    GridAdapter(Context context, List<GridElement> list) {
        myContext = context;
        gridList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_element, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridElement currentElem = gridList.get(position);
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

    }

    @Override
    public int getItemCount() {
        return gridList.size();
    }
/*
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
    }*/
}
