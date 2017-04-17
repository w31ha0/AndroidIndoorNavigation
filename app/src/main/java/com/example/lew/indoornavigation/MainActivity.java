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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;

    private long lastTimeGyro = 0;
    private long lastTimeAccelerometer = 0;
    private long lastTimeProcessing = 0;
    private long lastTimeMagnetic = 0;

    private double[] baseCurrentPosition;

    private float[] currentAcceleration;
    private float[] currentMagnetic;
    private float[] currentGyro;
    private boolean haveMagneticData = false;
    private boolean stoppedPitching = true;

    private ArrayList<Double> list_acc_magnitudes;
    private ArrayList<Double> list_gyros;
    private ArrayList<Float> list_bearings;

    public static double[] currentPos;
    public static float bearing = 0f;
    public static float prevBearing = -1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FloorMapView(this));

        list_acc_magnitudes = new ArrayList<>();
        list_bearings = new ArrayList<>();
        list_gyros = new ArrayList<>();

        currentGyro = new float[3];
        currentMagnetic = new float[3];
        currentAcceleration = new float[3];

        baseCurrentPosition = new double[2];
        baseCurrentPosition[0] = 0.0;
        baseCurrentPosition[1] = 0.0;
        currentPos = new double[2];
        currentPos[0] = 0.0;
        currentPos[1] = 0.0;

        RegisterListeners();
    }

        @Override
        public void onSensorChanged (SensorEvent e){
            long currTime = e.timestamp;
            switch (e.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    if (currTime - lastTimeGyro > Constants.DATA_SAMPLING_PERIOD) {
                        currentGyro = e.values;
                        double gyro_magnitude = Math.sqrt(currentGyro[0]* currentGyro[0]+ currentGyro[1]* currentGyro[1]+ currentGyro[2]* currentGyro[2]);
                        list_gyros.add(gyro_magnitude);
                        lastTimeGyro = e.timestamp;
                    }
                case Sensor.TYPE_PRESSURE:
                    //pressure_tv.setText("Pressure: " + String.valueOf(e.values[0]));
                case Sensor.TYPE_ACCELEROMETER:
                    if (currTime - lastTimeAccelerometer > Constants.DATA_SAMPLING_PERIOD) {
                        float[] acceleration = e.values;
                        double acc_magnitude = Math.sqrt(acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2]);;
                        if (acc_magnitude < 1000 && acc_magnitude > 1) {
                            list_acc_magnitudes.add(acc_magnitude);
                            list_bearings.add(bearing);
                            this.currentAcceleration = acceleration;
                        }
                        lastTimeAccelerometer = e.timestamp;
                    }
                    if (currTime - lastTimeProcessing > Constants.DATA_PROCESSING_PERIOD) {
                        currentPos = DataProcessing.computeCurrentPosition(baseCurrentPosition, list_gyros, list_acc_magnitudes, list_bearings);
                        if (list_acc_magnitudes.size() > Constants.MAX_SIZE_LIST){
                            baseCurrentPosition = currentPos;
                            list_acc_magnitudes.clear();
                            list_gyros.clear();
                        }

                        lastTimeProcessing = e.timestamp;
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    if (currTime - lastTimeMagnetic > Constants.DATA_SAMPLING_PERIOD) {
                        currentMagnetic = e.values;
                        haveMagneticData = true;
                    }
                    break;
                default:
                    break;
            }
                float[] R1 = new float[9];
                float[] orientation = new float[3];

                SensorManager.getRotationMatrix(R1, null, currentAcceleration, currentMagnetic);
                SensorManager.getOrientation(R1, orientation);
                float yaw = (float)(Math.toDegrees(orientation[0])+360)%360;
                float pitch = (float)(Math.toDegrees(orientation[1])+360)%360;
                float roll = (float)(Math.toDegrees(orientation[2])+360)%360;
            if (haveMagneticData){
                if (prevBearing == -1f)
                    prevBearing = yaw;
                else {
                    float change = yaw - prevBearing;
                    prevBearing = yaw;
                    bearing += change;
                }
                lastTimeMagnetic = currTime;
                haveMagneticData = false;
                /*if (DataProcessing.checkForPitch(list_gyros)) {
                    if (stoppedPitching == true) {
                        stoppedPitching = false;
                    }
                    if (prevBearing == -1f)
                        prevBearing = yaw;
                    else {
                        float change = yaw - prevBearing;
                        prevBearing = yaw;
                        bearing += change;
                        //System.out.println("BEARING "+bearing);
                    }


                    lastTimeMagnetic = currTime;
                    haveMagneticData = false;
                }else
                    stoppedPitching = true;*/
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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 48, SensorManager.SENSOR_DELAY_FASTEST);
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
