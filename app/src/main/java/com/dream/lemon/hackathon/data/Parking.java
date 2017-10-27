package com.dream.lemon.hackathon.data;

public class Parking {
    String id;
    double latitute;
    double longitude;

    public Parking(String id, double latitute, double longitude) {
        this.id = id;
        this.latitute = latitute;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }




}
