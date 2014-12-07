package com.fuzzingtheweb.stationstatus.tasks;


public class StationEntry {
    public String location;
    public String destination;
    public int minutes;

    public StationEntry(String location, String destination, int minutes) {
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
