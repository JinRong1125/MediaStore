package com.tti.ttimediastore.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.tti.ttimediastore.utils.Utils;

/**
 * Created by dylan_liang on 2017/5/19.
 */

public class AudioOutputManager extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener {

    private Context context;
    private AudioManager audioManager;
    private ComponentName componentName;
    private AudioPlayerController audioPlayerController;

    private int track;

    private static final float LOWER_VOLUME = 0.25f;
    private static final float USER_VOLUME = 1.0f;

    public AudioOutputManager() {

    }

    public AudioOutputManager(Context context, int track) {
        this.context = context;
        this.track = track;
        audioPlayerController = AudioPlayerController.getInstance();
        setAudioManager();
    }

    private void setAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        componentName = new ComponentName(context.getPackageName(), this.getClass().getName());

        if (audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioPlayerController.setTrack(track);
            audioManager.registerMediaButtonEventReceiver(componentName);
        }
    }

    public void disableManager() {
        audioManager.abandonAudioFocus(this);
        audioManager.unregisterMediaButtonEventReceiver(componentName);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                audioPlayerController.resume();
                audioPlayerController.setVolume(USER_VOLUME);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                audioPlayerController.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioPlayerController.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                audioPlayerController.setVolume(LOWER_VOLUME);
                break;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                AudioPlayerController audioPlayerController = AudioPlayerController.getInstance();
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        audioPlayerController.playpause();
                        break;
                }
            }
        }
    }

    public void adjustVolume(int direction) {
        Utils.adjustVolume(audioManager, direction);
    }
}
