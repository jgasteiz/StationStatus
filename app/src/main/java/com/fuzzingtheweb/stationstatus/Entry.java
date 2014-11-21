package com.fuzzingtheweb.stationstatus;


public class Entry {
    public String location;
    public String destination;
    public String timeTo;
    public String departTime;
    public String direction;

    Entry(String location, String destination, String timeTo, String departTime, String direction) {
        this.location = location;
        this.destination = destination;
        this.timeTo = timeTo;
        this.departTime = departTime;
        this.direction = direction;
    }

    public String getTimeTo() {
        if (this.timeTo.equals("-")) {
            return "Due";
        }
        return this.timeTo;
    }
}
