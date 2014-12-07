package com.fuzzingtheweb.stationstatus.tasks;

import android.os.AsyncTask;

import com.fuzzingtheweb.stationstatus.util.JSONParser;
import com.fuzzingtheweb.stationstatus.util.TFLJSONParser;
import com.fuzzingtheweb.stationstatus.util.Tuple;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class FetchStationsTask extends AsyncTask<Long, Void, List<Tuple>> {

    private static final String LOG_TAG = FetchStationsTask.class.getSimpleName();
    private String mLine;
    private OnStationsFetched mListener;
    private String APP_ID = "0399c806";
    private String API_KEY = "fe4d7f6e2a9cc15a96a76ce29ab0c069";

    public FetchStationsTask(OnStationsFetched listener, String line) {
        mListener = listener;
        mLine = line;
    }

    @Override
    protected List<Tuple> doInBackground(Long... params) {
        String url = String.format(Locale.ENGLISH, "http://transportapi.com/v3/uk/tube/%s.json?app_id=%s&api_key=%s", mLine, APP_ID, API_KEY);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = jsonParser.getJSONFromUrl(url);
        TFLJSONParser tfljsonParser = new TFLJSONParser();
        try {
            return tfljsonParser.getStationsList(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Tuple> stationList) {
        mListener.onStationsFetched(stationList);
    }
}
