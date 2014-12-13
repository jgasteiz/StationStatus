package com.fuzzingtheweb.stationstatus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.RemoteViews;

import com.fuzzingtheweb.stationstatus.data.DBHelper;
import com.fuzzingtheweb.stationstatus.data.Station;
import com.fuzzingtheweb.stationstatus.tasks.FetchStatusTask;
import com.fuzzingtheweb.stationstatus.tasks.OnStatusesFetched;
import com.fuzzingtheweb.stationstatus.tasks.Platform;
import com.fuzzingtheweb.stationstatus.tasks.StatusEntry;
import com.fuzzingtheweb.stationstatus.util.Tuple;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class StatusWidget extends AppWidgetProvider {

    private static final String UPDATE_ACTION = "com.fuzzingtheweb.stationstatus.UPDATE_ACTION";
    private static final String SWITCH_ACTION = "com.fuzzingtheweb.stationstatus.SWITCH_ACTION";
    private static final String WIDGET_IDS_KEY ="mywidgetproviderwidgetids";
    private static final String CURRENT_INDEX ="current_index";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateStatus(context, appWidgetManager, appWidgetIds[i], 0);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UPDATE_ACTION)) {
            int id = intent.getExtras().getInt(WIDGET_IDS_KEY);
            int currentIndex = intent.getExtras().getInt(CURRENT_INDEX);
            updateStatus(context, AppWidgetManager.getInstance(context), id, currentIndex);
        } else if (intent.getAction().equals(SWITCH_ACTION)) {
            int id = intent.getExtras().getInt(WIDGET_IDS_KEY);
            int previousIndex = intent.getExtras().getInt(CURRENT_INDEX);
            updateStatus(context, AppWidgetManager.getInstance(context), id, previousIndex + 1);
        }
        super.onReceive(context, intent);
    }

    private void updateStatus(final Context context, final AppWidgetManager appWidgetManager,
                              final int appWidgetId, int newStationIndex) {
        // Show loading
        showLoadingAppWidget(context, appWidgetManager, appWidgetId);

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Station> stationList = dbHelper.getStationList();
        if (stationList.size() <= newStationIndex) {
            newStationIndex = 0;
        }
        Station station = stationList.get(newStationIndex);

        final int stationIndex = newStationIndex;

        OnStatusesFetched onStatusesFetched = new OnStatusesFetched() {
            @Override
            public void onStatusesFetched(List<Platform> platformList) {
                updateAppWidget(context, appWidgetManager, appWidgetId, getResult(platformList), stationIndex);
            }
        };

        FetchStatusTask fetchStatusTask = new FetchStatusTask(
                onStatusesFetched,
                station.getStationCode(),
                station.getLineCode());
        fetchStatusTask.execute();
    }

    public Tuple<String, String> getResult(final List<Platform> platformList) {

        // If there's nothing, show message saying so.
        if (platformList.size() == 0) {
            return new Tuple<>("There is no incoming data", null);
        }

        String title = platformList.get(0).getStationName();

        String result = "";

        for (final Platform platform : platformList) {
            int numEntries = 0;
            for (final StatusEntry statusEntry : platform.getStatusEntryList()) {
                if (numEntries == 3) {
                    break;
                }
                result = result + statusEntry.destination + " - <strong>" + statusEntry.getTimeTo() + "</strong><br/>";
                numEntries++;
            }
            result = result + "<hr>";
        }

        return new Tuple<>(title, result);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Tuple<String, String> content, int stationIndex) {

        PendingIntent pendingIntent;

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        views.setTextViewText(R.id.appwidget_title, content.getLeft());
        views.setTextViewText(R.id.appwidget_text, Html.fromHtml(content.getRight()));

        Intent updateIntent = new Intent(context, StatusWidget.class);
        updateIntent.setAction(UPDATE_ACTION);
        updateIntent.putExtra(StatusWidget.WIDGET_IDS_KEY, appWidgetId);
        updateIntent.putExtra(StatusWidget.CURRENT_INDEX, stationIndex);
        pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.action_settings, pendingIntent);

        Intent switchStationIntent = new Intent(context, StatusWidget.class);
        switchStationIntent.setAction(SWITCH_ACTION);
        switchStationIntent.putExtra(StatusWidget.WIDGET_IDS_KEY, appWidgetId);
        switchStationIntent.putExtra(StatusWidget.CURRENT_INDEX, stationIndex);
        pendingIntent = PendingIntent.getBroadcast(context, 0, switchStationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.station_data, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void showLoadingAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        views.setTextViewText(R.id.appwidget_title, "Loading");
        views.setTextViewText(R.id.appwidget_text, "");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


