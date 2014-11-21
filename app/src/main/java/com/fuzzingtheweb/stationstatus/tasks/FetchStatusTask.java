package com.fuzzingtheweb.stationstatus.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import com.fuzzingtheweb.stationstatus.MainActivity;
import com.fuzzingtheweb.stationstatus.R;
import com.fuzzingtheweb.stationstatus.TflXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class FetchStatusTask extends AsyncTask<Long, Void, List<TflXmlParser.Entry>> {

    private static final String LOG_TAG = FetchStatusTask.class.getSimpleName();
    private MainActivity.StatusDetailFragment mContext;

    public FetchStatusTask(MainActivity.StatusDetailFragment context) {
        mContext = context;
    }

    @Override
    protected List<TflXmlParser.Entry> doInBackground(Long... params) {

        Uri.Builder b = Uri.parse("http://cloud.tfl.gov.uk").buildUpon();
        b.path("/TrackerNet/PredictionDetailed/N/HND");
        String url = b.build().toString();

        try {
            return loadXmlFromNetwork(url);
        } catch (IOException e) {
            return null;
        } catch (XmlPullParserException e) {
            return null;
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.

    /**
     *
     * @param urlString
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<TflXmlParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;

        TflXmlParser tflXmlParser = new TflXmlParser();
        List<TflXmlParser.Entry> entryList = null;
        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + mContext.getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + mContext.getString(R.string.updated) + " " +
                formatter.format(rightNow.getTime()) + "</em>");

        try {
            stream = downloadUrl(urlString);
            entryList = tflXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return entryList;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    protected void onPostExecute(List<TflXmlParser.Entry> entryList) {
        mContext.renderResult(entryList);
    }
}
