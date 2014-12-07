package com.fuzzingtheweb.stationstatus.tasks;

import java.util.ArrayList;
import java.util.List;

public class Platform {
    private String direction;
    private List<StatusEntry> statusEntryList;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    private String stationName;

    public Platform(String stationName) {
        this.statusEntryList = new ArrayList<StatusEntry>();
        this.direction = null;
        this.stationName = stationName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<StatusEntry> getStatusEntryList() {
        return statusEntryList;
    }

    public void setStatusEntryList(List<StatusEntry> statusEntryList) {
        this.statusEntryList = statusEntryList;
    }

    public void addEntry(StatusEntry statusEntry) {
        this.statusEntryList.add(statusEntry);
    }
}
