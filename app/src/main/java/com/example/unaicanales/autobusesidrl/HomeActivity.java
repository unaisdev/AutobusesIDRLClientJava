        package com.example.unaicanales.autobusesidrl;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unaicanales.autobusesidrl.fragments.AlertsFragment;
import com.example.unaicanales.autobusesidrl.fragments.HorariosFragment;
import com.example.unaicanales.autobusesidrl.fragments.MapFragment;
import com.example.unaicanales.autobusesidrl.models.Autobus;
import com.example.unaicanales.autobusesidrl.models.Parada;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity: ";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Ruta> rutas = new ArrayList<>();

    private Fragment fragActivo;
    private AlertsFragment alertsFragment = new AlertsFragment();
    private HorariosFragment horariosFragment = new HorariosFragment();
    private MapFragment mapFragment = new MapFragment();

    private DataInputStream br;
    private DataOutputStream bw;
    private ObjectInputStream brObject;
    private final int PORT_NUMBER = 7979;
    private final String NAME = "10.0.2.2";
    public static ArrayList<Autobus> autobuses = new ArrayList<>();
    public static ArrayList<Marker> markerAutobuses;
    public static ArrayList<Parada> paradas = new ArrayList<>();

    private GoogleSignInAccount cliente;
    private Spinner spinnerLinea;

    final public Runnable runnableListener = new Runnable() {
        @Override
        public void run() {
            try {
                String msg;
                while ((msg =br.readUTF())!=null) {
                    switch(findForWhat(msg)){
                        case "linea":
                            JSONObject jsonObject = findLinea(msg);
                            Log.e("---> ", jsonObject.toString());


                            break;
                        case "posBus":
                            //En caso de recibir un mensaje de este tipo, recibimos DONDE se encuentra el bus inicialmente, para pintarlo en el mapa
                            Autobus autobus = new Autobus(findBus(msg), findBus(msg), findCoordinates(msg));
                            autobuses.add(autobus);

                            Log.e("---> ", findBus(msg));
                            Log.e("---> ", findCoordinates(msg).toString());
                            break;

                        case "line":
                            //En caso de recibir un mensaje de este tipo, recibimos DONDE se encuentra el bus inicialmente, para pintarlo en el mapa
                            Ruta ruta = new Ruta();

                            Log.e("---> ", findBus(msg));
                            Log.e("---> ", findCoordinates(msg).toString());
                            break;
                        case "movBus":
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

                            MoverAutobus moverAutobus = new MoverAutobus(msg);

                            runOnUiThread(moverAutobus);

                            break;

                        case "posParada":
                            GeoPoint puntoParada = findCoordinatesParada(msg);
                            String nombre = findBus(msg);
                            ArrayList<String> lineasAsociadas = findLinesAsociated(msg);

                            Parada nuevaParada = new Parada(nombre, nombre, puntoParada.getLatitude(), puntoParada.getLongitude(), lineasAsociadas);
                            paradas.add(nuevaParada);

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


    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.alertas:
                    getSupportFragmentManager().beginTransaction().hide(fragActivo).show(alertsFragment).commit();
                    fragActivo = alertsFragment;

                    break;

                case R.id.mapa:
                    getSupportFragmentManager().beginTransaction().hide(fragActivo).show(mapFragment).commit();
                    fragActivo = mapFragment;

                    break;

                case R.id.horarios:
                    getSupportFragmentManager().beginTransaction().hide(fragActivo).show(horariosFragment).commit();
                    fragActivo = horariosFragment;

                    break;
            }
            return false;
        }
    };

    @Override
    protected  void onStart() {
        super.onStart();

        //Leer qué idioma estaba seleccionado
        Locale localization = Locale.getDefault();
        Configuration config = new Configuration();
        config.locale = localization;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.botNav);

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        //Hacemos que la vista del mapa se acerque a la localizacion del dispositivo
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, alertsFragment, "3").hide(alertsFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, horariosFragment, "2").hide(horariosFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mapFragment, "1").commit();
        fragActivo = mapFragment;

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    hListen.post(runnableListener);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

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


    //sacamos las coordenadas del mensaje
    public static GeoPoint findCoordinatesParada(String msg){
        String[] coordinates = findData(msg).substring(findData(msg).indexOf('|') + 1, findData(msg).lastIndexOf('|')).split(",");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        return new GeoPoint(latitude, longitude);
    }

    public static ArrayList<String> findLinesAsociated(String msg){
        ArrayList<String> lineasAsociadas = new ArrayList<>();
        String[] lineaSociadas = findData(msg).substring(findData(msg).lastIndexOf('|') + 1, findData(msg).length()).split(",");
        for (String linea: lineaSociadas)
            lineasAsociadas.add(linea);
        return lineasAsociadas;
    }


    //Metodo para mostrar y ocultar el menu de la Actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.opciones, menu);
        return true;
    }

    //Metodo para asignar las funciones al menu del actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.idioma:
                open_dialogo_lista_idiomas();
                return true;
            case R.id.salir:
                finish();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void open_dialogo_lista_idiomas(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(true);
        final CharSequence[] items = {"Castellano", "Euskera"};
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Locale localization = null;
                if (item == 0) localization = new Locale("es", "ES");
                else if (item == 1) localization = new Locale("eus", "EUS");
                else{
                    dialog.cancel();
                    return;
                }
                Locale.setDefault(localization);
                Configuration config = new Configuration();
                config.locale = localization;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                recreate();
            }
        });
        alertDialogBuilder.create().show();
    }

}
