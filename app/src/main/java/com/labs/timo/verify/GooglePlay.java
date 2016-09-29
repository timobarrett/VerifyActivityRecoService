package com.labs.timo.verify;

/**
 * Created by tim on 9/15/2016.
 */
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

public class GooglePlay implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    protected GoogleApiClient mGoogleApiClient;
    private final String LOG_TAG = "verify -"+GooglePlay.class.getSimpleName();
    private Context mAppContext;
    private boolean mGooglePlayInstalled;
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10000;

    public GooglePlay(Context appContext){
        mAppContext = appContext;
        verifyGooglePlayServices();
    }

    /**
     * called when the connection to google play services is suspended
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
    }

    /**
     * called when connected status returned from google play services
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        //           Log.d(LOG_TAG, "Connected to GoogleApiClient");
        requestActivityUpdates();
    }

    /**
     * called when connection to google play services fails
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    /**
     *
     * @param status
     */
    public void onResult(Status status){
        Toast.makeText(mAppContext,"GooglePlay.onResult Called",Toast.LENGTH_LONG);

    }
    /**
     * verify that google play services are installed
     */
    private void verifyGooglePlayServices(){
        //    Log.d(LOG_TAG, "IN verifyGooglePlayServices");
        GoogleApiAvailability glApi = GoogleApiAvailability.getInstance();
        if (ConnectionResult.SUCCESS != glApi.isGooglePlayServicesAvailable(mAppContext)){
            mGooglePlayInstalled = false;
            Toast.makeText(mAppContext,R.string.google_play_error,Toast.LENGTH_LONG);
        }
        else{
            mGooglePlayInstalled = true;
        }

    }
    /**
     * google play services installed setup to receive activity updates
     */
    protected synchronized void setupGoogleClientApi() {
        //          Log.d(LOG_TAG, "SETUP GOOGLE CLIENT API");
        if (!mGooglePlayInstalled){
            Log.e(LOG_TAG, "Google play API not installed - unstable");
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(mAppContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(ActivityRecognition.API)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    /**
     * stop acitivy recognition
     */

    public void stopActivityRecognition(){
        //    Log.d(LOG_TAG,"IN stopActivityRecognition");
        if (mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        }
    }
    /**
     * request activity updates from intentService performing activity reco
     */
    public void requestActivityUpdates() {
           Log.d(LOG_TAG, "IN requestActivityUpdates");
        if(!mGoogleApiClient.isConnected()){
            //        Log.d(LOG_TAG, "reuqestActivityUpdates googleAPIClient not connected");
            connect(true);
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);

    }

    /**
     * removes activity updates
     */
    public void removeActivityUpdates() {
        //           Log.d(LOG_TAG,"IN removeActivityUpdates");
        if (mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        }
    }

    /**
     *
     * @return PendingIntent
     */
    private PendingIntent getActivityDetectionPendingIntent() {

        //  Log.d(LOG_TAG,"IN getActivityDetectionPendingIntent ************")
        Intent intent = new Intent(mAppContext, ActivityDetectService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        return PendingIntent.getService(mAppContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * connect and disconnect google play service
     * @param connect
     */
    public void connect(boolean connect){
        //  Log.d(LOG_TAG,"IN GooglePlayConnect = " + connect);
        if (connect){
            mGoogleApiClient.connect();
        }else{
            mGoogleApiClient.disconnect();
        }
    }
}
