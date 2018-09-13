package com.progettoMP2018.clashers.worldbank.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.dao.DBHelper;
import com.progettoMP2018.clashers.worldbank.entity.FullQuery;
import com.progettoMP2018.clashers.worldbank.entity.GraphData;
import com.progettoMP2018.clashers.worldbank.entity.SavedRequest;
import com.progettoMP2018.clashers.worldbank.utility.BitmapHandler;
import com.progettoMP2018.clashers.worldbank.utility.CheckConnection;
import com.progettoMP2018.clashers.worldbank.utility.VolleyHelper;
import org.json.JSONArray;
import org.json.JSONException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityChart extends AppCompatActivity {
    private static final String TAG = ActivityChart.class.getSimpleName();
    public static final int PERMISSIONS_MULTIPLE_REQUEST = 101;
    private LineChart mChart;
    private String URL;
    FullQuery fullQuery;
    SavedRequest request;
    List<GraphData> dataChartList;
    Button btnSavePng;
    Context context = this;
    String countryName;
    String indicatorName;
    String countryiso2code;
    String indicatorId;
    JSONArray chartDataArray;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        dataChartList = new ArrayList<>();
        //request serve per l'accesso ai dati offline; sarà null se l'activity precedente non è "ActivityOfflineData"
        request = (SavedRequest) getIntent().getSerializableExtra("saved_request");
        if (request != null) {
            countryName = request.getCountryName();
            indicatorName = request.getIndicatorName();
            countryiso2code = request.getCountryIso2Code();
            indicatorId = request.getIndicatorId();
            parseJsonData(request.getJson());
        }

        //fullquery serve per usare i dati passati dall'activity precedente quando l'utente fa la scelta (topic/indicator/country)
        fullQuery = (FullQuery) getIntent().getSerializableExtra("item_selected");
        progressBar = findViewById(R.id.progressBar4);
        if (fullQuery != null) { //se la fullquery non è nulla, si prendono i dati dalle activity precedenti e si usano per fare il grafico
            countryName = fullQuery.getCountry().getName();
            indicatorName = fullQuery.getIndicator().getName();
            countryiso2code = fullQuery.getCountry().getIso2Code();
            indicatorId = fullQuery.getIndicator().getId();
            //controllo della connessione internet; se è assente rimanda l'utente alla main activity
            if (!CheckConnection.haveNetworkConnection(this)) {
                Intent intent = new Intent(ActivityChart.this, MainActivity.class);
                startActivity(intent);
                return;
            }
            if (progressBar.getVisibility()==View.GONE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            fetchChart(); // si compone il grafico con i suddetti dati
        }


        btnSavePng = (Button) findViewById(R.id.btnGraph);
        btnSavePng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) { //se si hanno i permessi per salvare in memoria esterna, allora si salva il grafico in galleria
                    saveChartImage();
                }
            }

        });
    }

    public void saveChartImage(){
        Bitmap bmp = mChart.getChartBitmap();
        BitmapHandler.saveToInternalStorage(ActivityChart.this, bmp, countryiso2code, indicatorId); //salva l'immagine del grafico nella cache
        MediaStore.Images.Media.insertImage(getContentResolver(), bmp, "", ""); //salva la bitmap in galleria
        Toast.makeText(getApplicationContext(), R.string.chart_saved_successfully, Toast.LENGTH_LONG).show(); //messaggio di avvenuto salvataggio del grafico
    }


    private boolean checkPermission() { //meccanismo di controllo dei permessi per la scrittura su memoria esterna
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (ActivityChart.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (ActivityChart.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(ActivityChart.this.findViewById(android.R.id.content),
                        R.string.grant_permission_external_storage,
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{Manifest.permission
                                                .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission
                                .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) { //meccanismo per controllare il risultato della richiesta di permessi

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean writeExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!writeExternalStorage && !readExternalStorage) {
                        Snackbar.make(ActivityChart.this.findViewById(android.R.id.content),
                                R.string.grant_perm_ext,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }


    public void showDialog() { //funzione che crea l'alert riguardante la mancanza di dati su grafico per talune countries
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.datamiss);
        alertBuilder.setMessage(R.string.no_chart_available);
        alertBuilder.setPositiveButton(R.string.back,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    private void fetchChart() { //funzione che crea il grafico riguardante una determinata country basandosi su un determinato indicator

        URL = "http://api.worldbank.org/v2/countries/" + fullQuery.getCountry().getIso2Code() + "/indicators/" + fullQuery.getIndicator().getId() + "/?per_page=3500&format=json";

        final DBHelper helper = new DBHelper(this);
        helper.open(); //si apre il db

        Cursor c = helper.getURL(URL); //tramite il cursore si controlla se nel db è presente questo
                                       //url in modo da non rifare tutto il procedimento di caricamento e scaricamento
        if (c.getCount() == 0) { //se il getCount è == 0 significa che non c'è l'url, quindi va fattp il caricamento tramite il server online
            //qui si fa la richiesta dell'url e si attende la risposta
            JsonArrayRequest request = new JsonArrayRequest(URL,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray
                                                       response) {
                            if (response == null) {
                                Toast.makeText(getApplicationContext(), R.string.chart_init_error, Toast.LENGTH_LONG).show();
                                return;
                            }
                            parseJsonData(response.toString());
                            helper.addURL(URL, response.toString());
                            helper.saveRequestIntoDatabase(response.toString(),
                                    fullQuery.getTopic().getValue(),
                                    fullQuery.getIndicator().getName(),
                                    fullQuery.getCountry().getName(),
                                    fullQuery.getIndicator().getId(),
                                    fullQuery.getCountry().getIso2Code());
                            helper.close(); //si chiude il db
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // errore nell'uso del file json
                    Log.e(TAG, getString(R.string.error) + error.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.noconnection, Toast.LENGTH_SHORT).show();
                    helper.close(); //si chiude il db
                }
            });
            VolleyHelper.getInstance().addToRequestQueue(request); //aggiunge la richiesta alla coda delle richieste
        } else { //altrimenti si prende il file json corrispondente all'url dal database
            c.moveToFirst();
            String json = c.getString(c.getColumnIndex("json"));
            parseJsonData(json);
            helper.close(); //si chiude il db
        }

    }


    void parseJsonData(String jsonString) { //funzione che analizza il contenuto del file json
        try {
            chartDataArray = (new JSONArray(jsonString)).getJSONArray(1); //si prende il primo array nel file json visto che è un array di array e il primo array non serve a nulla
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GraphData>>() {
            }.getType();
            //tramite il tool gson si trasformano i contenuti del json in oggetti riutilizzabili
            List<GraphData> items = gson.fromJson(String.valueOf(chartDataArray), listType); //si crea una lista di "GraphData" tramite gson
                                                                                             //prendendo il valore degli elementi in chartDataArray
            System.out.println(getString(R.string.item_size) + items.size());


            dataChartList.clear();
            dataChartList.addAll(items);

            if (dataChartList.isEmpty()) {  //se la lista di oggetti "GraphData" è vuota si mostra il dialog di alert
                showDialog();
                return;
            } else { //altrimenti si setta il grafico basandosi sulla suddetta lista
                progressBar.setVisibility(View.GONE);
                setGraph();
            }

        } catch (JSONException e) {
            showDialog();
        }


    }

    public void setGraph() { //funzione per settare il grafico con i valori presi dal json; si usa la libreria mpandroidchart

        ArrayList<Entry> yValues = new ArrayList<>(); //valori
        mChart = (LineChart) findViewById(R.id.linechart);

        mChart.setDragEnabled(true); //funzionalità di dragging (o anche detto panning) per il grafico attivata
        mChart.setScaleEnabled(true); //funzionalità di "scaling" del grafico attivata su entrambi gli assi
        mChart.setTouchEnabled(true); //funzionalità touchscreen attivata
        mChart.setPinchZoom(true); //funzione di pinch to zoom attivata
        mChart.setDoubleTapToZoomEnabled(true); //funzione di doppio tap per zoomare attivata

        for (int i = 0; i < dataChartList.size(); i++) {
            if (dataChartList.get(i).getValue() != 0) {  //non prende valori nulli
                yValues.add(new Entry(Float.parseFloat(dataChartList.get(i).getDate()), dataChartList.get(i).getValue()));
            }
        }
        Collections.sort(yValues, new EntryXComparator()); //Lista di oggetti di tipo Entry ordinata tramite EntryXComparator
        LineDataSet set1;
        set1 = new LineDataSet(yValues, countryName + "/" + indicatorName);
        set1.setFillAlpha(110); //si setta il valore "alpha" (trasparenza) che è usato per riempire la superficie della linea
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        if (!yValues.isEmpty()) {
            dataSets.add(set1);
        }

        LineData data = new LineData(dataSets);
        set1.setLineWidth(4f); //si imposta la larghezza della linea usata per tracciare il grafico
        set1.setCircleSize(3f); //si imposta lo spessore della circonferenza
        set1.setCircleRadius(6f); //si imposta la grandezza del raggio del pallino che corrisponde ai vari elementi nel grafico


        mChart.setData(data); //si impostano tutti i dati da inserire nel grafico

        mChart.notifyDataSetChanged(); //serve per notificare il cambiamento dei dati all'interno del grafico

        mChart.invalidate(); //si refresha il grafico
    }
}
