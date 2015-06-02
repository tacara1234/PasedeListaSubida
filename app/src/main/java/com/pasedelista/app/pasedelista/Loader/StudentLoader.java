package com.pasedelista.app.pasedelista.Loader;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.pasedelista.app.pasedelista.Database.PassContract;
import com.pasedelista.app.pasedelista.Model.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class StudentLoader extends AsyncTaskLoader<Student[]> {

    private static final String LOG_TAG = SessionLoader.class.getSimpleName();

    private Student[] mResult;

    public StudentLoader(Context context) {
        super(context);
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
    public Student[] loadInBackground() {
        try {
            Student[] students = getStudents();
            Log.d("STUDENTS", "IN DB");

            if (students == null || students.length == 0) {
                /* maybe they arent loaded yet */
                students = fetchStudents();
                Log.d("STUDENTS", "IN FILE");

            }

            return students;
        } catch (Exception e) {
            /* Error we delegate the error display to the loader caller */
            e.printStackTrace();

            return null;
        }
    }

    private Student[] fetchStudents() {

        /* fecthes the SD card root */
        File sdcard = Environment.getExternalStorageDirectory();

        Log.e( "SD CARD", sdcard.getAbsolutePath().toString() );

        /* Gets the students file */
        File file = new File(sdcard, "estudiantes.txt");

        try {
            //Read text from file
            StringBuilder text = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            ArrayList<Student> studentsList = new ArrayList<Student>();

            //Parsing the students from the text file
            StringTokenizer tokenizer = new StringTokenizer( text.toString(), "|");
            while( tokenizer.hasMoreElements() ){
                String name = tokenizer.nextToken();
                String dni = tokenizer.nextToken();

                studentsList.add( new Student( name, dni ) );
            }
        //studentsList.add( new Student( "LUIs", "123") );
        //studentsList.add( new Student( "PABLO", "321") );
        //studentsList.add( new Student( "JUAN", "456") );
        //studentsList.add( new Student( "PEDRO", "654") );
        //studentsList.add( new Student( "RICARDO", "135") );


        Student[] students = new Student[ studentsList.size() ];
            studentsList.toArray( students );

            if( students != null && students.length > 0 ){
                loadStudentsInDatabase( students );
            }

            return students;
        } catch (IOException e) {
          //  You'll need to add proper error handling here
          e.printStackTrace();
        }

        return null;
    }

    private void loadStudentsInDatabase( Student[] students ){
        final BatchOperation operations =
                new BatchOperation( PassContract.CONTENT_AUTHORITY, getContext().getContentResolver() );

        ContentValues values;

        for( Student student : students ){
            /* restart values */
            values = new ContentValues();


            final Uri studentUri = PassContract.Student.CONTENT_URI;

            final ContentProviderOperation.Builder op = ContentProviderOperation.newInsert( studentUri );
            final ContentProviderOperation categoryOperation = op.withValues( studentToContentValues( student, values) )
                    .withYieldAllowed( false ).build();
            operations.add( categoryOperation );
        }

        operations.execute();
    }

    public ContentValues studentToContentValues(@NonNull Student student,
                                         @Nullable ContentValues values){
        if( values == null ){
            values = new ContentValues();
        }

        values.put( PassContract.Student.NAME, student.getName() );
        values.put( PassContract.Student.DNI, student.getDNI() );
        return values;
    }

    private Student[] getStudents() {
        final ContentResolver cr = getContext().getContentResolver();
        Cursor sessionCursor = cr.query(PassContract.Student.CONTENT_URI, null /* projection */,
                null /* selection */, null /* selectionArgs */, null /* sortOrder */);

        if (sessionCursor == null || sessionCursor.getCount() == 0) {
            /* cannot instantiate the categories cursor */
            return null;
        }

        int totalStudents = sessionCursor.getCount();

        if (totalStudents > 0) {
            Student[] students = new Student[totalStudents];

            sessionCursor.moveToFirst();
            for (int i = 0; i < totalStudents; i++) {
                String name = sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(PassContract.Student.NAME));
                String dni = sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(PassContract.Student.DNI));

                students[i] = new Student(name, dni);

                sessionCursor.moveToNext();
            }

            sessionCursor.close();

            return students;
        } else {

            sessionCursor.close();
            return null;
        }
    }

    @Override
    public void deliverResult(Student[] data) {
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
    public void onCanceled(Student[] data) {
        super.onCanceled(data);
    }

}
