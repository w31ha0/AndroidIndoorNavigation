package com.example.lew.indoornavigation;

import android.os.Environment;

import org.apache.poi.hpsf.Util;

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
    private static final int WINDOW_PITCH = (int)(1 / Constants.DATA_SAMPLING_PERIOD_SEC);
    private static final double TURN_THRESHOLD = 1.5;
    private static final double PITCH_THRESHOLD = 1.5;

    private static final String DATA_FILE_NAME = "File.txt";

    public static double[] computeCurrentPosition(float savedHeight,int[][] walls,double[] initialPos, ArrayList<Double> gyros, ArrayList<Double> acc_magnitudes, ArrayList<Float> bearings){
        double prevAcc = 0;
        boolean increasing = false;
        int windowSize = 0;
        double decreaseCounter = 0;
        int noOfPeaks = 0;
        boolean first = true;
        double pos_x = initialPos[0];
        double pos_y = initialPos[1];
        double stride_length = savedHeight * 0.3;

        ArrayList<Double> processed_values = processRawAcceleration(acc_magnitudes);
        int index = -1;
        for (Double acc:processed_values){
            index++;
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
                        if (first){
                            first = false;
                            continue;
                        }
                        noOfPeaks++;
                        double bearing = bearings.get(index);
                        double raw_x = pos_x + Math.sin(Math.toRadians(bearing))*stride_length;
                        double raw_y = pos_y + Math.cos(Math.toRadians(bearing))*stride_length;
                        //System.out.println("Getting final destination from "+pos_x+","+pos_y+" to "+raw_x+","+raw_y+" at an angle of "+bearing+" with index "+index+" with list size "+acc_magnitudes.size());
                        double[] result = getFinalDestination(walls,pos_x,pos_y,raw_x,raw_y);
                        pos_x = result[0];
                        pos_y = result[1];

                        //System.out.println("Step detected at "+accIndex);
                    }
                    windowSize = 0;
                }
            }
            prevAcc = acc;
        }

        double[] pos =  new double[2];
        pos[0] = pos_x;
        pos[1] = pos_y;
        return pos;
    }

    public static double[] getFinalDestination(int[][] walls, double x1, double y1, double x2, double y2){
        double[] finalPosition = new double[2];
        double  lowestDistance = 1000;
        for(int i=0;i<walls.length;i++){
            int[] pt1 = { walls[i][0],walls[i][1]};
            int[] pt2 = { walls[(i+1)%walls.length][0],walls[(i+1)%walls.length][1] };
            double[] result = getIntersection(pt1[0],pt1[1],pt2[0],pt2[1],x1,y1,x2,y2);
            //System.out.println("Possible intersection at "+result[0]+","+result[1]);
            if(Utils.isBetween(x1,x2,result[0]) && Utils.isBetween(y1,y2,result[1]) //to make sure intersection is in the right direction
                    && Utils.isBetween(pt1[0],pt2[0],result[0]) && Utils.isBetween(pt1[1],pt2[1],result[1])){ //to make sure it coincides with wall
                System.out.println("Collision detected at "+result[0]+","+result[1]);
                finalPosition[0] = result[0]-0.01*(result[0] - x1);
                finalPosition[1] = result[1]-0.01*(result[1] - y1);
                return finalPosition;
            }else{
                finalPosition[0] = x2;
                finalPosition[1] = y2;
            }
        }
        //System.out.println("Nesult is "+finalPosition[0]+","+finalPosition[1]);
        return finalPosition;
    }

    public static double[] getIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        double m1 = (y2-y1)/(x2-x1);
        double m2 = (y4-y3)/(x4-x3);
        //System.out.println("Testing from wall "+x1+","+y1+" to "+x2+","+y2+" against point from "+x3+","+y3+" to "+x4+","+y4);
        //System.out.println("y-"+y1+"="+m1+"(x-"+x1+")");
        //System.out.println("y-"+y2+"="+m2+"(x-"+x2+")");

        double x =0.0;
        double y =0.0;
        if (Double.isInfinite(m1)){
             x = x1;
             y = m2*(x-x3)+y3;
        }else if( m1 == 0.0){
             y = y1;
             x = ((y-y3)/m2)+x3;
        }else{
             x = (m1 * x1 - m2 * x3 + y3 - y1) / (m1 - m2);
             y = (y3 * m1 - y1 * m2 + m1 * m2 * x1 - m1 * m2 * x3) / (m1 - m2);
        }

        double[] result = {x,y};
        //System.out.println("Intersection is at "+result[0]+","+result[1]);
        return result;
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
