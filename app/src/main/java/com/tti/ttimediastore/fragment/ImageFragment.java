package com.tti.ttimediastore.fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.tti.ttimediastore.R;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.manager.IrisActionManager;
import com.tti.ttimediastore.model.Image;

import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class ImageFragment extends Fragment implements IrisActionManager.IrisCommandListener {

    private BackgroundManager backgroundManager;

    private View view;
    private TextView nameView, noneView;
    private ImageView imageView;
    private ProgressBar progressBar;

    private ArrayList<Image> imageList;
    private int track;

    private String ModelType;
    private String imagePath;

    private Handler titleHandler;
    private Handler progressHandler;

    private AudioPlayerController audioPlayerController;

    private static final int TITLE_HIDE = 0;
    private static final int TITLE_TIMEOUT = 5000;

    private static final int PROGRESS_SHOW = 0;
    private static final int PROGRESS_HIDE = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        imageList = getActivity().getIntent().getParcelableArrayListExtra(Constants.ITEM_CONTENT);
        track = selectTrack(getActivity().getIntent().getStringExtra(Constants.ITEM_ID));
        ModelType = Constants.MODEL_TYPE;

        titleHandler = new TitleHandler();
        progressHandler = new ProgressHandler();

        initView();

        audioPlayerController = AudioPlayerController.getInstance();
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
        IrisActionManager.getInstance().setListener(this);
    }

    public void onPause() {
        super.onPause();
        IrisActionManager.getInstance().setListener(null);
    }

    public void onDestroy() {
        super.onDestroy();
        stopAllTask();
        backgroundManager.release();
    }

    private void stopAllTask() {
        if (titleHandler != null)
            titleHandler.removeCallbacksAndMessages(null);
        if (progressHandler != null)
            progressHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        noneView = (TextView) view.findViewById(R.id.noneView);

        nameView = (TextView) view.findViewById(R.id.nameView);
        setName();

        imageView = (ImageView) view.findViewById(R.id.imageView);
        setImage();
    }

    private void setName() {
        nameView.setText(imageList.get(track).getTitle());
    }

    @Override
    public void onIrisYes(String message) {

    }

    @Override
    public void onIrisNo(String message) {

    }

    @Override
    public void onIrisPlay(String message) {

    }

    @Override
    public void onIrisPause(String message) {

    }

    @Override
    public void onIrisVolumeUp(String message) {

    }

    @Override
    public void onIrisVolumeDown(String message) {

    }

    @Override
    public void onIrisMute(String message) {

    }

    @Override
    public void onIrisUnMute(String message) {

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

    private class TitleHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TITLE_HIDE:
                    hideTitle();
                    break;
            }
        }
    }

    private void showTitle() {
        titleHandler.removeMessages(TITLE_HIDE);
        titleHandler.sendMessageDelayed(titleHandler.obtainMessage(TITLE_HIDE), TITLE_TIMEOUT);

        nameView.setVisibility(View.VISIBLE);
    }

    private void stayShowTitle() {
        titleHandler.removeMessages(TITLE_HIDE);
        nameView.setVisibility(View.VISIBLE);
    }

    private void hideTitle() {
        nameView.setVisibility(View.GONE);
    }

    private class ProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_SHOW:
                    showProgress();
                    break;
                case PROGRESS_HIDE:
                    hideProgress();
                    break;
            }
        }
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void setImage() {
        imagePath = imageList.get(track).getPath();
        progressHandler.sendEmptyMessage(PROGRESS_SHOW);

        setGlideImage();
        showTitle();
/*
        if (ModelType.equals(Constants.IMAGE_ALL)) {
            if (Drawable.createFromPath(imagePath) != null) {
                setGlideImage();
                noneView.setVisibility(View.GONE);
                showTitle();
            }
            else {
                imageView.setImageDrawable(null);
                progressHandler.sendEmptyMessage(PROGRESS_HIDE);
                noneView.setVisibility(View.VISIBLE);
                stayShowTitle();
            }
        }
        else if (ModelType.equals(Constants.INPUT_IMAGE)) {
            setGlideImage();
            noneView.setVisibility(View.GONE);
            showTitle();
        }
*/
    }

    private int selectTrack(String trackId) {
        int track = 0;

        for (int i = 0; i < imageList.size(); i++) {
            if (imageList.get(i).getId().equals(trackId)) {
                track = i;
                break;
            }
        }

        return track;
    }

    private void setNextImage() {
        if (track < imageList.size() - 1) {
            track++;
            setName();
            setImage();
        }
    }

    private void setPreviousImage() {
        if (track > 0) {
            track--;
            setName();
            setImage();
        }
    }

    private void setGlideImage() {
        Glide.with(this)
                .load(imagePath)
                .apply(new RequestOptions().fitCenter().error(R.drawable.tti_logo))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressHandler.sendEmptyMessage(PROGRESS_HIDE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressHandler.sendEmptyMessage(PROGRESS_HIDE);
                        return false;
                    }
                })
                .into(imageView);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    setPreviousImage();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    setNextImage();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    setPreviousImage();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    setNextImage();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    audioPlayerController.playpause();
                    break;
                case KeyEvent.KEYCODE_ESCAPE:
                    getActivity().finish();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    getActivity().finish();
                    break;
            }
        }
        return false;
    }
}
