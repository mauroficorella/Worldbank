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
import com.progettoMP2018.clashers.worldbank.entity.SavedRequest;
import java.util.ArrayList;
import java.util.List;

public class OfflineDataAdapter extends RecyclerView.Adapter<OfflineDataAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<SavedRequest> elementsList;
    private List<SavedRequest> elementsListFiltered;
    private OfflineDataAdapterListener listener;

    //la sottostante classe viewholder viene usata per ridurre le invocazioni al metodo findviewbyid, si riciclano il
    //più possibile le view usate per visualizzare elementi, e il viewholder conserva i riferimenti
    //ai widget interni ad ogni elemento
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView topicName, indicatorName, countryName;
        public ImageView thumbnail;


        public MyViewHolder(View view) {
            super(view);
            topicName = view.findViewById(R.id.name);
            indicatorName = view.findViewById(R.id.sourceNote);
            countryName = view.findViewById(R.id.sourceNote2);
            thumbnail = view.findViewById(R.id.thumbnail);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //elemento della lista selezionato
                    listener.onElementSelected(elementsListFiltered.get(getAdapterPosition()));

                }
            });
        }
    }


    public OfflineDataAdapter(Context context, List<SavedRequest> elementsList, OfflineDataAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.elementsList = elementsList;
        this.elementsListFiltered = elementsList;
    }

    @NonNull
    @Override
    //metodo chiamato alla creazione dell'adapter e usato per inizializzare il ViewHolder
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //momento in cui un elemento della RecyclerView viene creato
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offline_element_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    //metodo chiamato per collegare il ViewHolder all'adapter; è dove si passano i dati al ViewHolder
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) { //momento in cui vengono recuperati i riferimenti agli elementi interni della RecyclerView da popolare con i nuovi dati
        final SavedRequest elements = elementsListFiltered.get(position);
        holder.topicName.setText(elements.getTopicName());
        holder.indicatorName.setText(elements.getIndicatorName());
        holder.countryName.setText(elements.getCountryName());
        Drawable myDrawable = context.getResources().getDrawable(R.drawable.img);
        holder.thumbnail.setImageDrawable(myDrawable);
    }

    @Override
    public int getItemCount() { //restituisce il numero di elementi nella elementsList
        return elementsListFiltered.size();
    }

    @Override
    public Filter getFilter() { //filtro per la ricerca
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty() || charSequence.length() < 3) {
                    elementsListFiltered = elementsList;
                } else { //il filtro sulla ricerca inizia quando i caratteri inseriti sono almeno 3
                    List<SavedRequest> filteredList = new ArrayList<>(); //creo una filtered list
                    for (SavedRequest row : elementsList) {
                        //se la row contiene la sequenza di caratteri inserita, allora aggiunge la row alla filteredlist
                        if (row.getTopicName().toLowerCase().contains(charString.toLowerCase())
                                || row.getIndicatorName().toLowerCase().contains(charString.toLowerCase())
                                || row.getCountryName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    elementsListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = elementsListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) { //pubblica i risultati basati sulla ricerca
                elementsListFiltered = (ArrayList<SavedRequest>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface OfflineDataAdapterListener {
        void onElementSelected(SavedRequest element);
    }
}
