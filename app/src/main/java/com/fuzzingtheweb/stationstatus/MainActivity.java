package com.fuzzingtheweb.stationstatus;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzzingtheweb.stationstatus.tasks.FetchStatusTask;

import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new StatusDetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            StatusDetailFragment fragment = ((StatusDetailFragment) getFragmentManager().findFragmentById(R.id.container));
            fragment.loadContent();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StatusDetailFragment extends Fragment {

        private LinearLayout mLayout;
        private RelativeLayout mProgressLayout;
        private LayoutInflater mLayoutInflater;
        private String mStation;
        private static final int NUM_MAX_ENTRIES = 3;
        private static final String LOG_TAG = StatusDetailFragment.class.getSimpleName();

        public StatusDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mLayout = ((LinearLayout) rootView.findViewById(android.R.id.content));
            mProgressLayout = ((RelativeLayout) rootView.findViewById(android.R.id.empty));
            mLayoutInflater = getActivity().getLayoutInflater();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

            mStation = settings.getString("example_list", null);

            loadContent();
            return rootView;
        }

        public void showContent() {
            mProgressLayout.setVisibility(View.GONE);
            mLayout.setVisibility(View.VISIBLE);
        }

        public void hideContent() {
            mLayout.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.VISIBLE);
        }

        public void loadContent() {
            if (mStation == null) {
                mLayout.removeAllViews();
                View view = mLayoutInflater.inflate(R.layout.platform_item, mLayout, false);
                ((TextView) view.findViewById(android.R.id.title)).setText("You must select a station in settings");
                mLayout.addView(view);
                return;
            }
            hideContent();
            FetchStatusTask fetchStatusTask = new FetchStatusTask(this, mStation);
            fetchStatusTask.execute();
        }

        public void setActionBarTitle(String title) {
            getActivity().getActionBar().setTitle(title);
        }

        public void renderResult(final List<Platform> platformList) {
            showContent();

            // Empty main layout.
            mLayout.removeAllViews();

            // If there's nothing, show message saying so.
            if (platformList.size() == 0) {
                View view = mLayoutInflater.inflate(R.layout.platform_item, mLayout, false);
                ((TextView) view.findViewById(android.R.id.title)).setText("There is no incoming data");
                mLayout.addView(view);
                return;
            }

            setActionBarTitle(platformList.get(0).getStationName());

            View platformView;
            View entryView;

            for (final Platform platform : platformList) {
                platformView = mLayoutInflater.inflate(R.layout.platform_item, mLayout, false);
                ((TextView) platformView.findViewById(android.R.id.title))
                        .setText(platform.getDirection());

                LinearLayout entryLayout = ((LinearLayout) platformView.findViewById(android.R.id.content));

                int numEntries = 0;
                for (final Entry entry : platform.getEntryList()) {
                    if (numEntries == NUM_MAX_ENTRIES) {
                        break;
                    }
                    entryView = mLayoutInflater.inflate(R.layout.entry_item, entryLayout, false);

                    ((TextView) entryView.findViewById(R.id.item_title))
                            .setText(entry.destination + " - " + entry.getTimeTo());
                    ((TextView) entryView.findViewById(R.id.item_subtitle))
                            .setText(entry.location);

                    entryLayout.addView(entryView);
                    numEntries++;
                }

                mLayout.addView(platformView);
            }
        }
    }
}
