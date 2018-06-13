package com.maliotis.traffic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.location.Address;
import android.location.Geocoder;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText locationSearch;
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    Location locationForGps;
    private boolean granted = false;
    private boolean first = false;
    Timer timer;
    TimerTask timerTask;
    Gps gps;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mToolbar = findViewById(R.id.my_toolbar);
        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mToolbar.setLogo(R.drawable.ic_directions_car_black_24dp);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSearch = findViewById(R.id.editText);
        gps = new Gps(this);
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
            if (gps.isGPSEnabled()) {
                try {
                    locationForGps = gps.GPS();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    if (gps.isGPSEnabled()) {
                        try {
                            locationForGps = gps.GPS();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    /**
     * Searches for the location from the EditText field !
     * with geocoder gets the address from the name of the location.
     * The address contains tha latlong values we want.
     *
     * @param view specifies the view (we implement it its this).
     * @author #petrosmaliotis
     */
    public void onMapSearch(View view) {

        String location = locationSearch.getText().toString();
        List<Address>addressList = null;

        if (!location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                // make the camera go to the searched place
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            } catch (IOException e) {
                // TODO: Here we need to handle the exception when the search doesn't exist
                //TODO: P.M Show a fragment UI explaining the user what went wrong
                e.printStackTrace();
            }

        }
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
                if (gps.isGPSEnabled() && granted) {
                    try {
                       locationForGps = gps.GPS();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
     *
     * @author #petrosmaliotis
     */
    private void changeMarkerOnMap() {
        LatLng usersLocation = new LatLng(locationForGps.getLatitude(),locationForGps.getLongitude());
        //Its a good practice to not over extend you code over that (white) line ->
        //So we 'break' our code to make it more readable!

        mMap.addMarker(new MarkerOptions().position(usersLocation)
                .title("Marker in usersLocation"));
        if (!first) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(usersLocation));
        }
        first = true;
    }

}
