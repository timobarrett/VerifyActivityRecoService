package com.labs.timo.verify;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.labs.timo.verify.data.VerifyContract;

import java.util.ArrayList;

/**
 * Created by tim on 9/16/2016.
 */
public class LocationDbProcessor {
    private Context mContext;
    private static final String LOG_TAG = "verify-" + LocationDbProcessor.class.getSimpleName();
    private LocationBroadcastReceiver mLocalBcastRecvr;
    protected int mActivity;
    protected double mLatLon[];
    protected int lastActivity;
    protected boolean mWasActive;

    /**
     * constructor
     * @param mContext
     */
    public LocationDbProcessor(Context mContext) {
//        mContext = getApplicationContext();
        mLocalBcastRecvr = new LocationBroadcastReceiver();
        mLatLon = new double[2];
        mLatLon[0] = 0d;
        mLatLon[1] = 0d;
        lastActivity = DetectedActivity.UNKNOWN;
        IntentFilter locationFilter = new IntentFilter();
        locationFilter.addAction(Constants.BROADCAST_LOCATION_ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalBcastRecvr, locationFilter);
    }

    /**
     * add a record to the location table
     * @param lat
     * @param lon
     */
    protected void insertLocationInfo(double lat, double lon) {
        //   Log.d(LOG_TAG, "IN updateDistanceInfo");
        String activity = "";
        ContentValues locationValues = new ContentValues();
        locationValues.put(VerifyContract.LocationEntry.COLUMN_COORD_LAT, lat);
        locationValues.put(VerifyContract.LocationEntry.COLUMN_COORD_LON, lon);
        locationValues.put(VerifyContract.LocationEntry.COLUMN_DATE_TIME, System.currentTimeMillis());
        locationValues.put(VerifyContract.LocationEntry.COLUMN_ACTIVITY, mActivity);
        mContext.getContentResolver().insert(VerifyContract.LocationEntry.CONTENT_URI, locationValues);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {
        protected float dist[] = new float[1];

        public LocationBroadcastReceiver() {
        }

        /**
         * Process the broadcast sent from ActivityDetectService
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_LOCATION_ACTION)) {
                Log.d(LOG_TAG, "onReceive LocationBroadcastReceiver");
                Bundle extras = intent.getExtras();
                mActivity = extras.getInt("Activity");
                Log.d(LOG_TAG, "onReceive - Activity = " + mActivity);
                if (mActivity != DetectedActivity.STILL && !mWasActive) {
                    mWasActive = true;
                } else if (mActivity == DetectedActivity.STILL && mWasActive) {
                    SingleLocationInfo.requestSingleUpdate(context,
                            new SingleLocationInfo.LocationCallback() {
                                @Override
                                public void onNewLocationAvailable(double lat, double lon) {
                                    Log.d(LOG_TAG, "LAT = " + lat + " LON = " + lon + " Activity = " + mActivity);
                                    if (mLatLon[0] != 0d && mLatLon[1] != 0d) {
                                        Location.distanceBetween(lat, lon, mLatLon[0], mLatLon[1], dist);
                                        if (dist[0] >= 160.934) {
                                            insertLocationInfo(lat, lon);
                                            mLatLon[0] = lat;
                                            mLatLon[1] = lon;
                                        }
                                    } else {
                                        insertLocationInfo(lat, lon);
                                        mLatLon[0] = lat;
                                        mLatLon[1] = lon;
                                    }
                                }

                            });
                }
                Log.d(LOG_TAG, "GOT HERE");
            }
        }
    }
}
