package com.sivag1.juiceboard.data;

/**
 * Created by sivag1 on 1/31/16.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;



public class MyProvider extends ContentProvider
{
    public static String AUTHORITY = "com.sivag1.juiceboard.data";
    public static final Uri JUICELEVELS_URI = Uri.parse("content://"+AUTHORITY+"/juicelevel");

    private MyDBHelper mDatabaseHelper;
    private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int JUICELEVEL = 0;
    private static final int JUICELEVELS = 1;

    static {
        sMatcher.addURI(AUTHORITY, "juicelevel/#", JUICELEVEL);
        sMatcher.addURI(AUTHORITY, "juicelevel", JUICELEVELS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case JUICELEVELS:
                rowsDeleted = cupboard().withDatabase(db).delete(JuiceLevel.class, selection, args);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public boolean onCreate() {
        mDatabaseHelper = new MyDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
            case JUICELEVELS:
                // this is the full query syntax, most of the time you can leave out projection etc
                // if the content provider returns a fixed set of data
                return cupboard().withDatabase(db).query(JuiceLevel.class).
                        withProjection(projection).
                        withSelection(selection, selectionArgs).
                        orderBy(sortOrder).
                        getCursor();
            case JUICELEVEL:
                return cupboard().withDatabase(db).query(JuiceLevel.class).
                        byId(ContentUris.parseId(uri)).
                        getCursor();
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
            case JUICELEVELS:
                cupboard().withDatabase(db).update(JuiceLevel.class, values, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
            case JUICELEVELS:
                cupboard().withDatabase(db).put(JuiceLevel.class, contentValues);
        }
        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (sMatcher.match(uri)) {
            case JUICELEVELS:
                for (ContentValues value : values) {
                    insert(uri, value);
                }

        }
        return 0;
    }
}

