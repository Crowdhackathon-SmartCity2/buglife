package com.maliotis.traffic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation,previousLocation;
    boolean firstmLastLocationIsUsed = true;
    Marker mCurrLocationMarker;
    EditText locationSearch;
    Button startButton;
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    Location locationForGps;
    Gps gps;
    float zoom = 17f;
    float tilt = 0f;
    float bearing = 0f;
    boolean followUser= false;
    boolean test = true;
    private FloatingActionButton fab;
    ArrayList<String> crashChild;
    //ArrayList<Long> trafficChild;
    Map<String,Long> trafficChild;
    private List<List<HashMap<String, String>>> routes;
    int seconds;
    TimerTask mTimerTask;
    Timer timer;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSearch = findViewById(R.id.editText);
        startButton = findViewById(R.id.start_button);
        startButton.setVisibility(View.INVISIBLE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    followUser = true;
                    drawRoute(routes);
                    countingSeconds();
            }
        });



        requestPermission();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        reference = db.getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectAllDb((Map<String,Object>) dataSnapshot.getValue());
                Log.d("DataSnapShot","Works :)");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (test) {
            reference.child("Crash").child("ValueForRoute").setValue("lueForRoute");
            test = false;
        }
    }

    private void collectAllDb(Map<String,Object> db) {
        //ArrayList<String> dbChilds = new ArrayList<>();

        for (Map.Entry<String,Object> entry: db.entrySet()) {

                Map<String, Object> name = (Map<String, Object>) entry.getValue();
                String key = entry.getKey();
                if (key.equals("Crash")) {
                    // Crash children will be here !!!
                    crashChild = new ArrayList<>();
                    for (Map.Entry<String,Object> entry1: name.entrySet()) {
                        String cChild = (String) entry1.getValue();
                        crashChild.add(cChild);
                        Log.d("CrashChild",cChild);
                    }

                } else if (key.equals("Traffic")) {
                    trafficChild = new HashMap<>();

                    for (Map.Entry<String,Object> entry2: name.entrySet()) {
                        long tChild = (long) entry2.getValue();
                        String kName = entry2.getKey();
                        trafficChild.put(kName,tChild);
                        Log.d("TrafficChild","Name: "+kName+", Value: "+tChild+"");
                    }
                }

        }
    }



    private void countingSeconds() {

        if (mTimerTask==null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    seconds++;
                    reference.child("Crash").child("ValueForRoute").setValue("" + seconds);
                    Log.d("Timer", "" + seconds);
                }
            };
            timer = new Timer();
            timer.schedule(mTimerTask,500L,1000L);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            FusedLocationProviderClient client = new FusedLocationProviderClient(this);
            //client.removeLocationUpdates();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        }
    }



    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {

        if (firstmLastLocationIsUsed){
            mLastLocation = location;
            previousLocation = mLastLocation;
            firstmLastLocationIsUsed = false;
        } else {
            previousLocation = mLastLocation;
            mLastLocation = location;
        }
        if (previousLocation.getLongitude() - mLastLocation.getLongitude() >= Math.abs(3) ||
                previousLocation.getLatitude() - mLastLocation.getLatitude() >= Math.abs(3)) {
            seconds = 0;
        }


        if (followUser) {
           //mCurrLocationMarker.remove();
           updateCameraToFollowUser();
        }
        else {

            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            //mCurrLocationMarker = mMap.addMarker(markerOptions);

            //move map camera and rotate
            updateCameraBearing(location.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    public void updateCameraToFollowUser() {
        LatLng latLng = new LatLng(mLastLocation.getLatitude(),
                mLastLocation.getLongitude());
        tilt = 37f;
        zoom = 17.5f;
        CameraPosition cameraPosition = new CameraPosition(latLng,zoom,tilt,bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
            try {
                if (gps.isGPSEnabled()) {
                    locationForGps = gps.GPS();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                urlFetch url = new urlFetch(this);
                url.getDirections(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()
                        ,address.getLatitude()
                        ,address.getLongitude());
                while (routes == null) {
                    routes = url.getRoutes();
                }
                startButton.setVisibility(View.VISIBLE);
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                // make the camera go to the searched place

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom),1000,null);


            } catch (IOException e) {
                // TODO: Here we need to handle the exception when the search doesn't exist
                //TODO: P.M Show a fragment UI explaining the user what went wrong
                showAlertDialog();
                e.printStackTrace();
            }

        }
    }


    /**
     * When map is read to load this function will be called
     * and will update the user's location every 2 sec  with start()
     * @param googleMap GoogleMap object
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
        //mMap.setTrafficEnabled(true);
    }


    public void drawRoute(List<List<HashMap<String, String>>> routes){
        ArrayList points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.CYAN);
            lineOptions.geodesic(true);

        }
        mMap.addPolyline(lineOptions);
    }

    public void drawTraffic (Map<String,Long> traffic) {
        //TODO: kostas 

    }

    private void updateCameraBearing(float bearing) {
        if ( mMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        mMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        drawable = (DrawableCompat.wrap(drawable)).mutate();

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    //TODO: P.M
    //Arguments needed in showAlertDialog....
    void showAlertDialog() {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title","Opps");
        args.putString("message","Something went wrong");
        args.putString("positiveButton","OK");
        args.putString("negativeButton","Cancel");
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(),"error_dialog");
    }
}
