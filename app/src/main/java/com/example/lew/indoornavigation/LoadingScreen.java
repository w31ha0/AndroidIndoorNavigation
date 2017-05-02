package com.example.lew.indoornavigation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Lew on 2/5/2017.
 */
public class LoadingScreen extends Activity implements BeaconConsumer {
    private BeaconManager manager;
    private TextView status;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);
        status = (TextView)findViewById(R.id.status);
        manager = BeaconManager.getInstanceForApplication(this);
        manager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        manager.bind(this);

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void onBeaconServiceConnect() {
        manager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                System.out.println("Entered");
                String UUID = String.valueOf(region.getId1());
                IndoorMap map = BluetoothDatabase.getMapFromUUID(UUID);
                if (map == null)
                    //status.setText("Invalid UUID Detected");
                    map = map;
                else{
                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {status.setText("Found your location...loading map now");
                    }
                });
                    Intent intent = new Intent(LoadingScreen.this, MainActivity.class);
                    intent.putExtra("Map",UUID);
                    startActivity(intent);
                }
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("Exited");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                System.out.println(state);
            }
        });

        try {
            manager.startMonitoringBeaconsInRegion(new Region("myBeacons",Identifier.parse("E6BF275E-0BB3-43E5-BF88-517F13A5A162"), null, null));
        } catch (RemoteException e) {
            status.setText("Error occured "+e);
        }
    }

}

