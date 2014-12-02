package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;

import com.fuzzingtheweb.stationstatus.JSONParser;
import com.fuzzingtheweb.stationstatus.SettingsActivity;
import com.fuzzingtheweb.stationstatus.TFLJSONParser;
import com.fuzzingtheweb.stationstatus.Tuple;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchStationsTask extends AsyncTask<Long, Void, List<Tuple>> {

    private static final String LOG_TAG = FetchStationsTask.class.getSimpleName();
    private SettingsActivity mContext;
    private String mStation;
    private String mLine;
    private String APP_ID = "0399c806";
    private String API_KEY = "fe4d7f6e2a9cc15a96a76ce29ab0c069";

    public FetchStationsTask(SettingsActivity context, String station, String line) {
        mContext = context;
        mStation = station;
        mLine = line;
    }

    @Override
    protected List<Tuple> doInBackground(Long... params) {

        String url = String.format(Locale.ENGLISH, "http://transportapi.com/v3/uk/tube/%s.json?app_id=%s&api_key=%s", mLine, APP_ID, API_KEY);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        TFLJSONParser tfljsonParser = new TFLJSONParser();
        return tfljsonParser.getStationsList(jsonObject);
    }

    protected void onPostExecute(List<Tuple> stationList) {
        mContext.renderResult(stationList);
    }
}
