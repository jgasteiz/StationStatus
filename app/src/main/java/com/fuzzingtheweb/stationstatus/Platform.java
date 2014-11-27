package com.fuzzingtheweb.stationstatus;

import java.util.ArrayList;
import java.util.List;

public class Platform {
    private String direction;
    private List<Entry> entryList;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    private String stationName;

    public Platform(String stationName) {
        this.entryList = new ArrayList<Entry>();
        this.direction = null;
        this.stationName = stationName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<Entry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    public void addEntry(Entry entry) {
        this.entryList.add(entry);
    }
}
