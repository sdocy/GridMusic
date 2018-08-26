package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

// view adapter for our grid view
public class SaveListAdapter extends RecyclerView.Adapter<SaveListAdapter.ViewHolder> {

    private Context myContext;
    private List<String> saveList;
    private TextView highlightedSaveFileText = null;        // used to change highlight when a diff save file is pressed

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView saveName;

        ViewHolder(View listItem) {
            super(listItem);

            saveName = listItem.findViewById(R.id.saveList_SaveName);

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CreateGridActivity) myContext).setSaveEditText(saveList.get(getAdapterPosition()));

                    if (highlightedSaveFileText != null) {
                        highlightedSaveFileText.setTextColor(myContext.getResources().getColor(R.color.highlightBlue));
                    }
                    highlightedSaveFileText = saveName;
                    highlightedSaveFileText.setTextColor(Color.BLUE);

                    saveName.setTextColor(Color.BLUE);
                }
            });
        }
    }

    SaveListAdapter(Context context, List<String> list) {
        myContext = context;
        saveList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.save_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentElem = saveList.get(position);

        holder.saveName.setText(currentElem);
    }

    @Override
    public int getItemCount() {
        return saveList.size();
    }
}
