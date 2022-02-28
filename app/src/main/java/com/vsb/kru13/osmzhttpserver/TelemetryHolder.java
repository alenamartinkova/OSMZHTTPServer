package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TelemetryHolder implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private Sensor acceleration;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    TelemetryHolder(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        this.acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, this.acceleration, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == this.acceleration) {
            float alpha = 0.8f;
            double[] gravity = new double[3];

            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            double[] linearAcceleration = new double[3];
            linearAcceleration[0] = sensorEvent.values[0] - gravity[0];
            linearAcceleration[1] = sensorEvent.values[1] - gravity[1];
            linearAcceleration[2] = sensorEvent.values[2] - gravity[2];
            
            Log.d("SENSORS-ACC-0", String.valueOf(linearAcceleration[0]));
            Log.d("SENSORS-ACC-1", String.valueOf(linearAcceleration[1]));
            Log.d("SENSORS-ACC-2", String.valueOf(linearAcceleration[2]));
        } else {
            float x = sensorEvent.values[0];
            Log.d("SENSORs-GYRO", String.valueOf(x));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
