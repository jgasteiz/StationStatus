package com.fuzzingtheweb.stationstatus.tasks;


public class StatusEntry {
    public String location;
    public String destination;
    public int minutes;

    public StatusEntry(String location, String destination, int minutes) {
        this.location = location;
        this.destination = destination;
        this.minutes = minutes;
    }

    public String getTimeTo() {
        if (minutes == 0) {
            return "Due";
        }
        return this.minutes + "min";
    }
}
