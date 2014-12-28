package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fuzzingtheweb.stationstatus.interfaces.OnBusStopsFetched;
import com.fuzzingtheweb.stationstatus.util.JSONParser;
import com.fuzzingtheweb.stationstatus.util.Resources;
import com.fuzzingtheweb.stationstatus.util.TFLJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchBusStopsTask extends AsyncTask<Long, Void, List<BusStop>> {

    private static final String LOG_TAG = FetchBusStopsTask.class.getSimpleName();
    private OnBusStopsFetched mListener;
    private double mLatitude;
    private double mLongitude;

    public FetchBusStopsTask(OnBusStopsFetched listener, double latitude, double longitude) {
        mListener = listener;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    protected List<BusStop> doInBackground(Long... params) {
        String url = String.format(Locale.ENGLISH, "%s/bus/stops/near.json?%s=%s&%s=%s&%s=%s&%s=%s",
                Resources.API_URL_BASE,
                Resources.URL_KEY_APP_ID,
                Resources.APP_ID,
                Resources.URL_KEY_API_KEY,
                Resources.API_KEY,
                Resources.URL_KEY_LAT,
                mLatitude,
                Resources.URL_KEY_LON,
                mLongitude);
        Log.d(LOG_TAG, url);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        TFLJSONParser tfljsonParser = new TFLJSONParser();
        try {
            return tfljsonParser.getBusStopList(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<BusStop> busStopList) {
        mListener.onBusStopsFetched(busStopList);
    }
}
