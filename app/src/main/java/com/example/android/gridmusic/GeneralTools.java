package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

// implements methods that will be useful to many activities
public class GeneralTools {

    private Context myContext;
    private Handler handler = new Handler();

    public final static int touchVibDelay = 50;

    GeneralTools(Context context) {
        myContext = context;

    }

    // Got this vibration code from a stackoverflow explanation of using vibration
    // https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
    public static void vibrate(int time, Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null) {
            return;
        }

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

    // briefly highlight text in a TextView
    public void flashText(TextView v, int highlightColor, int origColor, int delay) {
        v.setTextColor(myContext.getResources().getColor(highlightColor));
        handler.postDelayed(turnOffHighlight(v, origColor), delay);
    }

    public int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    // turn off textView highlight
    private Runnable turnOffHighlight(final TextView v, final int c) {
        return new Runnable() {
            public void run() {
                v.setTextColor(myContext.getResources().getColor(c));

            }
        };
    }
}
