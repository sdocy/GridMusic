package com.example.android.gridmusic;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

// implements methods that will be useful to many activities
public class GeneralTools {

    public final static int touchVibDelay = 50;

    GeneralTools() {
    }

    // Got this vibration code from a stackoverflow explanation of using vibration
    // https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
    public static void vibrate(Context context, int time) {
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
    public static void showToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        TextView toastMessage = toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.CYAN);
        toast.show();
    }

    // common message to show for non-supported functions
    public static void notSupported(Context context) {
        showToast(context, context.getString(R.string.notSupported));
    }

    // list all save files
    public static String[] listSaveFiles(Context context) {
        String[] files = context.fileList();
        Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    // briefly highlight text in a TextView
    public static void flashText(Context context, TextView v, int highlightColor, int origColor, int delay) {
        Handler handler = new Handler();

        v.setTextColor(context.getResources().getColor(highlightColor));
        handler.postDelayed(turnOffHighlight(context, v, origColor), delay);
    }

    // turn off textView highlight
    private static Runnable turnOffHighlight(final Context context, final TextView v, final int c) {
        return new Runnable() {
            public void run() {
                v.setTextColor(context.getResources().getColor(c));

            }
        };
    }
}
