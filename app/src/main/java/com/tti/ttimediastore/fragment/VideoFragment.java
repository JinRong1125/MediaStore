package com.tti.ttimediastore.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.tti.ttimediastore.R;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.dlna.dmr.DMRPlayerController;
import com.tti.ttimediastore.dlna.dmr.DMCStateListener;
import com.tti.ttimediastore.manager.IrisActionManager;
import com.tti.ttimediastore.manager.PIPVideoManager;
import com.tti.ttimediastore.model.OptionDialog;
import com.tti.ttimediastore.model.PlayerPanel;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.InfoDialog;
import com.tti.ttimediastore.model.Video;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class VideoFragment extends Fragment implements AudioManager.OnAudioFocusChangeListener,
        DMRPlayerController.OnDMRListener, PIPVideoManager.PIPFragmentListener, IrisActionManager.IrisCommandListener {

    private View view;
    private RelativeLayout bufferingLayout;
    private FrameLayout panelContainer;

    private SimpleExoPlayerView videoView;
    private SimpleExoPlayer player;
    private PlayerPanel playerPanel;
    private ImageButton playpauseButton, backwardButton, forwardButton, subtitlesButton;
    private SeekBar seekBar;
    private DataSource.Factory mediaDataSourceFactory;

    private MediaSource contentSource;
    private MediaSource withSubtitleSource;

    private Video video;

    private boolean isPause = false;
    private boolean isMirroring = false;
    private boolean isResetPosition = false;
    private boolean shouldResume = false;
    private boolean isMoving = false;
    private boolean isFinished = false;

    private SeekHandler seekHandler;
    private MoveHandler moveHandler;
    private DMCStateListener dmcStateListener;

    private AudioManager audioManager;

    private OptionDialog recordDialog;
    private InfoDialog errorDialog;
    private long recordPosition;

    private String ModelType;

    private int subtitlesMode;
    private static final int SUBTITLES_OFF = 0;
    private static final int SUBTITLES_ON = 1;

    private static final int BUTTON_SIZE = 80;

    private static final int ENABLE_MOVE = 0;

    private static final int ENABLE_SEEK = 0;
    private static final int DISABLE_SEEK = 1;
    private static final int DELAY_SEEK = 3000;

    private static final int DELAY_TIMEOUT = 100;

    private static final float LOWER_VOLUME = 0.25f;
    private static final float USER_VOLUME = 1.0f;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        initView();

        ModelType = Constants.MODEL_TYPE;

        video = getActivity().getIntent().getParcelableExtra(Constants.ITEM_CONTENT);
        Log.v("TTIMediaStore", "videoPath: " + video.getPath());

        createPlayer();
    }

    public void onResume() {
        super.onResume();
        if (!getActivity().isInPictureInPictureMode())
            setAppResume();
        adjustFullScreen(getActivity().getResources().getConfiguration());
        if (playerPanel != null)
            playerPanel.showPanel();
        IrisActionManager.getInstance().setListener(this);
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        if (!getActivity().isInPictureInPictureMode())
            setAppPause();
        IrisActionManager.getInstance().setListener(null);
        isPause = true;
    }

    public void onDestroy() {
        super.onDestroy();
        stopAllTask();
        storePosition();
        releasePlayer();
        finishContent();
    }

    @Override
    public void onDMRPlay() {
        setPlay();
    }

    @Override
    public void onDMRPause() {
        setPause();
    }

    @Override
    public void onDMRSeek(long seekTime) {
        if (player != null && playerPanel != null && seekBar.isEnabled()) {
            player.seekTo(seekTime);
            seekHandler.sendEmptyMessage(DISABLE_SEEK);
            seekHandler.sendMessageDelayed(seekHandler.obtainMessage(ENABLE_SEEK), DELAY_SEEK);
            playerPanel.showPanel();
        }
    }

    @Override
    public void onDMRStop() {
        dmcStateListener = null;
        getActivity().finish();
    }

    @Override
    public void onPIPFinish() {
        getActivity().finish();
    }

    @Override
    public void onIrisYes(String message) {
        if (recordDialog != null && recordDialog.isShowing())
            recordDialog.getYesButton().performClick();
        else if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
            getActivity().finish();
        }
    }

    @Override
    public void onIrisNo(String message) {
        if (recordDialog != null && recordDialog.isShowing())
            recordDialog.getNoButton().performClick();
        else if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
            getActivity().finish();
        }
    }

    @Override
    public void onIrisPlay(String message) {
        if (player != null && playerPanel != null)
            setPlay();
    }

    @Override
    public void onIrisPause(String message) {
        if (player != null && playerPanel != null)
            setPause();
    }

    @Override
    public void onIrisVolumeUp(String message) {
        Utils.adjustVolume(audioManager, AudioManager.ADJUST_RAISE);
    }

    @Override
    public void onIrisVolumeDown(String message) {
        Utils.adjustVolume(audioManager, AudioManager.ADJUST_LOWER);
    }

    @Override
    public void onIrisMute(String message) {
        Utils.adjustVolume(audioManager, AudioManager.ADJUST_MUTE);
    }

    @Override
    public void onIrisUnMute(String message) {
        Utils.adjustVolume(audioManager, AudioManager.ADJUST_UNMUTE);
    }

    @Override
    public void onIrisBack(String message) {
        getActivity().finish();
    }

    @Override
    public void onIrisClose(String message) {
        getActivity().finish();
    }

    @Override
    public void onIrisVideo(String message) {
        getActivity().finish();
    }

    @Override
    public void onIrisAudio(String message) {
        getActivity().finish();
    }

    @Override
    public void onIrisImage(String message) {
        getActivity().finish();
    }

    @Override
    public void onIrisMain(String message) {
        getActivity().finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.NOTIFICATION_MUTE)
            adjustVolume(AudioManager.ADJUST_MUTE);
        else if (requestCode == Constants.NOTIFICATION_UNMUTE)
            adjustVolume(AudioManager.ADJUST_UNMUTE);
    }

    private void adjustVolume(int direction) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
    }

    private void initView() {
        bufferingLayout = (RelativeLayout) view.findViewById(R.id.buffering_indicator);
        bufferingLayout.setVisibility(View.VISIBLE);

        panelContainer = (FrameLayout) view.findViewById(R.id.panelContainer);

        videoView = (SimpleExoPlayerView) view.findViewById(R.id.videoView);
        videoView.setFocusable(false);
        videoView.setUseController(false);

        seekHandler = new SeekHandler();
        moveHandler = new MoveHandler();

        dmcStateListener = DMCStateListener.getInstance();
    }

    private void createPlayer() {
        String videoPath = video.getPath();
        if (videoPath.startsWith("http://")) {
            try {
                URL url = new URL(videoPath);
                if (url.getPort() == 8181 && url.getPath().equals("/dlna/stream.flv")) {
                    DefaultLoadControl defaultLoadControl = new DefaultLoadControl(
                            new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
                            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                            1500,
                            3000,
                            DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
                            DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS);
                    TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
                    TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
                    player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, defaultLoadControl);
                    player.addListener(new EventListener());
                    videoView.setPlayer(player);

                    setAudioManager();
                    isMirroring = true;
                    panelContainer.setVisibility(View.GONE);
                }
                else
                    setDefaultPlayer();
            } catch (Exception e) {
                setDefaultPlayer();
                e.printStackTrace();
            }
        }
        else
            setDefaultPlayer();
    }

    private void setDefaultPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        player.addListener(new EventListener());
        videoView.setPlayer(player);

        setAudioManager();
    }

    private void setDMROption() {
        dmcStateListener.returnPlayer(player);
        DMRPlayerController.getInstance().setListener(this);
    }

    private class EventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(final boolean playWhenReady, int playbackState) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (playerPanel != null) {
                        playerPanel.switchPlayPause(playWhenReady);
                    }
                }
            });
            if (dmcStateListener != null) {
                if (playWhenReady)
                    dmcStateListener.setDMCPlay();
                else
                    dmcStateListener.setDMCPause();
            }

            switch (playbackState) {
                case Player.STATE_IDLE:
                    bufferingLayout.setVisibility(View.GONE);
                    break;
                case Player.STATE_READY:
                    setPanel();
                    bufferingLayout.setVisibility(View.GONE);
                    break;
                case Player.STATE_BUFFERING:
                    if (!isMirroring)
                        bufferingLayout.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_ENDED:
                    if (playerPanel != null)
                        playerPanel.showPanel();
                    bufferingLayout.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (withSubtitleSource != null) {
                withSubtitleSource = null;
                subtitlesMode = SUBTITLES_OFF;
                player.prepare(contentSource, isResetPosition, true);
                if (isResetPosition)
                    player.seekTo(recordPosition);
                return;
            }

            if (getActivity().isInPictureInPictureMode()) {
                getActivity().finish();
                return;
            }

            if (ModelType.equals(Constants.VIDEO_ALL) || ModelType.equals(Constants.VIDEO_RESUME))
                errorDialog = new InfoDialog(getActivity(), getString(R.string.video_error));
            else
                errorDialog = new InfoDialog(getActivity(), getString(R.string.media_error));

            errorDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_BACK
                            || keyCode == KeyEvent.KEYCODE_ESCAPE
                            && errorDialog.isShowing()) {
                        errorDialog.dismiss();
                        getActivity().finish();
                    }
                    return false;
                }
            });
            errorDialog.show();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }

    private void setAudioManager() {
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPause)
                        return;
                    setVideo();
                }
            }, 1000);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player != null)
                    player.setVolume(USER_VOLUME);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                getActivity().finish();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                getActivity().finish();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player != null)
                    player.setVolume(LOWER_VOLUME);
                break;
        }
    }

    private void setVideo() {
        if (ModelType.equals(Constants.VIDEO_ALL)
                || ModelType.equals(Constants.VIDEO_RESUME)) {
            ArrayList<Video> recordList = Utils.Preferences.getVideoRecord();

            if (recordList == null)
                setContentSource(true);
            else {
                int listSize = recordList.size();
                if (listSize == 0)
                    setContentSource(true);
                else {
                    for (int i = 0; i < listSize; i++) {
                        if (video.getId().equals(recordList.get(i).getId())) {
                            recordPosition = recordList.get(i).getRecord();
                            recordList.remove(i);
                            Utils.Preferences.setVideoRecord(recordList);

                            if (ModelType.equals(Constants.VIDEO_RESUME)) {
                                setContentSource(false);
                                player.seekTo(recordPosition);
                            }
                            else {
                                bufferingLayout.setVisibility(View.GONE);
                                setRecord();
                            }
                            return;
                        }
                    }

                    setContentSource(true);
                }
            }
        }
        else if (ModelType.equals(Constants.INTERNET_TO_VIDEO)
                || ModelType.equals(Constants.INPUT_MEDIA)
                || ModelType.equals(Constants.ACTION_MEDIA))
            setContentSource(true);
    }

    private void setRecord() {
        recordDialog = new OptionDialog(getActivity(), getString(R.string.video_record)
                + Utils.generateTime(recordPosition), new YesListener(), new NoListener());
        recordDialog.show();
        recordDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        || keyCode == KeyEvent.KEYCODE_ESCAPE
                        && recordDialog.isShowing()) {
                    setContentSource(true);
                    recordDialog.dismiss();
                }
                return false;
            }
        });
    }

    private class YesListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            setContentSource(false);
            player.seekTo(recordPosition);
            recordDialog.dismiss();
        }
    }

    private class NoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            setContentSource(true);
            recordDialog.dismiss();
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((TTIMediaStore) getActivity().getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private MediaSource buildMediaSource(Uri uri) {
        mediaDataSourceFactory = buildDataSourceFactory(true);
        int type = Util.inferContentType(uri.getLastPathSegment());

        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory,
                        new DefaultExtractorsFactory(), null, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void setContentSource(boolean isResetPosition) {
        this.isResetPosition = isResetPosition;

        String videoPath = video.getPath();
        contentSource = buildMediaSource(Uri.parse(videoPath));
        Uri subtitleUri = getSubtitlesFile(videoPath);

        subtitlesMode = Utils.Preferences.getSubtitlesMode();
        if (subtitleUri == null)
            player.prepare(contentSource, isResetPosition, true);
        else {
            MediaSource subtitleSource = new SingleSampleMediaSource(
                    subtitleUri,
                    mediaDataSourceFactory,
                    Format.createTextSampleFormat(
                            null,
                            MimeTypes.APPLICATION_SUBRIP,
                            Format.NO_VALUE,
                            null),
                    C.TIME_UNSET);
            withSubtitleSource = new MergingMediaSource(contentSource, subtitleSource);
            switch (subtitlesMode) {
                case SUBTITLES_OFF:
                    player.prepare(contentSource, isResetPosition, true);
                    break;
                case SUBTITLES_ON:
                    player.prepare(withSubtitleSource, isResetPosition, true);
                    break;
            }
        }

        player.setPlayWhenReady(true);
    }

    private Uri getSubtitlesFile(String videoPath) {
        if (videoPath.equals("."))
            return null;

        Uri subtitleUri = null;

        int lastIndex = 0;
        String fileName = "";
        String sourceLastSegment = Uri.parse(videoPath).getLastPathSegment();
        if (sourceLastSegment.contains(".")) {
            lastIndex = sourceLastSegment.lastIndexOf(".");
            if (lastIndex > 0)
                fileName = sourceLastSegment.substring(0, lastIndex);
        }
        else
            fileName = sourceLastSegment;

        if (videoPath.startsWith("file://")){
            videoPath = videoPath.replace("file://", "");
            File[] files = new File(videoPath).getParentFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(fileName)) {
                        Uri fileUri = Uri.fromFile(file);
                        String lastSegment = fileUri.getLastPathSegment();
                        if (lastSegment.substring(lastSegment.lastIndexOf(".")).equals(".srt")) {
                            subtitleUri = fileUri;
                            break;
                        }
                    }
                }
            }
        }

        return subtitleUri;
    }

    private void setPanel() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPause)
                    return;
                if (playerPanel == null) {
                    playerPanel = new PlayerPanel(getActivity(), player, video.getTitle());
                    panelContainer.addView(playerPanel.getPanelView());
                    if (!getActivity().isInPictureInPictureMode())
                        playerPanel.showPanel();

                    playpauseButton = playerPanel.getPlayPauseButton();
                    backwardButton = playerPanel.getBackwardButton();
                    forwardButton = playerPanel.getForwardButton();
                    subtitlesButton = playerPanel.getSubtitlesButton();
                    seekBar = playerPanel.getSeekBar();

                    playpauseButton.requestFocus();
                    setPanelListener();
                    setPlay();

                    setDMROption();
                }
            }
        }, DELAY_TIMEOUT);
    }

    private void setPanelListener() {
        playpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayPause();
            }
        });

        seekBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                        && seekBar.isFocused()) {
                    switch (keyEvent.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            if (seekBar.isEnabled()) {
                                playerPanel.stayShowPanel();
                                playerPanel.setCurrentTime(true);
                            }
                            break;
                        case KeyEvent.ACTION_UP:
                            setSeeking();
                            break;
                    }
                }
                return false;
            }
        });

        backwardButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && backwardButton.isFocused()) {
                    switch (keyEvent.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            if (player.getCurrentPosition() > 0 && seekBar.isEnabled()) {
                                seekBar.setProgress(seekBar.getProgress() - (int) (player.getDuration() / (100 * 1000)));
                                playerPanel.stayShowPanel();
                                playerPanel.setCurrentTime(true);
                            }
                            break;
                        case KeyEvent.ACTION_UP:
                            setSeeking();
                            break;
                    }
                }
                return false;
            }
        });

        forwardButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && forwardButton.isFocused()) {
                    switch (keyEvent.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            long duration = player.getDuration();
                            if (player.getCurrentPosition() < duration && seekBar.isEnabled()) {
                                seekBar.setProgress(seekBar.getProgress() + (int) (duration / (100 * 1000)));
                                playerPanel.stayShowPanel();
                                playerPanel.setCurrentTime(true);
                            }
                            break;
                        case KeyEvent.ACTION_UP:
                            setSeeking();
                            break;
                    }
                }
                return false;
            }
        });

        if (withSubtitleSource != null) {
            subtitlesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currentPosition = player.getCurrentPosition();
                    switch (subtitlesMode) {
                        case SUBTITLES_OFF:
                            subtitlesMode = SUBTITLES_ON;
                            player.prepare(withSubtitleSource, false, true);
                            setButtonImage(subtitlesButton, R.drawable.subtitles_check);
                            break;
                        case SUBTITLES_ON:
                            subtitlesMode = SUBTITLES_OFF;
                            player.prepare(contentSource, false, true);
                            setButtonImage(subtitlesButton, R.drawable.subtitles_icon);
                            break;
                    }
                    player.seekTo(currentPosition);
                }
            });
        }
        else
            subtitlesButton.setVisibility(View.GONE);
    }

    private void setButtonImage(final ImageButton button, int resourceId) {
        Glide.with(this)
                .load(resourceId)
                .apply(new RequestOptions().centerCrop())
                .into(new SimpleTarget<Drawable>(BUTTON_SIZE, BUTTON_SIZE) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        button.setImageDrawable(resource);
                    }
                });
    }

    private void setPlayPause() {
        if (player != null && playerPanel != null) {
            if (!player.getPlayWhenReady())
                player.setPlayWhenReady(true);
            else
                player.setPlayWhenReady(false);

            playpauseButton.requestFocus();
            playerPanel.showPanel();
        }
    }

    private void setAppResume() {
        if (player != null) {
            if (shouldResume)
                player.setPlayWhenReady(true);
        }
    }

    private void setAppPause() {
        if (player != null) {
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
                shouldResume = true;
            }
            else
                shouldResume = false;
        }
    }

    private void setPlay() {
        if (player != null) {
            if (!player.getPlayWhenReady())
                player.setPlayWhenReady(true);
            playerPanel.showPanel();
        }
    }

    private void setPause() {
        if (player != null) {
            if (player.getPlayWhenReady())
                player.setPlayWhenReady(false);
            playerPanel.showPanel();
        }
    }

    private void setSeeking() {
        if (seekBar.isEnabled()) {
            player.seekTo(seekBar.getProgress() * 1000);
            playerPanel.showPanel();
            playerPanel.setCurrentTime(false);
            seekHandler.sendEmptyMessage(DISABLE_SEEK);
            seekHandler.sendMessageDelayed(seekHandler.obtainMessage(ENABLE_SEEK), DELAY_SEEK);
        }
    }

    private class SeekHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISABLE_SEEK:
                    seekBar.setEnabled(false);
                    break;
                case ENABLE_SEEK:
                    seekBar.setEnabled(true);
                    break;
            }
        }
    }

    private void setMoving() {
        isMoving = true;
        moveHandler.sendMessageDelayed(moveHandler.obtainMessage(ENABLE_MOVE), DELAY_TIMEOUT);
    }

    private class MoveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_MOVE:
                    isMoving = false;
                    break;
            }
        }
    }

    private void setPIPMode() {
        if (!getActivity().isInPictureInPictureMode() && playerPanel != null) {
            playerPanel.hidePanel();
            getActivity().enterPictureInPictureMode();
            PIPVideoManager.getInstance().setListener(this);
            Constants.PIP_MODE = Constants.PIP_ON;
        }
    }

    private void adjustFullScreen(Configuration config) {
        final View decorView = getActivity().getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void stopAllTask() {
        PIPVideoManager.getInstance().setListener(null);
        DMRPlayerController.getInstance().setListener(null);
        if (dmcStateListener != null)
            dmcStateListener.setDMCStop();
        if (seekHandler != null)
            seekHandler.removeCallbacksAndMessages(null);
        if (moveHandler != null)
            moveHandler.removeCallbacksAndMessages(null);
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            videoView.setPlayer(null);
        }
    }

    private void storePosition() {
        if (ModelType.equals(Constants.VIDEO_ALL) || ModelType.equals(Constants.VIDEO_RESUME)) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            int playbackState = player.getPlaybackState();
            if (duration > 1000 && currentPosition >= 1000 && currentPosition < duration - 1000
                    && playbackState != Player.STATE_ENDED
                    && playbackState != Player.STATE_IDLE) {
                video.setRecord(currentPosition - 1000);
                ArrayList<Video> recordList = Utils.Preferences.getVideoRecord();

                if (recordList == null || recordList.size() < 1)
                    recordList = new ArrayList<>();

                recordList.add(video);
                Utils.Preferences.setVideoRecord(recordList);
            }
        }
    }

    private void finishContent() {
        if (isFinished)
            return;
        isFinished = true;
        if (audioManager != null)
            audioManager.abandonAudioFocus(this);
        Utils.Preferences.setSubtitlesMode(subtitlesMode);
        Constants.PIP_MODE = Constants.PIP_OFF;

        if (ModelType.equals(Constants.INTERNET_TO_VIDEO)) {
            Constants.MODEL_TYPE = Constants.OTHER_INTERNET;
            Intent intent = new Intent(getActivity(), ContentActivity.class);
            startActivity(intent);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isMoving)
            return false;
        setMoving();

        if (keyCode == KeyEvent.KEYCODE_ESCAPE
                || keyCode == KeyEvent.KEYCODE_BACK) {
            getActivity().finish();
        }
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            setPIPMode();
        }
        else if (player != null && playerPanel != null) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                setPlayPause();
            }
            else if (playerPanel.getPanelView().isShown()) {
                if (seekBar.isFocused()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            playpauseButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            playpauseButton.requestFocus();
                            break;
                    }
                }
                else if (playpauseButton.isFocused()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            seekBar.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            backwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            forwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            seekBar.requestFocus();
                            break;
                    }
                }
                else if (backwardButton.isFocused()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            seekBar.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            if (subtitlesButton.getVisibility() == View.VISIBLE)
                                subtitlesButton.requestFocus();
                            else
                                forwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            playpauseButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            seekBar.requestFocus();
                            break;
                    }
                }
                else if (forwardButton.isFocused()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            seekBar.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            playpauseButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (subtitlesButton.getVisibility() == View.VISIBLE)
                                subtitlesButton.requestFocus();
                            else
                                backwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            seekBar.requestFocus();
                            break;
                    }
                }
                else if (subtitlesButton.isFocused()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            seekBar.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            forwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            backwardButton.requestFocus();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            seekBar.requestFocus();
                            break;
                    }
                }
            }

            playerPanel.showPanel();
            playerPanel.getFocusView().requestFocus();
        }
        return false;
    }
}
