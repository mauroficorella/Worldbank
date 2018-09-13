package com.progettoMP2018.clashers.worldbank.activity;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.progettoMP2018.clashers.worldbank.utility.MyJsonParser;
import com.progettoMP2018.clashers.worldbank.utility.VolleyHelper;
import com.progettoMP2018.clashers.worldbank.utility.CheckConnection;
import com.progettoMP2018.clashers.worldbank.utility.MyDividerItemDecoration;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.adapter.CountryAdapter;
import com.progettoMP2018.clashers.worldbank.dao.DBHelper;
import com.progettoMP2018.clashers.worldbank.entity.Country;
import com.progettoMP2018.clashers.worldbank.entity.FullQuery;
import com.progettoMP2018.clashers.worldbank.utility.VolleyRequestListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityCountry extends AppCompatActivity implements CountryAdapter.CountryAdapterListener {
    private List<Country> countryList;
    private CountryAdapter mAdapter;
    private SearchView searchView;
    private Dialog myDialog;
    ProgressBar progressBar;

    //url del json da "fetchare"
    private static final String URL = "http://api.worldbank.org/v2/countries/?per_page=304&format=json";
    FullQuery fullQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);
        //Se arriviamo da ActivityIndicator prendiamo l'oggetto FullQuery con l'indicator già impostato in base alla scelta dell'utente
        fullQuery = (FullQuery) getIntent().getSerializableExtra("indicator_selected");
        progressBar = findViewById(R.id.progressBar3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //aggiunge "<" a sinistra della scritta nella toolbar
        getSupportActionBar().setTitle(R.string.toolbar_countries_title); //setta il titolo della toolbar
        recyclerView = findViewById(R.id.recycler_view);
        countryList = new ArrayList<>();
        mAdapter = new CountryAdapter(this, countryList, this);

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
            Intent intent = new Intent(ActivityCountry.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        if (progressBar.getVisibility()==View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        fetchCountry(); //si chiama la funzione di fetch delle country da json
    }

    private void fetchCountry() { //funzione che fetcha il json riguardante i countries
        final DBHelper helper = new DBHelper(this);
        helper.open(); //si apre il db
        Cursor c = helper.getURL(URL);
        if (c.getCount() == 0) { //allora l'URL non è già presente all'interno del database e dobbiamo effettuare la richiesta
            VolleyHelper.getInstance().getDataVolley(URL, new VolleyRequestListener() {
                @Override
                public void getResult(String result) {
                    if (!result.isEmpty() && !result.equals("error")) {
                        helper.addURL(URL, result);
                        updateCountryList(result); //aggiorniamo la lista dei countries in base al file json che ci viene restituito
                        helper.close();
                    } else if (result.equals("error")) {
                        Toast.makeText(getApplicationContext(), R.string.countries_error_fetch, Toast.LENGTH_LONG).show();
                        helper.close();
                    }
                }
            });
        } else { // altrimenti si prende il file json corrispondente all'url dal database
            // e si aggiorna la lista dei countries di conseguenza
            c.moveToFirst();
            updateCountryList(c.getString(c.getColumnIndex("json")));
            helper.close(); //si chiude il db
        }

    }

    //Metodo che aggiorna la lista dei Country in base al contenuto del file json
    public void updateCountryList(String json){
        List<Country> items = MyJsonParser.parseCountries(json);
        countryList.clear();
        countryList.addAll(items);
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
    public void onCountrySelected(Country country) {
        showDialog(country); //se viene selezionata una country mostro il suo contenuto su un nuovo dialog tramite la funzione showDialog
    }

    public void showDialog(final Country country) { //mostra un nuovo dialog contenente informazioni riguardo la country in questione
        Button btnSelectCountry;
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.country_details, null);
        myDialog = new Dialog(this);
        myDialog.setContentView(dialogLayout);
        TextView dialog_name = (TextView) myDialog.findViewById(R.id.details_name);
        TextView dialog_capital = (TextView) myDialog.findViewById(R.id.details_capital);
        TextView dialog_other_details = (TextView) myDialog.findViewById(R.id.other_details);
        btnSelectCountry = (Button) dialogLayout.findViewById(R.id.btn_select_country);
        dialog_name.setText(country.getName());
        if (!country.getCapitalCity().equals("")) { //se esiste la capitale della country selezionata allora la mostro, altrimenti no
            dialog_capital.setText(getString(R.string.capital_name, country.getCapitalCity()));
            dialog_other_details.setText(getString(R.string.country_other_details, country.getLongitude(), country.getLatitude()));
        }
        myDialog.show();
        btnSelectCountry.setOnClickListener(new View.OnClickListener() { //bottone per selezionare la country scelta e andare avanti alla nuova activity
            @Override
            public void onClick(View view) {
                if (fullQuery == null) { //se fullquery è null significa che siamo sulla prima activity della fase di scelta
                                         //e quindi apro l'activity successiva ovvero "ActivityTopic"
                    fullQuery = new FullQuery();
                    fullQuery.setCountry(country);
                    Intent i = new Intent(ActivityCountry.this, ActivityTopic.class);
                    i.putExtra("country_selected", fullQuery);
                    myDialog.dismiss();
                    startActivityForResult(i, 1);
                } else { //altrimenti se non è null va alla successiva activity ovvero "ActivityChart"
                    fullQuery.setCountry(country);
                    Intent i = new Intent(ActivityCountry.this, ActivityChart.class);
                    i.putExtra("item_selected", fullQuery);
                    myDialog.dismiss();
                    startActivity(i);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                fullQuery = null;
            }
        }
    }
}
