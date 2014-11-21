package com.fuzzingtheweb.stationstatus;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzzingtheweb.stationstatus.tasks.FetchStatusTask;

import java.util.List;


public class MainActivity extends ActionBarActivity {

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
            fragment.loadStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StatusDetailFragment extends Fragment {

        private ListView mListView;
        private RelativeLayout mProgressLayout;
        private static final String LOG_TAG = StatusDetailFragment.class.getSimpleName();

        public StatusDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mListView = ((ListView) rootView.findViewById(android.R.id.list));
            mProgressLayout = ((RelativeLayout) rootView.findViewById(android.R.id.empty));

            loadStatus();
            return rootView;
        }

        public void showStatus() {
            mProgressLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }

        public void hideStatus() {
            mListView.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.VISIBLE);
        }

        public void loadStatus() {
            hideStatus();
            FetchStatusTask fetchStatusTask = new FetchStatusTask(this);
            fetchStatusTask.execute();
        }

        public void renderResult(final List<Entry> entryList) {
            showStatus();


            ArrayAdapter<Entry> commentListAdapter = new ArrayAdapter<Entry> (
                    getActivity(),
                    R.layout.entry_item,
                    R.id.item_title,
                    entryList)
            {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Entry entry = entryList.get(position);

                    ((TextView) view.findViewById(R.id.item_title))
                            .setText(entry.destination + " - " + entry.getTimeTo());
                    ((TextView) view.findViewById(R.id.item_subtitle))
                            .setText(entry.location + " at " + entry.departTime);

                    return view;
                }
            };

            try {
                mListView.setAdapter(commentListAdapter);
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}
