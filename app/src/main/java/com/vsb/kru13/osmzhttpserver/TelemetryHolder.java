package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;

public class TelemetryHolder extends LocationAndSensor implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private Sensor acceleration;
    private Context context;

    TelemetryHolder(Activity activity) {
        this.context = activity.getApplicationContext();
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
                this.writeData(data, "acc", this.context);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                data = this.getGyroData(sensorEvent);
                this.writeData(data, "gyro", this.context);
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
}
