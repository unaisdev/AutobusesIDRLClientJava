package com.example.unaicanales.autobusesidrl.models;

import android.location.Geocoder;

import com.example.unaicanales.autobusesidrl.R;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ruta {
    private String id;
    private String nombre;
    private int color;
    private Polyline route;
    private ArrayList<Parada> paradas;
    private ArrayList<GeoPoint> rutaCompleta;

    public Ruta(){
        paradas = new ArrayList<>();
        rutaCompleta = new ArrayList<>();
    }

    public Ruta(String id, String nombre, int color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public Polyline getRoute() {
        return route;
    }

    public void setRoute(Polyline route) {
        this.route = route;
    }

    public void setColor(int color) {
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

    public ArrayList<GeoPoint> getRutaCompleta() {
        return rutaCompleta;
    }

    public void setRutaCompleta(ArrayList<GeoPoint> rutaCompleta) {
        this.rutaCompleta = rutaCompleta;
    }

}
