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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TelemetryHolder implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private Sensor acceleration;
    private Context context;
    private Activity activity;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    TelemetryHolder(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        this.acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.acceleration, SensorManager.SENSOR_DELAY_NORMAL);

        // Location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        JSONArray data;
        if (sensorEvent.sensor == this.acceleration) {
            try {
                data = this.getAccelerationData(sensorEvent);
                this.writeData(data, "acc");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                data = this.getGyroData(sensorEvent);
                this.writeData(data, "gyro");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Function that get acceleration data
     * @param sensorEvent
     * @return
     * @throws JSONException
     */
    private JSONArray getAccelerationData(SensorEvent sensorEvent) throws JSONException {
        double alpha = 0.8;
        double[] gravity = new double[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

        JSONArray array = new JSONArray();
        array.put(sensorEvent.values[0] - gravity[0]);
        array.put(sensorEvent.values[1] - gravity[1]);
        array.put(sensorEvent.values[2] - gravity[2]);

        //Log.d("SENSORS-ACC-0", String.valueOf(array.get(0)));
        //Log.d("SENSORS-ACC-1", String.valueOf(array.get(1)));
        //Log.d("SENSORS-ACC-2", String.valueOf(array.get(2)));

        return array;
    }

    /**
     * Function that gets gyro data
     *
     * @param sensorEvent
     * @return
     * @throws JSONException
     */
    private JSONArray getGyroData(SensorEvent sensorEvent) throws JSONException {
        // Axis of the rotation sample
        JSONArray array = new JSONArray();
        array.put(sensorEvent.values[0]);
        array.put(sensorEvent.values[1]);
        array.put(sensorEvent.values[2]);

        //Log.d("SENSORS-GYRO-X", String.valueOf(array.get(0)));
        //Log.d("SENSORS-GYRO-Y", String.valueOf(array.get(1)));
        //Log.d("SENSORS-GYRO-Z", String.valueOf(array.get(2)));

        return array;
    }

    /**
     * Function that writes data to JSON file
     *
     * @param array
     * @param name
     * @throws JSONException
     * @throws IOException
     */
    private void writeData(JSONArray array, String name) throws JSONException, IOException {
        JSONObject jsonObject = this.readFileAndReturnJSON();

        Log.d("JSON-name", name);
        //Log.d("JSON-bef", jsonObject.toString());
        jsonObject.put(name, array);

        //Log.d("JSON-af", jsonObject.toString());
        // Convert JsonObject to String Format
        String userString = jsonObject.toString();
        // Define the File Path and its Name
        File file = new File(this.context.getFilesDir(), "sensorData");
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    /**
     * Function that reads and returns JSON file
     *
     * @return
     * @throws IOException
     */
    private JSONObject readFileAndReturnJSON() throws IOException {
        File file = new File(context.getFilesDir(),"sensorData");
        JSONObject jsonObject = new JSONObject();

        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            String response = stringBuilder.toString();

            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }

        return jsonObject;
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
                        this.writeData(array, "location");
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
