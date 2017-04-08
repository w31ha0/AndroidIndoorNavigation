package com.example.lew.indoornavigation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private TextView bearing_tv;
    private TextView steps_tv;
    private TextView pressure_tv;
    private Button button;
    private Switch save;

    private SensorManager sensorManager;
    private Context context;
    private int noOfSteps = 0;
    private int baseNoOfSteps = 0;

    private boolean isRunning = false;
    private final float sampling_period = 0.01f * 1000000000f;
    private final float steps_latency = 1f * 1000000000f;
    private final int MAX_SIZE_LIST = 5000;
    private final double dataSamplingPeriod = 0.01;
    private final double turnThreshold = 1.5;

    private long lastGyroSampling = 0;
    private long lastTimeSampling = 0;
    private long lastTimeSteps = 0;
    private float prevBearing = -1f;
    private float bearing = 0f;

    private ArrayList<Double> acc_magnitudes;
    private ArrayList<Double> gyros;
    private PrintWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        bearing_tv = (TextView) findViewById(R.id.bearing);
        pressure_tv = (TextView) findViewById(R.id.pressure);
        steps_tv = (TextView) findViewById(R.id.steps);
        save = (Switch) findViewById(R.id.save);

        acc_magnitudes = new ArrayList<>();
        gyros = new ArrayList<>();

        if (isRunning || !IsKitKatWithStepCounter(context.getPackageManager()))
            //status.setText("This phone does not have sensors");
            return;

        RegisterListeners();

        try {
            writer = new PrintWriter("records.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        });
    }

        @Override
        public void onSensorChanged (SensorEvent e){
            long currTime = e.timestamp;
            switch (e.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    if (currTime - lastGyroSampling > sampling_period) {
                        double gyro_x = e.values[0];
                        double gyro_y = e.values[1];
                        double gyro_z = e.values[2];
                        double gyro = Math.sqrt(gyro_x*gyro_x+gyro_y*gyro_y+gyro_z*gyro_z);
                        //System.out.println(gyro);
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
                    if (currTime - lastTimeSampling > sampling_period) {
                        float[] acceleration = e.values;
                        double acc_magnitude = Math.sqrt(acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2]);
                        if (writer != null)
                            writer.println(acc_magnitude);
                        acc_magnitudes.add(acc_magnitude);
                        if (gyros != null)
                            //System.out.println(computeAverageTurn(gyros.size()-1));
                        lastTimeSampling = e.timestamp;
                    }
                    if (currTime - lastTimeSteps > steps_latency) {
                        calculateSteps();
                        lastTimeSteps = e.timestamp;
                    }
                    break;
                default:
            }
        }

    private boolean checkForTurn(int index){
        int window = (int)(1.5 / dataSamplingPeriod);
        int half = (int)window/2;
        int lower = index - half;
        int upper = index + half;
        if (lower < 0)
            lower = 0;
        if (upper >= gyros.size())
            upper = gyros.size()-1;
        for (int i = lower; i <= upper;i++) {
            //System.out.println("Comparing "+gyros.get(i)+" for index "+i);
            if (gyros.get(i) > turnThreshold) {
                //System.out.println("ABRUPT");
                return true;
            }
        }
        return false;
    }

    private double computeAverageTurn(int index){
        int window = (int) (0.162 * (1 / dataSamplingPeriod));
        double sum = 0;
        int half = window/2;
        int lower = index - half;
        int upper = index + half;
        if (lower < 0)
            lower = 0;
        if (upper >= gyros.size())
            upper = gyros.size()-1;
        for (int i = lower; i <= upper;i++)
            sum += Math.abs(gyros.get(i));
        return sum/(upper-lower+1);
    }

    private double getMax(ArrayList<Double> list){
        double max = 0.0;
        int index = 0;
        for (double value:list){
            if (value > max) {
                max = value;
                index = list.indexOf(value);
            }
        }
        return max;
    }

    private void printMax(){
        for (double gyro : gyros){
            if (gyro > turnThreshold)
                System.out.println(gyro+" at "+gyros.indexOf(gyro));
        }
    }

    private void calculateSteps(){
        //printMax();
        double prevAcc = 0;
        boolean increasing = false;
        int windowSize = 0;
        int windowTolerance = 6;
        double decreaseTolerance = 0.2;
        double decreaseCounter = 0;
        int noOfPeaks = 0;
        ArrayList<Double> processed_values = processRawAcceleration();
        for (Double acc:processed_values){
            if (acc > prevAcc) {
                increasing = true;
                windowSize++;
                decreaseCounter = 0.0;
            }
            else {
                decreaseCounter += Math.abs(prevAcc-acc);
                if (increasing && decreaseCounter>decreaseTolerance && windowSize>windowTolerance) {
                    increasing = false;
                    int accIndex = processed_values.indexOf(acc);
                    double ratio = (double)gyros.size()/(double)acc_magnitudes.size();
                    int gyroIndex = (int)(accIndex*(ratio));
                    if ( !checkForTurn(gyroIndex)) {
                        noOfPeaks++;
                        //System.out.println("Step detected at "+accIndex);
                    }
                    windowSize = 0;
                }
            }
            prevAcc = acc;
        }
        noOfSteps = baseNoOfSteps + noOfPeaks;
        steps_tv.setText("Steps: "+noOfSteps);
        if (acc_magnitudes.size()>MAX_SIZE_LIST){
            baseNoOfSteps = noOfSteps;
            acc_magnitudes.clear();
            gyros.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Acurracy changed "+accuracy);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void RegisterListeners() {
        isRunning = true;
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
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnregisterListeners();
        isRunning = false;
    }

    public boolean IsKitKatWithStepCounter(PackageManager pm) {

        // Require at least Android KitKat
        int currentApiVersion = (int) Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);

    }

    private ArrayList<Double> processRawAcceleration() {
        int windowSize = (int) (0.162 * (1 / dataSamplingPeriod));
        int global_lower_limit = 0;
        int global_upper_limit = acc_magnitudes.size() - 1;
        ArrayList<Double> list_local_mean_acceleration = new ArrayList<>();
        ArrayList<Double> list_local_acc_variance = new ArrayList<>();

        for (int i = 0; i < acc_magnitudes.size(); i++) {
            double sum = 0.0;
            double local_mean_acceleration;
            int lower_limit = i - windowSize;
            if (lower_limit < global_lower_limit)
                lower_limit = global_lower_limit;
            int upper_limit = i + windowSize;
            if (upper_limit > global_upper_limit)
                upper_limit = global_upper_limit;
            for (int j = lower_limit; j <= upper_limit; j++)
                sum += acc_magnitudes.get(j);
            local_mean_acceleration = sum / (2.0 * (double) windowSize + 1.0);
            list_local_mean_acceleration.add(local_mean_acceleration);
        }
        for (int i = 0; i < acc_magnitudes.size(); i++) {
            double sum = 0;
            double local_acc_variance;
            int lower_limit = i - windowSize;
            if (lower_limit < global_lower_limit)
                lower_limit = global_lower_limit;
            int upper_limit = i + windowSize;
            if (upper_limit > global_upper_limit)
                upper_limit = global_upper_limit;
            for (int j = lower_limit; j <= upper_limit; j++) {
                double diff = acc_magnitudes.get(j) - list_local_mean_acceleration.get(j);
                diff *= diff;
                sum += diff;
            }
            local_acc_variance = sum / (2.0 * (double) windowSize + 1.0);
            local_acc_variance = Math.sqrt(local_acc_variance);
            list_local_acc_variance.add(local_acc_variance);
        }
        saveDataToExcel(list_local_acc_variance,"File.txt");
        return list_local_acc_variance;
    }

    private void saveDataToExcel(ArrayList<Double> list,String filename){
        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+filename));
            for (int i=0;i<list.size();i++){
                    fos.write(String.valueOf(list.get(i))+"\t");
                    fos.write("\r\n");
                }
            fos.close();
            System.out.println("File saved successfully in "+ Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+filename);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e);
        }
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
