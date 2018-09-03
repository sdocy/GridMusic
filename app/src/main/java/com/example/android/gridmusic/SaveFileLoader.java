package com.example.android.gridmusic;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SaveFileLoader {


    // list all save files
    public static String[] listSaveFiles(Context context) {
        String[] files = context.fileList();
        Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    // return String of data read from file
    public static String loadGrid(Context context, String filename) {
        if (filename == null) {
            Log.e("ERROR", "PlayGridActivity:loadGrid received null filename");
            return null;
        }

        return readSaveFile(context, filename);
    }

    // return String of data read from file
    private static String readSaveFile(Context context, String filename) {
        String inData = null;

        try {
            FileInputStream inputStream = context.openFileInput(filename);
            int size = inputStream.available();

            byte[] buffer = new byte[size];

            int sizeRead = inputStream.read(buffer);
            if (sizeRead != size) {
                Log.e("ERROOR", "SaveFileLoader:readSaveFile read " + sizeRead + " out of " + size + " bytes");
            }

            inputStream.close();

            inData = new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inData != null) {
            Log.e("INDATA", inData);
        }

        return inData;
    }
}
