package com.fuzzingtheweb.stationstatus.interfaces;

import com.fuzzingtheweb.stationstatus.tasks.Platform;

import java.util.List;

public interface OnTubeStatusesFetched {
    void onTubeStatusesFetched(List<Platform> platformList);
}
