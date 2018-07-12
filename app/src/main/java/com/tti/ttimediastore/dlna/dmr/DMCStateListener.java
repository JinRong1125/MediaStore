package com.tti.ttimediastore.dlna.dmr;

import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Created by dylan_liang on 2017/8/8.
 */

public class DMCStateListener {

    public interface OnStateChangedListener {

        void onStreamReady(SimpleExoPlayer player);

        void onPlay();

        void onPause();

        void onStop();
    }

    private static DMCStateListener playerReturnListener;
    private OnStateChangedListener onStateChangedListener;

    public static DMCStateListener getInstance() {
        if (playerReturnListener == null)
            playerReturnListener = new DMCStateListener();
        return playerReturnListener;
    }

    public void setListener(OnStateChangedListener onPlayerCreatedListener) {
        this.onStateChangedListener = onPlayerCreatedListener;
    }

    public void returnPlayer(SimpleExoPlayer player) {
        if (onStateChangedListener != null)
            onStateChangedListener.onStreamReady(player);
    }

    public void setDMCPlay() {
        if (onStateChangedListener != null)
            onStateChangedListener.onPlay();
    }

    public void setDMCPause() {
        if (onStateChangedListener != null)
            onStateChangedListener.onPause();
    }

    public void setDMCStop() {
        if (onStateChangedListener != null)
            onStateChangedListener.onStop();
    }
}
