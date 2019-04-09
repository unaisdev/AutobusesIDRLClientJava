package com.example.unaicanales.autobusesidrl.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unaicanales.autobusesidrl.ConnServer;
import com.example.unaicanales.autobusesidrl.HomeActivity;
import com.example.unaicanales.autobusesidrl.R;
import com.example.unaicanales.autobusesidrl.SpinnerAdapter;
import com.example.unaicanales.autobusesidrl.models.Autobus;
import com.example.unaicanales.autobusesidrl.models.Parada;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.collection.ArraySortedMap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private static final String TAG = "/: ";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;
    private Spinner spinnerLinea;

    public ArrayList<Ruta> rutas;
    public ArrayList<Parada> paradas;
    public static ArrayList<Autobus> autobuses = ConnServer.autobuses;
    public ArrayList<GeoPoint> arrayListPuntosRuta;

    public static Map<String, Marker> markerAutobuses;
    public Map<String, Parada> mapParadas;

    public Map<String, Map<String, Parada>> mapRuta;

    private GoogleMap mMap;

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_home_mapa, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        spinnerLinea = (Spinner) rootView.findViewById(R.id.spinnerLinea);
        spinnerLinea.setOnItemSelectedListener(onItemSelectedListener);

        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        cargarAutobuses();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(43.339814, -1.791228), 11,0,0)));
    }


    private void cargarLineas(){
        db.collection("Ruta")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Ruta rut = new Ruta("A-00", getString(R.string.seleccionarRuta), R.color.gray);
                            rutas.add(rut);
                            for (DocumentSnapshot document : task.getResult()) {
                                Ruta ruta = document.toObject(Ruta.class);
                                rutas.add(ruta);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    public void adaptador(){
        SpinnerAdapter adapter = new SpinnerAdapter(this.getContext(), rutas);
        spinnerLinea.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinnerLinea.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void cargarAutobuses(){
        markerAutobuses = new HashMap<>();

        for (Autobus autobus: autobuses) {

            autobus.actualizarPos();

            final Marker aMarker = mMap.addMarker(autobus.getMarkerOptions());
            markerAutobuses.put(autobus.getId(), aMarker);
        }

        ConnServer.markerAutobuses = markerAutobuses;
    }


    public static void moverAutobus(String msg){
        //Recibimos el punto donde se encuentra el bus ahora, debido a que se encontraría en movimiento
        Iterator it = markerAutobuses.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            for (Autobus bus: MapFragment.autobuses) {
                if(pair.getKey().equals(bus.getId())){
                    Marker marker = (Marker) pair.getValue();

                    marker.setPosition(new LatLng(ConnServer.findCoordinates(msg).getLatitude(), ConnServer.findCoordinates(msg).getLongitude()));
                }
            }

            it.remove(); // avoids a ConcurrentModificationException
        }
    }
/*
    public void cargarParadasAutobuses(){



        paradas = new ArrayList<>();
        rutas = new ArrayList<>();
        autobuses = new ArrayList<>();
        markerAutobuses = new HashMap<>();
        mapParadas = new HashMap<>();
        arrayListPuntosRuta = new ArrayList<>();

        db.collection("Autobus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Autobus autobus = document.toObject(Autobus.class);

                                Log.e(TAG, document.getId() + " => " + document.getData().get("nombre"));
                                autobus.actualizarPos();

                                final Marker aMarker = mMap.addMarker(autobus.getMarkerOptions());
                                markerAutobuses.put(autobus.getId(), aMarker);
                                autobuses.add(autobus);
                                recargar(autobus);

                                Log.e(TAG, autobus.getMarkerOptions().toString());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("Ruta")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {

                                //Conseguir las paradas de la ruta completa desde Firestore
                                ArrayList<Object> paradas = (ArrayList) document.getData().get("paradas");
                                //Horario de las rutas
                                ArrayList<String> horario = (ArrayList) document.getData().get("horario");

                                for (Object parada : paradas) {
                                    String name = "";
                                    double longitude = 0;
                                    double latitude = 0;

                                    ArrayList<String> lineasAsociadas = new ArrayList<>();

                                    HashMap<String, String> hashMapParadas = (HashMap<String, String>) parada;
                                    Iterator it = hashMapParadas.entrySet().iterator();

                                    while (it.hasNext()) {

                                        Map.Entry pair = (Map.Entry)it.next();
                                        System.out.println(pair.getKey() + " = " + pair.getValue());
                                        switch (pair.getKey().toString()){
                                            case "_lat":
                                                latitude = Double.parseDouble(pair.getValue().toString());
                                                break;
                                            case "_lon":
                                                longitude = Double.parseDouble(pair.getValue().toString());
                                                break;
                                            case "name":
                                                name = (String) pair.getValue();
                                                break;
                                            case "lines":
                                                lineasAsociadas = (ArrayList<String>) pair.getValue();
                                                break;
                                        }

                                        it.remove(); // avoids a ConcurrentModificationException
                                    }
                                    Parada nuevaParada = new Parada(name, name, latitude, longitude, lineasAsociadas);
                                    mapParadas.put(nuevaParada.getId(), nuevaParada);
                                    String lineasParada = "";
                                    for (String line: lineasAsociadas) {
                                        lineasParada = lineasParada + line + ", ";
                                    }
                                    lineasParada = lineasParada.substring(0, lineasParada.length()-2);
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(nuevaParada.getLatLong().getLatitude(), nuevaParada.getLatLong().getLongitude()))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.parada_marker))
                                            .snippet(lineasParada)
                                            .title(nuevaParada.getNombre())
                                            .flat(true)
                                    );
                                }

                                //TODO Tenemos los tramos de la ruta, y los tiempos que tarda en llegar

                                //Ahora recogeremos los puntos que necesitamos para trazar las rutas
                                HashMap<String, Object> puntosRuta = (HashMap<String, Object>) document.getData().get("ruta");

                                int posicionArrayRuta = 0;
                                Ruta nuevaRuta = new Ruta();

                                Iterator it = puntosRuta.entrySet().iterator();

                                while (it.hasNext()) {
                                    ArrayList<HashMap<String, ArrayList<HashMap>>> trk;

                                    Map.Entry pair = (Map.Entry)it.next();
                                    System.out.println(pair.getKey() + " = " + pair.getValue());
                                    switch (pair.getKey().toString()){
                                        case "idRuta":
                                            nuevaRuta.setId(pair.getValue().toString());
                                            nuevaRuta.setNombre(pair.getValue().toString());
                                            for(String hora : horario){

                                            }

                                            break;
                                        case "color":
                                            nuevaRuta.setColor(Color.parseColor(pair.getValue().toString()));
                                            Log.e("COLORRRRRRRRRRRRRR", nuevaRuta.getColor() + "");
                                            break;
                                        case "trk":
                                            trk = (ArrayList<HashMap<String, ArrayList<HashMap>>>) pair.getValue();
                                            for (HashMap<String, ArrayList<HashMap>> trkseg: trk) {
                                                Iterator iterator = trkseg.entrySet().iterator();
                                                while (iterator.hasNext()) {

                                                    ArrayList<HashMap> trkpt;

                                                    Map.Entry entry = (Map.Entry)iterator.next();
                                                    System.out.println(entry.getKey() + " = " + entry.getValue());

                                                    switch (entry.getKey().toString()){
                                                        case "trkpt":
                                                            trkpt = (ArrayList<HashMap>) entry.getValue();
                                                            double lat = 0;
                                                            double lon = 0;
                                                            PolylineOptions rectOptions = new PolylineOptions().color(nuevaRuta.getColor());
                                                            Polyline polyline;

                                                            for (HashMap<String, Object> posLatLong: trkpt) {
                                                                Iterator iteratorPos = posLatLong.entrySet().iterator();
                                                                while (iteratorPos.hasNext()) {
                                                                    Map.Entry entry1 = (Map.Entry) iteratorPos.next();
                                                                    switch (entry1.getKey().toString()){
                                                                        case "_lat":
                                                                            lat = Double.parseDouble(entry1.getValue().toString());
                                                                            break;
                                                                        case "_lon":
                                                                            lon = Double.parseDouble(entry1.getValue().toString());
                                                                            break;
                                                                    }
                                                                }
                                                                GeoPoint geoPoint = new GeoPoint(lat, lon);

                                                                nuevaRuta.getRutaCompleta().add(geoPoint);
                                                                rectOptions.add(new LatLng(lat, lon));

                                                            }

                                                            nuevaRuta.setRoute(mMap.addPolyline(rectOptions));

                                                                break;

                                                        case "desc":
                                                            //desc = (String) entry.getValue();

                                                            break;

                                                        case "name":
                                                            //name = (String) entry.getValue();

                                                            break;
                                                    }

                                                }

                                            }
                                            break;
                                        case "name":

                                            break;
                                        case "lines":

                                            //lineasAsociadas = (ArrayList<String>) pair.getValue();
                                            break;

                                    }
                                    it.remove(); // avoids a ConcurrentModificationException
                                }

                                rutas.add(nuevaRuta);
                                Log.d(TAG, document.getId() + " => " + nuevaRuta);
                            }
                            adaptador();

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }*/
}