package com.fuzzingtheweb.stationstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fuzzingtheweb.stationstatus.tasks.FetchStationsTask;
import com.fuzzingtheweb.stationstatus.tasks.OnStationsFetched;
import com.fuzzingtheweb.stationstatus.util.PreferencesHandler;
import com.fuzzingtheweb.stationstatus.util.Tuple;

import java.util.List;


public class MyStationsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stations);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MyStationsFragment())
                    .commit();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MyStationsFragment extends DialogFragment {

        private String LOG_TAG = MyStationsFragment.class.getSimpleName();
        private CharSequence[] mLineNames;
        private CharSequence[] mLineKeys;
        private TextView mSelectedStation;

        public MyStationsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my_stations, container, false);

            mLineNames = getActivity().getResources().getTextArray(R.array.pref_list_line_names);
            mLineKeys = getActivity().getResources().getTextArray(R.array.pref_list_line_values);
            mSelectedStation = (TextView) rootView.findViewById(R.id.selected_station);

            String station = PreferencesHandler.getStation(getActivity());
            String line = PreferencesHandler.getLine(getActivity());

            if (station == null || line == null) {
                mSelectedStation.setVisibility(View.GONE);
            } else {
                mSelectedStation.setText("Line: " + line + ", Station: " + station);
            }

            rootView.findViewById(R.id.new_station).setOnClickListener(openDialogChooseLine);
            return rootView;
        }

        private void refreshSelectedStation() {
            String station = PreferencesHandler.getStation(getActivity());
            String line = PreferencesHandler.getLine(getActivity());
            mSelectedStation.setText("Line: " + line + ", Station: " + station);
            mSelectedStation.setVisibility(View.VISIBLE);
        }

        private View.OnClickListener openDialogChooseLine = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Pick a line")
                        .setItems(mLineNames, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final CharSequence selectedLine = mLineKeys[which];
                                Log.d(LOG_TAG, "Selected line: " + selectedLine);
                                OnStationsFetched onStationsFetched = new OnStationsFetched() {
                                    @Override
                                    public void onStationsFetched(List<Tuple> tupleList) {
                                        chooseStation(selectedLine.toString(), tupleList);
                                    }
                                };
                                FetchStationsTask stationsTask = new FetchStationsTask(onStationsFetched, selectedLine.toString());
                                stationsTask.execute();
                            }
                        });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        public void chooseStation(final String line, List<Tuple> stationTupleList) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final CharSequence[] stationNameList = new CharSequence[stationTupleList.size()];
            final CharSequence[] stationKeyList = new CharSequence[stationTupleList.size()];
            for (int i = 0; i < stationTupleList.size(); i++) {
                stationNameList[i] = (String) stationTupleList.get(i).getLeft();
                stationKeyList[i] = (String) stationTupleList.get(i).getRight();
            }

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle("Pick a station")
                    .setItems(stationNameList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CharSequence selectedStation = stationKeyList[which];
                            PreferencesHandler.setStation(getActivity(), selectedStation.toString());
                            PreferencesHandler.setLine(getActivity(), line);
                            refreshSelectedStation();
                        }
                    });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
