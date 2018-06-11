package com.example.android.gridmusic;

// Interface to allow GridAdapter to call methods from both CreateGridActivity and PlayGridActivity
// when a grid item is clicked.
public interface TheGridClicks {
    // method for GridAdapter to call for onClick()
    void theGridOnClick(int position);

    // method for GridAdapter to call for onLongClick()
    void theGridOnLongClick(int position);
}
