package com.labs.timo.verify.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by tim on 9/15/2016.
 */
public class VerifyProvider extends ContentProvider {

    private VerifyDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int LOCATION = 100;

    private static final String sLocationSettingSelection =
            VerifyContract.LocationEntry.TABLE_NAME +
                    //       "." + RunnerContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
                    "." + VerifyContract.LocationEntry.COLUMN_DATE_TIME + " = ? ";
    /**
     * onCreate called when the database is created
     * @return
     */
    @Override
    public boolean onCreate() {
        //  Log.d(LOG_TAG, "IN onCreate");
        mOpenHelper = new VerifyDbHelper(getContext());
        return true;
    }
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case LOCATION:
                return VerifyContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    /**
     * Call to update database table records
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case LOCATION:
                rowsUpdated = db.update(VerifyContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
    /**
     * call to insert database table records
     * @param uri
     * @param values
     * @return
     */
    @Override
    public  Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        //   Log.d(LOG_TAG, "IN insert" + match);
        switch (match) {
            case LOCATION: {
                long _id = db.insert(VerifyContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = VerifyContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    /**
     * call o delete database table records
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case LOCATION:
                rowsDeleted = db.delete(
                        VerifyContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
    /**
     * call to query database tables for records
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        VerifyContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VerifyContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, VerifyContract.PATH_LOCATION, LOCATION);
        return matcher;
    }
}
