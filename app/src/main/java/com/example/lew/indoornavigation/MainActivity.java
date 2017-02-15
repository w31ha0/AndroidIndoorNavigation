package com.example.lew.indoornavigation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private TextView acc_x;
    private TextView acc_y;
    private TextView acc_z;
    private TextView status;
    private TextView steps;
    private Switch switch1;

    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Context context;
    private int noOfSteps = 0;

    private boolean isRunning = false;
    private boolean isRecording = false;
    private final float sampling_period = 0.1f * 1000000000f;
    private final float acc_sensitivity = 1.5f;
    private long lastTime = 0;
    private float prevAcc = 0f;

    private ArrayList<String> accelerations_x;
    private ArrayList<String> accelerations_y;
    private ArrayList<String> accelerations_z;


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
        switch1 = (Switch)findViewById(R.id.switch1);

        accelerations_x = new ArrayList<String>();
        accelerations_y = new ArrayList<String>();
        accelerations_z = new ArrayList<String>();


        if (isRunning || !IsKitKatWithStepCounter(context.getPackageManager()))
            //status.setText("This phone does not have sensors");
            return;

        RegisterListeners(Sensor.TYPE_STEP_COUNTER);
        checkWritingPermission();

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isRecording = true;
                    accelerations_x = new ArrayList<String>();
                    accelerations_y = new ArrayList<String>();
                    accelerations_z = new ArrayList<String>();
                }
                else{
                    isRecording = false;
                    try {
                        saveDataToExcel(accelerations_x,accelerations_y,accelerations_z);
                    } catch (IOException e) {
                        e.printStackTrace();
                        status.setText("Problem saving data");
                    }
                }
                status.setText("Is Recording: "+String.valueOf(isRecording));
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        long currTime = e.timestamp;
        switch (e.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float[] acceleration = e.values;
                acc_x.setText("X:"+String.valueOf(acceleration[0]));
                acc_y.setText("Y:"+String.valueOf(acceleration[1]));
                acc_z.setText("Z:"+String.valueOf(acceleration[2]));

                if (currTime - lastTime > sampling_period){
                    float acc_diff = acceleration[2] - prevAcc ;
                    if (isRecording) {
                        System.out.println("X:"+String.valueOf(acceleration[0])+",Y:"+String.valueOf(acceleration[1])+",Z:"+String.valueOf(acceleration[2]));
                        accelerations_x.add(String.valueOf(acceleration[0]));
                        accelerations_y.add(String.valueOf(acceleration[1]));
                        accelerations_z.add(String.valueOf(acceleration[2]));
                    }
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void saveDataToExcel(ArrayList<String> acc_x, ArrayList<String> acc_y, ArrayList<String> acc_z)throws IOException{

        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+"File.txt"));
            for (int i=0;i<acc_x.size();i++){
                fos.write(acc_x.get(i)+"\t"+acc_x.get(i)+"\t"+acc_x.get(i));
                fos.write("\r\n");
            }
            fos.close();
            System.out.println("File saved successfully in "+Environment.getExternalStorageDirectory().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
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

    private void checkWritingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // permission wasn't granted
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }
    }

}
