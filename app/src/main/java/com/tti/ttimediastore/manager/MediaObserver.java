package com.tti.ttimediastore.manager;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by dylan_liang on 2017/5/22.
 */

public class MediaObserver extends ContentObserver {

    private Handler queryHandler;

    private static final int START_QUERY = 0;

    public MediaObserver(Handler handler) {
        super(handler);
    }

    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    public void onChange(boolean selfChange, Uri uri) {
        if (queryHandler != null) {
            queryHandler.removeCallbacksAndMessages(null);
            queryHandler.sendMessage(queryHandler.obtainMessage(START_QUERY));
        }
    }

    public void registerMediaObserver(Context context, Uri uri, Handler queryHandler) {
        context.getContentResolver().registerContentObserver(uri, false, this);
        this.queryHandler = queryHandler;
    }

    public void unregisterMediaObserver(Context context) {
        context.getContentResolver().unregisterContentObserver(this);
        queryHandler = null;
    }
}
