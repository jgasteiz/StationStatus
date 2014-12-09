package com.fuzzingtheweb.stationstatus.tasks;

import com.fuzzingtheweb.stationstatus.data.LineStation;

import java.util.List;

public interface OnStationsFetched {
    void onStationsFetched(List<LineStation> lineStationList);
}
