package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;

import com.fuzzingtheweb.stationstatus.JSONParser;
import com.fuzzingtheweb.stationstatus.MainActivity;
import com.fuzzingtheweb.stationstatus.Platform;
import com.fuzzingtheweb.stationstatus.TFLJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchStatusTask extends AsyncTask<Long, Void, List<Platform>> {

    private static final String LOG_TAG = FetchStatusTask.class.getSimpleName();
    private MainActivity.StatusDetailFragment mContext;
    private String mStation;
    private String mLine;
    private String APP_ID = "0399c806";
    private String API_KEY = "fe4d7f6e2a9cc15a96a76ce29ab0c069";

    public FetchStatusTask(MainActivity.StatusDetailFragment context, String station, String line) {
        mContext = context;
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
        mContext.renderResult(platformList);
    }
}
