package com.example.lew.indoornavigation;

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

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private TextView acc_x;
    private TextView acc_y;
    private TextView acc_z;
    private TextView status;
    private TextView steps;

    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Context context;
    private int noOfSteps = 0;

    private boolean isRunning = false;
    private final float sampling_period = 300000000f;
    private final float acc_sensitivity = 1.5f;
    private long lastTime = 0;
    private float prevAcc = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();

        acc_x = (TextView)findViewById(R.id.acc_x);
        acc_y = (TextView)findViewById(R.id.acc_y);
        acc_z = (TextView)findViewById(R.id.acc_z);
        status = (TextView)findViewById(R.id.status);
        steps = (TextView)findViewById(R.id.steps);

        if (isRunning || !IsKitKatWithStepCounter(context.getPackageManager()))
            //status.setText("This phone does not have sensors");
            return;


        RegisterListeners(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        long currTime = e.timestamp;
        switch (e.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.out.println("Current is "+currTime);
                System.out.println("Last is "+lastTime);
                System.out.println("Diff is "+(currTime - lastTime));

                float[] acceleration = e.values;
                acc_x.setText("X:"+String.valueOf(acceleration[0]));
                acc_y.setText("Y:"+String.valueOf(acceleration[1]));
                acc_z.setText("Z:"+String.valueOf(acceleration[2]));

                if (currTime - lastTime > sampling_period){
                    float acc_diff = acceleration[2] - prevAcc ;
                    status.setText("Difference is "+acc_diff);
                    if (acc_diff > acc_sensitivity) {
                        noOfSteps++;
                        steps.setText(String.valueOf(noOfSteps));
                    }
                    prevAcc = acceleration[2];
                    lastTime = e.timestamp;
                }
                break;
            default:
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Acurracy changed "+accuracy);
    }


    private void RegisterListeners(int sensorType) {
        isRunning = true;
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorAccel = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        boolean accelSupported = sensorManager.registerListener(this, sensorAccel,
                SensorManager.SENSOR_DELAY_FASTEST);
        System.out.println("Sensor Manager Resgistered");
        status.setText("Sensors registered");
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
        status.setText(currentApiVersion+","+pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)+","+pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR));
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);

    }

}
