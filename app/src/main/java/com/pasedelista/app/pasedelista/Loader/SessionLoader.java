package com.pasedelista.app.pasedelista.Loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.pasedelista.app.pasedelista.Database.PassContract;
import com.pasedelista.app.pasedelista.Model.Session;
import com.pasedelista.app.pasedelista.Model.Student;

import java.util.NoSuchElementException;


public class SessionLoader extends AsyncTaskLoader<Session> {

    private static final String LOG_TAG = SessionLoader.class.getSimpleName();

    private final int mSessionNumber;
    private Session mResult;

    public SessionLoader(Context context, int sessionNumber ) {
        super(context);
        mSessionNumber = sessionNumber;
    }

    @Override
    public void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public Session loadInBackground(){
        try{
            Session session = getBaseSession();
            if( session == null ){
                throw new NoSuchElementException( "There's no such session");
            }
            session.setStudents( getSessionStudents() );

            return session;
        }catch( Exception e ){
            /* Error we delegate the error display to the loader caller */
            e.printStackTrace();

            return null;
        }
    }

    private Session getBaseSession(){
        final ContentResolver cr = getContext().getContentResolver();
        Cursor sessionCursor = cr.query( PassContract.Session.buildSessionUri( mSessionNumber ), null /* projection */,
                null /* selection */ , null /* selectionArgs */, null /* sortOrder */);

        if( sessionCursor == null || sessionCursor.getCount() == 0 ){
            /* cannot instantiate the categories cursor */
            return null;
        }

        sessionCursor.moveToFirst();

        int sessionType = sessionCursor.getInt( sessionCursor.getColumnIndexOrThrow( PassContract.Session.TYPE ) );

        sessionCursor.close();

        return new Session( mSessionNumber, sessionType );
    }


    private Student[] getSessionStudents(){
        final ContentResolver cr = getContext().getContentResolver();
        Cursor sessionCursor = cr.query( PassContract.Student.buildStudentsFromSessionUri( mSessionNumber ), null /* projection */,
                null /* selection */ , null /* selectionArgs */, null /* sortOrder */);

        if( sessionCursor == null || sessionCursor.getCount() == 0 ){
            /* cannot instantiate the categories cursor */
            return null;
        }

        int totalStudents = sessionCursor.getCount();

        if( totalStudents > 0 ){
            Student[] students = new Student[ totalStudents ];

            sessionCursor.moveToFirst();
            for( int i = 0; i < totalStudents; i ++){
                String name = sessionCursor.getString( sessionCursor.getColumnIndexOrThrow( PassContract.Student.NAME ));
                String dni = sessionCursor.getString( sessionCursor.getColumnIndexOrThrow( PassContract.Student.DNI ));

                students[ i ] = new Student( name, dni );

                sessionCursor.moveToNext();
            }

            sessionCursor.close();

            return students;
        }else{

            sessionCursor.close();
            return null;
        }
    }

    @Override
    public void deliverResult( Session data) {
        if (isReset()) {
            return;
        }

        mResult = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

    }

    @Override
    protected void onReset() {
        onStopLoading(); // Ensure the loader has been stopped.
        if (mResult != null) {
            mResult = null;
        }
    }

    @Override
    public void onCanceled( Session data) {
        super.onCanceled(data);
    }

}