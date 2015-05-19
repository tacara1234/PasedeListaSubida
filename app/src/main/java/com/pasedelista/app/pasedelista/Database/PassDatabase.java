/*
 * TravelGuideDatabase.java 1.0 2014/02/18
 * 
 * Touchtastic (c) 2014
 */
package com.pasedelista.app.pasedelista.Database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;


public class PassDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "pass.db";

    public PassDatabase(Context context) {
        super(context, DATABASE_NAME, null /* cursorFactory */, DATABASE_VERSION);
    }

    public interface Tables {
        /* package */ static final String STUDENT = "student";
        /* package */ static final String ASSISTANCE = "assistance";
        /* package */ static final String SESSIONS = "sessions";
    }

    @SuppressLint("NewApi")
    @Override
    public void onConfigure (SQLiteDatabase db){
        super.onConfigure(db);
        //Activates the foreign keys
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);

        //Activates the foreign keys
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN ) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }
    }

    private void createTables(SQLiteDatabase db) {

        final StringBuilder strBuilder = new StringBuilder();

        /* Places table */
        strBuilder.append("CREATE TABLE ").append(Tables.STUDENT).append("(")
            .append(PassContract.Student.DNI).append(" TEXT PRIMARY KEY NOT NULL,")
            .append(PassContract.Student.NAME).append(" TEXT NOT NULL );");
        db.execSQL(strBuilder.toString());
        strBuilder.setLength(0);

        /* Places data table */
        strBuilder.append("CREATE TABLE ").append(Tables.SESSIONS).append("(")
                .append(PassContract.Session.SESSION_NUM).append(" INTEGER PRIMARY KEY,")
                .append(PassContract.Session.TYPE).append(" TEXT NOT NULL );");
        db.execSQL(strBuilder.toString());
        strBuilder.setLength(0);

        /* Places attributes table */
        strBuilder.append("CREATE TABLE ").append(Tables.ASSISTANCE).append("(")
                .append(PassContract.Assistance.DNI).append(" INTEGER NOT NULL,")
                .append(PassContract.Assistance.SESSION_NUM).append(" INTEGER NOT NULL,")
                .append("PRIMARY KEY(").append(PassContract.Assistance.DNI).append(",").append( PassContract.Assistance.SESSION_NUM)
                .append(") ); ");
        db.execSQL(strBuilder.toString());
        strBuilder.setLength(0);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        createTables(db);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ASSISTANCE);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.SESSIONS);
    }

}
