package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

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

    TelemetryHolder(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        this.acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.acceleration, SensorManager.SENSOR_DELAY_NORMAL);
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

    public JSONArray getAccelerationData(SensorEvent sensorEvent) throws JSONException {
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

    public JSONArray getGyroData(SensorEvent sensorEvent) throws JSONException {
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

    public void writeData(JSONArray array, String name) throws JSONException, IOException {
        JSONObject jsonObject = this.readFileAndReturnJSON();

        Log.d("JSON-name", name);
        Log.d("JSON-bef", jsonObject.toString());
        jsonObject.put(name, array);

        Log.d("JSON-af", jsonObject.toString());
        // Convert JsonObject to String Format
        String userString = jsonObject.toString();
        // Define the File Path and its Name
        File file = new File(this.context.getFilesDir(), "sensorData");
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    public JSONObject readFileAndReturnJSON() throws IOException {
        File file = new File(context.getFilesDir(),"sensorData");
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

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException err){
            Log.d("Error", err.toString());
        }

        return jsonObject;
    }
}
