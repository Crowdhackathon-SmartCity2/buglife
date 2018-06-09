package com.maliotis.traffic;

import android.Manifest;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location mLocation;
    protected GoogleApiClient mGoogleApiClient;
    private double mLatitude = 37.9908164;
    private double mLongitude = 23.6682991;
    private boolean granted = false;

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
            mLocation = getLastLocation();
            if (mLocation == null) {
                Log.v("Location", "Location was null");
            } else {
                mLatitude = mLocation.getLatitude();
                mLongitude = mLocation.getLongitude();
            }
        }
    }

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (isGPSEnabled() && granted) {
                    GPS();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /////////////////TESTING////////////////////////////////////////////
        // Add polylines and polygons to the map. This section shows just
        // a single polyline. Read the rest of the tutorial to learn more.
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(37.316, 23.421),
                        new LatLng(37.947, 23.692),
                        new LatLng(37.464, 23.991),
                        new LatLng(37.601, 23.417),
                        new LatLng(37.406, 23.448),
                        new LatLng(37.591, 23.409)));
        polyline1.setEndCap(new RoundCap());
        polyline1.setWidth(5);
        polyline1.setColor(0xff000000);
        polyline1.setJointType(JointType.ROUND);
        ////////////////////////////////////////////////////////////////////

        LatLng usersLocation = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(usersLocation).title("Marker in usersLocation"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(usersLocation));

    }


}
