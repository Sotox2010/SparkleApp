package com.jesussoto.android.sparkle.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class SparkleService extends Service {

    public static void executeSparqlQuery(String endpointUrl, String sparqlQuery) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


}
