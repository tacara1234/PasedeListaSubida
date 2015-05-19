/*
 * TravelGuideContract.java 1.0 2014/02/18
 * 
 * Touchtastic (c) 2014
 */
package com.pasedelista.app.pasedelista.Database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

public final class PassContract {

    public static final String NAME = "com.pasedelista.app.pasedelista";

    public static final String CONTENT_AUTHORITY = buildContentAuthority();
    /* package */ static final Uri BASE_CONTENT_URI = buildBaseUri();
    /* package */ static final String PATH_STUDENTS = "students";
    /* package */ static final String PATH_ASSISTANCE = "assistance";
    /* package */ static final String PATH_SESSIONS = "sessions";

    private PassContract() {
        /* Do nothing */
    }

    /* package */ interface StudentsColumns{
        public static final String DNI = "dni";
        public static final String NAME = "name";
    }

    /* package */ interface AssistanceColumns{
        public static final String DNI = "assistance_dni";
        public static final String SESSION_NUM = "assistance_session_num";
    }

    /* package */ interface SessionColumns {
        public static final String SESSION_NUM = "session_num";
        public static final String TYPE = "type";
    }

    public static final String buildContentType(String baseContentUri, String table) {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + baseContentUri + "." + table;
    }

    public static final String buildContentAuthority() {
        return NAME + ".provider";
    }

    public static final Uri buildBaseUri() {
        return Uri.parse("content://" + buildContentAuthority());
    }

    /**
     * Student representation for the provider
     * */
    public static abstract class Student implements StudentsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath( PATH_STUDENTS ).build();

        /* package */ static final String CONTENT_TYPE = buildContentType(
                CONTENT_AUTHORITY, PassDatabase.Tables.STUDENT);

        public static Uri buildStudentUri( long studentUri ){
            return ContentUris.withAppendedId(CONTENT_URI, studentUri);
        }

        public static Uri buildStudentsFromSessionUri( long sessionId ){
            return ContentUris.withAppendedId(CONTENT_URI, sessionId);
        }
    }

    /**
     * Assistance representation for the provider
     * */
    public static abstract class Assistance implements AssistanceColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ASSISTANCE).build();

        /* package */ static final String CONTENT_TYPE = buildContentType(
                CONTENT_AUTHORITY, PassDatabase.Tables.ASSISTANCE);
    }

    /**
     *Session representation for the provider
     * */
    public static abstract class Session implements SessionColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath( PATH_SESSIONS ).build();

        /* package */ static final String CONTENT_TYPE = buildContentType(
                CONTENT_AUTHORITY, PassDatabase.Tables.SESSIONS);

        public static String getSessionId(Uri uri){
            return uri.getLastPathSegment();
        }

        public static Uri buildSessionUri( long sessionId ){
            return ContentUris.withAppendedId(CONTENT_URI, sessionId);
        }

    }

}
