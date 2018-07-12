package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.dlna.upnp.UPnPFragment;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class UPnPActivity extends Activity {

    private UPnPFragment upnpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upnp);
        getUPnPFragment();
    }

    public void onBackPressed() {
        if (upnpFragment.goPreviousStack()) {
            upnpFragment = null;
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
            finish();
        }
    }

    private void getUPnPFragment() {
        upnpFragment = (UPnPFragment) getFragmentManager().findFragmentById(R.id.upnp_fragment);
    }
}
