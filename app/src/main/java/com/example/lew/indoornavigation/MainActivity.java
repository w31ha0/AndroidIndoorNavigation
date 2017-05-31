package com.example.lew.indoornavigation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;

    private long lastTimeGyro = 0;
    private long lastTimeAccelerometer = 0;
    private long lastTimeProcessing = 0;
    private long lastTimeMagnetic = 0;
    private float basePressure = 0f;
    private String UUID;
    private int floor;
    private WifiManager wifiManager;

    private float[] currentAcceleration;
    private float[] currentMagnetic;
    private float[] currentGyro;
    private boolean haveMagneticData = false;
    private boolean stoppedPitching = true;
    private ArrayList<Float> bases;

    private ArrayList<Double> list_acc_magnitudes;
    private ArrayList<Double> list_gyros;
    private ArrayList<Float> list_bearings;
    private ArrayList<Integer> list_rssi;

    public static float bearing = 0f;
    public static float prevBearing = -1f;
    public static float pressure;
    public static float base;
    public static int rssi;
    public static String mac;
    private float savedHeight;
    private FloorMapView mapView;
    private IndoorMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        UUID = getIntent().getExtras().getString("Map");
        savedHeight = getIntent().getExtras().getFloat("height");
        System.out.println("Using height of "+savedHeight);
        System.out.println("Bundle is "+getIntent().getExtras().getInt("Floor"));
        this.floor = getIntent().getExtras().getInt("Floor");
        map = BluetoothDatabase.getMapFromUUIDAndFloor(UUID,this.floor);
        System.out.println("Getting floor "+this.floor);
        mapView = new FloorMapView(this,map.getAllWalls(),map.getWaps(),map.getWIDTH_MAP(),map.getHEIGHT_MAP(),map.getBasePositionX(),map.getBasePositionY(),map.getIconSizecm(),map.getPixelTocm(),map.getDrawable());
        setContentView(mapView);
        Singleton.getInstance().setMapLoaded(true);
        MainActivity.bearing = map.getInitialBearing();


        list_acc_magnitudes = new ArrayList<>();
        list_bearings = new ArrayList<>();
        list_gyros = new ArrayList<>();
        bases = new ArrayList<>();
        list_rssi = new ArrayList<>();

        currentGyro = new float[3];
        currentMagnetic = new float[3];
        currentAcceleration = new float[3];

        RegisterListeners();

        //verifyStoragePermissions(this);
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
                    if (Math.abs(e.values[0]) < 100)
                        return;
                    if (bases.size() < Constants.PRESSURE_LIST_SIZE) {
                        bases.add(e.values[0]);
                        return;
                    }
                    pressure = Math.abs(e.values[0]);
                    base = basePressure;
                    float averagePressure = Utils.getAverage(bases);
                    //System.out.println("Difference is "+(pressure - averagePressure)+" as base is "+averagePressure+" and current is "+pressure);
                    if ( pressure - averagePressure > Constants.FLOOR_PRESSURE_DIFFERENCE){
                        if (BluetoothDatabase.getMapFromUUIDAndFloor(UUID,this.floor-1) == null)
                            return;
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putExtra("Map", UUID);
                        intent.putExtra("Floor",this.floor-1);
                        intent.putExtra("height", savedHeight);
                        startActivity(intent);
                        bases.clear();
                        finish();
                    }
                    else if ( averagePressure - pressure > Constants.FLOOR_PRESSURE_DIFFERENCE ){
                        if (BluetoothDatabase.getMapFromUUIDAndFloor(UUID,this.floor+1) == null)
                            return;
                        System.out.println("Transiting to next level");
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putExtra("Map", UUID);
                        intent.putExtra("Floor",this.floor+1);
                        intent.putExtra("height", savedHeight);
                        startActivity(intent);
                        bases.clear();
                        finish();
                    }
                    //pressure_tv.setText("Pressure: " + String.valueOf(e.values[0]));
                case Sensor.TYPE_ACCELEROMETER:
                    if (currTime - lastTimeAccelerometer > Constants.DATA_SAMPLING_PERIOD) {
                        float[] acceleration = e.values;
                        //System.out.println(acceleration[0]+","+acceleration[1]+","+acceleration[2]);
                        double acc_magnitude = Math.sqrt(acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2]);;
                        if (acc_magnitude < 800 && acc_magnitude > 1) {
                            int rssi = wifiManager.getConnectionInfo().getRssi();
                            MainActivity.rssi = rssi;
                            list_rssi.add(rssi);
                            list_acc_magnitudes.add(acc_magnitude);
                            list_bearings.add(bearing);
                            this.currentAcceleration = acceleration;
                        }
                        lastTimeAccelerometer = e.timestamp;
                    }
                    if (currTime - lastTimeProcessing > Constants.DATA_PROCESSING_PERIOD) {
                        double[] initialPos = new double[2];
                        initialPos[0] = mapView.getBasePositionX();
                        initialPos[1] = mapView.getBasePositionY();
                        System.out.println("Computing position");
                        double[] currentPos = DataProcessing.computeCurrentPosition(savedHeight,mapView.getAllWalls(),initialPos, list_gyros, list_acc_magnitudes, list_bearings,list_rssi,map.getWaps());

                        if (currentPos[2] == 1) {
                            if ( (bearing <= 90 && bearing >= 0) || (bearing <= 360 && bearing >= 270)) {
                                if (Math.abs(bearing - 0) <= Constants.BEARING_ADJUSTMENT_TOLERANCE)
                                    bearing = 0;
                            }
                            else if (bearing <= 270 && bearing >= 90 ){
                                if (Math.abs(bearing - 180) <= Constants.BEARING_ADJUSTMENT_TOLERANCE)
                                    bearing = 180;
                            }
                        }
                        else if (currentPos[2] == 0){
                            if ( bearing <= 180 && bearing >= 0) {
                                if (Math.abs(bearing - 90) <= Constants.BEARING_ADJUSTMENT_TOLERANCE)
                                    bearing = 90;
                            }
                            else if (bearing >= 180 && bearing <= 360){
                                if (Math.abs(bearing - 270) <= Constants.BEARING_ADJUSTMENT_TOLERANCE)
                                    bearing = 270;
                            }
                        }
                        if (bearing%90 == 0)
                            System.out.println("Bearing corrected to "+bearing);
                        //System.out.println("Determined final position to be at "+currentPos[0]+","+currentPos[1]);
                        mapView.setCurrentPos(currentPos);
                        if (list_acc_magnitudes.size() > Constants.MAX_SIZE_LIST ){
                            System.out.println("Clearing lists");
                            mapView.setBasePositionX((int) currentPos[0]);
                            mapView.setBasePositionY((int) currentPos[1]);
                            list_acc_magnitudes.clear();
                            list_gyros.clear();
                            list_bearings.clear();
                            list_rssi.clear();
                        }

                        lastTimeProcessing = e.timestamp;
                    }
                    break;
                case Sensor.TYPE_ORIENTATION:
                    float yaw = e.values[0];
                    if (prevBearing == -1f)
                        prevBearing = yaw;
                    else {
                        float change = yaw - prevBearing;
                        prevBearing = yaw;
                        bearing = (bearing + change);
                        if (bearing < 0)
                            bearing += 360;
                        else if (bearing > 360)
                            bearing -= 360;
                    }
                case Sensor.TYPE_MAGNETIC_FIELD:
                    /*
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
                    bearing = (bearing + change);
                    if (bearing < 0)
                        bearing += 360;
                    else if (bearing > 360)
                        bearing -= 360;
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
        DataProcessing.saveDataToExcel(list_rssi);
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

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }

}
