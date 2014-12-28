package com.fuzzingtheweb.stationstatus.interfaces;

import com.fuzzingtheweb.stationstatus.tasks.BusStop;
import com.fuzzingtheweb.stationstatus.tasks.StatusEntry;

import java.util.List;

public interface OnBusStopsFetched {
    void onBusStopsFetched(List<BusStop> busStopList);
}
