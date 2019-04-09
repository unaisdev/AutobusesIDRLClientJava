package com.example.unaicanales.autobusesidrl.models;

import com.example.unaicanales.autobusesidrl.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

public class Autobus {

    private String id;
    private String nombre;
    private Marker marker;
    private MarkerOptions markerOptions;
    private GeoPoint latLong;
    private LatLng coord;

    public Autobus(){
    }

    public Autobus(String id, String nombre, GeoPoint latLong) {
        this.id = id;
        this.nombre = nombre;
        this.latLong = latLong;
        this.coord = new LatLng(latLong.getLatitude(), latLong.getLongitude());

    }

    public LatLng getCoord() {
        return coord;
    }

    public void setCoord(LatLng coord) {
        this.coord = coord;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
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

    public GeoPoint getLatLong() {
        return latLong;
    }

    public void setLatLong(GeoPoint latLong) {
        this.latLong = latLong;
    }

    public void actualizarPos(){
        markerOptions = new MarkerOptions()
                .position(new LatLng(latLong.getLatitude(), latLong.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus))
                .title(nombre)
                .snippet("ESTO ES UN BUSETO")
                .flat(true);
    }

    @Override
    public String toString(){
        return "Nombre: " + nombre + " | LAT: " + latLong.getLatitude() + "| LONG: " + latLong.getLongitude();
    }
}
