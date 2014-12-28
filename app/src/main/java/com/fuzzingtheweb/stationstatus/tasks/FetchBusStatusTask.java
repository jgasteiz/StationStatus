package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fuzzingtheweb.stationstatus.interfaces.OnBusStatusesFetched;
import com.fuzzingtheweb.stationstatus.util.JSONParser;
import com.fuzzingtheweb.stationstatus.util.Resources;
import com.fuzzingtheweb.stationstatus.util.TFLJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchBusStatusTask extends AsyncTask<Long, Void, List<StatusEntry>> {

    private static final String LOG_TAG = FetchBusStatusTask.class.getSimpleName();
    private OnBusStatusesFetched mListener;
    private String mStopId;

    public FetchBusStatusTask(OnBusStatusesFetched listener, String stopId) {
        mListener = listener;
        mStopId = stopId;
        mStopId = "490000106B";
    }

    @Override
    protected List<StatusEntry> doInBackground(Long... params) {
        String url = String.format(Locale.ENGLISH, "%s/bus/stop/%s/live.json?%s=%s&%s=%s&group=route&nextbuses=no",
                Resources.API_URL_BASE,
                mStopId,
                Resources.URL_KEY_APP_ID,
                Resources.APP_ID,
                Resources.URL_KEY_API_KEY,
                Resources.API_KEY);
        Log.d(LOG_TAG, url);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        TFLJSONParser tfljsonParser = new TFLJSONParser();
        try {
            return tfljsonParser.getBusStatusEntryList(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<StatusEntry> statusEntryList) {
        mListener.onBusStatusesFetched(statusEntryList);
    }
}
