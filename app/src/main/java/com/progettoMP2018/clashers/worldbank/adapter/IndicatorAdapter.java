package com.progettoMP2018.clashers.worldbank.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.Filterable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.progettoMP2018.clashers.worldbank.R;
import com.progettoMP2018.clashers.worldbank.entity.Indicator;


import java.util.ArrayList;
import java.util.List;


public class IndicatorAdapter extends RecyclerView.Adapter<IndicatorAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Indicator> indicatorList;
    private List<Indicator> indicatorListFiltered;
    private IndicatorAdapterListener listener;


    //la sottostante classe viewholder viene usata per ridurre le invocazioni al metodo findviewbyid, si riciclano il
    //più possibile le view usate per visualizzare elementi, e il viewholder conserva i riferimenti
    //ai widget interni ad ogni elemento
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, sourceNotes;
        public ImageView thumbnail;


        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            sourceNotes = view.findViewById(R.id.sourceNote);
            thumbnail = view.findViewById(R.id.thumbnail);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //indicator selezionato
                    listener.onIndicatorSelected(indicatorListFiltered.get(getAdapterPosition()));

                }
            });
        }
    }


    public IndicatorAdapter(Context context, List<Indicator> indicatorList, IndicatorAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.indicatorList = indicatorList;
        this.indicatorListFiltered = indicatorList;
    }

    @NonNull
    @Override
    //metodo chiamato alla creazione dell'adapter e usato per inizializzare il ViewHolder
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //momento in cui un elemento della RecyclerView viene creato
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    //metodo chiamato per collegare il ViewHolder all'adapter; è dove si passano i dati al ViewHolder
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) { //momento in cui vengono recuperati i riferimenti agli elementi interni della RecyclerView da popolare con i nuovi dati
        final Indicator indicator = indicatorListFiltered.get(position);
        holder.name.setText(indicator.getName());
        Drawable myDrawable = context.getResources().getDrawable(R.drawable.indicator);
        holder.thumbnail.setImageDrawable(myDrawable);
    }

    @Override
    public int getItemCount() { //restituisce il numero di elementi nella indicatorList
        return indicatorListFiltered.size();
    }

    @Override
    public Filter getFilter() { //filtro per la ricerca
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty() || charSequence.length() < 3) {
                    indicatorListFiltered = indicatorList;
                } else { //il filtro sulla ricerca inizia quando i caratteri inseriti sono almeno 3
                    List<Indicator> filteredList = new ArrayList<>(); //creo una filtered list
                    for (Indicator row : indicatorList) {
                        //se la row contiene la sequenza di caratteri inserita, allora aggiunge la row alla filteredlist
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    indicatorListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = indicatorListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) { //pubblica i risultati basati sulla ricerca
                indicatorListFiltered = (ArrayList<Indicator>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface IndicatorAdapterListener {
        void onIndicatorSelected(Indicator indicator);
    }
}
