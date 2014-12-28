package com.fuzzingtheweb.stationstatus.tasks;


public class BusStop {
    private double latitude;
    private double longitude;
    private double distance;
    private String name;
    private String locality;
    private String indicator;
    private String bearing;
    private String mode;
    private String smscode;
    private String atcocode;

    public BusStop(double latitude, double longitude, double distance, String name, String locality, String indicator, String bearing, String mode, String smscode, String atcocode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.name = name;
        this.locality = locality;
        this.indicator = indicator;
        this.bearing = bearing;
        this.mode = mode;
        this.smscode = smscode;
        this.atcocode = atcocode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSmscode() {
        return smscode;
    }

    public void setSmscode(String smscode) {
        this.smscode = smscode;
    }

    public String getAtcocode() {
        return atcocode;
    }

    public void setAtcocode(String atcocode) {
        this.atcocode = atcocode;
    }
}
