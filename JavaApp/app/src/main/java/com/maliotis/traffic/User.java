package com.maliotis.traffic;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Traffic");

    private int id;
    private int points;
    private String position;
    private String nextWaypoint;

    public int getId() { return id; }

    public int getPoints() { return points; }

    public void setId(int id) { this.id = id; }

    public void setPoints(int points) { this.points = points; }

    public void setPosition(LatLng position) { this.position = position + ""; }

    public void setNextWaypoint(LatLng nextWaypoint) { this.nextWaypoint = nextWaypoint + ""; }

    public User(int id, int points, LatLng position) {
        this.id = id;
        this.points = points;
        this.position = position + "";
    }

    public void sendWaypoint(){
        String formatedKey = (position + "c" + nextWaypoint).replace('.','a').replace(',','b').replace("lat/lng: (", "").replace(")", "");
        myRef.child(formatedKey).setValue(0L);
    }
}
