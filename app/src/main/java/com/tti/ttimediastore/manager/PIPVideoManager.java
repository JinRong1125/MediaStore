package com.tti.ttimediastore.manager;

/**
 * Created by dylan_liang on 2017/5/17.
 */

public class PIPVideoManager {

    public interface PIPFragmentListener {

        void onPIPFinish();
    }

    private static PIPVideoManager videoPIPManager;
    private PIPFragmentListener pipFragmentListener;

    public static PIPVideoManager getInstance() {
        if (videoPIPManager == null)
            videoPIPManager = new PIPVideoManager();
        return videoPIPManager;
    }

    public void setListener(PIPFragmentListener pipFragmentListener) {
        this.pipFragmentListener = pipFragmentListener;
    }

    public void finishPIP() {
        if (pipFragmentListener != null) {
            pipFragmentListener.onPIPFinish();
        }
    }
}
