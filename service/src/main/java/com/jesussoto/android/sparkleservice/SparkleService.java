package com.jesussoto.android.sparkleservice;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.jesussoto.android.sparkleservice.model.ResultMetadata;
import com.jesussoto.android.sparkleservice.sync.StringConverter;
import com.jesussoto.android.sparkleservice.sync.TypedOutputString;
import com.jesussoto.android.sparkleservice.sync.WebServiceApi;

import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.RetrofitError;


public class SparkleService extends Service {

    private static final String LOG_TAG = SparkleService.class.getSimpleName();

    /**
     * Intent actions for broadcast.
     */
    private static final String  ACTION_QUERY_RESULT_DOWLOAD_COMPLETE =
            "com.android.sparkleservice.intent.action.QUERY_RESULT_DOWNLOAD_COMPLETE";
    private static final String  ACTION_QUERY_RESULT_DOWLOAD_FAILURE =
            "com.android.sparkleservice.intent.action.QUERY_RESULT_DOWNLOAD_FAILURE";

    /**
     * Incomming messages from the client.
     */
    static final int MSG_SAY_HELLO = 1;
    static final int MSG_EXECUTE_QUERY = 2;
    static final int MSG_EXECUTE_QUERY_FOR_DOWNLOAD = 3;
    static final int MSG_DUMP = 4;
    static final int MSG_DOWNLOAD_RESULT = 5;

    /**
     * Response messages.
     */
    static final int MSG_DELIVER_RESULT = 1;
    static final int MSG_ERROR = 2;
    static final int MSG_DELIVER_RESULT_METADATA = 3;
    static final int MSG_DELIVER_DOWNLOAD_ID = 4;

    /**
     * Arguments.
     */
    static final String ARG_ENDPOINT_URL = "arg_endpoint_url";
    static final String ARG_FORMAT = "arg_format";
    static final String ARG_SPARQL_QUERY = "arg_sparql_query";
    static final String ARG_RESULT_METADATA = "arg_result_metadata";

    /**
     * Response messages.
     */
    static final String DATA_QUERY_RESULT= "data_query_result";
    static final String DATA_QUERY_RESULT_METADATA = "data_query_result_metadata";
    static final String DATA_ERROR_MESSAGE = "data_error_message";
    static final String DATA_DOWNLOAD_ID = "data_download_id";

    static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    static final String EXTRA_LOCAL_URI = "extra_local_uri";
    static final String EXTRA_REASON = "extra_reason";

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
                case MSG_EXECUTE_QUERY_FOR_DOWNLOAD:
                    executeQueryForDownload(msg.getData(), msg.replyTo);
                    break;
                case MSG_DOWNLOAD_RESULT:
                    downloadResults(msg.getData(), msg.replyTo);
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
     * Store ids of query download requests.
     */
    private static ArrayList<Long> sDownloadRequestIds = new ArrayList<>();

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "onBind(" + intent.getAction() + ")");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
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
        /*Message msg = mServiceHandler.obtainMessage();
        msg.what = MSG_START_DOWNLOAD;
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);*/

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
            sendErrorMessage("Missing argumets! You must provide the correct arguments for a proper query execution.", replyTo);
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
                    new TypedOutputString(
                            sparqlQuery.replace("\t", " ").replace("\n", "").replace("\r", "")),
                    format);

            deliverResult(result, replyTo);
        } catch (RetrofitError error) {
            Log.e(LOG_TAG, "RetrofitError: " + error.getMessage());
            sendErrorMessage(error.getMessage(), replyTo);
        }
    }

    private void executeQueryForDownload(Bundle args, Messenger replyTo) {
        if (args == null) {
            sendErrorMessage("Missing argumets! You must provide the correct arguments for a proper query execution.", replyTo);
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
            String metadata = webService.queryDownload(
                    new TypedOutputString(
                            sparqlQuery.replace("\t", " ").replace("\n", "").replace("\r", "")),
                    format);

            deliverResultMetadata(metadata, replyTo);
        } catch (RetrofitError error) {
            Log.e(LOG_TAG, "RetrofitError: " + error.getMessage());
            sendErrorMessage(error.getMessage(), replyTo);
        }
    }

    private void downloadResults(Bundle args, Messenger replyTo) {
        Log.d(LOG_TAG, "downloadResults()");

        if (args != null && args.containsKey(ARG_RESULT_METADATA)) {
            String metadataString = args.getString(ARG_RESULT_METADATA);
            ResultMetadata metadata = new Gson().fromJson(metadataString, ResultMetadata.class);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(metadata.getUrl()));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle(metadata.getFilename());
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/Sparkle", metadata.getFilename());

            long downloadId = manager.enqueue(request);
            sDownloadRequestIds.add(downloadId);

            sendDownloadIdMessage(downloadId, replyTo);
        } else {
            sendErrorMessage(getString(R.string.error_missing_args), replyTo);
        }
    }

    private void deliverResult(String result, Messenger replyTo) {
        Log.i(LOG_TAG, "deliverResult()");
        Log.i(LOG_TAG, result);

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

    private void deliverResultMetadata(String metadata, Messenger replyTo) {
        Log.i(LOG_TAG, "deliverResultMetadata()");
        Log.i(LOG_TAG, "Metadata = " + metadata);

        Bundle data = new Bundle();
        data.putString(DATA_QUERY_RESULT_METADATA, metadata);

        Message msg =  Message.obtain();
        msg.what = MSG_DELIVER_RESULT_METADATA;
        msg.setData(data);

        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException: " + e.getMessage());
        }
    }

    private void sendDownloadIdMessage(long downloadId, Messenger replyTo) {
        Bundle data = new Bundle();
        data.putLong(DATA_DOWNLOAD_ID, downloadId);

        Message msg = Message.obtain();
        msg.what = MSG_DELIVER_DOWNLOAD_ID;
        msg.setData(data);

        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException: " + e.getMessage());
        }
    }

    private void sendErrorMessage(String message, Messenger replyTo) {
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


    public static class DownloadReceiver extends BroadcastReceiver {
        public static final String LOG_TAG = DownloadReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "DownloadReceiver.onReceive(" + intent.getAction() + ")");
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadId != -1 && sDownloadRequestIds.contains(downloadId)) {
                DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = manager.query(query);

                if (cursor.moveToFirst()) {
                    int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    switch (cursor.getInt(statusColumnIndex)) {
                        case DownloadManager.STATUS_SUCCESSFUL: {
                            Log.d(LOG_TAG, "ID: " + downloadId + ", status: SUCCESSFUL");

                            String uriString = cursor.getString(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            sendSuccessBroadcast(context, downloadId, uriString);
                            break;
                        }
                        default: {
                            Log.d(LOG_TAG, "ID: " + downloadId + ", status: FAILED");
                            Log.d(LOG_TAG, "Reason:  " + cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                            sendFailureBroadcast(context, downloadId, cursor.getString(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_REASON)));
                        }
                    }
                }

                sDownloadRequestIds.remove(downloadId);
            }
        }

        public void sendSuccessBroadcast(Context context, long downloadId, String uriString) {
            Intent intent = new Intent(ACTION_QUERY_RESULT_DOWLOAD_COMPLETE);
            intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
            intent.putExtra(EXTRA_LOCAL_URI, uriString);
            context.sendBroadcast(intent);
        }

        public void sendFailureBroadcast(Context context, long downloadId, String reason) {
            Intent intent = new Intent(ACTION_QUERY_RESULT_DOWLOAD_FAILURE);
            intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
            intent.putExtra(EXTRA_REASON, reason);
            context.sendBroadcast(intent);
        }
    }

}
