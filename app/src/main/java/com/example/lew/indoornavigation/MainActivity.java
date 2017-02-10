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
    private TextView step_detector;
    private TextView status;

    private int stepCounter = 0;
    private int stepDetector = 0;

    private SensorManager sensorManager;
    private Sensor sensor;
    private Context context;

    private boolean isRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();

        step_detector = (TextView)findViewById(R.id.step_detector);
        status = (TextView)findViewById(R.id.status);

        if (isRunning || !IsKitKatWithStepCounter(context.getPackageManager()))
            //status.setText("This phone does not have sensors");
            return;


        RegisterListeners(Sensor.TYPE_STEP_DETECTOR);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        status.setText("Step Detected!");
        switch (e.sensor.getType()) {
            case Sensor.TYPE_STEP_DETECTOR:
                stepDetector++;
                step_detector.setText(stepDetector);
                System.out.println("New Step detected.Total number of steps:"+stepCounter);
                step_detector.setText(String.valueOf(step_detector));
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
        sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
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
        UnregisterListeners ();
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
