package com.tti.ttimediastore.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.presenter.CardPresenter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/5/3.
 */

public class ItemSearchFragment extends SearchFragment implements SearchFragment.SearchResultProvider {

    private BackgroundManager backgroundManager;
    private SpinnerFragment spinnerFragment;
    private NoneFragment noneFragment;

    private ArrayObjectAdapter rowsAdapter;
    private ArrayObjectAdapter videoRowAdapter;
    private ArrayObjectAdapter audioRowAdapter;
    private ArrayObjectAdapter imageRowAdapter;
    private ListRow videoRow, audioRow, imageRow;

    private Uri queryUri;
    private AsyncTask queryTask;
    private AdapterHandler adapterHandler;

    private ArrayList<Audio> audioList;
    private ArrayList<Image> imageList;

    private boolean isVideoRowAdded = false;
    private boolean isAudioRowadded = false;
    private boolean isImageRowAdded = false;
    private boolean shouldResumeTask = false;
    private String searchText;

    private AudioPlayerController audioPlayerController;

    private static final int REQUEST_SPEECH = 0;
    private static final int START_ADD = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFragment();
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
        backgroundManager.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wallpaper));
        if (shouldResumeTask) {
            clearResult();
            queryTask = new queryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchText);
        }
        shouldResumeTask = false;
    }

    public void onPause() {
        super.onPause();
        if (adapterHandler != null)
            adapterHandler.removeCallbacksAndMessages(null);
        if (queryTask != null) {
            if (queryTask.getStatus() == AsyncTask.Status.RUNNING)
                shouldResumeTask = true;
            queryTask.cancel(true);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        backgroundManager.release();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            setSpeechRecognition();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK) {
            String speechText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            if (!speechText.trim().equals(""))
                setSearchQuery(speechText, false);
        }
    }

    private void setupFragment() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED)
            setSpeechRecognition();

        spinnerFragment = new SpinnerFragment();
        getActivity().getFragmentManager().beginTransaction()
                .add(R.id.search_fragment, spinnerFragment).commit();
        hideProgress();

        noneFragment = new NoneFragment();
        getActivity().getFragmentManager().beginTransaction()
                .add(R.id.search_fragment, noneFragment).commit();
        hideNone();

        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        videoRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        audioRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        imageRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        videoRow = new ListRow(new HeaderItem(getString(R.string.catalog_video)), videoRowAdapter);
        audioRow = new ListRow(new HeaderItem(getString(R.string.catalog_audio)), audioRowAdapter);
        imageRow = new ListRow(new HeaderItem(getString(R.string.catalog_image)), imageRowAdapter);
        setSearchResultProvider(this);


        queryUri = MediaStore.Files.getContentUri("external");
        audioList = new ArrayList<>();
        imageList = new ArrayList<>();

        adapterHandler = new AdapterHandler();
        audioPlayerController = AudioPlayerController.getInstance();
    }

    private void setSpeechRecognition() {
        setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
            @Override
            public void recognizeSpeech() {
                try {
                    startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.speech_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    @Override
    public ObjectAdapter getResultsAdapter() {
        return rowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        if (adapterHandler != null)
            adapterHandler.removeCallbacksAndMessages(null);
        if (queryTask != null)
            queryTask.cancel(true);
        clearResult();
        if (!newQuery.trim().equals("")) {
            queryTask = new queryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, newQuery);
            searchText = newQuery;
        }
        return true;
    }

    private void clearResult() {
        rowsAdapter.clear();
        isVideoRowAdded = false;
        isAudioRowadded = false;
        isImageRowAdded = false;

        videoRowAdapter.clear();
        audioRowAdapter.clear();
        imageRowAdapter.clear();
        audioList.clear();
        imageList.clear();

        hideProgress();
        hideNone();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private class queryTask extends AsyncTask<String, Void, Void> {
        String queryText;

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Void doInBackground(String... params) {
            queryText = params[0];
            queryMedia(queryText);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (rowsAdapter.size() == 0)
                showNone();
            hideProgress();
        }
    }

    private Cursor getCursor(Uri uri, String selection) {
        return getActivity().getContentResolver().query(uri, null, selection, null, null);
    }

    private String getCursorData(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    private void queryMedia(String queryText) {
        String selection = MediaStore.Files.FileColumns.TITLE + " LIKE ?";
        Cursor cursor = getActivity().getContentResolver().query(
                queryUri, null, selection, new String[]{ "%" + queryText + "%" }, null);

        if (cursor == null)
            return;
        if (!cursor.moveToFirst())
            return;
        do {
            String data = getCursorData(cursor, MediaStore.Files.FileColumns.DATA);
            if (data == null)
                return;

            String id = getCursorData(cursor, MediaStore.Files.FileColumns._ID);
            String title = getCursorData(cursor, MediaStore.Files.FileColumns.TITLE);
            String path = Uri.fromFile(new File(data)).toString();

            switch (cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    Video video = new Video();
                    video.setId(id);
                    video.setTitle(title);
                    video.setPath(path);
                    video.setThumbnail(getThumbnail(Long.parseLong(id)));

                    addMessageObject(video);
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                    Audio audio = new Audio();
                    audio.setId(id);
                    audio.setTitle(title);
                    audio.setPath(path);

                    Cursor audioCursor = getCursor(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Media._ID + "='" + id + "'");
                    if (audioCursor != null && audioCursor.moveToFirst()) {
                        audio.setArtist(getCursorData(audioCursor, MediaStore.Audio.Media.ARTIST));
                        audio.setAlbum(getCursorData(audioCursor, MediaStore.Audio.Media.ALBUM));
                        audio.setCover(getAlbumArt(getCursorData(audioCursor, MediaStore.Audio.Media.ALBUM_ID)));
                        audioCursor.close();
                    }

                    addMessageObject(audio);
                    audioList.add(audio);
                    break;
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    Image image = new Image();
                    image.setId(id);
                    image.setTitle(title);
                    image.setPath(path);

                    addMessageObject(image);
                    imageList.add(image);
                    break;
            }
        } while (cursor.moveToNext());
        cursor.close();
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

    private class AdapterHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_ADD) {
                if (msg.obj instanceof Video) {
                    videoRowAdapter.add(msg.obj);
                    if (!isVideoRowAdded) {
                        isVideoRowAdded = true;
                        rowsAdapter.add(videoRow);
                    }
                }
                else if (msg.obj instanceof Audio) {
                    audioRowAdapter.add(msg.obj);
                    if (!isAudioRowadded) {
                        isAudioRowadded = true;
                        rowsAdapter.add(audioRow);
                    }
                }
                else if (msg.obj instanceof Image) {
                    imageRowAdapter.add(msg.obj);
                    if (!isImageRowAdded) {
                        isImageRowAdded = true;
                        rowsAdapter.add(imageRow);
                    }
                }
            }
        }
    }

    private void addMessageObject(Object object) {
        adapterHandler.sendMessage(adapterHandler.obtainMessage(START_ADD, object));
    }

    private void setEventListener() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Video) {
                    Video video = (Video) item;
                    Constants.MODEL_TYPE = Constants.VIDEO_ALL;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, video);
                    getActivity().startActivity(intent);
                } else if (item instanceof Audio) {
                    Audio audio = (Audio) item;
                    audioPlayerController.finish();
                    Constants.MODEL_TYPE = Constants.AUDIO_SEARCH;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, audioList);
                    intent.putExtra(Constants.ITEM_ID, audio.getId());
                    getActivity().startActivity(intent);
                } else if (item instanceof Image) {
                    Image image = (Image) item;
                    Constants.MODEL_TYPE = Constants.IMAGE_ALL;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.putExtra(Constants.ITEM_CONTENT, imageList);
                    intent.putExtra(Constants.ITEM_ID, image.getId());
                    getActivity().startActivity(intent);
                }
            }
        });
    }
}
