package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.fragment.ItemGridFragment;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class ItemGridActivity extends Activity {

    private ItemGridFragment itemGridFragment;

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
        if (itemGridFragment.shouldFinish())
            backtoMainActivity();
    }

    private void getItemGridFragment() {
        itemGridFragment = (ItemGridFragment) getFragmentManager().findFragmentById(R.id.item_grid_fragment);
    }

    private void createView() {
        Utils.Preferences.setItemViewState(STATE_RUNNING);
        setContentView(R.layout.activity_grid);
        getItemGridFragment();
        Utils.updateMediaStore(this);
    }

    public void backtoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);
        Utils.Preferences.setItemViewState(STATE_CLOSED);
        finish();
    }
}
