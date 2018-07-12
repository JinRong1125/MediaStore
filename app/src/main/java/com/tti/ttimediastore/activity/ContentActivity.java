package com.tti.ttimediastore.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.fragment.AudioFragment;
import com.tti.ttimediastore.fragment.ImageFragment;
import com.tti.ttimediastore.fragment.InternetFragment;
import com.tti.ttimediastore.fragment.VideoFragment;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class ContentActivity extends Activity {

    private Fragment currentFragmet;
    private String ModelType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        setFragment();
    }

    private void setFragment() {
        ModelType = Constants.MODEL_TYPE;

        if (ModelType.equals(Constants.VIDEO_ALL)
                || ModelType.equals(Constants.VIDEO_RESUME)
                || ModelType.equals(Constants.INTERNET_TO_VIDEO)
                || ModelType.equals(Constants.INPUT_MEDIA)
                || ModelType.equals(Constants.ACTION_MEDIA))
            currentFragmet = new VideoFragment();
        else if (ModelType.equals(Constants.AUDIO_ARTISTS)
                || ModelType.equals(Constants.AUDIO_ALBUMS)
                || ModelType.equals(Constants.AUDIO_GENRES)
                || ModelType.equals(Constants.AUDIO_ALL)
                || ModelType.equals(Constants.AUDIO_SEARCH)
                || ModelType.equals(Constants.INPUT_AUDIO))
            currentFragmet = new AudioFragment();
        else if (ModelType.equals(Constants.IMAGE_ALL)
                || ModelType.equals(Constants.INPUT_IMAGE))
            currentFragmet = new ImageFragment();
        else if (ModelType.equals(Constants.OTHER_INTERNET))
            currentFragmet = new InternetFragment();

        getFragmentManager().beginTransaction().replace(R.id.contentContainer, currentFragmet).commit();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(currentFragmet instanceof VideoFragment) {
            ((VideoFragment) currentFragmet).onKeyDown(keyCode, event);
            return true;
        }
        else if (currentFragmet instanceof AudioFragment) {
            ((AudioFragment) currentFragmet).onKeyDown(keyCode, event);
            return true;
        }
        else if (currentFragmet instanceof ImageFragment) {
            ((ImageFragment) currentFragmet).onKeyDown(keyCode, event);
            return true;
        }
        else if (currentFragmet instanceof InternetFragment) {
            ((InternetFragment) currentFragmet).onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }
}
