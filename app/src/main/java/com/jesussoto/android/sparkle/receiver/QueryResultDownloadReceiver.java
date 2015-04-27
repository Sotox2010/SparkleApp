package com.jesussoto.android.sparkle.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class QueryResultDownloadReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = QueryResultDownloadReceiver.class.getSimpleName();

    private static final String  ACTION_QUERY_RESULT_DOWLOAD_COMPLETE =
            "com.android.sparkleservice.intent.action.QUERY_RESULT_DOWNLOAD_COMPLETE";
    private static final String  ACTION_QUERY_RESULT_DOWLOAD_FAILURE =
            "com.android.sparkleservice.intent.action.QUERY_RESULT_DOWNLOAD_FAILURE";

    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_LOCAL_URI = "extra_local_uri";
    public static final String EXTRA_REASON = "extra_reason";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Action = " + intent.getAction());
        if (intent.getAction().equals(ACTION_QUERY_RESULT_DOWLOAD_COMPLETE)) {
            Log.i(LOG_TAG, "EXTRA_DOWNLOAD_ID = " + intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1));
            Log.i(LOG_TAG, "EXTRA_LOCAL_URI = " + intent.getStringExtra(EXTRA_LOCAL_URI));
            Log.d(LOG_TAG, "DOWNLOAD FINISHED: " + System.currentTimeMillis());

        } else if (intent.getAction().equals(ACTION_QUERY_RESULT_DOWLOAD_FAILURE)) {
            Log.i(LOG_TAG, "EXTRA_REASON = " + intent.getStringExtra(EXTRA_REASON));
        }
    }
}
