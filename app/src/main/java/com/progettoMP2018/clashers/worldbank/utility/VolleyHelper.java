package com.progettoMP2018.clashers.worldbank.utility;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;

public class VolleyHelper extends Application { //è una classe singleton in cui il volley viene istanziato
    public static final String TAG = VolleyHelper.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static VolleyHelper mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized VolleyHelper getInstance() {
        return mInstance;
    }

    /*
    Metodo che ritorna un'istanza di RequestQueue.
    Questo tipo di implementazione assicura che la variabile
    sia istanziata solamente una volta e la stessa istanza
    venga usata all'interno di tutta l'applicazione.
    */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /*
        Metodo che aggiunge una richiesta (Request) alla
        singola istanza di RequestQueue
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void getDataVolley(String url, final VolleyRequestListener listener) {
        /*
            Si effettua la richiesta di un JSONArray attraverso l'url da cui recuperare il file Json,
            un'implementazione dell'interfaccia Response.Listener(), che verrà invocata se la richiesta
            ha successo, e un'implementazione dell'interfaccia Error.Listener(), che verrà invocata se
            un qualsiasi errore viene incontrato durante il processamento della richiesta.
        */
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG + ": ", "somePostRequest Response : " + response.toString());
                        if (response.toString() != null) {
                            listener.getResult(response.toString()); //viene invocato il metodo per la gestione della risposta che
                                                                     // sarà specifico per ogni classe che invocherà questo metodo
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error.networkResponse != null) {
                    Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                    listener.getResult("error");
                }
            }
        });
        addToRequestQueue(request);
    }
}
