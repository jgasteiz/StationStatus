package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.fuzzingtheweb.stationstatus.interfaces.OnTubeStatusesFetched;
import com.fuzzingtheweb.stationstatus.util.JSONParser;
import com.fuzzingtheweb.stationstatus.util.Resources;
import com.fuzzingtheweb.stationstatus.util.TFLJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchTubeStatusTask extends AsyncTask<Long, Void, List<Platform>> {

    private static final String LOG_TAG = FetchTubeStatusTask.class.getSimpleName();
    private OnTubeStatusesFetched mListener;
    private String mStation;
    private String mLine;

    public FetchTubeStatusTask(OnTubeStatusesFetched listener, String station, String line) {
        mListener = listener;
        mStation = station;
        mLine = line;
    }

    @Override
    protected List<Platform> doInBackground(Long... params) {

        String url = String.format(Locale.ENGLISH, "%s/tube/%s/%s/live.json?%s=%s&%s=%s",
                Resources.API_URL_BASE,
                mLine,
                mStation,
                Resources.URL_KEY_APP_ID,
                Resources.APP_ID,
                Resources.URL_KEY_API_KEY,
                Resources.API_KEY);
        Log.d(LOG_TAG, url);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        TFLJSONParser tfljsonParser = new TFLJSONParser();
        try {
            return tfljsonParser.getPlatformList(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<Platform> platformList) {
        mListener.onTubeStatusesFetched(platformList);
    }
}
