package com.maliotis.traffic;

public class User {
    private int id;
    private int points;
    private String polyline;


    public String getPolyline() {
        return polyline;
    }

    public int getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public User(int id, int points) {
       this.id = id;
        this.points = points;
    }
}
