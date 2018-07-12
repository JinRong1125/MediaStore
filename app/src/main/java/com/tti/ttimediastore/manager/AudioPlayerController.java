package com.tti.ttimediastore.manager;

import android.os.Handler;
import android.os.Message;

/**
 * Created by dylan_liang on 2017/5/17.
 */

public class AudioPlayerController {

    public interface OnPlayerListener {

        void onAudioTrackSet(int track);

        void onAudioPlayPause();

        void onAudioResume();

        void onAudioPause();

        void onAudioFinish();

        void onAudioVolumeSet(float volume);
    }

    private static AudioPlayerController audioPlayerController;
    private OnPlayerListener onPlayerListener;

    private ClickHandler clickHandler;

    private boolean isPress = false;

    private static final int ENABLE_PRESS = 0;
    private static final int DELAY_PRESS = 150;

    public static AudioPlayerController getInstance() {
        if (audioPlayerController == null)
            audioPlayerController = new AudioPlayerController();
        return audioPlayerController;
    }

    public void setListener(OnPlayerListener onPlayerListener) {
        this.onPlayerListener = onPlayerListener;
        clickHandler = new ClickHandler();
    }

    public OnPlayerListener getListener() {
        return onPlayerListener;
    }

    public void setTrack(int track) {
        if (onPlayerListener != null)
            onPlayerListener.onAudioTrackSet(track);
    }

    public void playpause() {
        if (onPlayerListener != null && !isPress)
            setClick();
    }

    public void resume() {
        if (onPlayerListener != null)
            onPlayerListener.onAudioResume();
    }

    public void pause() {
        if (onPlayerListener != null)
            onPlayerListener.onAudioPause();
    }

    public void finish() {
        if (onPlayerListener != null)
            onPlayerListener.onAudioFinish();
    }

    public void setVolume(float volume) {
        if (onPlayerListener != null)
            onPlayerListener.onAudioVolumeSet(volume);
    }

    private void setClick() {
        isPress = true;
        onPlayerListener.onAudioPlayPause();
        clickHandler.sendMessageDelayed(clickHandler.obtainMessage(ENABLE_PRESS), DELAY_PRESS);
    }

    private class ClickHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_PRESS:
                    isPress = false;
                    break;
            }
        }
    }
}
