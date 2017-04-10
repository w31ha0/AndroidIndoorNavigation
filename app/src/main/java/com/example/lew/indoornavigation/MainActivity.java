package com.example.lew.indoornavigation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private TextView bearing_tv;
    private TextView steps_tv;
    private TextView pressure_tv;

    private SensorManager sensorManager;

    private long lastGyroSampling = 0;
    private long lastTimeSampling = 0;
    private long lastTimeSteps = 0;
    private float prevBearing = -1f;
    private float bearing = 0f;
    private int baseNoOfSteps = 0;

    private ArrayList<Double> acc_magnitudes;
    private ArrayList<Double> gyros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bearing_tv = (TextView) findViewById(R.id.bearing);
        pressure_tv = (TextView) findViewById(R.id.pressure);
        steps_tv = (TextView) findViewById(R.id.steps);

        acc_magnitudes = new ArrayList<>();
        gyros = new ArrayList<>();

        RegisterListeners();
    }

        @Override
        public void onSensorChanged (SensorEvent e){
            long currTime = e.timestamp;
            switch (e.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    if (currTime - lastGyroSampling > Constants.DATA_SAMPLING_PERIOD) {
                        double gyro_x = e.values[0];
                        double gyro_y = e.values[1];
                        double gyro_z = e.values[2];
                        double gyro = Math.sqrt(gyro_x*gyro_x+gyro_y*gyro_y+gyro_z*gyro_z);
                        gyros.add(gyro);
                        lastGyroSampling = e.timestamp;
                    }
                case Sensor.TYPE_PRESSURE:
                    //pressure_tv.setText("Pressure: " + String.valueOf(e.values[0]));
                case Sensor.TYPE_ORIENTATION:
                    float degree = Math.round(e.values[0]);
                    if (prevBearing == -1f)
                        prevBearing = degree;
                    else {
                        float change = degree - prevBearing;
                        prevBearing = degree;
                        bearing += change;
                        //bearing_tv.setText("Bearing: " + String.valueOf(bearing));
                    }
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    if (currTime - lastTimeSampling > Constants.DATA_SAMPLING_PERIOD) {
                        float[] acceleration = e.values;
                        double acc_magnitude = Math.sqrt(acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2]);;
                        acc_magnitudes.add(acc_magnitude);
                        lastTimeSampling = e.timestamp;
                    }
                    if (currTime - lastTimeSteps > Constants.DATA_PROCESSING_PERIOD) {
                        int noOfSteps = DataProcessing.calculateSteps(baseNoOfSteps,gyros,acc_magnitudes);
                        steps_tv.setText("Steps: "+noOfSteps);
                        if (acc_magnitudes.size() > Constants.MAX_SIZE_LIST){
                            baseNoOfSteps = noOfSteps;
                            acc_magnitudes.clear();
                            gyros.clear();
                        }

                        lastTimeSteps = e.timestamp;
                    }
                    break;
                default:
            }
        }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Acurracy changed "+accuracy);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void RegisterListeners() {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 48, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 48, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 48, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 48, SensorManager.SENSOR_DELAY_FASTEST);
        System.out.println("Sensor Manager Resgistered");
    }


    void UnregisterListeners() {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        System.out.println("Sensor listener unregistered.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnregisterListeners();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
            } else {
                // permission wasn't granted
            }
        }
    }

}
