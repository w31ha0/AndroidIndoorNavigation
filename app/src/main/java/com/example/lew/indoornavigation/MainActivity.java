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

    private long lastTimeGyro = 0;
    private long lastTimeAccelerometer = 0;
    private long lastTimeProcessing = 0;
    private long lastTimeMagnetic = 0;

    private float prevBearing = -1f;
    private float bearing = 0f;
    private int baseNoOfSteps = 0;

    private float[] acceleration;
    private float[] magnetic;
    private float[] gyro;
    private boolean haveMagneticData = false;
    private boolean stoppedPitching = true;
    private double offsetPitchBegan = 0;

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
        gyro = new float[3];
        magnetic = new float[3];
        acceleration = new float[3];

        RegisterListeners();
    }

        @Override
        public void onSensorChanged (SensorEvent e){
            long currTime = e.timestamp;
            switch (e.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    if (currTime - lastTimeGyro > Constants.DATA_SAMPLING_PERIOD) {
                        gyro = e.values;
                        double gyro_magnitude = Math.sqrt(gyro[0]*gyro[0]+gyro[1]*gyro[1]+gyro[2]*gyro[2]);
                        gyros.add(gyro_magnitude);
                        lastTimeGyro = e.timestamp;
                    }
                case Sensor.TYPE_PRESSURE:
                    //pressure_tv.setText("Pressure: " + String.valueOf(e.values[0]));
                case Sensor.TYPE_ACCELEROMETER:
                    if (currTime - lastTimeAccelerometer > Constants.DATA_SAMPLING_PERIOD) {
                        acceleration = e.values;
                        double acc_magnitude = Math.sqrt(acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2]);;
                        acc_magnitudes.add(acc_magnitude);
                        lastTimeAccelerometer = e.timestamp;
                    }
                    if (currTime - lastTimeProcessing > Constants.DATA_PROCESSING_PERIOD) {
                        int noOfSteps = DataProcessing.calculateSteps(baseNoOfSteps,gyros,acc_magnitudes);
                        steps_tv.setText("Steps: "+noOfSteps);
                        if (acc_magnitudes.size() > Constants.MAX_SIZE_LIST){
                            baseNoOfSteps = noOfSteps;
                            acc_magnitudes.clear();
                            gyros.clear();
                        }

                        lastTimeProcessing = e.timestamp;
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    if (currTime - lastTimeMagnetic > Constants.DATA_SAMPLING_PERIOD) {
                        magnetic = e.values;
                        haveMagneticData = true;
                    }
                    break;
                default:
                    break;
            }
                float[] R1 = new float[9];
                float[] orientation = new float[3];

                SensorManager.getRotationMatrix(R1, null, acceleration, magnetic);
                SensorManager.getOrientation(R1, orientation);
                float yaw = (float)(Math.toDegrees(orientation[0])+360)%360;
                float pitch = (float)(Math.toDegrees(orientation[1])+360)%360;
                float roll = (float)(Math.toDegrees(orientation[2])+360)%360;
            //System.out.println(stoppedPitching);
            if (haveMagneticData){
                if (DataProcessing.checkForPitch(gyros)) {
                    if (stoppedPitching == true) {
                        stoppedPitching = false;
                        offsetPitchBegan = yaw - bearing;
                        System.out.println("YAW: "+yaw+",BEARING "+bearing);
                    }
                    if (prevBearing == -1f)
                        prevBearing = yaw;
                    else {
                        float change = yaw - prevBearing;
                        prevBearing = yaw;
                        bearing += change;
                        bearing_tv.setText("Bearing: " + String.valueOf(bearing));
                    }

                    lastTimeMagnetic = currTime;
                    haveMagneticData = false;
                }else
                    stoppedPitching = true;
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
