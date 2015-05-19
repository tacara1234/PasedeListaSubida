package com.pasedelista.app.pasedelista.Activity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pasedelista.app.pasedelista.Database.PassContract;
import com.pasedelista.app.pasedelista.Loader.SessionLoader;
import com.pasedelista.app.pasedelista.Model.Session;
import com.pasedelista.app.pasedelista.Model.Student;
import com.pasedelista.app.pasedelista.R;

public class ShowListActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Session>{

    private ListView mPassList;
    private Spinner mSpinnerSessionNumber;
    private StudentsAdapter mStudentsAdapter;

    private int mSessionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        mPassList = (ListView) findViewById(R.id.list_pass);
        mSpinnerSessionNumber = (Spinner) findViewById(R.id.spinner_session_num);

        /* Loads session numbers */
        new GetSessionNumbers().execute((Void[]) null);

        mSpinnerSessionNumber.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSessionNumber = ( int ) mSpinnerSessionNumber.getItemAtPosition( position );
                getAssistanceList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                /* do nothing */
            }
        });
    }

    private void getAssistanceList(){
         /* fetching the students from the database */
        getSupportLoaderManager().restartLoader(1 /* loader id */, null /* bundle */, ShowListActivity.this /* callback */);
    }

    @Override
    public Loader<Session> onCreateLoader(int i, Bundle bundle) {
        return new SessionLoader( ShowListActivity.this /* context */, mSessionNumber );
    }

    @Override
    public void onLoadFinished(Loader<Session> sessionLoader, Session session) {
        if ( session == null) {
            Toast.makeText( ShowListActivity.this, "Error al obtener estudiantes", Toast.LENGTH_SHORT).show();
        } else {
            mStudentsAdapter = new StudentsAdapter( ShowListActivity.this /* context */, session.getStudents() );
            mPassList.setAdapter(mStudentsAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Session> loader) {
        /* do nothing */
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
                convertView = mInflater.inflate(R.layout.item_student, parent, false /* attachToRoot */);

                holder = new ViewHolder();
                holder.studentTxtView = ( TextView ) convertView.findViewById(R.id.txt_student_name);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.studentTxtView.setText(student.getName());

            return convertView;
        }

        /* package */ class ViewHolder {
            TextView studentTxtView;
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
                        android.R.layout.simple_spinner_item, numbers);
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


}
