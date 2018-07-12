package com.tti.ttimediastore.dlna.dmr;

/**
 * Created by dylan_liang on 2017/5/17.
 */

public class DMRPlayerController {

    public interface OnDMRListener {

        void onDMRPlay();

        void onDMRPause();

        void onDMRSeek(long seekTime);

        void onDMRStop();
    }

    private static DMRPlayerController dmrPlayerController;
    private OnDMRListener onDMRListener;

    private DMRPlayerController() {}

    public static DMRPlayerController getInstance() {
        if (dmrPlayerController == null)
            dmrPlayerController = new DMRPlayerController();
        return dmrPlayerController;
    }

    public void setListener(OnDMRListener onDMRListener) {
        this.onDMRListener = onDMRListener;
    }

    public OnDMRListener getListener() {
        return onDMRListener;
    }

    public void play() {
        if (onDMRListener != null)
            onDMRListener.onDMRPlay();
    }

    public void pause() {
        if (onDMRListener != null)
            onDMRListener.onDMRPause();
    }

    public void seek(long seekTime) {
        if (onDMRListener != null)
            onDMRListener.onDMRSeek(seekTime);
    }

    public void stop() {
        if (onDMRListener != null)
            onDMRListener.onDMRStop();
    }
}
