package com.labs.timo.verify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by tim on 9/15/2016.
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    protected static boolean mGpsPermissionGranted = false;
    protected boolean mPermissionResponceRequested = false;
    protected final int GPS_PERMISSION = 7;
    protected static GooglePlay gp = null;
    protected LocationDbProcessor ldbp;

    /**
     * called when app is created
     * @param savedInstance
     */
    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.main_activity);
        verifyPermissions();
        if (mGpsPermissionGranted){
            startApp();

        }
        ldbp = new LocationDbProcessor(this);
    }

    /**
     * want to hold off starting the app until all permissions
     * have been verified and setup is complete
     *
     */
    protected void startApp(){
        Intent intent = new Intent(this,ActivityDetectService.class);
        startService(intent);
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        //       Log.d(LOG_TAG, "ON START CALLED");
        super.onStart();
    }

    /**
     * called when onStop called -
     *  stopactivityrecognition causes stack trace - google play not connected yet
     *  order matters!!
     *  NOTE: onStop is called when the UI is obscured.  Don't want to disconnect googleplay
     *        when UI blanks or get no activities.
     */
    @Override
    protected void onStop() {
//        Log.d(LOG_TAG, "IN onStop");
        super.onStop();
    }

    /**
     * called when application is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy(){

        super.onDestroy();
    }
    @Override
    protected void onResume() {
        //    Log.d(LOG_TAG,"IN onResume");
        super.onResume();

    }

    /**
     * OnRequestPermissionsResult
     *      used to verify that the user has allowed need permissions for the app
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //    Log.i(LOG_TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case GPS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission to GPS Granted");
                    mGpsPermissionGranted = true;
                    startApp();
                } else {
                    Log.d(LOG_TAG, "Permission to GPS Denied");
                }
            }
        }
    }

    /**
     * prompts the user for permission to access location
     * information
     */
    protected void verifyPermissions() {
        //    Log.d(LOG_TAG, "IN verifyPermissions");
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
                mPermissionResponceRequested = true;
            }
            else{ mGpsPermissionGranted= true;}
        } else {
            int result = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED) {
                mGpsPermissionGranted = true;
            }
        }
    }
}
