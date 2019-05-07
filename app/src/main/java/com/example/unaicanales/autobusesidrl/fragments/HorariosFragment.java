package com.example.unaicanales.autobusesidrl.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.unaicanales.autobusesidrl.HomeActivity;
import com.example.unaicanales.autobusesidrl.R;
import com.example.unaicanales.autobusesidrl.SpinnerAdapter;
import com.example.unaicanales.autobusesidrl.models.Parada;
import com.example.unaicanales.autobusesidrl.models.Ruta;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Belal on 1/23/2018.
 */

public class HorariosFragment extends Fragment {

    private static final String TAG = "HorariosFragment:////";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TableLayout table;
    private Spinner spinnerLinea;
    public ArrayList<Ruta> rutas;
    public ArrayList<String> horarioRecogido;
    public HashMap<String, ArrayList<String>> horarios;


    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            table.removeAllViews();
            int contCol = 0;
            int columns = 4;

            TableRow row = new TableRow(getContext());

            TableLayout.LayoutParams tableRowParams=
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableLayout.LayoutParams tableParams=
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

            int leftMargin=20;
            int topMargin=20;
            int rightMargin=20;
            int bottomMargin=20;

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            tableRowParams.gravity = Gravity.CENTER;

            row.setLayoutParams(tableRowParams);

            for(String hora: horarios.get(rutas.get(position).getNombre())){
                // create a new TextView
                TextView t = new TextView(getContext());
                t.setPadding(10, 0, 10, 0);
                // set the text to "text xx"
                t.setText(hora);

                // add the TextView and the CheckBox to the new TableRow
                row.addView(t);

                contCol++;

                // add the TableRow to the TableLayout
                if(contCol > columns){
                    // create a new TableRow
                    table.addView(row, tableParams);
                    contCol = 0;
                    row = new TableRow(getContext());
                    row.setLayoutParams(tableRowParams);

                }
            }
    }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home_horarios, container, false);
        table = (TableLayout) rootView.findViewById(R.id.tableLayout);
        spinnerLinea = (Spinner) rootView.findViewById(R.id.spinnerLinea);
        spinnerLinea.setOnItemSelectedListener(onItemSelectedListener);
        cargarLineasHorarios();

        return rootView;
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
    }

    private void cargarLineasHorarios(){
        rutas = new ArrayList<>();
        horarioRecogido = new ArrayList<>();
        horarios = new HashMap<>();

        db.collection("Ruta")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {

                                //Horario de las rutas
                                ArrayList<String> horario = (ArrayList) document.getData().get("horario");

                                //Ahora recogeremos los puntos que necesitamos para trazar las rutas
                                HashMap<String, Object> puntosRuta = (HashMap<String, Object>) document.getData().get("ruta");

                                Iterator it = puntosRuta.entrySet().iterator();

                                Ruta nuevaRuta = new Ruta();
                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry)it.next();
                                    System.out.println(pair.getKey() + " = " + pair.getValue());
                                    switch (pair.getKey().toString()){
                                        case "idRuta":
                                            nuevaRuta.setId(pair.getValue().toString());
                                            nuevaRuta.setNombre(pair.getValue().toString());
                                            break;
                                        case "color":
                                            nuevaRuta.setColor((pair.getValue().toString()));
                                            break;
                                    }
                                    it.remove(); // avoids a ConcurrentModificationException
                                }

                                rutas.add(nuevaRuta);
                                horarios.put(nuevaRuta.getId(), horario);

                                horarioRecogido.clear();
                                Log.d(TAG, document.getId() + " => " + nuevaRuta);
                            }
                            adaptador();

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

}