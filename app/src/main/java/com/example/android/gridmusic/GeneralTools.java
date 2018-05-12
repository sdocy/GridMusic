package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

// implements methods that will be useful to many activities
public class GeneralTools {

    private Context myContext;
    private Vibrator vibrator;

    GeneralTools(Context context) {
        myContext = context;
        vibrator = (Vibrator) myContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    // Got this vibration code from a stackoverflow explanation of using vibration
    // https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
    public void vibrate(int time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(time);
        }
    }

    // show a toast popup message
    public void showToast(String msg) {
        Toast toast = Toast.makeText(myContext, msg, Toast.LENGTH_LONG);
        TextView toastMessage = toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.CYAN);
        toast.show();
    }

    // common message to show for non-supported functions
    public void notSupported() {
        showToast(myContext.getString(R.string.notSupported));
    }
}