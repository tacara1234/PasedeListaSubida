package com.pasedelista.app.pasedelista.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pasedelista.app.pasedelista.Database.PassContract;
import com.pasedelista.app.pasedelista.Model.Session;
import com.pasedelista.app.pasedelista.R;

public class CreateSessionActivity extends ActionBarActivity implements View.OnClickListener{

    private RadioGroup mTypeRadioGroup;
    private EditText mSessionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);

        mSessionNumber = ( EditText ) findViewById( R.id.edt_session_num );
        mTypeRadioGroup = ( RadioGroup ) findViewById( R.id.rdio_group_session_type);

        findViewById( R.id.bttn_create_session ).setOnClickListener( CreateSessionActivity.this );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isSessionNumberValid( int sessionNumber ){
        return ( sessionNumber > 0 );
    }

    private int getSessionTypeSelected(){
        int selectedId = mTypeRadioGroup.getCheckedRadioButtonId();
        if( selectedId == R.id.rdio_session_practice ){
            return Session.TYPE_PRACTICE;
        }else{
            return Session.TYPE_THEORY;
        }
    }

    @Override
    public void onClick( View view ){

        if(mSessionNumber.getText().toString().length()==0){
            Toast.makeText( CreateSessionActivity.this, "Faltan Datos para crear la Sesion", Toast.LENGTH_SHORT ).show();
            return;
        }
        int sessionNumber = Integer.parseInt( mSessionNumber.getText().toString() );
        if( !isSessionNumberValid( sessionNumber) ){
            Toast.makeText( CreateSessionActivity.this, "Numero de sesión invalido", Toast.LENGTH_SHORT ).show();
            return;
        }
        if(sessionNumber==0){

        }

        new CreateSessionTask( CreateSessionActivity.this /* context */, sessionNumber, getSessionTypeSelected() ).execute( ( Void[] ) null);
    }

    public class CreateSessionTask extends AsyncTask<Void, Void, Boolean> {

        private Context mAppContext;
        private int mSessionNumber;
        private int mSessionType;

        public CreateSessionTask( Context context, int sessionNumber, int sessionType ){
            mAppContext = context.getApplicationContext();
            mSessionNumber = sessionNumber;
            mSessionType = sessionType;
        }

        @Override
        protected Boolean doInBackground( Void... params ){
            Uri result = mAppContext.getContentResolver().insert( PassContract.Session.buildSessionUri( mSessionNumber),
                    createSessionContentValues() );
            return ( result != null );
        }

        @Override
        protected void onPostExecute( Boolean areRowsAffected ){
            if( !areRowsAffected){
                /* error inserting */
                Toast.makeText( mAppContext, "Error al insertar la sesión en la base de datos", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText( mAppContext, "Sesión Añadida con éxito", Toast.LENGTH_SHORT).show();

                /* we end the Activity */
                finish();
            }
        }

        public ContentValues createSessionContentValues(){
            ContentValues values = new ContentValues();

            values.put(PassContract.Session.SESSION_NUM, mSessionNumber );
            values.put(PassContract.Session.TYPE, mSessionType );

            return values;
        }
    }
}

