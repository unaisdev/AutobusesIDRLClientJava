package com.example.unaicanales.autobusesidrl.models;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.List;

public class Parada {

    private String id;
    private String nombre;
    private double latitud;
    private double longitud;
    private GeoPoint latLong;
    private List<String> lineas;

    public Parada(){

    }

    public Parada(String id, String nombre, double latitud, double longitud, List<String> lineas) {
        this.id = id;
        this.nombre = nombre;
        this.latLong = new GeoPoint(latitud, longitud);
        this.lineas = lineas;
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

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public GeoPoint getLatLong() {
        return latLong;
    }

    public void setLatLong(GeoPoint latLong) {
        this.latLong = latLong;
    }

    public List<String> getLineas() {
        return lineas;
    }

    public void setLineas(List<String> lineas) {
        this.lineas = lineas;
    }
}
