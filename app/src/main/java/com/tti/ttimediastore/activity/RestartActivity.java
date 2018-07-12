package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.os.Bundle;

public class RestartActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        moveTaskToBack(true);
        finish();
    }
}
