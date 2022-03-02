package com.vsb.kru13.osmzhttpserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class LocationHolder extends LocationAndSensor {
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;
    private Activity activity;

    LocationHolder(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(30000);
        this.locationRequest.setFastestInterval(5000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
    }

    /**
     * Function that updates GPS information
     */
    private void updateGPS() {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);

        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.fusedLocationClient.getLastLocation().addOnSuccessListener(this.activity, location -> {
                if (location != null) {
                    Log.d("LAT", String.valueOf(location.getLatitude()));
                    Log.d("LONG", String.valueOf(location.getLongitude()));
                    Log.d("ALT", String.valueOf(location.getAltitude()));

                    JSONArray array = new JSONArray();
                    array.put("lat:"+location.getLatitude());
                    array.put("long:"+location.getLongitude());
                    array.put("alt:"+location.getAltitude());

                    try {
                        this.writeData(array, "location", this.context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("LOC-NOT", "TEST");
                }
            });
        } else {
            Log.d("PERM", "not");
        }
    }
}
