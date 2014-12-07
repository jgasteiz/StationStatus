package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;

import com.fuzzingtheweb.stationstatus.util.JSONParser;
import com.fuzzingtheweb.stationstatus.util.TFLJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchStatusTask extends AsyncTask<Long, Void, List<Platform>> {

    private static final String LOG_TAG = FetchStatusTask.class.getSimpleName();
    private OnStatusesFetched mListener;
    private String mStation;
    private String mLine;
    private String APP_ID = "0399c806";
    private String API_KEY = "fe4d7f6e2a9cc15a96a76ce29ab0c069";

    public FetchStatusTask(OnStatusesFetched listener, String station, String line) {
        mListener = listener;
        mStation = station;
        mLine = line;
    }

    @Override
    protected List<Platform> doInBackground(Long... params) {

        String url = String.format(Locale.ENGLISH, "http://transportapi.com/v3/uk/tube/%s/%s/live.json?app_id=%s&api_key=%s", mLine, mStation, APP_ID, API_KEY);
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
        mListener.onStatusesFetched(platformList);
    }
}
