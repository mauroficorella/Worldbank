package com.progettoMP2018.clashers.worldbank.utility;

import android.content.Context;
import android.widget.Toast;

import com.progettoMP2018.clashers.worldbank.R;

import java.io.File;

//classe per gestire il contenuto della cache
public class CacheHandler {
    Context context;

    public CacheHandler(Context context) {
        this.context = context;
    }

    //metodo che cancella la cache
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (deleteDir(dir)) {
                Toast.makeText(context, R.string.cache_deleted, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
        }
    }
    //Metodo che cancella una cartella. Verrà invocato per eliminare la
    //cartella contente tutte le immagini dei grafici salvate
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
