package com.tti.ttimediastore.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tti.ttimediastore.activity.ItemGridActivity;
import com.tti.ttimediastore.activity.MainActivity;
import com.tti.ttimediastore.constants.Constants;

/**
 * Created by dylan_liang on 2017/12/20.
 */

public class IrisActionReceiver extends BroadcastReceiver {

    private IrisActionManager irisActionManager = IrisActionManager.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals("IRIS_MEDIA_STORE")) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String message = bundle.getString("message");
            if (message == null)
                return;
            if (message.trim().isEmpty())
                return;
            parseMessage(context, message.toLowerCase());
        }
    }

    private void parseMessage(Context context, String message) {
        if (message.contains("yes") || message.contains("ok")) {
            irisActionManager.yes(message);
        }
        else if (message.contains("no")) {
            irisActionManager.no(message);
        }
        else if (message.contains("play") || message.contains("start")) {
            irisActionManager.play(message);
        }
        else if (message.contains("pause") || message.contains("stop")) {
            irisActionManager.pause(message);
        }
        else if (message.contains("volume") || message.contains("sound")) {
            if (message.contains("up") || message.contains("increase"))
                irisActionManager.volumeUp(message);
            else if (message.contains("down") || message.contains("decrease"))
                irisActionManager.volumeDown(message);
//            else if (message.contains("mute") || message.contains("off"))
//                irisActionManager.mute(message);
//            else if (message.contains("unmute") || message.contains("on"))
//                irisActionManager.unmute(message);
        }
        else if (message.contains("back")) {
            Constants.IRIS_EVENT = Constants.EVENT_BACK;
            irisActionManager.back(message);
        }
        else if (message.contains("close")) {
            Constants.IRIS_EVENT = Constants.EVENT_CLOSE;
            irisActionManager.close(message);
        }
        else if (message.contains("video") || message.contains("movie")) {
            if (Constants.APP_STATE == Constants.STATE_FOREGROUND) {
                Constants.IRIS_EVENT = Constants.EVENT_VIDEO;
                irisActionManager.video(message);
            }
            else {
                Constants.MODEL_TYPE = Constants.VIDEO_ALL;
                Intent intent = new Intent(context, ItemGridActivity.class);
                context.startActivity(intent);
            }
        }
        else if (message.contains("audio") || message.contains("music")) {
            if (Constants.APP_STATE == Constants.STATE_FOREGROUND) {
                Constants.IRIS_EVENT = Constants.EVENT_AUDIO;
                irisActionManager.audio(message);
            }
            else {
                Constants.MODEL_TYPE = Constants.AUDIO_ALL;
                Intent intent = new Intent(context, ItemGridActivity.class);
                context.startActivity(intent);
            }
        }
        else if (message.contains("image") || message.contains("picture")) {
            if (Constants.APP_STATE == Constants.STATE_FOREGROUND) {
                Constants.IRIS_EVENT = Constants.EVENT_IMAGE;
                irisActionManager.image(message);
            }
            else {
                Constants.MODEL_TYPE = Constants.IMAGE_ALL;
                Intent intent = new Intent(context, ItemGridActivity.class);
                context.startActivity(intent);
            }
        }
        else if (message.contains("open")) {
            if (Constants.APP_STATE == Constants.STATE_FOREGROUND) {
                Constants.IRIS_EVENT = Constants.EVENT_MAIN;
                irisActionManager.main(message);
            }
            else {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
