package com.fuzzingtheweb.stationstatus.util;


import com.fuzzingtheweb.stationstatus.tasks.Platform;
import com.fuzzingtheweb.stationstatus.tasks.StationEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TFLJSONParser {

    private static final String LOG_TAG = TFLJSONParser.class.getSimpleName();

    public List<Platform> getPlatformList(JSONObject jsonObject) throws JSONException {

        List<Platform> platformList = new ArrayList<Platform>();

        if (jsonObject == null) {
            return platformList;
        }

        String stationName = jsonObject.getString("station_name");
        Iterator lineIterator = jsonObject.getJSONObject("lines").keys();
        while (lineIterator.hasNext()) {

            JSONObject platforms = jsonObject.getJSONObject("lines")
                    .getJSONObject((String) lineIterator.next()).getJSONObject("platforms");
            Iterator platformIterator = platforms.keys();

            while (platformIterator.hasNext()) {
                String direction = (String) platformIterator.next();

                Platform platform = new Platform(stationName);
                platform.setDirection(direction);

                JSONArray entryListJSON = platforms.getJSONObject(direction).getJSONArray("departures");
                List<StationEntry> stationEntryList = getEntryList(entryListJSON);
                platform.setStationEntryList(stationEntryList);
                platformList.add(platform);
            }
        }
        return platformList;
    }

    public List<StationEntry> getEntryList(JSONArray jsonArray) throws JSONException {
        List<StationEntry> stationEntryList = new ArrayList<StationEntry>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject entryJSON = (JSONObject) jsonArray.get(i);
            stationEntryList.add(new StationEntry(
                    entryJSON.getString("location"),
                    entryJSON.getString("destination_name"),
                    entryJSON.getInt("best_departure_estimate_mins")));
        }
        return stationEntryList;
    }

    public List<Tuple> getStationsList(JSONObject jsonObject) throws JSONException {
        JSONArray stationListJSON = jsonObject.getJSONArray("stations");
        List<Tuple> stationList = new ArrayList<Tuple>();
        for (int i = 0; i < stationListJSON.length(); i++) {
            JSONObject stationJSON = (JSONObject) stationListJSON.get(i);
            stationList.add(new Tuple<String, String>(stationJSON.getString("name"), stationJSON.getString("station_code")));
        }
        return stationList;
    }
}
