package com.fuzzingtheweb.stationstatus.interfaces;

import com.fuzzingtheweb.stationstatus.tasks.StatusEntry;

import java.util.List;

public interface OnBusStatusesFetched {
    void onBusStatusesFetched(List<StatusEntry> statusEntryList);
}
