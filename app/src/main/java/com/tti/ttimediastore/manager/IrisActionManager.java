package com.tti.ttimediastore.manager;

/**
 * Created by dylan_liang on 2017/12/20.
 */

public class IrisActionManager {

    public interface IrisCommandListener {

        void onIrisYes(String message);

        void onIrisNo(String message);

        void onIrisPlay(String message);

        void onIrisPause(String message);

        void onIrisVolumeUp(String message);

        void onIrisVolumeDown(String message);

        void onIrisMute(String message);

        void onIrisUnMute(String message);

        void onIrisBack(String message);

        void onIrisClose(String message);

        void onIrisVideo(String message);

        void onIrisAudio(String message);

        void onIrisImage(String message);

        void onIrisMain(String message);
    }

    private static IrisActionManager irisActionManager;
    private IrisCommandListener irisCommandListener;

    public static IrisActionManager getInstance() {
        if (irisActionManager == null)
            irisActionManager = new IrisActionManager();
        return irisActionManager;
    }

    public void setListener(IrisCommandListener irisCommandListener) {
        this.irisCommandListener = irisCommandListener;
    }

    public void yes(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisYes(message);
    }

    public void no(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisNo(message);
    }

    public void play(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisPlay(message);
    }

    public void pause(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisPause(message);
    }

    public void volumeUp(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisVolumeUp(message);
    }

    public void volumeDown(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisVolumeDown(message);
    }

    public void mute(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisMute(message);
    }

    public void unmute(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisUnMute(message);
    }

    public void back(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisBack(message);
    }

    public void close(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisClose(message);
    }

    public void video(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisVideo(message);
    }

    public void audio(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisAudio(message);
    }

    public void image(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisImage(message);
    }

    public void main(String message) {
        if (irisCommandListener != null)
            irisCommandListener.onIrisMain(message);
    }
}
