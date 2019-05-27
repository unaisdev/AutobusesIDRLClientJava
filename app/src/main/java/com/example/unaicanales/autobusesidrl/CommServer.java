package com.example.unaicanales.autobusesidrl;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.unaicanales.autobusesidrl.models.Autobus;
import com.example.unaicanales.autobusesidrl.models.Parada;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// CLASE DE COMUNICACION CON EL SERVIDOR

public class CommServer {

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    public static ArrayList<Autobus> autobuses = new ArrayList<>();
    public static ArrayList<Ruta> rutas = new ArrayList<>();
    public static ArrayList<Marker> markerAutobuses;
    public static String msg;
    private static final String CHANNEL_ID ="Alertas";
    private NotificationManager notificationManager;
    private Activity activity;
    public static ArrayList<Parada> paradas = new ArrayList<>();
    private Marker aMarker;

    public static GoogleMap mMap;

    public CommServer(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Activity activity){
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.activity = activity;
        //Necesitamos hacer uso de hTread para crear un proceso, y asignarle los runnables que debe ejecutar
        HandlerThread hTListen = new HandlerThread("listening");
        hTListen.start();

        //Metemos en la cola el Runnable
        Handler hListen = new Handler(hTListen.getLooper());
        hListen.post(runnableListener);
    }

    final public Runnable runnableListener = new Runnable() {
        @Override
        public void run() {
            try {
                while ((msg = dataInputStream.readUTF())!=null) {
                    System.out.println(msg);
                    switch(findForWhat(msg)){
                        case "linea":
                            lineUps();

                            break;
                        case "movBus":
                            System.out.println("EE");
                            busMovement();

                            break;

                        case "posParada":
                            stationPos();

                            break;

                        case "alerta":
                            alertComes();

                            break;

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void alertComes(){
        String[] alerta = findData(msg).split(",");

        notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationId = new Random().nextInt(60000);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity.getApplicationContext(), CHANNEL_ID)
                .setContentTitle(alerta[0])
                .setContentText(alerta[1]);

        switch (findTypeAlert(msg)){
            case "Accidente":
                mBuilder.setSmallIcon(R.drawable.ic_accidente);
                break;

            case "Desviacion":
                mBuilder.setSmallIcon(R.drawable.ic_desviacion);
                break;

            case "Averia":
                mBuilder.setSmallIcon(R.drawable.ic_averia);
                break;

            case "Retraso":
                mBuilder.setSmallIcon(R.drawable.ic_retraso);
                break;
        }

        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId /* ID of notification */, mBuilder.build());
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }


    private void stationPos(){
        GeoPoint puntoParada = findCoordinatesParada(msg);
        String nombreBus = findBus(msg);
        ArrayList<String> lineasAsociadas = findLinesAsociated(msg);

        Parada nuevaParada = new Parada(nombreBus, nombreBus, puntoParada.getLatitude(), puntoParada.getLongitude(), lineasAsociadas);
        paradas.add(nuevaParada);

    }

    private void busMovement(){
        //Recibimos el punto donde se encuentra el bus ahora, debido a que se encontraría en movimientoç
        Log.e("---> ", msg);


        class MoverAutobus implements Runnable{
            private String msg;

            public MoverAutobus(String msg){
                this.msg = msg;
            }

            @Override
            public void run() {
                //Recibimos el punto donde se encuentra el bus ahora, debido a que se encontraría en movimiento

                for(int i = 0; i < markerAutobuses.size(); i++){
                    if(markerAutobuses.get(i).getTitle().equals(findBus(msg))){
                        markerAutobuses.get(i).setPosition(new LatLng(findCoordinates(msg).getLatitude(), findCoordinates(msg).getLongitude()));
                    }
                }


            }
        }

        class AddMarker implements Runnable{
            private Autobus autobus;

            public AddMarker(Autobus autobus){
                this.autobus = autobus;
            }

            @Override
            public void run() {
                //Recibimos el punto donde se encuentra el bus ahora, debido a que se encontraría en movimiento
                aMarker = mMap.addMarker(autobus.getMarkerOptions());
                markerAutobuses.add(aMarker);
            }
        }

        if(markerAutobuses == null){
            markerAutobuses = new ArrayList<>();
            Autobus autobus = new Autobus(findBus(msg), findBus(msg), findCoordinates(msg));
            autobuses.add(autobus);
            autobus.actualizarPos();

            AddMarker addMarker = new AddMarker(autobus);
            activity.runOnUiThread(addMarker);

        }else{
            MoverAutobus moverAutobus = new MoverAutobus(msg);
            activity.runOnUiThread(moverAutobus);
        }
    }

    private void lineUps() throws JSONException {
        String[] lineaArray = findDataLine(msg);
        List<LatLng> puntosRuta = new ArrayList<>();
        String nombre = lineaArray[0];
        String color = lineaArray[1];
        JSONArray jsonArray = findRouteArray(msg);

        for (int i = 0; i < jsonArray.length(); i++)
        {
            double latitude = jsonArray.getJSONObject(i).getDouble("_lat");
            double longitude = jsonArray.getJSONObject(i).getDouble("_lon");

            LatLng nuevoPunto = new LatLng(latitude, longitude);
            puntosRuta.add(nuevoPunto);
        }

        Ruta ruta = new Ruta(nombre, nombre, color);
        ruta.setRutaCompleta(puntosRuta);

        rutas.add(ruta);
        Log.e("---> ", msg);

    }

    private String[] findDataLine(String msg){
        return findData(msg).split(",");
    }

    private JSONArray findRouteArray(String msg) throws JSONException {
        return new JSONArray(findData(msg).substring(findData(msg).indexOf('['), findData(msg).length()));
    }

    private String findTypeAlert(String msg){
        return findData(msg).substring(0, findData(msg).indexOf(','));
    }

    //Metodo que se encarga de ver que tipo de mensaje llega desde el servidor
    public static String findForWhat(String msg){
        return msg.substring(0, msg.indexOf(':'));
    }

    public static JSONObject findLinea(String msg) throws JSONException {
        return new JSONObject(msg.substring(msg.indexOf(':' + 1), msg.length()));
    }

    //Metodo que recoge el mensaje en sí
    private static String findData(String msg){
        return msg.substring(msg.indexOf(':') + 1, msg.length());
    }

    //metodo que busca el bus al que pertenecen las coordenadas que llegan
    private static String findBus(String msg){
        return findData(msg).substring(0, findData(msg).indexOf('|'));
    }

    //sacamos las coordenadas del mensaje
    private static GeoPoint findCoordinates(String msg){
        String[] coordinates = findData(msg).substring(findData(msg).indexOf('|') + 1, findData(msg).length()).split(",");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        return new GeoPoint(latitude, longitude);
    }


    //sacamos las coordenadas del mensaje
    private static GeoPoint findCoordinatesParada(String msg){
        String[] coordinates = findData(msg).substring(findData(msg).indexOf('|') + 1, findData(msg).lastIndexOf('|')).split(",");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        return new GeoPoint(latitude, longitude);
    }

    private static ArrayList<String> findLinesAsociated(String msg){
        ArrayList<String> lineasAsociadas = new ArrayList<>();
        String[] lineaSociadas = findData(msg).substring(findData(msg).lastIndexOf('|') + 1, findData(msg).length()).split(",");
        for (String linea: lineaSociadas)
            lineasAsociadas.add(linea);
        return lineasAsociadas;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence channelName ="channelName";
        String channelDesc = "channelDesc";

        NotificationChannel notificationChannel;
        notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription(channelDesc);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
