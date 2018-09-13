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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.progettoMP2018.clashers.worldbank.utility.CheckConnection;
import com.progettoMP2018.clashers.worldbank.utility.VolleyHelper;
import com.progettoMP2018.clashers.worldbank.utility.MyDividerItemDecoration;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.adapter.TopicsAdapter;
import com.progettoMP2018.clashers.worldbank.dao.DBHelper;
import com.progettoMP2018.clashers.worldbank.entity.FullQuery;
import com.progettoMP2018.clashers.worldbank.entity.Topic;
import com.progettoMP2018.clashers.worldbank.utility.MyJsonParser;
import com.progettoMP2018.clashers.worldbank.utility.VolleyRequestListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityTopic extends AppCompatActivity implements TopicsAdapter.TopicsAdapterListener {
    private static List<Topic> topicList;
    private static TopicsAdapter mAdapter;
    private SearchView searchView;
    private Dialog myDialog;
    //url del json da "fetchare"
    private static final String URL = "http://api.worldbank.org/v2/topics/?format=json";
    FullQuery fullQuery;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        //Se la selezione parte da Country, si estrapola l'oggetto FullQuery precedentemente istanziato
        //nella CountryActivity che avrà già il valore di country impostato, altrimenti fullQuery viene impostato a null
        fullQuery = (FullQuery) getIntent().getSerializableExtra("country_selected");
        progressBar = findViewById(R.id.progressBar2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //aggiunge "<" a sinistra della scritta nella toolbar
        getSupportActionBar().setTitle(R.string.toolbar_topics_title);//setta il titolo della toolbar
        recyclerView = findViewById(R.id.recycler_view);
        topicList = new ArrayList<>();
        mAdapter = new TopicsAdapter(this, topicList, this);

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
            Intent intent = new Intent(ActivityTopic.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        if (progressBar.getVisibility()==View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        fetchTopics(); //si chiama la funzione di fetch dei topics da json
    }


    private void fetchTopics() { //funzione che fetcha il json riguardante i topics
        final DBHelper helper = new DBHelper(this);
        helper.open(); //si apre il db
        Cursor c = helper.getURL(URL);
        if (c.getCount() == 0) { //allora l'URL non è già presente all'interno del database e dobbiamo effettuare la richiesta
            VolleyHelper.getInstance().getDataVolley(URL, new VolleyRequestListener() {
                @Override
                public void getResult(String result) {
                    if (!result.isEmpty() && !result.equals("error")) {
                        helper.addURL(URL, result);
                        updateTopicList(result); //aggiorniamo la lista dei topic in base al file json che ci viene restituito
                    } else if (result.equals("error")) {
                        Toast.makeText(getApplicationContext(), R.string.fetch_topics, Toast.LENGTH_LONG).show();
                    }
                    helper.close();
                }
            });
        } else { //altrimenti si prende il file json corrispondente all'url dal database
                 //e si aggiorna la lista dei topic di conseguenza
            c.moveToFirst();
            updateTopicList(c.getString(c.getColumnIndex("json")));
            helper.close();
        }

    }

    //Metodo che aggiorna la lista dei Topic in base ai topic nel file json
    public void updateTopicList(String json){
        List<Topic> items = MyJsonParser.parseTopics(json);
        topicList.clear();
        topicList.addAll(items);
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
        //in ascolto per cercare nell'activity in base a modifiche sul testo nel campo di ricerca
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
                onBackPressed();
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
        setResult(RESULT_OK); //da ActivityTopic torno indietro ad ActivityCountry e si deve reimpostare fullQuery a null
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
    public void onTopicSelected(Topic topic) {
        showDialog(topic); //se viene selezionato un topic si mostra il suo contenuto su un nuovo dialog tramite la funzione showDialog
    }

    public void showDialog(final Topic topic) { //mostra un nuovo dialog contenente informazioni riguardo il topic in questione
        Button btnSelectTopic;
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.topic_details, null);
        myDialog = new Dialog(this);
        myDialog.setContentView(dialogLayout);
        TextView dialog_name = (TextView) myDialog.findViewById(R.id.details_name);
        TextView dialog_sourceNote = (TextView) myDialog.findViewById(R.id.details_sourcenote);
        dialog_sourceNote.setMovementMethod(new ScrollingMovementMethod());
        btnSelectTopic = (Button) dialogLayout.findViewById(R.id.btn_select_topic);
        dialog_name.setText(topic.getValue());
        dialog_sourceNote.setText(topic.getSourceNote());
        myDialog.show();
        btnSelectTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //bottone per selezionare il topic scelto e andare avanti alla nuova activity
                if (fullQuery == null) { //se fullquery è null significa che sto nell'activity topic come prima activity
                    fullQuery = new FullQuery();
                }
                fullQuery.setTopic(topic);  //altrimenti se non è null significa che ho già selezionato un country e quindi vado alla successiva activity ovvero indicator
                Intent i = new Intent(ActivityTopic.this, ActivityIndicator.class);
                i.putExtra("topic_selected", fullQuery);
                myDialog.dismiss();
                startActivity(i);
            }
        });
    }


}