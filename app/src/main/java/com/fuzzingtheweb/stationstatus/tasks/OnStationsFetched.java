package com.fuzzingtheweb.stationstatus.tasks;

import com.fuzzingtheweb.stationstatus.util.Tuple;

import java.util.List;

public interface OnStationsFetched {
    void onStationsFetched(List<Tuple> tupleList);
}
