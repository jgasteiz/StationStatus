package com.fuzzingtheweb.stationstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.fuzzingtheweb.stationstatus.data.DBHelper;
import com.fuzzingtheweb.stationstatus.data.LineStation;
import com.fuzzingtheweb.stationstatus.data.Station;
import com.fuzzingtheweb.stationstatus.tasks.FetchStationsTask;
import com.fuzzingtheweb.stationstatus.tasks.OnStationsFetched;

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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Pick a line")
                    .setItems(mLineNames, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final CharSequence selectedLine = mLineKeys[which];
                            Log.d(LOG_TAG, "Selected line: " + selectedLine);

                            // Check if the stations for this line are in the database
                            ArrayList<LineStation> lineStationList = mDbHelper.getLineStations(selectedLine.toString());

                            if (lineStationList.size() > 0) {
                                Log.d(LOG_TAG, "Stations are cached, fetching them from database");
                                chooseStation(selectedLine.toString(), lineStationList);
                            } else {
                                Log.d(LOG_TAG, "No cache, fetching them from API");
                                OnStationsFetched onStationsFetched = new OnStationsFetched() {
                                    @Override
                                    public void onStationsFetched(List<LineStation> lineStationList) {
                                        // Save them to the database
                                        mDbHelper.addLineStationList(lineStationList);
                                        chooseStation(selectedLine.toString(), lineStationList);
                                    }
                                };
                                FetchStationsTask stationsTask = new FetchStationsTask(onStationsFetched, selectedLine.toString());
                                stationsTask.execute();
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void chooseStation(final String line, List<LineStation> lineStationList) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final CharSequence[] stationNameList = new CharSequence[lineStationList.size()];
            final CharSequence[] stationKeyList = new CharSequence[lineStationList.size()];
            for (int i = 0; i < lineStationList.size(); i++) {
                stationNameList[i] = lineStationList.get(i).getStationName();
                stationKeyList[i] = lineStationList.get(i).getStationCode();
            }

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle("Pick a station")
                    .setItems(stationNameList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Station station = new Station(
                                    stationNameList[which].toString(),
                                    stationKeyList[which].toString(),
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
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
