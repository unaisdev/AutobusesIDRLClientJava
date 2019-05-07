        package com.example.unaicanales.autobusesidrl;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
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
import java.util.Random;

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
    private final String NAME = "192.168.1.3";
    public static ArrayList<Autobus> autobuses = new ArrayList<>();
    public static ArrayList<Marker> markerAutobuses;

    private GoogleSignInAccount cliente;
    private Spinner spinnerLinea;



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

        new ConnServer(this).execute();

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
