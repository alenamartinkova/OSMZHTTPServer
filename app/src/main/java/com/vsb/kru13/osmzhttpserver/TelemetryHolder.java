package com.vsb.kru13.osmzhttpserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class TelemetryHolder implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private Sensor acceleration;
    private Context context;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Activity activity;

    private float gyroX = 0;
    private float gyroY = 0;
    private float gyroZ = 0;

    private float accX = 0;
    private float accY = 0;
    private float accZ = 0;

    private double latitude = 0;
    private double longitude = 0;
    private double altitude = 0;

    TelemetryHolder(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        this.acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(10000);
        this.locationRequest.setFastestInterval(5000);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);

        updateGPS();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == this.acceleration) {
            this.getAccelerationData(sensorEvent);
        } else {
            this.getGyroData(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Function that get acceleration data
     * @param sensorEvent
     * @return
     */
    private void getAccelerationData(SensorEvent sensorEvent) {
        float alpha = 0.8f;
        float[] gravity = new float[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

        this.accX = sensorEvent.values[0] - gravity[0];
        this.accY = sensorEvent.values[1] - gravity[1];
        this.accZ = sensorEvent.values[2] - gravity[2];
    }

    /**
     * Function that gets gyro data
     *
     * @param sensorEvent
     * @return
     */
    private void getGyroData(SensorEvent sensorEvent) {
        // Axis of the rotation sample
        this.gyroX = sensorEvent.values[0];
        this.gyroY = sensorEvent.values[1];
        this.gyroZ = sensorEvent.values[2];
    }

    /**
     * Function that updates GPS information
     */
    public void updateGPS() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.fusedLocationClient.getLastLocation().addOnSuccessListener(this.activity, location -> {
                if (location != null) {
                    this.latitude = location.getLatitude();
                    this.longitude = location.getLongitude();
                    this.altitude = location.getAltitude();

                } else {
                    Log.d("LOC-NOT", "LOC NOT");
                }
            });
        } else {
            Log.d("PERM", "not");
        }
    }

    public String getData() {
        return "{"
            + "\"accelerometer\": {"
                + "\"x\": " + this.accX + ","
                + "\"y\": " + this.accY + ","
                + "\"z\": " + this.accZ
            + "},"

            + "\"gyro\": {"
                + "\"x\": " + this.gyroX + ","
                + "\"y\": " + this.gyroY + ","
                + "\"z\": " + this.gyroZ
            + "},"

            + "\"location\": {"
                + "\"latitude\": " + this.latitude + ","
                + "\"longitude\": " + this.longitude + ","
                + "\"altitude\": " + this.altitude
            + "}"
        + "}";
    }
}
