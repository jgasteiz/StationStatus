package com.fuzzingtheweb.stationstatus.data;


public class Station {
    private String mId;
    private String mStationName;
    private String mStationCode;
    private String mLineName;
    private String mLineCode;

    public Station(String id, String stationName, String stationCode, String lineName, String lineCode) {
        mId = id;
        mStationName = stationName;
        mStationCode = stationCode;
        mLineName = lineName;
        mLineCode = lineCode;
    }

    public Station(String stationName, String stationCode, String lineName, String lineCode) {
        mStationName = stationName;
        mStationCode = stationCode;
        mLineName = lineName;
        mLineCode = lineCode;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getStationName() {
        return mStationName;
    }

    public void setStationName(String stationName) {
        mStationName = stationName;
    }

    public String getStationCode() {
        return mStationCode;
    }

    public void setStationCode(String stationCode) {
        mStationCode = stationCode;
    }

    public String getLineName() {
        return mLineName;
    }

    public void setLineName(String lineName) {
        mLineName = lineName;
    }

    public String getLineCode() {
        return mLineCode;
    }

    public void setLineCode(String lineCode) {
        mLineCode = lineCode;
    }
}
