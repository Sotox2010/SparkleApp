package com.jesussoto.android.sparkle.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.jesussoto.android.sparkle.sync.WebServiceApi;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;


public class SparkleService extends Service {

    private static final String LOG_TAG = SparkleService.class.getSimpleName();

    /**
     * Incomming messages from the client.
     */
    static final int MSG_SAY_HELLO = 1;
    static final int MSG_EXECUTE_QUERY = 2;
    static final int MSG_EXECUTE_QUERY_DOWNLOAD = 3;
    static final int MSG_DUMP = 4;
    static final int MSG_START_DOWNLOAD = 1324;

    /**
     * Response messages.
     */
    static final int MSG_DELIVER_RESULT = 1;
    static final int MSG_ERROR = 2;

    static final String ARG_ENDPOINT_URL = "endpoint_url";
    static final String ARG_FORMAT = "format";
    static final String ARG_SPARQL_QUERY = "sparql_query";

    static final String DATA_QUERY_RESULT= "query_result";
    static final String DATA_ERROR_MESSAGE = "error_message";

    /**
     * Handler of incoming messages from clients.
     */
    class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG, "Message received: " + msg.what);

            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_EXECUTE_QUERY:
                    executeQuery(msg.getData(), msg.replyTo);
                    break;
                case MSG_EXECUTE_QUERY_DOWNLOAD:
                    break;
                case MSG_START_DOWNLOAD:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceHandler mServiceHandler;

    /**
     * Target we publish for clients to send messages to ServiceHandler.
     */
    private Messenger mMessenger;

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    public static void executeSparqlQuery(String endpointUrl, String sparqlQuery) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mMessenger = new Messenger(mServiceHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_START_DOWNLOAD;
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(), "unbinding", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }


    private void executeQuery(Bundle args, Messenger replyTo) {
        if (args == null) {
            sendErrorMessage(replyTo, "Missing argumets! You must provide the correct arguments for a proper query execution.");
            return;
        }

        String endpointUrl = args.getString(ARG_ENDPOINT_URL);
        int format = args.getInt(ARG_FORMAT);
        String sparqlQuery = args.getString(ARG_SPARQL_QUERY);

        WebServiceApi webService = new RestAdapter.Builder()
                .setEndpoint(endpointUrl)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new StringConverter())
                .build()
                .create(WebServiceApi.class);

        try {
            String result = webService.query(
                    new TypedOutputString(sparqlQuery.replace("\t", " ").replace("\n", " ").replace("\r", " ")),
                    format);
            deliverResult(replyTo, result);
        } catch (RetrofitError error) {
            Log.e(LOG_TAG, "RetrofitError: " + error.getMessage());
        }
    }

    private void deliverResult(Messenger replyTo, String result) {
        Log.w(LOG_TAG, result);
        Bundle data = new Bundle();
        data.putString(DATA_QUERY_RESULT, result);

        Message msg = Message.obtain();
        msg.what = MSG_DELIVER_RESULT;
        msg.setData(data);

        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException: " + e.getMessage());
        }
    }

    private void sendErrorMessage(Messenger replyTo, String message) {
        Bundle data = new Bundle();
        data.putString(DATA_ERROR_MESSAGE, message);

        Message msg = Message.obtain();
        msg.what = MSG_ERROR;
        msg.setData(data);

        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException: " + e.getMessage());
        }
    }

}
