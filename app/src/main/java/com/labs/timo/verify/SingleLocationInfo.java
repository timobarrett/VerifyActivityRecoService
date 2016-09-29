package com.labs.timo.verify;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by tim on 9/15/2016.
 */
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


/**
 * Created by tim on 9/16/2016.
 */
public class SingleLocationInfo {
    protected static final String LOG_TAG = "besafe -" + SingleLocationInfo.class.getSimpleName();
    private static LocationListener ll;
    private static final long TIMEOUT = 1000;
    private static boolean locationReceived = false;

    public static interface LocationCallback {
        public void onNewLocationAvailable(double lat, double lon);
    }

    /**
     * calls back to calling thread, note this is for low grain: if you want higher precision, swap the
     * contents of the else and if. Also be sure to check gps permission/settings are allowed.
     * call usually takes <10ms
     * @param context
     * @param callback
     */

    public static void requestSingleUpdate(final Context context, final LocationCallback callback) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        ll = null;
        if (isGPSEnabled) {
            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            try {
                locationManager.requestSingleUpdate(criteria, ll = new LocationListener() {

                    public void onLocationChanged(Location location) {
                        Log.d(LOG_TAG,"onLocationChanged - LAT = "+location.getLatitude() + " LON = " + location.getLongitude());
                        locationReceived = true;
                        callback.onNewLocationAvailable(location.getLatitude(), location.getLongitude());
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }, null);
            } catch (SecurityException se) {
                Log.d(LOG_TAG, "Security Exception");
            }

            //the following runs regardless and F's the lat and long collected
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    try{
                        locationManager.removeUpdates(ll);
                        if (!locationReceived) {
                            callback.onNewLocationAvailable(0d, 0d);
                        }
                    }catch(SecurityException s){
                        Log.d(LOG_TAG,"Exception - stopping LocationListener");
                    }
                }
            },TIMEOUT);
        }
    }
}
