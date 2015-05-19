package com.pasedelista.app.pasedelista.Activity;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.pasedelista.app.pasedelista.Database.PassContract;
import com.pasedelista.app.pasedelista.Loader.BatchOperation;
import com.pasedelista.app.pasedelista.Loader.StudentLoader;
import com.pasedelista.app.pasedelista.Model.Student;
import com.pasedelista.app.pasedelista.R;

public class ListPassActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Student[]>,
        View.OnClickListener {

    private ListView mPassList;
    private Spinner mSpinnerSessionNumber;
    private StudentsAdapter mStudentsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pass);

        mPassList = (ListView) findViewById(R.id.list_pass);
        mSpinnerSessionNumber = (Spinner) findViewById(R.id.spinner_session_num);

        findViewById( R.id.bttn_pass_list ).setOnClickListener( this );

        /* Loads session numbers */
        new GetSessionNumbers().execute((Void[]) null);

        /* fetching the students from the database */
        getSupportLoaderManager().initLoader(1 /* loader id */, null /* bundle */, ListPassActivity.this /* callback */);
    }

    @Override
    public Loader<Student[]> onCreateLoader(int i, Bundle bundle) {
        return new StudentLoader(ListPassActivity.this /* context */);
    }

    @Override
    public void onLoadFinished(Loader<Student[]> loader, Student[] students) {
        if (students == null) {
            Toast.makeText(ListPassActivity.this, "Error al obtener estudiantes", Toast.LENGTH_SHORT).show();
        } else {
            mStudentsAdapter = new StudentsAdapter(ListPassActivity.this /* context */, students);
            mPassList.setAdapter(mStudentsAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Student[]> loader) {
        /* do nothing */
    }

    @Override
    public void onClick(View v) {
        if (mStudentsAdapter == null || mStudentsAdapter.getCount() == 0) {
            Toast.makeText(ListPassActivity.this, "No se puede realizar el pase sin alumnos", Toast.LENGTH_SHORT).show();
        } else {
            passList();
        }
    }

    private void passList() {
        new PassListTask( ListPassActivity.this /* context */, mStudentsAdapter.getStudents() /* students */,
                Integer.parseInt( mSpinnerSessionNumber.getSelectedItem().toString() ) /* selected session */ ).execute( (Void[]) null );
    }

    public class StudentsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Student[] mStudents;

        public StudentsAdapter(Context context, Student[] students) {
            mStudents = students;
            mInflater = LayoutInflater.from(context);
        }

        public Student[] getStudents(){
            return mStudents;
        }

        @Override
        public int getCount() {
            return (mStudents == null) ? 0 : mStudents.length;
        }

        @Override
        public Object getItem(int position) {
            return (mStudents == null) ? null : mStudents[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Student student = (Student) getItem(position);

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_student_pass, parent, false /* attachToRoot */);

                holder = new ViewHolder();
                holder.studentCheck = (CheckBox) convertView.findViewById(R.id.chk_student_assist);
                convertView.setTag(holder);

                /* updates if the student assisted or not */
                holder.studentCheck.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        student.setAssisted( isChecked );
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.studentCheck.setText(student.getName());
            holder.studentCheck.setChecked(student.isAssisted());

            return convertView;
        }

        /* package */ class ViewHolder {
            CheckBox studentCheck;
        }
    }

    public class GetSessionNumbers extends AsyncTask<Void, Void, Integer[]> {

        private Context mContext;

        public GetSessionNumbers() {
            mContext = getApplicationContext();
        }

        @Override
        protected Integer[] doInBackground(Void... params) {
            return getSessionNumbers();
        }

        @Override
        public void onPostExecute(Integer[] numbers) {
            if (numbers != null && numbers.length > 0) {
                ArrayAdapter<Integer> numbersAdapter = new ArrayAdapter<Integer>(mContext,
                        android.R.layout.simple_spinner_dropdown_item, numbers);
                mSpinnerSessionNumber.setAdapter(numbersAdapter);
            }
        }

        private Integer[] getSessionNumbers() {
            final ContentResolver cr = mContext.getContentResolver();
            Cursor sessionCursor = cr.query(PassContract.Session.CONTENT_URI, null /* projection */,
                    null /* selection */, null /* selectionArgs */, null /* sortOrder */);

            if (sessionCursor == null || sessionCursor.getCount() == 0) {
            /* cannot instantiate the categories cursor */
                return null;
            }

            int totalSessions = sessionCursor.getCount();

            if (totalSessions > 0) {
                Integer[] sessionNumbers = new Integer[totalSessions];

                sessionCursor.moveToFirst();
                for (int i = 0; i < totalSessions; i++) {
                    Integer number = sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(PassContract.Session.SESSION_NUM));

                    sessionNumbers[i] = number;

                    sessionCursor.moveToNext();
                }

                sessionCursor.close();

                return sessionNumbers;
            } else {

                sessionCursor.close();
                return null;
            }
        }
    }

    public class PassListTask extends AsyncTask<Void, Void, Boolean> {

        private Context mAppContext;
        private int mSessionNumber;
        private Student[] mStudents;

        public PassListTask(Context context, Student[] students, int sessionNumber) {
            mAppContext = context.getApplicationContext();
            mSessionNumber = sessionNumber;
            mStudents = students;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            addAssistance(mStudents);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean areRowsAffected) {
            if (!areRowsAffected) {
                /* error inserting */
                Toast.makeText(mAppContext, "Error al pasar la lista", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mAppContext, "Pase de lista logrado con éxito", Toast.LENGTH_SHORT).show();

                /* we end the Activity */
                finish();
            }
        }

        private void addAssistance(Student[] students) {
            final BatchOperation operations =
                    new BatchOperation(PassContract.CONTENT_AUTHORITY, mAppContext.getContentResolver());

            ContentValues values;

            for (Student student : students) {
                if( !student.isAssisted() ){
                    continue;
                }

                /* restart values */
                values = new ContentValues();


                final Uri assistanceUri = PassContract.Assistance.CONTENT_URI;

                final ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(assistanceUri);
                final ContentProviderOperation categoryOperation = op.withValues(assistanceToContentValues(student, mSessionNumber, values))
                        .withYieldAllowed(false).build();
                operations.add(categoryOperation);
            }

            operations.execute();
        }

        public ContentValues assistanceToContentValues(@NonNull Student student, int sessionNumber,
                                                       @Nullable ContentValues values) {
            if (values == null) {
                values = new ContentValues();
            }

            values.put(PassContract.Assistance.DNI, student.getDNI());
            values.put(PassContract.Assistance.SESSION_NUM, sessionNumber);
            return values;
        }
    }
}
