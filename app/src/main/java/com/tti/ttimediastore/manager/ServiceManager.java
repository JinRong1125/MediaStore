package com.tti.ttimediastore.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.tti.ttimediastore.activity.RestartActivity;
import com.tti.ttimediastore.application.ApplicationStateListener;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.dlna.dmr.MediaRenderer;
import com.tti.ttimediastore.dlna.dms.controller.cling.Factory;
import com.tti.ttimediastore.dlna.dms.controller.upnp.IUpnpServiceController;
import com.tti.ttimediastore.dlna.dms.model.cling.UpnpService;
import com.tti.ttimediastore.dlna.dms.model.upnp.IFactory;

import org.fourthline.cling.android.AndroidUpnpService;

/**
 * Created by dylan_liang on 2017/8/7.
 */

public class ServiceManager implements ApplicationStateListener {

    public static AndroidUpnpService androidUpnpService = null;
    public static IUpnpServiceController upnpServiceController = null;

    private boolean isInBackground;
    private boolean isNeedRestart;
    private boolean isRestarting;

    public ServiceManager() {
        isInBackground = true;
        isNeedRestart = false;
        isRestarting = false;
        registerReceiver();
    }

    private void startDLNAService() {
        TTIMediaStore.getInstance().bindService(new Intent(TTIMediaStore.getInstance(),
                UpnpService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopDLNAService() {
        TTIMediaStore.getInstance().unbindService(serviceConnection);
        serviceConnection.onServiceDisconnected(null);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            androidUpnpService = (AndroidUpnpService) service;
            MediaRenderer renderer = new MediaRenderer(TTIMediaStore.getInstance());
            androidUpnpService.getRegistry().addDevice(renderer.getDevice());
            IFactory factory = new Factory();
            upnpServiceController = factory.createUpnpServiceController(TTIMediaStore.getInstance());
            upnpServiceController.getServiceListener().startMediaServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            androidUpnpService = null;
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    TTIMediaStore.getInstance(),
                    0,
                    new Intent(TTIMediaStore.getInstance(), RestartActivity.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) TTIMediaStore.getInstance().getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
            Runtime.getRuntime().exit(0);
        }
    };

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        TTIMediaStore.getInstance().registerReceiver(connectivityReceiver, intentFilter);
    }

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) TTIMediaStore.getInstance()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (isRestarting)
                        return;
                    if (!isInBackground) {
                        isNeedRestart = true;
                        return;
                    }
                    processService();
                }
            }
        }
    };

    private void processService() {
        if (androidUpnpService == null) {
            startDLNAService();
            return;
        }
        isRestarting = true;
        stopDLNAService();
    }

    @Override
    public void onForeground() {
        isInBackground = false;
    }

    @Override
    public void onBackground() {
        isInBackground = true;
        if (isNeedRestart) {
            isNeedRestart = false;
            processService();
        }
    }
}
