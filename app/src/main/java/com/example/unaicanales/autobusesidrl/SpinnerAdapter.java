package com.example.unaicanales.autobusesidrl;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.unaicanales.autobusesidrl.models.Ruta;

import java.util.ArrayList;
import java.util.List;


public class SpinnerAdapter extends ArrayAdapter<String> {

    ArrayList<Ruta> rutas;
    Context context;

    public SpinnerAdapter(@NonNull Context context, ArrayList<Ruta> rutas) {
        super(context, R.layout.spinner_item);
        this.rutas = rutas;
        this.context = context;
    }

    /**
     * Return the number of items in the list.
     * If you don’t override this method, the spinner list will be empty.
     */
    @Override
    public int getCount() {
        return rutas.size();
    }

    /**
     * This is where we work with initializing the views
     * that we added them in the custom layout.
     */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        SpinnerItems spinnerItems = new SpinnerItems();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_item, parent, false);
            spinnerItems.idText = convertView.findViewById(R.id.idLinea);
            spinnerItems.lineaText = convertView.findViewById(R.id.nombreLinea);
            convertView.setTag(spinnerItems);
        } else {
            spinnerItems = (SpinnerItems) convertView.getTag();
        }
        Log.d("Soucer image", String.valueOf(rutas.get(position)));
        Ruta ruta = rutas.get(position);
        spinnerItems.idText.setText(ruta.getId());
        spinnerItems.idText.setBackgroundColor(ruta.getColor());
        spinnerItems.lineaText.setText(ruta.getNombre());

        return convertView;
    }

    /**
     * This will show the data when you tap on Android spinner,
     * if you don’t override this method your app
     * will crash when you try to tap on the spinner.
     */

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getDropDownView(position, convertView, parent);
        return getView(position, convertView, parent);
    }


    private static class SpinnerItems {
        TextView idText;
        TextView lineaText;
    }
}