package com.fuzzingtheweb.stationstatus.interfaces;

import com.fuzzingtheweb.stationstatus.data.LineStation;

import java.util.List;

public interface OnStationsFetched {
    void onStationsFetched(List<LineStation> lineStationList);
}
