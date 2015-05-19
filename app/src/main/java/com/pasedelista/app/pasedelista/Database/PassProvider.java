/*
 * TravelGuideProvider.java 1.0 2014/02/18
 * 
 * Touchtastic (c) 2014
 */
package com.pasedelista.app.pasedelista.Database;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class PassProvider extends ContentProvider {

    private static final String LOG_TAG = PassProvider.class.getSimpleName();

    private static final int CODE_SESSION = 1;
    private static final int CODE_LIST_STUDENTS = 2;
    private static final int CODE_LIST_SESSION_STUDENTS = 3;
    private static final int CODE_LIST_SESSIONS = 4;
    private static final int CODE_ADD_ASSISTANCE = 5;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PassDatabase mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final String authority = PassContract.CONTENT_AUTHORITY;
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Sessions URIs
        matcher.addURI(authority, PassContract.PATH_SESSIONS, CODE_LIST_SESSIONS);
        matcher.addURI(authority, PassContract.PATH_SESSIONS + "/#", CODE_SESSION);

        //Assistance URIs
        matcher.addURI(authority, PassContract.PATH_ASSISTANCE, CODE_ADD_ASSISTANCE);

        //Students URIs
        matcher.addURI(authority, PassContract.PATH_STUDENTS, CODE_LIST_STUDENTS);
        matcher.addURI(authority, PassContract.PATH_STUDENTS + "/#", CODE_LIST_SESSION_STUDENTS);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_SESSION:
                return PassContract.Session.CONTENT_TYPE;
            case CODE_LIST_STUDENTS:
                return PassContract.Student.CONTENT_TYPE;
            case CODE_LIST_SESSION_STUDENTS:
                return PassContract.Student.CONTENT_TYPE;
            case CODE_LIST_SESSIONS:
                return PassContract.Session.CONTENT_TYPE;
            case CODE_ADD_ASSISTANCE:
                return PassContract.Assistance.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);

        }
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new PassDatabase(context);
        return true;
    }

    /**
     * Apply the given set of {@link android.content.ContentProviderOperation}, executing inside a
     * {@link android.database.sqlite.SQLiteDatabase} transaction. All changes will be rolled back if any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i)
                        .apply(this /* provider */, results /* backRefs */, i /* numBackRefs */);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        String id;
        String limit = null;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        final int match = sUriMatcher.match(uri);


        switch (match) {
            case CODE_LIST_SESSION_STUDENTS:
                id = PassContract.Session.getSessionId(uri);
              //  queryBuilder.setTables("SELECT * FROM " + PassDatabase.Tables.STUDENT
                //        + " WHERE " + PassContract.Student.DNI + " IN ( SELECT * FROM " + PassDatabase.Tables.ASSISTANCE + " WHERE "
                  //      + PassContract.Assistance.SESSION_NUM + " = " + id + ")");

                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("(SELECT *")
                        .append(" FROM ").append( PassDatabase.Tables.ASSISTANCE ).append(" INNER JOIN ").append(PassDatabase.Tables.STUDENT )
                        .append(" ON ").append(PassContract.Assistance.DNI).append(" = ").append(PassContract.Student.DNI).append(")");

                queryBuilder.appendWhere( PassContract.Assistance.SESSION_NUM + " = " + id );
                queryBuilder.setTables( strBuilder.toString() );

                break;
            case CODE_LIST_STUDENTS:
                queryBuilder.setTables(PassDatabase.Tables.STUDENT);
                break;
            case CODE_SESSION:
                queryBuilder.setTables(PassDatabase.Tables.SESSIONS);

                String sessionNumber = PassContract.Session.getSessionId( uri );
                queryBuilder.appendWhere(PassContract.Session.SESSION_NUM + " = " + sessionNumber);
                break;
            case CODE_LIST_SESSIONS:
                queryBuilder.setTables(PassDatabase.Tables.SESSIONS);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        String query = queryBuilder.buildQuery(projection, selection, null /*groupBy*/, null, sortOrder, limit);
        Log.d("RAW QUERY", query);

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                null /* groupBy */, null /* having */, sortOrder, limit);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final Uri resultUri;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_ADD_ASSISTANCE:
                db.insert(PassDatabase.Tables.ASSISTANCE, null /*nullColumnHack*/, values);
                resultUri = Uri.parse(PassContract.PATH_ASSISTANCE + "/#");
                break;
            case CODE_SESSION:
                db.insert(PassDatabase.Tables.SESSIONS, null /*nullColumnHack*/, values);
                resultUri = Uri.parse(PassContract.PATH_SESSIONS + "/#");
                break;
            case CODE_LIST_STUDENTS:
                db.insert(PassDatabase.Tables.STUDENT, null /* nullColumnHack */, values);
                resultUri = Uri.parse(PassContract.PATH_STUDENTS);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri + " : " + match);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0; /* function not implemented */
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0; /* function not implemented */
    }

}
