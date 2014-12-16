package com.fuzzingtheweb.stationstatus;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fuzzingtheweb.stationstatus.data.DBHelper;
import com.fuzzingtheweb.stationstatus.data.Station;
import com.fuzzingtheweb.stationstatus.interfaces.OnTubeStatusesFetched;
import com.fuzzingtheweb.stationstatus.tasks.FetchTubeStatusTask;
import com.fuzzingtheweb.stationstatus.tasks.Platform;
import com.fuzzingtheweb.stationstatus.tasks.StatusEntry;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private boolean mRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, StatusDetailFragment.newInstance(position))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem refreshingLayout = menu.findItem(R.id.refreshing);
        if (mRefreshing) {
            refreshingLayout.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            refreshingLayout.setVisible(false);
        }
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
            Intent intent = new Intent(this, MyStationsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void showProgressBar() {
        mRefreshing = true;
        invalidateOptionsMenu();
    }

    public void hideProgressBar() {
        mRefreshing = false;
        invalidateOptionsMenu();
    }

    public static class StatusDetailFragment extends Fragment {

        private Button mSettingsButton;
        private int mStationIndex;
        private DBHelper mDbHelper;
        private View mRootView;
        private Station mSelectedStation;
        private static final int NUM_MAX_ENTRIES = 3;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static StatusDetailFragment newInstance(int stationIndex) {
            StatusDetailFragment fragment = new StatusDetailFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, stationIndex);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mRootView = inflater.inflate(R.layout.fragment_main, container, false);

            mSettingsButton = ((Button) mRootView.findViewById(R.id.add_a_station));
            mSettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MyStationsActivity.class);
                    intent.putExtra(MyStationsActivity.NO_STATIONS, true);
                    startActivity(intent);
                }
            });

            mDbHelper = new DBHelper(getActivity());

            loadContent();

            return mRootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mStationIndex = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        public void loadContent() {

            if (!isNetworkAvailable()) {
                mRootView.findViewById(R.id.no_connection).setVisibility(View.VISIBLE);
                return;
            }

            mRootView.findViewById(R.id.no_connection).setVisibility(View.GONE);

            ScrollView stationDataView = ((ScrollView) mRootView.findViewById(R.id.station_data));
            ArrayList<Station> stationList = mDbHelper.getStationList();

            if (stationList.size() > 0) {
                mSettingsButton.setVisibility(View.GONE);
                stationDataView.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).showProgressBar();

                OnTubeStatusesFetched onTubeStatusesFetched = new OnTubeStatusesFetched() {
                    @Override
                    public void onTubeStatusesFetched(List<Platform> platformList) {
                        renderResult(platformList);
                    }
                };

                mSelectedStation = stationList.get(mStationIndex);

                FetchTubeStatusTask fetchTubeStatusTask = new FetchTubeStatusTask(
                        onTubeStatusesFetched,
                        mSelectedStation.getStationCode(),
                        mSelectedStation.getLineCode());
                fetchTubeStatusTask.execute();
            } else {
                stationDataView.setVisibility(View.GONE);
                mSettingsButton.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).hideProgressBar();
            }
        }

        public void renderResult(final List<Platform> platformList) {
            ((MainActivity) getActivity()).hideProgressBar();

            LinearLayout mainLayout = ((LinearLayout) mRootView.findViewById(android.R.id.content));
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();

            // Empty main layout.
            mainLayout.removeAllViews();

            // If there's nothing, show message saying so.
            if (platformList.size() == 0) {
                mRootView.findViewById(R.id.no_connection).setVisibility(View.VISIBLE);
                ((TextView) mRootView.findViewById(R.id.no_connection)).setText("There is no incoming data");
                return;
            }

            String title = platformList.get(0).getStationName();
            getActivity().getActionBar().setTitle(title);

            View platformView;
            View entryView;

            for (final Platform platform : platformList) {
                platformView = layoutInflater.inflate(R.layout.platform_item, mainLayout, false);
                ((TextView) platformView.findViewById(android.R.id.title))
                        .setText(platform.getDirection());

                LinearLayout entryLayout = ((LinearLayout) platformView.findViewById(android.R.id.content));

                int numEntries = 0;
                for (final StatusEntry statusEntry : platform.getStatusEntryList()) {
                    if (numEntries == NUM_MAX_ENTRIES) {
                        break;
                    }
                    entryView = layoutInflater.inflate(R.layout.status_entry_item, entryLayout, false);

                    int resourceId = getResources().getIdentifier(
                            mSelectedStation.getLineCode(), "drawable", getActivity().getPackageName());

                    ((ImageView) entryView.findViewById(R.id.station_icon))
                            .setImageDrawable(getResources().getDrawable(resourceId));
                    ((TextView) entryView.findViewById(R.id.item_destination))
                            .setText(statusEntry.destination);
                    ((TextView) entryView.findViewById(R.id.item_time_to))
                            .setText(statusEntry.getTimeTo());
                    ((TextView) entryView.findViewById(R.id.item_subtitle))
                            .setText(statusEntry.location);

                    entryLayout.addView(entryView);
                    numEntries++;
                }

                mainLayout.addView(platformView);
            }
        }
    }

}
