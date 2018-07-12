package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.utils.Utils;

/**
 * Created by dylan_liang on 2017/12/22.
 */

public class ActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkInputMedia();
            }
            else
                finish();
        }
    }

    private void checkPermission() {
        if (Utils.isPermissionGranted(this))
            checkInputMedia();
        else
            Utils.requestMultiplePermissions(this);
    }

    private void checkInputMedia() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            setInputMedia(bundle.getString("title"), bundle.getString("path"));
        else
            finish();
    }

    private void setInputMedia(String title, String path) {
        Constants.MODEL_TYPE = Constants.ACTION_MEDIA;
        Video video = new Video();
        video.setTitle(title);
        video.setPath(path);
        Intent intent = new Intent();
        intent.putExtra(Constants.ITEM_CONTENT, video);
        startActivity(intent.setClass(this, ContentActivity.class));
        finish();
    }
}
