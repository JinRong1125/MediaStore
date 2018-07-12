package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.Video;

public class MainActivity extends Activity {

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
                TTIMediaStore.getInstance().startDLNA();
            }
            else
                finish();
        }
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void checkPermission() {
        if (Utils.isPermissionGranted(this))
            checkInputMedia();
        else
            Utils.requestMultiplePermissions(this);
    }

    private void checkInputMedia() {
        Intent intent = getIntent();
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_VIEW)) {
                String data = intent.getDataString();
                if (data != null)
                    setInputMedia(URLUtil.guessFileName(data, null, null), data);
                else
                    setMainLayout();
            }
            else
                setMainLayout();
        }
        else
            setMainLayout();
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

    private void setMainLayout() {
        Constants.MODEL_TYPE = null;
        setContentView(R.layout.activity_main);
    }
}
