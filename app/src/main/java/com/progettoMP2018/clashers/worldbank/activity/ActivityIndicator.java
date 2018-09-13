package com.progettoMP2018.clashers.worldbank.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.reflect.TypeToken;
import com.progettoMP2018.clashers.worldbank.entity.Topic;
import com.progettoMP2018.clashers.worldbank.utility.CheckConnection;
import com.progettoMP2018.clashers.worldbank.utility.MyJsonParser;
import com.progettoMP2018.clashers.worldbank.utility.VolleyHelper;
import com.progettoMP2018.clashers.worldbank.utility.MyDividerItemDecoration;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.adapter.IndicatorAdapter;
import com.progettoMP2018.clashers.worldbank.dao.DBHelper;
import com.progettoMP2018.clashers.worldbank.entity.FullQuery;
import com.progettoMP2018.clashers.worldbank.entity.Indicator;
import com.progettoMP2018.clashers.worldbank.utility.VolleyRequestListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityIndicator extends AppCompatActivity implements IndicatorAdapter.IndicatorAdapterListener {
    private List<Indicator> indicatorList;
    private IndicatorAdapter mAdapter;
    private SearchView searchView;
    public Dialog myDialog;
    public Button btnSelectIndicator;
    FullQuery fullQuery;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;
        super.onCreate(savedInstanceState);
        //Se si arriva da ActivityTopic prendiamo l'oggetto FullQuery con il topic già impostato in base alla scelta dell'utente
        fullQuery = (FullQuery) getIntent().getSerializableExtra("topic_selected");
        setContentView(R.layout.activity_indicator);
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //aggiunge "<" a sinistra della scritta nella toolbar
        getSupportActionBar().setTitle(R.string.toolbar_indicator_title); //setta il titolo della toolbar

        recyclerView = findViewById(R.id.recycler_view);
        indicatorList = new ArrayList<>();
        mAdapter = new IndicatorAdapter(this, indicatorList, this);

        // barra di notifica col background bianco
        whiteNotificationBar(recyclerView);
        //si crea il layout tramite recyclerview
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);

        //controllo della connessione internet; se è assente rimanda l'utente alla main activity
        if (!CheckConnection.haveNetworkConnection(this)) {
            Intent intent = new Intent(ActivityIndicator.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        if (progressBar.getVisibility()==View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        fetchIndicator(); //si chiama la funzione di fetch degli indicators da json
    }

    private void fetchIndicator() { //funzione che fetcha il json riguardante gli indicators
        final String URL = "http://api.worldbank.org/v2/topics/" + fullQuery.getTopic().getId() + "/indicators/?per_page=3500&format=json";
        final DBHelper helper = new DBHelper(this);
        helper.open(); //si apre il db
        Cursor c = helper.getURL(URL);
        if (c.getCount() == 0) { //allora l'URL non è già presente all'interno del database e dobbiamo effettuare la richiesta tramite volley
            VolleyHelper.getInstance().getDataVolley(URL, new VolleyRequestListener() {
                @Override
                public void getResult(String result) {
                    if (!result.isEmpty() && !result.equals("error")) {
                        helper.addURL(URL, result);
                        updateIndicatorList(result); //aggiorniamo la lista degli indicator in base al file json che ci viene restituito
                    } else if (result.equals("error")) {
                        Toast.makeText(getApplicationContext(), R.string.indicator_error_fetch, Toast.LENGTH_LONG).show();
                    }
                    helper.close();
                }
            });
        } else { //altrimenti si prende il file json corrispondente all'url dal database
                 //e si aggiorna la lista degli indicator di conseguenza
            c.moveToFirst();
            updateIndicatorList(c.getString(c.getColumnIndex("json")));
            helper.close();
        }
    }

    //Metodo che aggiorna la lista degli Indicator in base al contenuto del file json corrispondente
    public void updateIndicatorList(String json){
        List<Indicator> items = MyJsonParser.parseIndicators(json);
        indicatorList.clear();
        indicatorList.addAll(items);
        progressBar.setVisibility(View.GONE);
        //si fa il refresh della recycler view
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //configurazione per ricercare nella activity tramite SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        //in ascolto per cercare nell'activity in base a modifiche sul testo della query del campo di ricerca
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filtra il contenuto dell'activity quando una query nel campo di ricerca viene mandata in ricerca
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //filtra il contenuto dell'activity quando il testo della query cambia
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //qui si gestiscono i click sull'action bar
        int id = item.getItemId();

        switch (id) {
            // qui si reagisce al bottone sull'action bar
            case R.id.action_search:
                return true;

            case android.R.id.home:
                onBackPressed(); //torna all'activity precedente
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //si chiude la search view quando si clicca il pulsante di back
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void whiteNotificationBar(View view) { //setta la notificationbar di colore bianco
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onIndicatorSelected(Indicator indicator) {
        showDialog(indicator); //se viene selezionato un indicator si mostra il suo contenuto su un nuovo dialog tramite la funzione showDialog
    }


    public void showDialog(final Indicator indicator) { //mostra un nuovo dialog contenente informazioni riguardo l'indicator in questione

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.indicator_details, null); //todo
        myDialog = new Dialog(this);
        myDialog.setContentView(dialogLayout);
        TextView dialog_name = (TextView) myDialog.findViewById(R.id.details_name);
        TextView dialog_sourceNote = (TextView) myDialog.findViewById(R.id.details_sourcenote);
        dialog_sourceNote.setMovementMethod(new ScrollingMovementMethod());
        btnSelectIndicator = dialogLayout.findViewById(R.id.btn_select_indicator);
        dialog_name.setText(indicator.getName());
        dialog_sourceNote.setText(indicator.getSourceNote());
        myDialog.show();
        btnSelectIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //bottone per selezionare l'indicator scelto e andare avanti alla nuova activity
                fullQuery.setIndicator(indicator);
                if (fullQuery.getCountry().getIso2Code() == null) { //se questo dato è null significa che non
                                                                    //ho selezionato nessuna country e che provengo dall'"ActivityTopic"
                                                                    //come prima activity e quindi apro l'activity successiva ovvero "ActivityCountry"
                    Intent i = new Intent(ActivityIndicator.this, ActivityCountry.class);
                    i.putExtra("indicator_selected", fullQuery);
                    myDialog.dismiss();
                    startActivity(i);
                } else { //altrimenti se non è null significa che ho già selezionato country e quindi vado alla successiva activity ovvero "ActivityChart"

                    Intent i = new Intent(ActivityIndicator.this, ActivityChart.class);
                    i.putExtra("item_selected", fullQuery);
                    myDialog.dismiss();
                    startActivity(i);
                }
            }
        });
    }


}
