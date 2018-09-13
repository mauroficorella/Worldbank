package com.progettoMP2018.clashers.worldbank.activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.adapter.CellAdapter;
import com.progettoMP2018.clashers.worldbank.entity.Cell;
import com.progettoMP2018.clashers.worldbank.utility.BitmapHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SavedChartsActivity extends AppCompatActivity implements CellAdapter.CellAdapterListener {

    ArrayList<Cell> cells;
    CellAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_charts);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //aggiunge "<" a sinistra della scritta nella toolbar
        getSupportActionBar().setTitle(R.string.saved_charts_toolbar); //setta il titolo della toolbar
        //si usa la recyclerview per impostare il layout
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true); //tramite questa funzione la grandezza della recycler view non cambierà a seconda del contenuto che avrà al suo interno

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        //si carica l'immagine dallo storage interno dove viene salvata dall'activity chart
        ArrayList<String> images = BitmapHandler.loadImageFromStorage("/data/data/com.progettoMP2018.clashers.worldbank/app_imageDir");
        if (images.size() == 0) { //se l'array ha grandezza zero significa che non c'è nulla dentro e quindi l'immagine non esiste; restituisce un alert
            showDialog();
        }
        cells = prepareData(images);
        adapter = new CellAdapter(getApplicationContext(), cells, this);
        recyclerView.setAdapter(adapter); //qui si sistemano le celle ottenute nella prepareData nella recyclerview
    }



    private ArrayList<Cell> prepareData(ArrayList<String> images) { //metodo che sistema un'immagine ottenuta in celle e restituisce un array di celle
        ArrayList<Cell> cellsArray = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            Cell cell = new Cell();
            cell.setPath(images.get(i));
            cellsArray.add(cell);
        }
        return cellsArray;
    }

    public void showDialog() { //funzione che restituisce l'alert collegato al fatto che l'immagine non viene trovata
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SavedChartsActivity.this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.missing_img);
        alertBuilder.setMessage(R.string.missing_img_description);
        alertBuilder.setPositiveButton(R.string.back,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //qui si gestiscono i click sull'action bar
        int id = item.getItemId();

        switch (id) {
            // qui si reagisce al bottone sull'action bar
            case R.id.item_delete:
                showDialogAndDelete();
                return true;
            case android.R.id.home:
                onBackPressed(); //torna indietro all'"ActivityMain"
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCellSelected(Cell cell) { //funzione che, una volta selezionata un'immagine, rimanda all'activity
                                            //"FullImageActivity" portandosi dietro l'immagine selezionata
        Intent i = new Intent(SavedChartsActivity.this, FullImageActivity.class);
        i.putExtra("image_selected", cell.getPath());
        startActivity(i);
    }

    //funzione che al click su "delete" elimina l'intero contenuto della cartella contenente i grafici salvati
    public void deleteAllImages()   {
        String path = "/data/data/com.progettoMP2018.clashers.worldbank/app_imageDir";
        File dir = new File(path);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            cells.clear();
            adapter.notifyDataSetChanged();
        }
    }

    public void showDialogAndDelete() { //funzione che restituisce l'alert per confermare l'eliminazione delle immagini
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SavedChartsActivity.this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.delete_imgs);
        alertBuilder.setMessage(R.string.deletemenu_description);
        alertBuilder.setPositiveButton(R.string.positive_deletemenu, //bottone per confermare la cancellazione
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllImages();
                    }
                });
        alertBuilder.setNegativeButton(R.string.negative_deletemenu, //bottone per annullare l'operazione e lasciare tutto com'è
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}


