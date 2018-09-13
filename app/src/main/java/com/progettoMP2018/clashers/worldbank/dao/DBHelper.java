package com.progettoMP2018.clashers.worldbank.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper { //funzione che serve ad istanziare il database "worldbankDB.db"
    private static String NAME = "worldbankDB.db";
    private static int VERSION = 1; //indica la versione del db, in caso di futuri aggiornamenti dell'app
    private String[] columns = { //colonne riguardanti la tabella contenente l'url
            "url",
            "json"
    };
    private String[] columns1 = { //colonne riguardanti la tabella contenente i dati del json analizzato dall'url
            "json",
            "topic",
            "indicator",
            "country",
            "indicator_id",
            "country_iso2code"
    };
    private SQLiteDatabase db;
    private static final String TABLE_URL_NAME = "json_url";
    private static final String TABLE_REQUEST_NAME = "requests";

    public DBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { //qui creo le tabelle vere e proprie con queste due query
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_URL_NAME + " (\n" + //query riguardante la tabella contenente l'url
                "url TEXT NOT NULL PRIMARY KEY,\n" +
                "json TEXT\n" +
                ");";

        String query1 = "CREATE TABLE IF NOT EXISTS " + TABLE_REQUEST_NAME + " (\n" + //query riguardante la tabella contenente i dati del json
                "json TEXT,\n" +
                "topic TEXT,\n" +
                "indicator TEXT,\n" +
                "country TEXT,\n" +
                "indicator_id TEXT,\n" +
                "country_iso2code TEXT,\n" +
                "PRIMARY KEY (topic, indicator_id, country_iso2code)\n" +
                ");";
        //le funzioni sottostanti servono per eseguire le suddette query
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.execSQL(query1);
    }

    //funzione che serve per aggiornare il database in caso di update dell'app
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int version_from, int version_to) {
        //questo database è solo una cache per i dati scaricati da internet, quindi la sua policy di
        //aggiornamento consiste semplicemente nel cancellare i dati presenti al suo interno
        //e riscaricare tutto da capo
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_URL_NAME);
        onCreate(db);
    }

    //funzione che serve per tornare indietro a una vecchia versione del db
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void open() { //funzione che permette di aprire il database
        db = getWritableDatabase();
    }

    public void close() { //funzione per chiudere il database
        db.close();
    }

    public Cursor getURL(String url) { //cursore che permette di risalire all'url
        Cursor cur = db
                .query("json_url", columns,
                        "url = '" + url + "'", null,
                        null, null, null);
        return cur;
    }

    public void addURL(String url, String json) { //funzione per aggiungere url e json nella tabella correlata "TABLE_URL_NAME"
        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("json", json);
        db.insert(TABLE_URL_NAME, null,
                values);
    }

    //la funzione sottostante permette di salvare tutti i dati riguardanti il json analizzato nella tabella correlata "TABLE_REQUEST_NAME"
    public void saveRequestIntoDatabase(String json, String topic, String indicator, String country, String indicatorId, String country_iso2code) {
        ContentValues values = new ContentValues();
        values.put("json", json);
        values.put("topic", topic);
        values.put("indicator", indicator);
        values.put("country", country);
        values.put("indicator_id", indicatorId);
        values.put("country_iso2code", country_iso2code);
        db.insert(TABLE_REQUEST_NAME, null,
                values);
    }

    public Cursor getSavedRequests() { //funzione che permette di ottenere tramite cursore la tabella contenente i dati del json
        Cursor cur = db
                .query("requests", columns1,
                        null, null,
                        null, null, null);
        return cur;
    }

    public void deleteAllJson() { //funzione che serve a cancellare tutti i json dalla tabella contenente gli url "TABLE_URL_NAME"
        db.delete(TABLE_URL_NAME, null, null);
    }

    public void deleteSavedRequests() { //funzione che serve a cancellare tutti i dati riguardanti i json nella tabella "TABLE_REQUEST_NAME"
        db.delete(TABLE_REQUEST_NAME, null, null);
    }
}
