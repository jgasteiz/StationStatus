package com.fuzzingtheweb.stationstatus.tasks;

import java.util.List;

public interface OnStatusesFetched {
    void onStatusesFetched(List<Platform> platformList);
}
