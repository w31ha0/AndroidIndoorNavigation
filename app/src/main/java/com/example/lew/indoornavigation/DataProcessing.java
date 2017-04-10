package com.example.lew.indoornavigation;

import android.os.Environment;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by Lew on 10/4/2017.
 */
public class DataProcessing {
    private static final int WINDOW_TOLERANCE = 6;
    private static final double DECREASE_TOLERANCE = 0.2;

    private static final int WINDOW_TURNS = (int)(1.5 / Constants.DATA_SAMPLING_PERIOD_SEC);
    private static final int WINDOW_PITCH = (int)(1.5 / Constants.DATA_SAMPLING_PERIOD_SEC);
    private static final double TURN_THRESHOLD = 1.5;
    private static final double PITCH_THRESHOLD = 0.9;

    private static final String DATA_FILE_NAME = "File.txt";

    public static int calculateSteps(int baseNoOfSteps,ArrayList<Double> gyros,ArrayList<Double> acc_magnitudes){
        double prevAcc = 0;
        boolean increasing = false;
        int windowSize = 0;
        double decreaseCounter = 0;
        int noOfPeaks = 0;
        ArrayList<Double> processed_values = processRawAcceleration(acc_magnitudes);
        for (Double acc:processed_values){
            if (acc > prevAcc) {
                increasing = true;
                windowSize++;
                decreaseCounter = 0.0;
            }
            else {
                decreaseCounter += Math.abs(prevAcc-acc);
                if (increasing && decreaseCounter>DECREASE_TOLERANCE && windowSize>WINDOW_TOLERANCE) {
                    increasing = false;
                    int accIndex = processed_values.indexOf(acc);
                    double ratio = (double)gyros.size()/(double)acc_magnitudes.size();
                    int gyroIndex = (int)(accIndex*(ratio));
                    if ( !checkForTurn(gyroIndex,gyros)) {
                        noOfPeaks++;
                        //System.out.println("Step detected at "+accIndex);
                    }
                    windowSize = 0;
                }
            }
            prevAcc = acc;
        }
        int noOfSteps = baseNoOfSteps + noOfPeaks;
        if (acc_magnitudes.size()> Constants.MAX_SIZE_LIST){
            acc_magnitudes.clear();
            gyros.clear();
        }
        return noOfSteps;
    }

    private static ArrayList<Double> processRawAcceleration(ArrayList<Double> acc_magnitudes) {
        int windowSize = (int) (0.162 * (1 / Constants.DATA_SAMPLING_PERIOD_SEC));
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
        //saveDataToExcel(list_local_acc_variance);
        return list_local_acc_variance;
    }

    public static boolean checkForPitch(ArrayList<Double> gyros){
        int lower = gyros.size()-WINDOW_PITCH;
        int upper = gyros.size()-1;
        if (lower < 0)
            lower = 0;
        for (int i = lower; i <= upper;i++) {
            if (gyros.get(i) > PITCH_THRESHOLD)
                return true;
        }
        return false;
    }

    private static boolean checkForTurn(int index,ArrayList<Double> gyros){
        int half = WINDOW_TURNS/2;
        int lower = index - half;
        int upper = index + half;
        if (lower < 0)
            lower = 0;
        if (upper >= gyros.size())
            upper = gyros.size()-1;
        for (int i = lower; i <= upper;i++) {
            if (gyros.get(i) > TURN_THRESHOLD)
                return true;
        }
        return false;
    }

    private static void saveDataToExcel(ArrayList<Double> list){
        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+DATA_FILE_NAME));
            for (int i=0;i<list.size();i++){
                fos.write(String.valueOf(list.get(i))+"\t");
                fos.write("\r\n");
            }
            fos.close();
            System.out.println("File saved successfully in "+ Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DATA_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
