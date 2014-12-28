package com.fuzzingtheweb.stationstatus;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.fuzzingtheweb.stationstatus.interfaces.OnBusStopsFetched;
import com.fuzzingtheweb.stationstatus.tasks.BusStop;
import com.fuzzingtheweb.stationstatus.tasks.FetchBusStopsTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private boolean mRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_refresh:
                loadStops(mMap.getCameraPosition().target);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                double latitude = 51.583476;
                double longitude = -0.222942;
                loadStops(new LatLng(latitude, longitude));
            }
        }
    }

    public void showProgressBar() {
        mRefreshing = true;
        invalidateOptionsMenu();
    }

    public void hideProgressBar() {
        mRefreshing = false;
        invalidateOptionsMenu();
    }

    private void loadStops(LatLng latLng) {
        mMap.clear();
        showProgressBar();

        OnBusStopsFetched onBusStopsFetched = new OnBusStopsFetched() {
            @Override
            public void onBusStopsFetched(List<BusStop> busStopList) {
                renderStops(busStopList);
            }
        };
        FetchBusStopsTask fetchBusStopsTask = new FetchBusStopsTask(onBusStopsFetched, latLng.latitude, latLng.longitude);
        fetchBusStopsTask.execute();
    }

    private void renderStops(List<BusStop> busStopList) {
        double latitude;
        double longitude;
        String stopName;

        LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();

        for (BusStop busStop : busStopList) {
            latitude = busStop.getLatitude();
            longitude = busStop.getLongitude();
            stopName = busStop.getName();

            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stopName));
            latLngBoundsBuilder.include(new LatLng(latitude, longitude));
        }

        LatLngBounds bounds = latLngBoundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));

        hideProgressBar();
    }
}
