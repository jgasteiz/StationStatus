package com.fuzzingtheweb.stationstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fuzzingtheweb.stationstatus.data.DBHelper;
import com.fuzzingtheweb.stationstatus.data.LineStation;
import com.fuzzingtheweb.stationstatus.data.Station;
import com.fuzzingtheweb.stationstatus.interfaces.OnStationsFetched;
import com.fuzzingtheweb.stationstatus.tasks.FetchStationsTask;

import java.util.ArrayList;
import java.util.List;


public class MyStationsActivity extends Activity {

    public static final String NO_STATIONS = "no_stations";
    private static boolean mNoStations = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stations);

        Intent intent = getIntent();
        if (intent.hasExtra(NO_STATIONS)) {
            mNoStations = true;
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, MyStationsFragment.newInstance(mNoStations))
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MyStationsFragment extends ListFragment {

        private String LOG_TAG = MyStationsFragment.class.getSimpleName();
        private CharSequence[] mLineNames;
        private CharSequence[] mLineKeys;
        private ListView mStationListView;
        private ArrayList<Station> mStationList;
        private boolean mNoStations;

        private AlertDialog.Builder mBuilder;
        private AlertDialog mDialog;

        public final static int REMOVE_STATION = 0;

        private DBHelper mDbHelper;

        /**
         * Create a new instance of MyStationsFragment, providing "noStations"
         * as an argument.
         */
        static MyStationsFragment newInstance(boolean noStations) {
            MyStationsFragment f = new MyStationsFragment();

            Bundle args = new Bundle();
            args.putBoolean(MyStationsActivity.NO_STATIONS, noStations);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNoStations = getArguments() != null && getArguments().getBoolean(NO_STATIONS);
            mDbHelper = new DBHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my_stations, container, false);

            mLineNames = getActivity().getResources().getTextArray(R.array.pref_list_line_names);
            mLineKeys = getActivity().getResources().getTextArray(R.array.pref_list_line_values);
            mStationListView = (ListView) rootView.findViewById(android.R.id.list);

            mBuilder = new AlertDialog.Builder(getActivity());

            loadStations();

            rootView.findViewById(R.id.new_station).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseLine();
                }
            });

            // Open the station picker directly if there are no stations.
            if (mNoStations) {
                chooseLine();
            }

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            registerForContextMenu(mStationListView);
        }

        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            l.showContextMenuForChild(v);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(0, REMOVE_STATION, 0, R.string.remove_station);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            boolean result = super.onContextItemSelected(item);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Station station = mStationList.get(info.position);

            switch (item.getItemId()) {
                case MyStationsFragment.REMOVE_STATION:
                    long deleteResult = mDbHelper.deleteStation(station);
                    if (deleteResult > 0) {
                        Toast.makeText(
                                getActivity(),
                                station.getStationName() + " " + getResources().getString(R.string.station_deleted),
                                Toast.LENGTH_SHORT).show();
                        loadStations();
                    }
                    break;
            }
            return result;
        }

        private void loadStations() {
            mStationList = mDbHelper.getStationList();

            ArrayAdapter<Station> stationListAdapter = new ArrayAdapter<Station> (
                    getActivity(),
                    R.layout.station_item,
                    R.id.station_name,
                    mStationList)
            {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Station station = mStationList.get(position);

                    int resourceId = getResources().getIdentifier(
                            station.getLineCode(), "drawable", getActivity().getPackageName());
                    ((ImageView) view.findViewById(R.id.station_icon))
                            .setImageDrawable(getResources().getDrawable(resourceId));
                    ((TextView) view.findViewById(R.id.station_name))
                            .setText(station.getStationName());
                    ((TextView) view.findViewById(R.id.line_name))
                            .setText(station.getLineName());

                    return view;
                }
            };

            mStationListView.setAdapter(stationListAdapter);
        }

        public void chooseLine() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.dialog_layout, null);

            // Define titleView, listView and progressBar to be populated.
            final TextView titleView = (TextView) dialogLayout.findViewById(android.R.id.title);
            final ListView listView = (ListView) dialogLayout.findViewById(android.R.id.list);
            final ProgressBar progressBar = (ProgressBar) dialogLayout.findViewById(android.R.id.progress);

            // Define ArrayAdapter to set the list items.
            ArrayAdapter<CharSequence> lineListAdapter = new ArrayAdapter<CharSequence> (
                    getActivity(),
                    R.layout.dialog_list_item,
                    android.R.id.title,
                    mLineNames)
            {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ((TextView) view.findViewById(android.R.id.title))
                            .setText(mLineNames[position].toString());
                    return view;
                }
            };

            // Set title and items on the list.
            titleView.setText("Pick a line");
            listView.setAdapter(lineListAdapter);

            // Set click listeners on the list items.
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final CharSequence selectedLine = mLineKeys[position];
                    Log.d(LOG_TAG, "Selected line: " + selectedLine);

                    // Check if the stations for this line are in the database
                    ArrayList<LineStation> lineStationList = mDbHelper.getLineStations(selectedLine.toString());

                    if (lineStationList.size() > 0) {
                        Log.d(LOG_TAG, "Stations are cached, fetching them from database");
                        // Load the stations in the list
                        chooseStation(listView, titleView, selectedLine.toString(), lineStationList);
                    }
                    // Load it from the API if it's not stored.
                    else {
                        Log.d(LOG_TAG, "No cache, fetching them from API");

                        // Show progressbar
                        listView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        // Callback to run when the stations are fetched.
                        OnStationsFetched onStationsFetched = new OnStationsFetched() {
                            @Override
                            public void onStationsFetched(List<LineStation> lineStationList) {
                                // Save them to the database
                                mDbHelper.addLineStationList(lineStationList);

                                // Hide progressbar
                                progressBar.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);

                                // Load the stations in the list
                                chooseStation(listView, titleView, selectedLine.toString(), lineStationList);
                            }
                        };
                        FetchStationsTask stationsTask = new FetchStationsTask(onStationsFetched, selectedLine.toString());
                        stationsTask.execute();
                    }
                }
            });

            mBuilder.setView(dialogLayout);
            mDialog = mBuilder.create();
            mDialog.show();
        }

        public void chooseStation(ListView listView, TextView titleView, final String line, final List<LineStation> lineStationList) {

            // Define ArrayAdapter to set the station items.
            ArrayAdapter<LineStation> stationListAdapter = new ArrayAdapter<LineStation> (
                    getActivity(),
                    R.layout.dialog_list_item,
                    android.R.id.title,
                    lineStationList)
            {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ((TextView) view.findViewById(android.R.id.title))
                            .setText(lineStationList.get(position).getStationName());
                    return view;
                }
            };

            // Set title and items on the list.
            titleView.setText("Pick a station");
            listView.setAdapter(stationListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Station station = new Station(
                            lineStationList.get(position).getStationName(),
                            lineStationList.get(position).getStationCode(),
                            line,
                            line
                    );
                    long createResult = mDbHelper.addStation(station);
                    if (createResult > 0) {
                        Toast.makeText(
                                getActivity(),
                                station.getStationName() + " " + getResources().getString(R.string.station_added),
                                Toast.LENGTH_SHORT).show();
                        loadStations();
                        mDialog.hide();
                    }
                }
            });
        }
    }
}
