package com.tti.ttimediastore.manager;

import com.tti.ttimediastore.model.Audio;

/**
 * Created by dylan_liang on 2017/5/17.
 */

public class AudioTrackUpdater {

    public interface OnTrackUpdater {

        void onTrackUpdate();

        void onTrackListEnd();
    }

    private static AudioTrackUpdater audioTrackUpdater;
    private OnTrackUpdater onTrackUpdater;
    private Audio audio;

    public static AudioTrackUpdater getInstance() {
        if (audioTrackUpdater == null)
            audioTrackUpdater = new AudioTrackUpdater();
        return audioTrackUpdater;
    }

    public void setListener(OnTrackUpdater onTrackUpdater) {
        this.onTrackUpdater = onTrackUpdater;
    }

    public void updateTrack(Audio audio) {
        if (onTrackUpdater != null) {
            this.audio = audio;
            onTrackUpdater.onTrackUpdate();
        }
    }

    public Audio getUpdateTrack() {
        return audio;
    }

    public void endTrackList() {
        if (onTrackUpdater != null)
            onTrackUpdater.onTrackListEnd();
    }
}
