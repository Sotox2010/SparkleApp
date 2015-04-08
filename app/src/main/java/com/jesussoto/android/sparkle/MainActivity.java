package com.jesussoto.android.sparkle;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.jesussoto.android.sparkle.service.SparkleService;
import com.jesussoto.android.sparkle.widget.FloatingLabelEditText;
import com.jesussoto.android.sparkle.widget.FloatingLabelSpinner;

import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String[] RESULT_FORMATS = {
            "RDF/XML",
            "RDF/XML-ABBREV",
            "RDF/JSON",
            "TURTLE",
            "N-TRIPLES",
            "N3"
    };

    static final int MSG_SAY_HELLO = 1;
    static final int MSG_EXECUTE_QUERY = 2;
    static final int MSG_EXECUTE_QUERY_DOWNLOAD = 3;
    static final int MSG_DUMP = 4;

    static final int MSG_DELIVER_RESULT = 1;
    static final int MSG_ERROR = 2;

    static final String ARG_ENDPOINT_URL = "endpoint_url";
    static final String ARG_FORMAT = "format";
    static final String ARG_SPARQL_QUERY = "sparql_query";

    static final String DATA_QUERY_RESULT= "query_result";
    static final String DATA_ERROR_MESSAGE = "error_message";

    private Toolbar mToolbarActionBar;
    private FloatingLabelEditText mEndpointUrlView;
    private FloatingLabelEditText mSparqlQueryView;
    private FloatingLabelSpinner mFormatSpinner;

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Handler for Service response messages.
     */
    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELIVER_RESULT: {
                    Bundle result = msg.getData();
                    Log.i(LOG_TAG, "MSG_DELIVER_RESULT");
                    Log.i(LOG_TAG, result.getString(DATA_QUERY_RESULT));

                    Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                    intent.putExtra(ResultsActivity.EXTRA_RESULT, result.getString(DATA_QUERY_RESULT));
                    startActivity(intent);
                    break;
                }
                case MSG_ERROR: {
                    Bundle result = msg.getData();
                    Log.e(LOG_TAG, "MSG_ERROR");
                    Log.e(LOG_TAG, result.getString(DATA_ERROR_MESSAGE));
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Messenger mMessenger = new Messenger(new ResponseHandler());

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

        View executeQueryFab = findViewById(R.id.execute_fab);
        executeQueryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendExecuteQueryMessage();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            executeQueryFab.setOutlineProvider(new ViewOutlineProvider() {
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

        Bundle args = new Bundle();
        args.putString(ARG_ENDPOINT_URL, mEndpointUrlView.getText().toString());
        args.putInt(ARG_FORMAT, mFormatSpinner.getSelectedItemPosition());
        args.putString(ARG_SPARQL_QUERY, mSparqlQueryView.getText().toString());

        Message msg = new Message();
        msg.what = MSG_EXECUTE_QUERY;
        msg.replyTo = mMessenger;
        msg.setData(args);

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
