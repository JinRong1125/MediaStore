package com.tti.ttimediastore.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.SimpleExoPlayer;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.constants.Constants;

/**
 * Created by dylan_liang on 2017/4/17.
 */

public class PlayerPanel {

    private View panelView;
    private TextView titleView, currentTime, endTime;
    private ImageButton playpauseButton, backwardButton, forwardButton, subtitlesButton;
    private SeekBar seekBar;

    private View focusView;

    private SimpleExoPlayer player;

    private PanelHandler panelHandler;
    private FocusHandler focusHandler;

    private Context context;
    private String title;
    private boolean isTracking = false;

    private int panelType;
    private static final int VIDEO_TYPE = 0;
    private static final int AUDIO_TYPE = 1;

    private int buttonSize;
    private static final int VIDEO_SIZE = 80;
    private static final int AUDIO_SIZE = 60;

    private static final int PANEL_HIDE = 0;
    private static final int PANEL_SHOW = 1;
    private static final int PANEL_PROGRESS = 2;
    private static final int UPDATE_TIMEOUT = 1000;
    private static final int PANEL_TIMEOUT = 5000;

    private static final int BUTTON_FOCUSED = 0;
    private static final int BUTTON_UNFOCUS = 1;
    private static final float SEEKBAR_SIZE = 5f;

    public PlayerPanel(Context context, SimpleExoPlayer player, String title) {
        this.context = context;
        this.player = player;
        this.title = title;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        String ModelType = Constants.MODEL_TYPE;
        if (ModelType.equals(Constants.VIDEO_ALL) ||
                ModelType.equals(Constants.VIDEO_RESUME) ||
                ModelType.equals(Constants.INTERNET_TO_VIDEO) ||
                ModelType.equals(Constants.INPUT_MEDIA) ||
                ModelType.equals(Constants.ACTION_MEDIA)) {
            panelType = VIDEO_TYPE;
            buttonSize = VIDEO_SIZE;
            panelView = layoutInflater.inflate(R.layout.panel_video, null, false);
        }
        else {
            panelType = AUDIO_TYPE;
            buttonSize = AUDIO_SIZE;
            panelView = layoutInflater.inflate(R.layout.panel_audio, null, false);
        }

        panelHandler = new PanelHandler();
        focusHandler = new FocusHandler();

        if (player != null)
            initView();
    }

