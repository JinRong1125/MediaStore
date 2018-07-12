package com.tti.ttimediastore.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.activity.ItemGridActivity;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.manager.IrisActionManager;
import com.tti.ttimediastore.manager.MediaObserver;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.AudioAlbum;
import com.tti.ttimediastore.model.AudioArtist;
import com.tti.ttimediastore.model.AudioGenre;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.presenter.CardPresenter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class ItemGridFragment extends VerticalGridFragment implements IrisActionManager.IrisCommandListener {

    private VerticalGridPresenter gridPresenter;
    private ArrayObjectAdapter itemAdapter;

    private BackgroundManager backgroundManager;
    private SpinnerFragment spinnerFragment;
    private NoneFragment noneFragment;

    private QueryHandler queryHandler;
    private AdapterHandler adapterHandler;
    private AsyncTask queryTask;
    private MediaObserver mediaObserver;

    private String modelType;
    private Uri uri;
    private Uri observerUri;

    private boolean isPause = false;
    private boolean shouldResumeTask = false;
    private boolean isChangedinPause = false;
    private boolean isAudioSub = false;

    private static final int START_QUERY = 0;
    private static final int START_ADD = 1;

    private static final int NUM_COLUMNS = 5;
    private static final String MEDIA_EXTERNAL = "external";

    private ArrayList<Video> videoList;
    private ArrayList<Audio> audioList;
    private ArrayList<Image> imageList;

    private AudioPlayerController audioPlayerController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.IRIS_EVENT = Constants.EVENT_NONE;
        setupFragment();
        setItem();
        setEventListener();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backgroundManager = BackgroundManager.getInstance(getActivity());
        if (!backgroundManager.isAttached())
            backgroundManager.attach(getActivity().getWindow());
    }

    public void onResume() {
        super.onResume();
        if (Constants.IRIS_EVENT == Constants.EVENT_CLOSE) {
            ((ItemGridActivity) getActivity()).backtoMainActivity();
            getActivity().moveTaskToBack(true);
            return;
        }
        else if (Constants.IRIS_EVENT == Constants.EVENT_VIDEO ||
                Constants.IRIS_EVENT == Constants.EVENT_AUDIO ||
                Constants.IRIS_EVENT == Constants.EVENT_IMAGE ||
                Constants.IRIS_EVENT == Constants.EVENT_MAIN) {
            ((ItemGridActivity) getActivity()).backtoMainActivity();
            return;
        }

        backgroundManager.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wallpaper));

        if (shouldResumeTask || isChangedinPause) {
            if (queryHandler != null) {
                queryHandler.removeCallbacksAndMessages(null);
                queryHandler.sendMessage(queryHandler.obtainMessage(START_QUERY));
            }
        }
        IrisActionManager.getInstance().setListener(this);

        shouldResumeTask = false;
        isChangedinPause = false;
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        stopAllTask();
        hideProgress();
        IrisActionManager.getInstance().setListener(null);
        isPause = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mediaObserver.unregisterMediaObserver(getActivity());
    }

    @Override
    public void onIrisYes(String message) {

    }

    @Override
    public void onIrisNo(String message) {

    }

    @Override
    public void onIrisPlay(String message) {
        if (isPause)
            return;
        message = message.replace("play", "").replace("start", "").trim();
        if (modelType.equals(Constants.VIDEO_ALL)) {
            for (Video video : videoList) {
                if (video.getTitle().toLowerCase().contains(message)) {
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, video);
                    getActivity().startActivity(intent);
                    return;
                }
            }
        }
        else if (modelType.equals(Constants.AUDIO_ALL)) {
            for (Audio audio : audioList) {
                if (audio.getTitle().toLowerCase().contains(message)) {
                    audioPlayerController.finish();
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, audioList);
                    intent.putExtra(Constants.ITEM_ID, audio.getId());
                    getActivity().startActivity(intent);
                    return;
                }
            }
        }
        else if (modelType.equals(Constants.IMAGE_ALL)) {
            for (Image image : imageList) {
                if (image.getTitle().toLowerCase().contains(message)) {
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, imageList);
                    intent.putExtra(Constants.ITEM_ID, image.getId());
                    getActivity().startActivity(intent);
                    return;
                }
            }
        }
        Toast.makeText(getActivity(), "\"" + message + "\" " +
                getString(R.string.iris_not_found), Toast.LENGTH_SHORT).show();
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
        getActivity().onBackPressed();
    }

    @Override
    public void onIrisClose(String message) {
        ((ItemGridActivity) getActivity()).backtoMainActivity();
        getActivity().moveTaskToBack(true);
    }

    @Override
    public void onIrisVideo(String message) {
        ((ItemGridActivity) getActivity()).backtoMainActivity();
    }

    @Override
    public void onIrisAudio(String message) {
        ((ItemGridActivity) getActivity()).backtoMainActivity();
    }

    @Override
    public void onIrisImage(String message) {
        ((ItemGridActivity) getActivity()).backtoMainActivity();
    }

    @Override
    public void onIrisMain(String message) {
        ((ItemGridActivity) getActivity()).backtoMainActivity();
    }

    private void stopAllTask() {
        if (queryHandler != null)
            queryHandler.removeCallbacksAndMessages(null);
        if (queryTask != null) {
            if (queryTask.getStatus() == AsyncTask.Status.RUNNING)
                shouldResumeTask = true;
            queryTask.cancel(true);
        }
        if (adapterHandler != null)
            adapterHandler.removeCallbacksAndMessages(null);
    }

    private void setupFragment() {
        gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);
        itemAdapter = new ArrayObjectAdapter(new CardPresenter());
        setAdapter(itemAdapter);

        audioPlayerController = AudioPlayerController.getInstance();
    }

    private void setItem() {
        modelType = Constants.MODEL_TYPE;

        if (modelType.equals(Constants.VIDEO_ALL))
            setTitle(getString(R.string.video_all));
        else if (modelType.equals(Constants.AUDIO_ARTISTS))
            setTitle(getString(R.string.audio_artists));
        else if (modelType.equals(Constants.AUDIO_ALBUMS))
            setTitle(getString(R.string.audio_albums));
        else if (modelType.equals(Constants.AUDIO_GENRES))
            setTitle(getString(R.string.audio_genres));
        else if (modelType.equals(Constants.AUDIO_ALL))
            setTitle(getString(R.string.audio_all));
        else if (modelType.equals(Constants.IMAGE_ALL))
            setTitle(getString(R.string.image_all));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            queryHandler = new QueryHandler();
            adapterHandler = new AdapterHandler();
            mediaObserver = new MediaObserver(null);

            spinnerFragment = new SpinnerFragment();
            getActivity().getFragmentManager().beginTransaction()
                    .add(R.id.item_grid_fragment, spinnerFragment).commit();
            hideProgress();

            noneFragment = new NoneFragment();
            getActivity().getFragmentManager().beginTransaction()
                    .add(R.id.item_grid_fragment, noneFragment).commit();
            hideNone();

            if (modelType.equals(Constants.VIDEO_ALL)) {
                videoList = new ArrayList<>();
                observerUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }
            else if (modelType.equals(Constants.IMAGE_ALL)) {
                imageList = new ArrayList<>();
                observerUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            else {
                audioList = new ArrayList<>();
                observerUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            queryHandler.sendMessage(queryHandler.obtainMessage(START_QUERY));
            mediaObserver.registerMediaObserver(getActivity(), observerUri, queryHandler);
        }
    }

    private void addMessageObject(Object object) {
        adapterHandler.sendMessage(adapterHandler.obtainMessage(START_ADD, object));
    }

    private void setStartQuery() {
        itemAdapter.clear();
        showProgress();
        hideNone();
    }

    private void setEndQuery() {
        if (itemAdapter.size() == 0)
            showNone();
        itemAdapter.notifyItemRangeChanged(0, itemAdapter.size() - 1);
        setAdapter(itemAdapter);
        hideProgress();
    }

    private void showProgress() {
        getActivity().getFragmentManager().beginTransaction().show(spinnerFragment).commit();
    }

    private void hideProgress() {
        getActivity().getFragmentManager().beginTransaction().hide(spinnerFragment).commit();
    }

    private void showNone() {
        getActivity().getFragmentManager().beginTransaction().show(noneFragment).commit();
    }

    private void hideNone() {
        getActivity().getFragmentManager().beginTransaction().hide(noneFragment).commit();
    }

    public boolean shouldFinish() {
        if (!isAudioSub)
            return true;

        queryHandler.sendMessage(queryHandler.obtainMessage(START_QUERY));
        isAudioSub = false;
        return false;
    }

    private class QueryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_QUERY) {
                if (!isPause) {
                    if (queryTask != null)
                        queryTask.cancel(true);
                    if (adapterHandler != null)
                        adapterHandler.removeCallbacksAndMessages(null);

                    if (modelType.equals(Constants.VIDEO_ALL)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        queryTask = new setVideo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (modelType.equals(Constants.AUDIO_ARTISTS)) {
                        uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
                        setTitle(getString(R.string.audio_artists));
                        queryTask = new setAudioArtist().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (modelType.equals(Constants.AUDIO_ALBUMS)) {
                        uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                        queryTask = new setAudioAlbum().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (modelType.equals(Constants.AUDIO_GENRES)) {
                        uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
                        setTitle(getString(R.string.audio_genres));
                        queryTask = new setAudioGenre().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (modelType.equals(Constants.AUDIO_ALL)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        queryTask = new setAudio().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else if (modelType.equals(Constants.IMAGE_ALL)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        queryTask = new setImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
                else
                    isChangedinPause = true;
            }
        }
    }

    private class AdapterHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_ADD) {
                itemAdapter.add(msg.obj);
            }
        }
    }

    private Cursor getCursor(Uri uri) {
        return getActivity().getContentResolver().query(uri, null, null, null, null);
    }

    private Cursor getCursor(Uri uri, String selection) {
        return getActivity().getContentResolver().query(uri, null, selection, null, null);
    }

    private String getCursorData(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    private class setVideo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
            videoList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = getCursor(uri);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Video video = new Video();

                        String data = getCursorData(cursor, MediaStore.Video.Media.DATA);
                        if (data != null) {
                            video.setId(getCursorData(cursor, MediaStore.Video.Media._ID));
                            video.setTitle(getCursorData(cursor, MediaStore.Video.Media.TITLE));
                            video.setPath(Uri.fromFile(new File(data)).toString());
                            video.setThumbnail(getThumbnail(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))));
                            addMessageObject(video);
                            videoList.add(video);

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private String getThumbnail(long videoId) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MediaStore.Video.Thumbnails.getThumbnail(contentResolver, videoId, MediaStore.Video.Thumbnails.MINI_KIND, null);
        Cursor cursor = getCursor(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Thumbnails.VIDEO_ID + "='" + videoId + "'");
        if (cursor == null)
            return null;
        if (cursor.moveToFirst()) {
            String thumbnail = getCursorData(cursor, MediaStore.Video.Thumbnails.DATA);
            cursor.close();
            return thumbnail;
        }
        else {
            cursor.close();
            return null;
        }
    }

    private class setAudioArtist extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = getCursor(uri);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        AudioArtist audioArtist = new AudioArtist();
                        audioArtist.setId(getCursorData(cursor, MediaStore.Audio.Artists._ID));
                        audioArtist.setArtist(getCursorData(cursor, MediaStore.Audio.Artists.ARTIST));
                        audioArtist.setCover(getAlbumArt(getCursorData(cursor, MediaStore.Audio.Artists._ID)));
                        addMessageObject(audioArtist);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            setEndQuery();
        }
    }

    private class queryAudioArtist extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
            audioList.clear();
        }

        @Override
        protected Void doInBackground(String... params) {
            String artist = params[0];
            Cursor cursor = getCursor(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Audio.Media.ARTIST + "='" + artist + "'");

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Audio audio = new Audio();

                        String data = getCursorData(cursor, MediaStore.Audio.Media.DATA);
                        if (data != null) {
                            audio.setId(getCursorData(cursor, MediaStore.Audio.Media._ID));
                            audio.setTitle(getCursorData(cursor, MediaStore.Audio.Media.TITLE));
                            audio.setArtist(getCursorData(cursor, MediaStore.Audio.Media.ARTIST));
                            audio.setAlbum(getCursorData(cursor, MediaStore.Audio.Media.ALBUM));
                            audio.setCover(getAlbumArt(getCursorData(cursor, MediaStore.Audio.Media.ALBUM_ID)));
                            audio.setPath(Uri.fromFile(new File(data)).toString());
                            addMessageObject(audio);
                            audioList.add(audio);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private class setAudioAlbum extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                setStartQuery();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = getCursor(uri);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            AudioAlbum audioAlbum = new AudioAlbum();
                            audioAlbum.setId(getCursorData(cursor, MediaStore.Audio.Albums._ID));
                            audioAlbum.setAlbum(getCursorData(cursor, MediaStore.Audio.Albums.ALBUM));
                            audioAlbum.setCover(getCursorData(cursor, MediaStore.Audio.Albums.ALBUM_ART));
                            addMessageObject(audioAlbum);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setEndQuery();
            }
        }

    private ArrayList<Audio> queryAudioAlbum(String album) {
        audioList.clear();

        Cursor cursor = getCursor(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.ALBUM + "='" + album + "'");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String data = getCursorData(cursor, MediaStore.Audio.Media.DATA);
                    if (data != null) {
                        Audio audio = new Audio();
                        audio.setId(getCursorData(cursor, MediaStore.Audio.Media._ID));
                        audio.setTitle(getCursorData(cursor, MediaStore.Audio.Media.TITLE));
                        audio.setArtist(getCursorData(cursor, MediaStore.Audio.Media.ARTIST));
                        audio.setAlbum(getCursorData(cursor, MediaStore.Audio.Media.ALBUM));
                        audio.setCover(getCursorData(cursor, MediaStore.Audio.Media.ALBUM_ID));
                        audio.setPath(Uri.fromFile(new File(data)).toString());
                        audioList.add(audio);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return audioList;
    }

    private class setAudioGenre extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = getCursor(uri);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        AudioGenre audioGenre = new AudioGenre();
                        audioGenre.setId(getCursorData(cursor, MediaStore.Audio.Genres._ID));
                        audioGenre.setGenre(getCursorData(cursor, MediaStore.Audio.Genres.NAME));
                        audioGenre.setCover(getCoverByGenre(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID))));
                        addMessageObject(audioGenre);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private String getCoverByGenre(long id) {
        Cursor cursor = getCursor(MediaStore.Audio.Genres.Members.getContentUri(MEDIA_EXTERNAL, id));
        if (cursor == null)
            return null;
        if (cursor.moveToFirst()) {
            String albumId = getCursorData(cursor, MediaStore.Audio.Media.ALBUM_ID);
            cursor.close();
            return getAlbumArt(albumId);
        }
        else {
            cursor.close();
            return null;
        }
    }

    private class queryAudioGenre extends AsyncTask<Long, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
            audioList.clear();
        }

        @Override
        protected Void doInBackground(Long... params) {
            long id = params[0];
            Cursor cursor = getCursor(MediaStore.Audio.Genres.Members.getContentUri(MEDIA_EXTERNAL, id));

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Audio audio = new Audio();

                        String data = getCursorData(cursor, MediaStore.Audio.Media.DATA);
                        if (data != null) {
                            audio.setId(getCursorData(cursor, MediaStore.Audio.Media._ID));
                            audio.setTitle(getCursorData(cursor, MediaStore.Audio.Media.TITLE));
                            audio.setArtist(getCursorData(cursor, MediaStore.Audio.Media.ARTIST));
                            audio.setAlbum(getCursorData(cursor, MediaStore.Audio.Media.ALBUM));
                            audio.setCover(getAlbumArt(getCursorData(cursor, MediaStore.Audio.Media.ALBUM_ID)));
                            audio.setPath(Uri.fromFile(new File(data)).toString());
                            addMessageObject(audio);
                            audioList.add(audio);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private class setAudio extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
            audioList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = getCursor(uri);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Audio audio = new Audio();

                        String data = getCursorData(cursor, MediaStore.Audio.Media.DATA);
                        if (data != null) {
                            audio.setId(getCursorData(cursor, MediaStore.Audio.Media._ID));
                            audio.setTitle(getCursorData(cursor, MediaStore.Audio.Media.TITLE));
                            audio.setArtist(getCursorData(cursor, MediaStore.Audio.Media.ARTIST));
                            audio.setAlbum(getCursorData(cursor, MediaStore.Audio.Media.ALBUM));
                            audio.setCover(getAlbumArt(getCursorData(cursor, MediaStore.Audio.Media.ALBUM_ID)));
                            audio.setPath(Uri.fromFile(new File(data)).toString());
                            addMessageObject(audio);
                            audioList.add(audio);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private String getAlbumArt(String id) {
        Cursor cursor = getCursor(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Albums._ID + "='" + id + "'");
        if (cursor == null)
            return null;
        if (cursor.moveToFirst()) {
            String albumArt = getCursorData(cursor, MediaStore.Audio.Albums.ALBUM_ART);
            cursor.close();
            return albumArt;
        }
        else {
            cursor.close();
            return null;
        }
    }

    private class setImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            setStartQuery();
            imageList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = getCursor(uri);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Image image = new Image();

                        String data = getCursorData(cursor, MediaStore.Images.Media.DATA);
                        if (data != null) {
                            image.setId(getCursorData(cursor, MediaStore.Images.Media._ID));
                            image.setTitle(getCursorData(cursor, MediaStore.Images.Media.TITLE));
                            image.setPath(Uri.fromFile(new File(data)).toString());
                            addMessageObject(image);
                            imageList.add(image);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setEndQuery();
        }
    }

    private void setEventListener() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Video) {
                    Video video = (Video) item;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, video);
                    getActivity().startActivity(intent);
                } else if (item instanceof AudioArtist) {
                    AudioArtist audioArtist = (AudioArtist) item;
                    String artist = audioArtist.getArtist();
                    queryTask = new queryAudioArtist().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, artist);
                    setTitle(artist);
                    isAudioSub = true;
                } else if (item instanceof AudioAlbum) {
                    AudioAlbum audioAlbum = (AudioAlbum) item;
                    if (queryAudioAlbum(audioAlbum.getAlbum()).size() > 0) {
                        audioPlayerController.finish();
                        Intent intent = new Intent(getActivity(), ContentActivity.class);
                        intent.putExtra(Constants.ITEM_CONTENT, audioList);
                        intent.putExtra(Constants.ITEM_ID, audioList.get(0).getId());
                        getActivity().startActivity(intent);
                    }
                } else if (item instanceof AudioGenre) {
                    AudioGenre audioGenre = (AudioGenre) item;
                    queryTask = new queryAudioGenre().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, Long.parseLong(audioGenre.getId()));
                    setTitle(audioGenre.getGenre());
                    isAudioSub = true;
                } else if (item instanceof Audio) {
                    Audio audio = (Audio) item;
                    audioPlayerController.finish();
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, audioList);
                    intent.putExtra(Constants.ITEM_ID, audio.getId());
                    getActivity().startActivity(intent);
                } else if (item instanceof Image) {
                    Image image = (Image) item;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, imageList);
                    intent.putExtra(Constants.ITEM_ID, image.getId());
                    getActivity().startActivity(intent);
                }
            }
        });
    }
}
