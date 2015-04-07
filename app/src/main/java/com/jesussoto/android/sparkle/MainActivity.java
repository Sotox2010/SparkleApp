package com.jesussoto.android.sparkle;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.jesussoto.android.sparkle.service.SparkleService;
import com.jesussoto.android.sparkle.widget.FloatingLabelEditText;
import com.jesussoto.android.sparkle.widget.FloatingLabelSpinner;

import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    public static final String[] RESULT_FORMATS = {
            "RDF/XML",
            "RDF/XML-ABBREV",
            "RDF/JSON",
            "TURTLE",
            "N-TRIPLES",
            "N3"
    };

    private Toolbar mToolbarActionBar;
    private FloatingLabelEditText mEndpointUrlView;
    private FloatingLabelEditText mSparqlQueryView;
    private FloatingLabelSpinner mFormatSpinner;
    private View mExecuteQueryFab;

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbarActionBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEndpointUrlView = (FloatingLabelEditText) findViewById(R.id.endpoint_url);
        mFormatSpinner = (FloatingLabelSpinner) findViewById(R.id.format_spinner);
        mSparqlQueryView = (FloatingLabelEditText) findViewById(R.id.sparql_query);
        mExecuteQueryFab = findViewById(R.id.execute_fab);

        mExecuteQueryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) return;

                Message msg = msg
                mService.send();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mExecuteQueryFab.setOutlineProvider(new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        }

        FloatingLabelSpinnerAdapter<String> adapter =
                new FloatingLabelSpinnerAdapter<String>(this, Arrays.asList(RESULT_FORMATS));

        mFormatSpinner.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to Sparkle service.
        bindService(new Intent(this, SparkleService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service.
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void sendExecuteQueryMessage() {
        if (!mBound) return;

        Message msg = new Message();
        msg.
    }
}
