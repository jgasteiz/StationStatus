package com.fuzzingtheweb.stationstatus;


public class Entry {
    public String location;
    public String destination;
    public int minutes;

    public Entry(String location, String destination, int minutes) {
        this.location = location;
        this.destination = destination;
        this.minutes = minutes;
    }

    public String getTimeTo() {
        if (minutes == 0) {
            return "Due";
        }
        return Integer.toString(this.minutes);
    }
}
