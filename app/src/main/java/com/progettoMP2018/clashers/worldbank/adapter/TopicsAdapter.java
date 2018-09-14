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
import com.progettoMP2018.clashers.worldbank.entity.Topic;

import java.util.ArrayList;
import java.util.List;


public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Topic> topicList;
    private List<Topic> topicListFiltered;
    private TopicsAdapterListener listener;

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
                    //topic selezionato
                    listener.onTopicSelected(topicListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    public TopicsAdapter(Context context, List<Topic> topicList, TopicsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.topicList = topicList;
        this.topicListFiltered = topicList;
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
        final Topic topic = topicListFiltered.get(position);
        holder.name.setText(topic.getValue());
        Drawable myDrawable = context.getResources().getDrawable(R.drawable.img);
        holder.thumbnail.setImageDrawable(myDrawable);
    }

    @Override
    public int getItemCount() { //restituisce il numero di elementi nella topicList
        return topicListFiltered.size();
    }

    @Override
    public Filter getFilter() { //filtro per la ricerca
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty() || charSequence.length() < 3) {
                    topicListFiltered = topicList;
                } else { //il filtro sulla ricerca inizia quando i caratteri inseriti sono almeno 3
                    List<Topic> filteredList = new ArrayList<>(); //creo una filtered list
                    for (Topic row : topicList) {
                        //se la row contiene la sequenza di caratteri inserita, allora aggiunge la row alla filteredlist
                        if (row.getValue().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    topicListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = topicListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) { //pubblica i risultati basati sulla ricerca
                topicListFiltered = (ArrayList<Topic>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface TopicsAdapterListener {
        void onTopicSelected(Topic topic);
    }
}