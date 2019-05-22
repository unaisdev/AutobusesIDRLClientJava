package com.example.unaicanales.autobusesidrl;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.unaicanales.autobusesidrl.fragments.MapFragment;
import com.example.unaicanales.autobusesidrl.models.Autobus;
import com.example.unaicanales.autobusesidrl.models.Parada;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConnServer extends AsyncTask<Runnable, Void, Boolean>{

    private final int PORT_NUMBER = 7979;
    private final String NAME = "192.168.1.3";
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Activity activity;

    public ConnServer(Activity activity){
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Runnable... runnables) {
        try {
            Socket socket = new Socket(NAME, PORT_NUMBER);

            System.out.println("SOCKET CONECTADO!");

            //Abrimos la tuberia de escucha
            dataInputStream = new DataInputStream(socket.getInputStream());
            //brObject = new ObjectInputStream(socket.getInputStream());

            //Abrimos la tuberia del envio, aunque probablemente no se use
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            CommServer commServer = new CommServer(dataInputStream, dataOutputStream, activity);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
