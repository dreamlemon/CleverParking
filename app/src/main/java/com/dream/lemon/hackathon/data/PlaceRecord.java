package com.dream.lemon.hackathon.data;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;

public class PlaceRecord extends RealmObject {

    String address;
    String name;
    String latLng;
    String attributions;

    public PlaceRecord() {

    }

    public PlaceRecord(String address, String name, String latLng,
                       String attributions) {
        this.address = address;
        this.name = name;
        this.latLng = latLng;
        this.attributions = attributions;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }
}
