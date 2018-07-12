package com.tti.ttimediastore.application;

/**
 * Created by dylan_liang on 2017/11/8.
 */

public interface ApplicationStateListener {

    public void onForeground();

    public void onBackground();
}
