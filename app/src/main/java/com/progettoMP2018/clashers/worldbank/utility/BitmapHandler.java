package com.progettoMP2018.clashers.worldbank.utility;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

//classe che gestisce il salvataggio e il caricamento delle bitmap
public class BitmapHandler {

    public static String saveToInternalStorage(Context context, Bitmap bitmapImage, String country, String indicator) {
        ContextWrapper cw = new ContextWrapper(context);
        // path verso /data/data/"nomeapp"/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "chart_" + country + "_" + indicator + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            //Qui si usa il metodo di compressione sull'oggetto BitMap per scrivere l'immagine sull'OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public static ArrayList<String> loadImageFromStorage(String path) {
        ArrayList<String> absolutePathOfImageList = new ArrayList<>();
        File dir = new File(path); // path della directory /data/data/"nomeapp"/imageDir
        File file[] = dir.listFiles();
        if (file != null) {
            for (File f : file) {
                absolutePathOfImageList.add(f.getAbsolutePath());
            }
        }
        return absolutePathOfImageList;
    }
}
