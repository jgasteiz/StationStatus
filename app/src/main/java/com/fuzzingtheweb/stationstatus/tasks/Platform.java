package com.fuzzingtheweb.stationstatus.tasks;

import java.util.ArrayList;
import java.util.List;

public class Platform {
    private String direction;
    private List<StationEntry> stationEntryList;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    private String stationName;

    public Platform(String stationName) {
        this.stationEntryList = new ArrayList<StationEntry>();
        this.direction = null;
        this.stationName = stationName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<StationEntry> getStationEntryList() {
        return stationEntryList;
    }

    public void setStationEntryList(List<StationEntry> stationEntryList) {
        this.stationEntryList = stationEntryList;
    }

    public void addEntry(StationEntry stationEntry) {
        this.stationEntryList.add(stationEntry);
    }
}
