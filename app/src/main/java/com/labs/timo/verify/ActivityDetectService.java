package com.labs.timo.verify;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.labs.timo.verify.Constants.DETECTION_INTERVAL_IN_MILLISECONDS;

/**
 * Created by tim on 9/27/2016.
 */

public class ActivityDetectService extends Service {
    private static final String LOG_TAG = "verify - "+ ActivityDetectService.class.getSimpleName();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private GoogleApiClient mGoogleApiClient;
    protected GooglePlay gp = null;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG,"handleMessage = " +msg);
        }

        /**
         * called to hand the intent off the the LocationDbProcessor
         * @param intent
         */
        public void handleIntent(Intent intent){
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result == null){Log.d(LOG_TAG,"IntentResultNULL================");}
            else {
                DetectedActivity probableActivity = result.getMostProbableActivity();
                //   ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
                Intent localIntent = new Intent(Constants.BROADCAST_LOCATION_ACTION);
                localIntent.putExtra("Activity", probableActivity.getType());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
            }
        }
    }

    /**
     * called when the service is created
     */
    @Override
    public void onCreate() {
        Log.d(LOG_TAG,"onCreate");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    /**
     * startCommand is called when activity update received as well as at startup
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG,"onStartCommand");
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        if (gp == null){
            gp = new GooglePlay(getApplicationContext());
            gp.setupGoogleClientApi();
            gp.connect(true);
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        mServiceHandler.handleIntent(intent);
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * not binding to this service so not supported.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /**
     * destroy this service - stopping the activity recognition requests
     */
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"onDestroy");
        Toast.makeText(this, "service done", Toast.LENGTH_LONG).show();
        if (gp != null) {
            if (gp.mGoogleApiClient.isConnected()) {
                gp.stopActivityRecognition();
            }
            gp.removeActivityUpdates();
        }
    }

}
