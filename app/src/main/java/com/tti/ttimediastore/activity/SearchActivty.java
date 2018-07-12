package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;

/**
 * Created by dylan_liang on 2017/5/3.
 */

public class SearchActivty extends Activity {

    private static final int STATE_CLOSED = 0;
    private static final int STATE_RUNNING = 1;

    private static final int STATE_RESUME = 0;
    private static final int STATE_PAUSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (Utils.Preferences.getItemViewState()) {
            case STATE_CLOSED:
                createView();
                break;
            case STATE_RUNNING:
                if (Utils.Preferences.getItemViewLife() == STATE_RESUME)
                    createView();
                else
                    backtoMainActivity();
                break;
        }
    }

    public void onResume() {
        super.onResume();
        Utils.Preferences.setItemViewLife(STATE_RESUME);
    }

    public void onPause() {
        super.onPause();
        Utils.Preferences.setItemViewLife(STATE_PAUSE);
    }

    public void onBackPressed() {
        backtoMainActivity();
    }

    private void createView() {
        Utils.Preferences.setItemViewState(STATE_RUNNING);
        setContentView(R.layout.activity_search);
        Utils.updateMediaStore(this);
    }

    private void backtoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);
        Utils.Preferences.setItemViewState(STATE_CLOSED);
        finish();
    }
}
