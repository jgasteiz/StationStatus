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

    private static final String UPDATE_ACTION = "com.fuzzingtheweb.stationstatus.update_action";
    private static final String WIDGET_IDS_KEY ="mywidgetproviderwidgetids";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateStatus(context, appWidgetManager, appWidgetIds[i]);
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
            updateStatus(context, AppWidgetManager.getInstance(context), id);
        }
        super.onReceive(context, intent);
    }

    private void updateStatus(final Context context, final AppWidgetManager appWidgetManager,
                              final int appWidgetId) {
        // Show loading
        showLoadingAppWidget(context, appWidgetManager, appWidgetId);

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Station> stationList = dbHelper.getStationList();
        Station station = stationList.get(0);

        OnStatusesFetched onStatusesFetched = new OnStatusesFetched() {
            @Override
            public void onStatusesFetched(List<Platform> platformList) {
                updateAppWidget(context, appWidgetManager, appWidgetId, getResult(platformList));
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
                                int appWidgetId, Tuple<String, String> content) {

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        views.setTextViewText(R.id.appwidget_title, content.getLeft());
        views.setTextViewText(R.id.appwidget_text, Html.fromHtml(content.getRight()));

        // Create an Intent to launch ExampleActivity
        Intent updateIntent = new Intent(context, StatusWidget.class);
        updateIntent.setAction(UPDATE_ACTION);
        updateIntent.putExtra(StatusWidget.WIDGET_IDS_KEY, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.action_settings, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void showLoadingAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        views.setTextViewText(R.id.appwidget_text, "Loading");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


