package com.progettoMP2018.clashers.worldbank.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import com.progettoMP2018.clashers.worldbank.R;

public class CheckConnection { //serve per avvisare l'utente quando l'app parte con wifi/dati mobili disattivati; permette di attivarli
    Context context;

    public CheckConnection (Context context){
        this.context = context;
    }

    /*
        Attraverso questo metodo controlliamo che sia abilitato almeno
        uno dei due tra Wi-Fi e Dati Mobili
     */
    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    /*
        Questo metodo, in caso in cui l'utente non abbia nessun tipo di
        connessione internet abilitata (Wi-Fi o Dati Mobili) mostra un AlertDialog
        con tre bottoni di cui il primo e il secondo reindirizzeranno l'utente rispettivamente
        alle impostazioni per abilitare il Wi-Fi e i Dati Mobili, il terzo peremetterà di
        chiudere l'applicazione nel caso in cui l'utente non voglia abilitare la connessione.
     */
    public static void showNoConnectionDialog(final Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(context.getString(R.string.connection_dialog_description));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.connection_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //reindirizza l'utente alle impostazione per accendere il Wi-Fi
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.connection_negative_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //reindirizza l'utente alle impostazione per accendere i Dati Mobili
                        context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,context.getString(R.string.connection_neutral_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //si esce dall'applicazione
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(startMain);

            }
        });
        alertDialog.show();
    }
}
