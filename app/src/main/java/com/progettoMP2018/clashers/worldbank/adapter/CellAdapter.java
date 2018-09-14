package com.progettoMP2018.clashers.worldbank.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.entity.Cell;
import java.util.ArrayList;

public class CellAdapter extends RecyclerView.Adapter<CellAdapter.ViewHolder> {
    private ArrayList<Cell> galleryList;
    private CellAdapterListener listener;
    private Context context;

    public CellAdapter(Context context, ArrayList<Cell> galleryList, CellAdapterListener listener) {
        this.galleryList = galleryList;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    //metodo chiamato alla creazione dell'adapter e usato per inizializzare il ViewHolder
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) { //viene creato un elemento della RecyclerView
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.saved_chart_cell, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    //metodo chiamato per collegare il ViewHolder all'adapter; è dove si passano i dati al ViewHolder
    public void onBindViewHolder(@NonNull CellAdapter.ViewHolder viewHolder, final int i) { //momento in cui vengono recuperati i riferimenti agli elementi interni della RecyclerView da popolare con i nuovi dati
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP); //setta il modo di visualizzazione dell'immagine
        viewHolder.img.setImageBitmap(BitmapFactory.decodeFile(galleryList.get(i).getPath())); //decodifica la bmp passata come parametro
        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCellSelected(galleryList.get(i)); //al click su un'immagine nella lista restituisce quella stessa immagine
            }
        });
    }

    @Override
    public int getItemCount() { //restituisce il numero di elementi nella gallerylist
        return galleryList.size();
    }

    //la sottostante classe viewholder viene usata per ridurre le invocazioni al metodo findviewbyid, si riciclano il
    //più possibile le view usate per visualizzare elementi, e il viewholder conserva i riferimenti
    //ai widget interni ad ogni elemento
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView img;
        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
        }
    }

    public interface CellAdapterListener {
        void onCellSelected(Cell cell);
    }
}
