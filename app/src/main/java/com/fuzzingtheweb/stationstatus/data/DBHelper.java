package com.fuzzingtheweb.stationstatus.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "FeedReader.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_STATION_ENTRIES =
            "CREATE TABLE " + StationEntry.TABLE_NAME + " (" +
                    StationEntry._ID + " INTEGER PRIMARY KEY," +
                    StationEntry.COLUMN_NAME_STATION_NAME + TEXT_TYPE + COMMA_SEP +
                    StationEntry.COLUMN_NAME_STATION_CODE + TEXT_TYPE + COMMA_SEP +
                    StationEntry.COLUMN_NAME_LINE_CODE + TEXT_TYPE + COMMA_SEP +
                    StationEntry.COLUMN_NAME_LINE_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_LINE_STATION_ENTRIES =
            "CREATE TABLE " + LineStationEntry.TABLE_NAME + " (" +
                    LineStationEntry._ID + " INTEGER PRIMARY KEY," +
                    LineStationEntry.COLUMN_NAME_STATION_NAME + TEXT_TYPE + COMMA_SEP +
                    LineStationEntry.COLUMN_NAME_STATION_CODE + TEXT_TYPE + COMMA_SEP +
                    LineStationEntry.COLUMN_NAME_LINE_CODE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_STATION_ENTRIES =
            "DROP TABLE IF EXISTS " + StationEntry.TABLE_NAME;

    private static final String SQL_DELETE_LINE_STATION_ENTRIES =
            "DROP TABLE IF EXISTS " + StationEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATION_ENTRIES);
        db.execSQL(SQL_CREATE_LINE_STATION_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_STATION_ENTRIES);
        db.execSQL(SQL_DELETE_LINE_STATION_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long addStation(Station station) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(StationEntry.COLUMN_NAME_STATION_NAME, station.getStationName());
        values.put(StationEntry.COLUMN_NAME_STATION_CODE, station.getStationCode());
        values.put(StationEntry.COLUMN_NAME_LINE_NAME, station.getLineName());
        values.put(StationEntry.COLUMN_NAME_LINE_CODE, station.getLineCode());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(StationEntry.TABLE_NAME, null, values);
    }

    public ArrayList<Station> getStationList() {
        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StationEntry._ID,
                StationEntry.COLUMN_NAME_STATION_NAME,
                StationEntry.COLUMN_NAME_STATION_CODE,
                StationEntry.COLUMN_NAME_LINE_NAME,
                StationEntry.COLUMN_NAME_LINE_CODE,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = StationEntry.COLUMN_NAME_STATION_NAME + " ASC";

        Cursor cursor = db.query(
                StationEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Station> stationList = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            int idIndex = cursor.getColumnIndex(StationEntry._ID);
            int stationNameIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_STATION_NAME);
            int stationCodeIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_STATION_CODE);
            int lineNameIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_LINE_NAME);
            int lineCodeIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_LINE_CODE);

            Station station = new Station(
                    cursor.getString(idIndex),
                    cursor.getString(stationNameIndex),
                    cursor.getString(stationCodeIndex),
                    cursor.getString(lineNameIndex),
                    cursor.getString(lineCodeIndex)
            );
            stationList.add(station);
        }

        return stationList;
    }

    public long deleteStation(Station station) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = StationEntry._ID + " = " + station.getId();
        return db.delete(StationEntry.TABLE_NAME, selection, null);
    }

    public boolean addLineStationList(List<LineStation> lineStationList) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values;

        db.beginTransaction();
        try {
            for (LineStation lineStation : lineStationList) {
                // Create a new map of values, where column names are the keys
                values = new ContentValues();

                values.put(LineStationEntry.COLUMN_NAME_STATION_NAME, lineStation.getStationName());
                values.put(LineStationEntry.COLUMN_NAME_STATION_CODE, lineStation.getStationCode());
                values.put(LineStationEntry.COLUMN_NAME_LINE_CODE, lineStation.getLineCode());

                // Insert the new row, returning the primary key value of the new row
                db.insert(LineStationEntry.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Error in transaction", e.toString());
            return false;
        }

        db.endTransaction();
        return true;
    }

    /**
     * Check if for a given line there are LineStations
     * @param lineCode code of the line to check
     * @return true if the line has LineStation entries
     */
    public ArrayList<LineStation> getLineStations(String lineCode) {
        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LineStationEntry._ID,
                LineStationEntry.COLUMN_NAME_STATION_NAME,
                LineStationEntry.COLUMN_NAME_STATION_CODE,
                LineStationEntry.COLUMN_NAME_LINE_CODE
        };

        // We're interested in LineStation with the lineCode
        String whereClause = LineStationEntry.COLUMN_NAME_LINE_CODE + " = ?";
        String[] whereArgs = new String[] {
                lineCode
        };

        Cursor cursor = db.query(
                LineStationEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereClause,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        ArrayList<LineStation> lineStationList = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            int stationNameIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_STATION_NAME);
            int stationCodeIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_STATION_CODE);
            int lineCodeIndex = cursor.getColumnIndex(StationEntry.COLUMN_NAME_LINE_CODE);

            LineStation lineStation = new LineStation(
                    cursor.getString(stationNameIndex),
                    cursor.getString(stationCodeIndex),
                    cursor.getString(lineCodeIndex)
            );
            lineStationList.add(lineStation);
        }

        return lineStationList;
    }

    public static abstract class LineStationEntry implements BaseColumns {
        public static final String TABLE_NAME = "line_station";
        public static final String COLUMN_NAME_STATION_NAME = "station_name";
        public static final String COLUMN_NAME_STATION_CODE = "station_code";
        public static final String COLUMN_NAME_LINE_CODE = "line_code";
    }

    public static abstract class StationEntry implements BaseColumns {
        public static final String TABLE_NAME = "station";
        public static final String COLUMN_NAME_STATION_NAME = "station_name";
        public static final String COLUMN_NAME_STATION_CODE = "station_code";
        public static final String COLUMN_NAME_LINE_NAME = "line_name";
        public static final String COLUMN_NAME_LINE_CODE = "line_code";
    }
}