    private void initView() {
        titleView = (TextView) panelView.findViewById(R.id.titleView);
        titleView.setText(title);

        playpauseButton = (ImageButton) panelView.findViewById(R.id.playpauseButton);
        setButtonImage(playpauseButton, R.drawable.pause_icon);
        playpauseButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_FOCUSED, playpauseButton));
                    focusView = view;
                }
                else {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_UNFOCUS, playpauseButton));
                }
            }
        });
        focusView = playpauseButton;

        backwardButton = (ImageButton) panelView.findViewById(R.id.backwardButton);
        setButtonImage(backwardButton, R.drawable.backward_icon);
        backwardButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_FOCUSED, backwardButton));
                    focusView = view;
                }
                else {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_UNFOCUS, backwardButton));
                }
            }
        });

        forwardButton = (ImageButton) panelView.findViewById(R.id.forwardButton);
        setButtonImage(forwardButton, R.drawable.forward_icon);
        forwardButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_FOCUSED, forwardButton));
                    focusView = view;
                }
                else {
                    focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_UNFOCUS, forwardButton));
                }
            }
        });

        if (panelType == VIDEO_TYPE) {
            subtitlesButton = (ImageButton) panelView.findViewById(R.id.subtitlesButton);
            int subtitlesMode = Utils.Preferences.getSubtitlesMode();
            switch (subtitlesMode) {
                case 0:
                    setButtonImage(subtitlesButton, R.drawable.subtitles_icon);
                    break;
                case 1:
                    setButtonImage(subtitlesButton, R.drawable.subtitles_check);
                    break;
            }
            subtitlesButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean isFocused) {
                    if (isFocused) {
                        focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_FOCUSED, subtitlesButton));
                        focusView = view;
                    }
                    else {
                        focusHandler.sendMessage(focusHandler.obtainMessage(BUTTON_UNFOCUS, subtitlesButton));
                    }
                }
            });
        }

        currentTime =  (TextView) panelView.findViewById(R.id.startTime);

        endTime =  (TextView) panelView.findViewById(R.id.endTime);
        if (player.getDuration() > 0)
            endTime.setText(Utils.generateTime(player.getDuration()));

        seekBar = (SeekBar) panelView.findViewById(R.id.progressBar);
        seekBar.setProgressTintList(ColorStateList.valueOf(context.getColor(R.color.focus_green)));
        seekBar.setSecondaryProgressTintList(ColorStateList.valueOf(context.getColor(R.color.mid_gray)));
        seekBar.setScaleY(SEEKBAR_SIZE);
        if (player.getDuration() > 0)
            seekBar.setMax((int) (player.getDuration() / 1000));
        else {
            seekBar.setMax(1);
            seekBar.setProgress(1);
            forwardButton.setEnabled(false);
        }
        seekBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) focusView = view;
            }
        });
    }

    private void setButtonImage(final ImageButton button, int resourceId) {
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions().centerCrop())
                .into(new SimpleTarget<Drawable>(buttonSize, buttonSize) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        button.setImageDrawable(resource);
                    }
                });
    }

    private class PanelHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PANEL_HIDE:
                    panelView.setVisibility(View.GONE);
                    break;
                case PANEL_SHOW:
                    panelView.setVisibility(View.VISIBLE);
                    if (!isTracking) {
                        long currentPosition = player.getCurrentPosition();
                        setProgress(currentPosition, player.getBufferedPosition());

                        long tolerance = UPDATE_TIMEOUT - (currentPosition % UPDATE_TIMEOUT);
                        if (tolerance >= 100)
                            panelHandler.sendMessageDelayed(
                                    panelHandler.obtainMessage(PANEL_PROGRESS), tolerance);
                        else
                            panelHandler.sendMessageDelayed(
                                    panelHandler.obtainMessage(PANEL_PROGRESS), UPDATE_TIMEOUT);
                    }
                    break;
                case PANEL_PROGRESS:
                    if (panelView.isShown() && !isTracking) {
                        setProgress(player.getCurrentPosition(), player.getBufferedPosition());

                        panelHandler.sendMessageDelayed(
                                panelHandler.obtainMessage(PANEL_PROGRESS), UPDATE_TIMEOUT);
                    }
            }
        }
    }

    public void showPanel() {
        panelHandler.removeMessages(PANEL_HIDE);
        panelHandler.sendEmptyMessage(PANEL_SHOW);
        panelHandler.sendMessageDelayed(panelHandler.obtainMessage(PANEL_HIDE), PANEL_TIMEOUT);
    }

    public void stayShowPanel() {
        panelHandler.removeMessages(PANEL_HIDE);
        panelHandler.sendEmptyMessage(PANEL_SHOW);
    }

    public void hidePanel() {
        panelHandler.removeMessages(PANEL_SHOW);
        panelHandler.sendEmptyMessage(PANEL_HIDE);
    }

    private class FocusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ImageButton imageButton = (ImageButton) msg.obj;
            switch (msg.what) {
                case BUTTON_FOCUSED:
                    imageButton.setBackgroundResource(R.drawable.focus_design);
                    break;
                case BUTTON_UNFOCUS:
                    imageButton.setBackgroundResource(R.color.transparent);
                    break;
            }
        }
    }

    public void switchPlayPause(Boolean IsPlaying) {
        if (!IsPlaying)
            setButtonImage(playpauseButton, R.drawable.play_icon);
        else
            setButtonImage(playpauseButton, R.drawable.pause_icon);
    }

    public void setProgress(long position, long buffer) {
        seekBar.setProgress((int) (position / 1000));
        seekBar.setSecondaryProgress((int) (buffer / 1000));
        currentTime.setText(Utils.generateTime(position));
    }

    public View getPanelView() {
        return panelView;
    }

    public View getFocusView() {
        return focusView;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setCurrentTime(boolean isTracking) {
        this.isTracking = isTracking;
        if (isTracking)
            currentTime.setText(Utils.generateTime(seekBar.getProgress() * 1000));
    }

    public ImageButton getPlayPauseButton() {
        return playpauseButton;
    }

    public ImageButton getBackwardButton() {
        return backwardButton;
    }

    public ImageButton getForwardButton() {
        return forwardButton;
    }

    public ImageButton getSubtitlesButton() {
        return subtitlesButton;
    }

    public void updateMedia(String title) {
        titleView.setText(title);
        if (player.getDuration() > 0) {
            seekBar.setMax((int) (player.getDuration() / 1000));
            endTime.setText(Utils.generateTime(player.getDuration()));
        }
        else {
            endTime.setText("");
            seekBar.setMax(1);
            seekBar.setProgress(1);
        }
    }
}
