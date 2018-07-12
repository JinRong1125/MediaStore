package com.tti.ttimediastore.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.tti.ttimediastore.R;

import com.tti.ttimediastore.activity.SearchActivty;
import com.tti.ttimediastore.manager.IrisActionManager;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.activity.ItemGridActivity;
import com.tti.ttimediastore.activity.MainActivity;
import com.tti.ttimediastore.activity.UPnPActivity;
import com.tti.ttimediastore.manager.AudioOutputManager;
import com.tti.ttimediastore.manager.AudioTrackUpdater;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.manager.PIPVideoManager;
import com.tti.ttimediastore.model.PlayerPanel;
import com.tti.ttimediastore.adapter.SongListAdapter;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.InfoDialog;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class AudioFragment extends Fragment implements AudioPlayerController.OnPlayerListener, IrisActionManager.IrisCommandListener {

    private View view;
    private FrameLayout panelContainer;
    private TextView titleView, repeatText;
    private RecyclerView listView;
    private ImageView albumView;
    private ImageButton shuffleButton, repeatButton;

    private BackgroundManager backgroundManager;

    private SongListAdapter listAdapter;

    private SimpleExoPlayer player;
    private PlayerPanel playerPanel;
    private ImageButton playpauseButton, forwardButton, backwardButton;
    private SeekBar seekBar;

    private InfoDialog errorDialog;

    private ArrayList<Audio> audioList;
    private Audio audio;

    private int tracksSize;
    private int track;
    private int selectItem;
    private int moveItem;
    private int playMode;

    private ArrayList<Integer> shuffleList;
    private int shuffleTrack;

    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isLoop = false;

    private boolean isButtonPress = false;
    private boolean isLongPress = false;
    private boolean isMoving = false;

    private boolean shouldResume = false;
    private boolean isPause = false;

    private ButtonHandler buttonHandler;
    private MoveHandler moveHandler;
    private FocusHandler focusHandler;

    private AudioTrackUpdater audioTrackUpdater;
    private AudioOutputManager audioOutputManager;

    private static final int NORMAL_MODE = 0;
    private static final int SHUFFLE_MODE = 1;
    private static final int REPEAT_MODE = 2;
    private static final int LOOP_MODE = 3;
    private static final int ALL_MODE = 4;

    private static final int SHORT_CLICK = 0;
    private static final int LONG_PRESS = 1;
    private static final int PRESS_TIMEOUT = 1000;

    private static final int PREVIOUS_TRACK = 0;
    private static final int NEXT_TRACK = 1;
    private static final int VISIBLE_ITEM = 6;

    private static final int ENABLE_MOVE = 0;

    private static final int ENABLE_FOCUS = 0;
    private static final int DELAY_TIMEOUT = 100;

    private static final int BUTTON_SIZE = 80;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        audioList = getActivity().getIntent().getParcelableArrayListExtra(Constants.ITEM_CONTENT);
        tracksSize = audioList.size();
        track = selectTrack(getActivity().getIntent().getStringExtra(Constants.ITEM_ID));
        selectItem = track;

        playMode = Utils.Preferences.getPlayMode();

        buttonHandler = new ButtonHandler();
        moveHandler = new MoveHandler();
        focusHandler = new FocusHandler();

        initView();
        createPlayer();

        audioTrackUpdater = AudioTrackUpdater.getInstance();
        AudioPlayerController.getInstance().setListener(this);
        audioOutputManager = new AudioOutputManager(getActivity(), track);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backgroundManager = BackgroundManager.getInstance(getActivity());
        if (!backgroundManager.isAttached())
            backgroundManager.attach(getActivity().getWindow());
    }

    public void onResume() {
        super.onResume();
        backgroundManager.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wallpaper));
        setResumeFocus();

        if (playerPanel != null) {
            playerPanel.stayShowPanel();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playerPanel.switchPlayPause(player.getPlayWhenReady());
                }
            }, DELAY_TIMEOUT);
        }
        IrisActionManager.getInstance().setListener(this);
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        IrisActionManager.getInstance().setListener(null);
        isPause = true;
    }

    public void onDestroy() {
        super.onDestroy();
        stopAllTask();
        releasePlayer();
        backgroundManager.release();
    }

    @Override
    public void onAudioTrackSet(int track) {
        setAudio(track);
    }

    @Override
    public void onAudioPlayPause() {
        unfocusListItem();
        setPlayPause();
    }

    @Override
    public void onAudioResume() {
        setResume();
    }

    @Override
    public void onAudioPause() {
        setPause();
    }

    @Override
    public void onAudioFinish() {
        setFinish();
    }

    @Override
    public void onAudioVolumeSet(float volume) {
        setVolume(volume);
    }

    @Override
    public void onIrisYes(String message) {
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
            getActivity().finish();
        }
    }

    @Override
    public void onIrisNo(String message) {
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
            getActivity().finish();
        }
    }

    @Override
    public void onIrisPlay(String message) {
        if (player != null && playerPanel != null) {
            setResume();
            playerPanel.stayShowPanel();
        }
    }

    @Override
    public void onIrisPause(String message) {
        if (player != null && playerPanel != null) {
            setPause();
            playerPanel.stayShowPanel();
        }
    }

    @Override
    public void onIrisVolumeUp(String message) {
        if (audioOutputManager != null)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_RAISE);
    }

    @Override
    public void onIrisVolumeDown(String message) {
        if (audioOutputManager != null)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_LOWER);
    }

    @Override
    public void onIrisMute(String message) {
        if (audioOutputManager != null)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_MUTE);
    }

    @Override
    public void onIrisUnMute(String message) {
        if (audioOutputManager != null)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_UNMUTE);
    }

    @Override
    public void onIrisBack(String message) {
        backtoActivity();
    }

    @Override
    public void onIrisClose(String message) {
        backtoActivity();
    }

    @Override
    public void onIrisVideo(String message) {
        backtoActivity();
    }

    @Override
    public void onIrisAudio(String message) {
        backtoActivity();
    }

    @Override
    public void onIrisImage(String message) {
        backtoActivity();
    }

    @Override
    public void onIrisMain(String message) {
        backtoActivity();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.NOTIFICATION_MUTE)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_MUTE);
        else if (requestCode == Constants.NOTIFICATION_UNMUTE)
            audioOutputManager.adjustVolume(AudioManager.ADJUST_UNMUTE);
    }

    private void stopAllTask() {
        AudioPlayerController.getInstance().setListener(null);
        if (buttonHandler != null)
            buttonHandler.removeCallbacksAndMessages(null);
        if (moveHandler != null)
            moveHandler.removeCallbacksAndMessages(null);
        if (focusHandler != null)
            focusHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        panelContainer = (FrameLayout) view.findViewById(R.id.panelContainer);

        titleView = (TextView) view.findViewById(R.id.albumTitle);
        albumView = (ImageView) view.findViewById(R.id.albumView);
        albumView.setBackground(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        listView = (RecyclerView) view.findViewById(R.id.songList);
        listView.setLayoutManager(layoutManager);
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track != selectItem) {
                    track = selectItem;
                    setAudio(track);
                    startAudio();
                }
            }
        });
        listAdapter = new SongListAdapter(audioList);
        listView.setAdapter(listAdapter);

        listView.scrollToPosition(selectItem - 2);
        listView.requestFocus();

        repeatText = (TextView) view.findViewById(R.id.repeatText);
        setPlayMode();
    }

    private void setPlayMode() {
        shuffleButton = (ImageButton) view.findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShuffle();
            }
        });
        shuffleButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean isFocused) {
                if (isFocused)
                    shuffleButton.setBackgroundResource(R.drawable.focus_design);
                else
                    shuffleButton.setBackgroundResource(R.color.transparent);
            }
        });

        repeatButton = (ImageButton) view.findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeat();
            }
        });
        repeatButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean isFocused) {
                if (isFocused)
                    repeatButton.setBackgroundResource(R.drawable.focus_design);
                else
                    repeatButton.setBackgroundResource(R.color.transparent);

            }
        });

        switch (playMode) {
            case NORMAL_MODE:
                setButtonImage(shuffleButton, R.drawable.shuffle_icon);
                setButtonImage(repeatButton, R.drawable.repeat_icon);
                break;
            case SHUFFLE_MODE:
                setButtonImage(shuffleButton, R.drawable.shuffle_check);
                setButtonImage(repeatButton, R.drawable.repeat_icon);
                isShuffle = true;

                shuffleTrack = 0;
                shuffleList = new ArrayList<>();
                for (int i = 0; i < tracksSize; i++)
                    shuffleList.add(i);
                Collections.shuffle(shuffleList);

                break;
            case REPEAT_MODE:
                setButtonImage(shuffleButton, R.drawable.shuffle_icon);
                setButtonImage(repeatButton, R.drawable.repeat_check);
                isRepeat = true;
                break;
            case LOOP_MODE:
                setButtonImage(shuffleButton, R.drawable.shuffle_icon);
                setButtonImage(repeatButton, R.drawable.repeat_check);
                repeatText.setText("1");
                isRepeat = true;
                isLoop = true;
                break;
            case ALL_MODE:
                setButtonImage(shuffleButton, R.drawable.shuffle_check);
                setButtonImage(repeatButton, R.drawable.repeat_check);
                isShuffle = true;
                isRepeat = true;
                break;
        }
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

    private void createPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        player.addListener(new EventListener());
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
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playerPanel != null)
                playerPanel.switchPlayPause(playWhenReady);

            if (audio.getIsPlaying() != playWhenReady){
                audio.setIsPlaying(playWhenReady);
                Utils.Preferences.setResumeAudio(audio);
                audioTrackUpdater.updateTrack(audio);
            }

            if (playWhenReady && Constants.PIP_MODE == Constants.PIP_ON) {
                Constants.PIP_MODE = Constants.PIP_OFF;
                PIPVideoManager.getInstance().finishPIP();
            }

            switch (playbackState) {
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_READY:
                    setPanel();
                    playerPanel.updateMedia(audioList.get(track).getTitle());
                    break;
                case Player.STATE_BUFFERING:
                    break;
                case Player.STATE_ENDED:
                    switch (playMode) {
                        case NORMAL_MODE:
                            if (track != tracksSize - 1) {
                                setNextTrack();
                            }
                            else {
                                unfocusListItem();
                                track = 0;
                                selectItem = 0;
                                setStop();
                                setAudio(track);
                                listView.scrollToPosition(0);
                                setItemFocus();
                                setEnd();
                            }
                            break;
                        case SHUFFLE_MODE:
                            if (shuffleTrack != tracksSize) {
                                unfocusListItem();
                                track = shuffleList.get(shuffleTrack);
                                selectItem = track;
                                shuffleTrack++;
                                setAudio(track);
                                startAudio();
                                listView.scrollToPosition(selectItem);
                                setItemFocus();
                            }
                            else {
                                player.seekTo(0);
                                player.setPlayWhenReady(false);
                                shuffleTrack = 0;
                                setEnd();
                            }
                            break;
                        case REPEAT_MODE:
                            if (track != tracksSize - 1) {
                                setNextTrack();
                            }
                            else {
                                unfocusListItem();
                                track = 0;
                                selectItem = 0;
                                setAudio(track);
                                startAudio();
                                listView.scrollToPosition(0);
                                setItemFocus();
                            }
                            break;
                        case LOOP_MODE:
                            player.seekTo(0);
                            break;
                        case ALL_MODE:
                            unfocusListItem();
                            track = (int) (Math.random() * (tracksSize - 1));
                            setAudio(track);
                            startAudio();

                            if (track != selectItem) {
                                if (track > selectItem) {
                                    moveItem = selectItem;
                                    selectItem = track;
                                    scrollTrackPosition(NEXT_TRACK);
                                }
                                else {
                                    moveItem = selectItem;
                                    selectItem = track;
                                    scrollTrackPosition(PREVIOUS_TRACK);
                                }
                            }

                            setItemFocus();
                            break;
                    }
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
            errorDialog = new InfoDialog(getActivity(), getString(R.string.audio_error));
            errorDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_BACK
                            || keyCode == KeyEvent.KEYCODE_ESCAPE
                            && errorDialog.isShowing()) {
                        errorDialog.dismiss();
                        setFinish();
                    }
                    return false;
                }
            });
            if (!isPause)
                errorDialog.show();
            else {
                Toast.makeText(getActivity(), R.string.audio_error, Toast.LENGTH_SHORT).show();
                setFinish();
                setEnd();
            }
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

    private int selectTrack(String trackId) {
        int track = 0;

        for (int i = 0; i < tracksSize; i++) {
            if (audioList.get(i).getId().equals(trackId)) {
                track = i;
                break;
            }
        }

        return track;
    }

    public void setAudio(int track) {
        audio = audioList.get(track);

        String album = audio.getAlbum();
        if (album != null && !album.trim().isEmpty())
            titleView.setText(album);
        else
            titleView.setText("");

        Utils.setFitCenterImage(getActivity(), albumView, Utils.getAlbumCover(getActivity(), audio.getPath()));

        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(audio.getPath()),
                mediaDataSourceFactory, new DefaultExtractorsFactory(), null, null);

        player.prepare(mediaSource);
        Utils.Preferences.setResumeAudio(audio);
        audioTrackUpdater.updateTrack(audio);
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((TTIMediaStore) getActivity().getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private void startAudio() {
        if (player!= null && playerPanel != null) {
            player.seekTo(0);
            player.setPlayWhenReady(true);
        }
    }

    private void setPanel() {
        if (playerPanel == null) {
            playerPanel = new PlayerPanel(getActivity(), player, audioList.get(track).getTitle());
            panelContainer.addView(playerPanel.getPanelView());
            playerPanel.stayShowPanel();

            playpauseButton = playerPanel.getPlayPauseButton();
            forwardButton = playerPanel.getForwardButton();
            backwardButton = playerPanel.getBackwardButton();
            seekBar = playerPanel.getSeekBar();

            setPanelListener();
            startAudio();
        }
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
                            playerPanel.setCurrentTime(true);
                            break;
                        case KeyEvent.ACTION_UP:
                            player.seekTo(seekBar.getProgress() * 1000);
                            playerPanel.setCurrentTime(false);
                            playerPanel.stayShowPanel();
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
                            if (!isButtonPress) {
                                setButtonAction();
                                isButtonPress = true;
                            }

                            if (isLongPress && player.getCurrentPosition() > 0) {
                                seekBar.setProgress(seekBar.getProgress() - (int) (player.getDuration() / (100 * 1000)));
                                playerPanel.setCurrentTime(true);
                            }
                            break;
                        case KeyEvent.ACTION_UP:
                            if (!isLongPress)
                                setPreviousTrack();
                            else
                                player.seekTo(seekBar.getProgress() * 1000);

                            playerPanel.setCurrentTime(false);
                            playerPanel.stayShowPanel();
                            buttonHandler.removeMessages(LONG_PRESS);
                            isButtonPress = false;
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
                            if (!isButtonPress) {
                                setButtonAction();
                                isButtonPress = true;
                            }

                            long duration = player.getDuration();
                            if (isLongPress && player.getCurrentPosition() < duration) {
                                seekBar.setProgress(seekBar.getProgress() + (int) (duration / (100 * 1000)));
                                playerPanel.setCurrentTime(true);
                            }
                            break;
                        case KeyEvent.ACTION_UP:
                            if (!isLongPress)
                                setNextTrack();
                            else
                                player.seekTo(seekBar.getProgress() * 1000);

                            playerPanel.setCurrentTime(false);
                            playerPanel.stayShowPanel();
                            buttonHandler.removeMessages(LONG_PRESS);
                            isButtonPress = false;
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void setButtonAction() {
        buttonHandler.sendMessage(buttonHandler.obtainMessage(SHORT_CLICK));
        buttonHandler.sendMessageDelayed(buttonHandler.obtainMessage(LONG_PRESS), PRESS_TIMEOUT);
    }

    private class ButtonHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHORT_CLICK:
                    isLongPress = false;
                    break;
                case LONG_PRESS:
                    isLongPress = true;
                    break;
            }
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void setPlayPause() {
        if (player != null && playerPanel != null) {
            if (!player.getPlayWhenReady()) {
                player.setPlayWhenReady(true);
                if (isPause)
                    Toast.makeText(getActivity(), R.string.act_playing, Toast.LENGTH_SHORT).show();
            }
            else {
                player.setPlayWhenReady(false);
                if (isPause)
                    Toast.makeText(getActivity(), R.string.act_pause, Toast.LENGTH_SHORT).show();
            }

            playpauseButton.requestFocus();
        }
    }

    private void setResume() {
        if (player != null) {
            if (shouldResume)
                player.setPlayWhenReady(true);
        }
    }

    private void setPause() {
        if (player != null) {
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
                shouldResume = true;
            }
            else
                shouldResume = false;
        }
    }

    private void setFinish() {
        AudioPlayerController.getInstance().setListener(null);
        audioOutputManager.disableManager();
        Utils.Preferences.setPlayMode(playMode);
        getActivity().finish();
    }

    private void setVolume(float volume) {
        player.setVolume(volume);
    }

    private void setStop() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && playerPanel != null) {
                    player.setPlayWhenReady(false);
                    player.seekTo(0);
                }
            }
        }, DELAY_TIMEOUT);
    }

    private void setEnd() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                audioTrackUpdater.endTrackList();
            }
        }, DELAY_TIMEOUT);
    }

    private void setShuffle() {
        if (!isShuffle) {
            isShuffle = true;
            if (isLoop) {
                isLoop = false;
                repeatText.setText("");
            }
            shuffleTrack = 0;
            shuffleList = new ArrayList<>();
            for (int i = 0; i < tracksSize; i++)
                shuffleList.add(i);
            Collections.shuffle(shuffleList);

            setButtonImage(shuffleButton, R.drawable.shuffle_check);

            Toast.makeText(getActivity(), R.string.shuffle_on, Toast.LENGTH_SHORT).show();
        }
        else {
            isShuffle = false;

            setButtonImage(shuffleButton, R.drawable.shuffle_icon);

            Toast.makeText(getActivity(), R.string.shuffle_off, Toast.LENGTH_SHORT).show();
        }

        checkMode();
    }

    private void setRepeat() {
        if (!isRepeat) {
            isRepeat = true;

            setButtonImage(repeatButton, R.drawable.repeat_check);

            Toast.makeText(getActivity(), R.string.repeat_on, Toast.LENGTH_SHORT).show();
        }
        else if (!isLoop) {
            isLoop = true;
            if (isShuffle) {
                isShuffle = false;
                setButtonImage(shuffleButton, R.drawable.shuffle_icon);
            }
            repeatText.setText("1");

            Toast.makeText(getActivity(), R.string.repeat_loop, Toast.LENGTH_SHORT).show();
        }
        else {
            isRepeat = false;
            isLoop = false;
            repeatText.setText("");

            setButtonImage(repeatButton, R.drawable.repeat_icon);

            Toast.makeText(getActivity(), R.string.repeat_off, Toast.LENGTH_SHORT).show();
        }

        checkMode();
    }

    private void checkMode() {
        if (isLoop)
            playMode = LOOP_MODE;
        else if (!isShuffle && !isRepeat)
            playMode = NORMAL_MODE;
        else if (isShuffle && !isRepeat)
            playMode = SHUFFLE_MODE;
        else if (!isShuffle && isRepeat)
            playMode = REPEAT_MODE;
        else
            playMode = ALL_MODE;
    }

    private void setPreviousTrack() {
        if (track > 0) {
            moveItem = selectItem;
            unfocusListItem();
            track--;
            setAudio(track);
            startAudio();
            selectItem = track;
            scrollTrackPosition(PREVIOUS_TRACK);
            setItemFocus();
        }
    }

    private void setNextTrack() {
        if (track < tracksSize - 1) {
            moveItem = selectItem;
            unfocusListItem();
            track++;
            setAudio(track);
            startAudio();
            selectItem = track;
            scrollTrackPosition(NEXT_TRACK);
            setItemFocus();
        }
    }

    private void scrollTrackPosition(int direction) {
        if (tracksSize > VISIBLE_ITEM) {
            if (selectItem > tracksSize - 5)
                listView.scrollToPosition(tracksSize - 1);
            else if (selectItem < 4)
                listView.scrollToPosition(0);
            else {
                switch (direction) {
                    case PREVIOUS_TRACK:
                        if (moveItem < selectItem)
                            listView.scrollToPosition(selectItem + 3);
                        else
                            listView.scrollToPosition(selectItem - 2);
                        break;
                    case NEXT_TRACK:
                        if (moveItem > selectItem)
                            listView.scrollToPosition(selectItem - 3);
                        else
                            listView.scrollToPosition(selectItem + 2);
                        break;
                }
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
    
    private void focusListItem() {
        listAdapter.notifyItemChanged(selectItem, Constants.FOCUS_ITEM);
    }
    
    private void unfocusListItem() {
        listAdapter.notifyItemChanged(selectItem, Constants.UNFOCUS_ITEM);
    }

    private void setResumeFocus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (listView.isFocused())
                    focusListItem();
                for (int i = 0; i < tracksSize; i++) {
                    if (i != selectItem)
                        listAdapter.notifyItemChanged(i, Constants.UNFOCUS_ITEM);
                }
            }
        }, DELAY_TIMEOUT);
    }

    private void setItemFocus() {
        focusHandler.sendMessageDelayed(focusHandler.obtainMessage(ENABLE_FOCUS), DELAY_TIMEOUT);
        listView.requestFocus();
    }

    private class FocusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_FOCUS:
                    focusListItem();
                    break;
            }
        }
    }

    private void backtoActivity() {
        String ModelType = Constants.MODEL_TYPE;
        Intent intent;

        if (ModelType.equals(Constants.AUDIO_RESUME))
            intent = new Intent(getActivity(), MainActivity.class);
        else if (ModelType.equals(Constants.AUDIO_SEARCH))
            intent = new Intent(getActivity(), SearchActivty.class);
        else if (ModelType.equals(Constants.INPUT_AUDIO))
            intent = new Intent(getActivity(), UPnPActivity.class);
        else
            intent = new Intent(getActivity(), ItemGridActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        getActivity().startActivity(intent);

        if (playerPanel == null)
            setFinish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isMoving)
            return false;
        setMoving();

        if (keyCode == KeyEvent.KEYCODE_ESCAPE
                || keyCode == KeyEvent.KEYCODE_BACK) {
            backtoActivity();
        }
        else if (player != null && playerPanel != null) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                unfocusListItem();
                setPlayPause();
            }
            else if (listView.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (selectItem > 0) {
                            unfocusListItem();
                            selectItem--;
                            listView.scrollToPosition(selectItem - 2);
                            focusListItem();
                        }
                        else {
                            unfocusListItem();
                            playpauseButton.requestFocus();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (selectItem < tracksSize - 1) {
                            unfocusListItem();
                            selectItem++;
                            listView.scrollToPosition(selectItem + 2);
                            focusListItem();
                        }
                        else {
                            unfocusListItem();
                            playpauseButton.requestFocus();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        unfocusListItem();
                        playpauseButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        unfocusListItem();
                        playpauseButton.requestFocus();
                        break;
                }
            }
            else if (shuffleButton.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        repeatButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        setItemFocus();
                        break;
                }
            }
            else if (repeatButton.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        shuffleButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        seekBar.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        setItemFocus();
                        break;
                }
            }
            else if (seekBar.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        repeatButton.requestFocus();
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
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        backwardButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        forwardButton.requestFocus();
                        break;
                }
            }
            else if (backwardButton.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        seekBar.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        playpauseButton.requestFocus();
                        break;
                }
            }
            else if (forwardButton.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        seekBar.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        setItemFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        playpauseButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        setItemFocus();
                        break;
                }
            }
        }
        return false;
    }
}
