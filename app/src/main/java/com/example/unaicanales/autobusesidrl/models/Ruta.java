package com.example.unaicanales.autobusesidrl.models;

import android.graphics.Color;
import android.location.Geocoder;

import com.example.unaicanales.autobusesidrl.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ruta {
    private String id;
    private String nombre;
    private String color;
    private Polyline route;
    private ArrayList<Parada> paradas;
    private List<LatLng> rutaCompleta;
    private PolylineOptions rectOptions;

    public Ruta(){
        paradas = new ArrayList<>();
        rutaCompleta = new ArrayList<>();
    }

    public Ruta(String id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.rectOptions = new PolylineOptions();
        rectOptions.color(Color.parseColor(color));
        rectOptions.width(4);
    }

    public PolylineOptions getRectOptions() {
        return rectOptions;
    }

    public void setRectOptions(PolylineOptions rectOptions) {
        this.rectOptions = rectOptions;
    }

    public String getColor() {
        return color;
    }

    public Polyline getRoute() {
        return route;
    }

    public void setRoute(Polyline route) {
        this.route = route;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Parada> getParadas() {
        return paradas;
    }

    public void setParadas(ArrayList<Parada> paradas) {
        this.paradas = paradas;
    }

    public List<LatLng> getRutaCompleta() {
        return rutaCompleta;
    }

    public void setRutaCompleta(List<LatLng> rutaCompleta) {
        this.rutaCompleta = rutaCompleta;
    }

}
