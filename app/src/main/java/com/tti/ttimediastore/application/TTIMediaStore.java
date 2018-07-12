package com.tti.ttimediastore.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.tti.ttimediastore.manager.IrisActionReceiver;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.ServiceManager;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class TTIMediaStore extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static TTIMediaStore instance;

    protected String userAgent;

    private static ApplicationStateListener applicationStateListener;

    public synchronized static TTIMediaStore getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        startDLNA();
        registerReceiver(new IrisActionReceiver(), new IntentFilter("IRIS_MEDIA_STORE"));
        userAgent = Util.getUserAgent(this, Constants.APP_NAME);
        Constants.APP_STATE = Constants.STATE_BACKGROUND;

        registerActivityLifecycleCallbacks(this);
    }

    @SuppressLint("MissingSuperCall")
    public void onTrimMemory(final int level) {
        if (applicationStateListener == null)
            return;
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
                || level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
                || level == ComponentCallbacks2.TRIM_MEMORY_MODERATE
                || level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            Constants.APP_STATE = Constants.STATE_BACKGROUND;
            applicationStateListener.onBackground();
        }
    }

    public void startDLNA() {
        if (applicationStateListener != null)
            return;
        if (!Utils.isPermissionGranted(getInstance()))
            return;
        applicationStateListener = new ServiceManager();
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Constants.APP_STATE = Constants.STATE_FOREGROUND;
        if (applicationStateListener != null)
            applicationStateListener.onForeground();
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
