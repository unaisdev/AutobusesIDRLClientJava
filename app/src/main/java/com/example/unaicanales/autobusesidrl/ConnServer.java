package com.example.unaicanales.autobusesidrl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.unaicanales.autobusesidrl.fragments.MapFragment;
import com.example.unaicanales.autobusesidrl.models.Autobus;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;

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
import java.util.Map;

public class ConnServer extends AsyncTask<Runnable, Void, Boolean>{

    private DataInputStream br;
    private DataOutputStream bw;
    private ObjectInputStream brObject;
    private final int PORT_NUMBER = 7979;
    private final String NAME = "10.0.2.2";
    public static ArrayList<Autobus> autobuses = new ArrayList<>();
    public static Map<String, Marker> markerAutobuses;


    @Override
    protected Boolean doInBackground(Runnable... runnableListener) {
        try {
            Socket socket = new Socket(NAME, PORT_NUMBER);

            System.out.println("SOCKET CONECTADO!");

            //Abrimos la tuberia de escucha
            br = new DataInputStream(socket.getInputStream());
            //brObject = new ObjectInputStream(socket.getInputStream());

            //Abrimos la tuberia del envio, aunque probablemente no se use
            bw = new DataOutputStream(socket.getOutputStream());

            //Necesitamos hacer uso de hTread para crear un proceso, y asignarle los runnables que debe ejecutar
            HandlerThread hTListen = new HandlerThread("listening");
            hTListen.start();

            //Metemos en la cola el Runnable
            Handler hListen = new Handler(hTListen.getLooper());
            hListen.post(runnableListener[0]);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    //Metodo que se encarga de ver que tipo de mensaje llega desde el servidor
    public static String findForWhat(String msg){
        return msg.substring(0, msg.indexOf(':'));
    }

    public static JSONObject findLinea(String msg) throws JSONException {
        return new JSONObject(msg.substring(msg.indexOf(':' + 1 , msg.length())));
    }

    //Metodo que recoge el mensaje en sí
    public static String findData(String msg){
        return msg.substring(msg.indexOf(':') + 1, msg.length());
    }

    //metodo que busca el bus al que pertenecen las coordenadas que llegan
    public static String findBus(String msg){
        return findData(msg).substring(0, findData(msg).indexOf('|'));
    }

    //sacamos las coordenadas del mensaje
    public static GeoPoint findCoordinates(String msg){
        String[] coordinates = findData(msg).substring(findData(msg).indexOf('|') + 1, findData(msg).length()).split(",");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        return new GeoPoint(latitude, longitude);
    }

    final private Runnable enviarMensaje = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    //AQUI HACEMOS PARA ENVIAR MENSAJES O PEDIR LAS PARADAS SE PODRIA
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    };


}
