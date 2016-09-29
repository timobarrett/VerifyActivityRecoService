package com.labs.timo.verify.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tim on 9/15/2016.
 */
    public class VerifyDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        private static final int DATABASE_VERSION = 1;

        static final String DATABASE_NAME = "verify.db";

        public VerifyDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * onCreate call for the database
         * @param sqLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            //    Log.d("RUNNERDBHELPER", "IN onCreate");
            // Create a table to hold locations.  A location consists of the string supplied in the
            // location setting, the city name, and the latitude and longitude
            final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + VerifyContract.LocationEntry.TABLE_NAME + " (" +
                    VerifyContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                    VerifyContract.LocationEntry.COLUMN_DATE_TIME + " INTEGER NOT NULL, " +
                    VerifyContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                    VerifyContract.LocationEntry.COLUMN_COORD_LON + " REAL NOT NULL, " +
                    VerifyContract.LocationEntry.COLUMN_ACTIVITY + " TEXT NOT NULL " +
                    " );";
            sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        }

        /**
         * onUpgrade will be called when the database version is incremented
         * @param sqLiteDatabase
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VerifyContract.LocationEntry.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }

    }
