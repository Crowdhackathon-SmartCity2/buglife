package com.maliotis.traffic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.List;

public class Gps {

    private Context mContext;
    private LocationManager locationManager;

    Gps(Context context) {
        mContext = context;
    }


    /**
     * Checks for Network or Gps provider
     * @return whether gps is enabled or not
     * @author #petrosmaliotis
     */
    public boolean isGPSEnabled() {
        boolean tf;
        boolean ft;
        boolean ret = true;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        tf = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        ft = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String locationProviders = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!tf && !ft && (locationProviders == null || locationProviders.equals(""))) {
            ret = false;
        }

        return ret;

    }


    /**
     * Gets the last known location prioritizing with the better provider (Wifi) (Data) (Gps)
     *
     * @author #petrosmaliotis
     * @return location object
     */
    private Location getLastLocation() {
        Location bLocation = null;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");

        } else {
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bLocation == null || l.getAccuracy() < bLocation.getAccuracy()) {
                    bLocation = l;
                }
            }

        }

        return bLocation;
    }

    public Location GPS() throws Exception{
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");
            throw new Exception("Permissions were not handled correctly");

        } else {
            Location location = getLastLocation();
            if (location == null) {
                Log.v("Location", "Location was null");
                throw new Exception("Location was null");
            } else {

                return location;
            }

        }
    }
}
