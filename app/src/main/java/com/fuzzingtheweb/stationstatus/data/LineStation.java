package com.fuzzingtheweb.stationstatus.data;


// Class for the stations contained in a line
public class LineStation {
    private String mLineCode;
    private String mStationCode;
    private String mStationName;

    public LineStation(String stationName, String stationCode, String lineCode) {
        mStationName = stationName;
        mStationCode = stationCode;
        mLineCode = lineCode;
    }

    public String getLineCode() {
        return mLineCode;
    }

    public void setLineCode(String lineCode) {
        mLineCode = lineCode;
    }

    public String getStationCode() {
        return mStationCode;
    }

    public void setStationCode(String stationCode) {
        mStationCode = stationCode;
    }

    public String getStationName() {
        return mStationName;
    }

    public void setStationName(String stationName) {
        mStationName = stationName;
    }
}
