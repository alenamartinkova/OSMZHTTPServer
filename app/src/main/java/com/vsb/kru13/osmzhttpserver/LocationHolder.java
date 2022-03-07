package com.vsb.kru13.osmzhttpserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class LocationHolder extends LocationAndSensor {
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;
    private Activity activity;
    private LocationCallback locationCallback;

    LocationHolder(Activity activity) {
        Looper.prepare();
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(10000);
        this.locationRequest.setFastestInterval(5000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);

        updateGPS();
    }

    /**
     * Function that updates GPS information
     */
    public void updateGPS() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.fusedLocationClient.getLastLocation().addOnSuccessListener(this.activity, location -> {
                if (location != null) {
                    Log.d("LAT", String.valueOf(location.getLatitude()));
                    Log.d("LONG", String.valueOf(location.getLongitude()));
                    Log.d("ALT", String.valueOf(location.getAltitude()));

                    JSONArray array = new JSONArray();

                    try {
                        array.put(location.getLatitude());
                        array.put(location.getLongitude());
                        array.put(location.getAltitude());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        this.writeData(array, "location", this.context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("LOC-NOT", "LOC NOT");
                }
            });
        } else {
            Log.d("PERM", "not");
        }
    }
}
