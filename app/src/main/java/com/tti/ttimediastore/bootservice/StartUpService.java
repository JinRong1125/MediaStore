package com.tti.ttimediastore.bootservice;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.tti.ttimediastore.application.TTIMediaStore;

/**
 * Created by dylan_liang on 2017/11/13.
 */

public class StartUpService extends JobIntentService {

    public static final int JOB_ID = 0x01;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, StartUpService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        TTIMediaStore.getInstance();
    }
}
