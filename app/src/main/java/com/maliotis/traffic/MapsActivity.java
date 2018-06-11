package com.maliotis.traffic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.RoundCap;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    private LocationManager locationManager;
    private double mLatitude = 37.9908164;
    private double mLongitude = 23.6682991;
    private boolean granted = false;
    Timer timer;
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestPermission();
    }

    /**
     * Tries to request permission if its not granted by the user
     * otherwise continues to execute the dependant code from the permissions
     *
     * @author #petrosmaliotis
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_GPS);

            }
        } else {
            // Permission has already been granted
            if (isGPSEnabled()) {
                GPS();
                granted = true;
            }
        }
    }

    /**
     * Checks the users response to the Permission requested
     * It loops through the permissions we currently want to be granted
     *
     * @param requestCode The requested code for its permission
     * @param permissions The array of permissions we are asking
     * @param grantResults The array of results to the requested permissions
     * @author #petrosmaliotis
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (isGPSEnabled()) {
                        GPS();
                        granted = true;
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void GPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTAG", "Something went wrong");

        } else {
            //mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location = getLastLocation();
            if (location == null) {
                Log.v("Location", "Location was null");
            } else {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
            }
        }
    }

    /**
     * Gets the last known location prioritizing with the better provider (Wifi) (Data) (Gps)
     *
     * @author #petrosmaliotis
     * @return location object
     */
    private Location getLastLocation() {
        Location bLocation = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


    /**
     * Checks for Network or Gps provider
     * @return whether gps is enabled or not
     * @author #petrosmaliotis
     */
    public boolean isGPSEnabled() {
        boolean tf;
        boolean ft;
        boolean ret = true;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        tf = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        ft = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!tf && !ft && (locationProviders == null || locationProviders.equals(""))) {
            ret = false;
        }

        return ret;

    }


    /**
     * When map is read to load this function will be called
     * and will update the user's location every 2 sec  with start()
     * @see #start()
     * @param googleMap GoogleMap object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isGPSEnabled() && granted) {
                    GPS();
                    //Runs on the main thread where gui happens!!!!(Don't run GUI on back threads)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //ChangeMarkerOnMap() changes the GUI !!!
                            changeMarkerOnMap();
                            //Yay!! Works P.M !
                            Log.d("Change on map","Works :)");
                        }
                    });
                }
            }
        };

        start();
    }

    public void start() {
        if(timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask,2000,1000);
        }
    }

    /**
     * Will be called when the user arrives to his destination!
     * To stop the update of his location!
     * @author #petrosmaliotis
     */
    public void stop() {
        timer.cancel();
        timer = null;
    }

    /**
     * Changes the pin on the map!
     * Takes NO parameters the values needed are global !
     * @see #mLatitude
     * @see #mLongitude
     * @author #petrosmaliotis
     */
    private void changeMarkerOnMap() {
        LatLng usersLocation = new LatLng(mLatitude, mLongitude);
        //Its a good practice to not over extend you code over that (white) line ->
        //So we 'break' our code to make it more readable!
        mMap.addMarker(new MarkerOptions().position(usersLocation)
                .title("Marker in usersLocation"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(usersLocation));
    }

}
