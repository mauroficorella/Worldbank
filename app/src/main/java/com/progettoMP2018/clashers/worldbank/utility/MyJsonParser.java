package com.progettoMP2018.clashers.worldbank.utility;



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import com.progettoMP2018.clashers.worldbank.entity.Country;
import com.progettoMP2018.clashers.worldbank.entity.GraphData;
import com.progettoMP2018.clashers.worldbank.entity.Indicator;
import com.progettoMP2018.clashers.worldbank.entity.Topic;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;

public class MyJsonParser {

    public static List<Topic> parseTopics(String jsonString) {
        try {
            JSONArray topicArray = (new JSONArray(jsonString)).getJSONArray(1); //si prende il primo array nel file json visto che è un array di array e il primo array non serve a nulla
            Gson gson = new Gson(); //tramite il tool gson si trasformano i contenuti del json in oggetti riutilizzabili
            Type listType = new TypeToken<List<Topic>>() {
            }.getType();
            List<Topic> items = gson.fromJson(String.valueOf(topicArray), listType); //si crea una lista di "Topic" tramite gson prendendo il valore degli elementi in topicArray
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Indicator> parseIndicators(String jsonString) {
        try {
            JSONArray indicatorArray = (new JSONArray(jsonString)).getJSONArray(1); //si prende il primo array nel file json visto che è un array di array e il primo array non serve a nulla
            Gson gson = new Gson(); //tramite il tool gson si trasformano i contenuti del json in oggetti riutilizzabili
            Type listType = new TypeToken<List<Indicator>>() {}.getType();
            List<Indicator> items = gson.fromJson(String.valueOf(indicatorArray), listType); //si crea una lista di "Indicator" tramite gson prendendo il valore degli elementi in indicatorArray
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static List<Country> parseCountries(String jsonString) {
        try {
            JSONArray countryArray = (new JSONArray(jsonString)).getJSONArray(1); //si prende il primo array nel file json visto che è un array di array e il primo array non serve a nulla
            Gson gson = new Gson(); //tramite il tool gson si trasformano i contenuti del json in oggetti riutilizzabili
            Type listType = new TypeToken<List<Country>>() {}.getType();
            List<Country> items = gson.fromJson(String.valueOf(countryArray), listType); //si crea una lista di "Country" tramite gson prendendo il valore degli elementi in countryArray
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static List<GraphData> parseChartData(String jsonString) {
        try {
            JSONArray chartDataArray = (new JSONArray(jsonString)).getJSONArray(1); //si prende il primo array nel file json visto che è un array di array e il primo array non serve a nulla
            Gson gson = new Gson(); //tramite il tool gson si trasformano i contenuti del json in oggetti riutilizzabili
            Type listType = new TypeToken<List<GraphData>>() {
            }.getType();
            List<GraphData> items = gson.fromJson(String.valueOf(chartDataArray), listType); //si crea una lista di "GraphData" tramite gson prendendo il valore degli elementi in chartDataArray
            return items;
        } catch (JSONException e) {
            return null;
        }
    }

}
