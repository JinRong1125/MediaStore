package com.tti.ttimediastore.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.activity.ItemGridActivity;
import com.tti.ttimediastore.R;
import com.tti.ttimediastore.activity.SearchActivty;
import com.tti.ttimediastore.manager.IrisActionManager;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.activity.UPnPActivity;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.manager.AudioTrackUpdater;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Option;
import com.tti.ttimediastore.model.OptionDialog;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.presenter.ListRowPrestenter;

import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/4/7.
 */

public class CatalogFragment extends BrowseFragment implements AudioTrackUpdater.OnTrackUpdater, IrisActionManager.IrisCommandListener {

    private ArrayObjectAdapter catalogAdapter;
    private ArrayObjectAdapter listRowAdapter;
    private ArrayObjectAdapter videoListAdapter;
    private ArrayObjectAdapter audioListAdapter;
    private ArrayObjectAdapter imageListAdapter;

    private BackgroundManager backgroundManager;
    private ListRowPrestenter listRowPrestenter;
    private HeaderItem headerItem;
    private Option option;

    private OptionDialog deleteDialog;

    private boolean isPause = false;
    private boolean isToastShown = false;
    private boolean isUpdated = false;

    private Activity activity;

    private Audio audio;
    private ArrayList<Image> imageList;

    private static final int IMAGE_ITEM = 4;

    private static final int DELAY_UPDATE = 750;

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity == null) return;

        this.activity = activity;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Constants.IRIS_EVENT = Constants.EVENT_NONE;
        setUIElements();
        setCatalog();
    }

    public void onResume() {
        super.onResume();
        if (Constants.IRIS_EVENT == Constants.EVENT_VIDEO)  {
            Constants.IRIS_EVENT = Constants.EVENT_NONE;
            Constants.MODEL_TYPE = Constants.VIDEO_ALL;
            Intent intent = new Intent(getActivity(), ItemGridActivity.class);
            startActivity(intent);
        }
        else if (Constants.IRIS_EVENT == Constants.EVENT_AUDIO)  {
            Constants.IRIS_EVENT = Constants.EVENT_NONE;
            Constants.MODEL_TYPE = Constants.AUDIO_ALL;
            Intent intent = new Intent(getActivity(), ItemGridActivity.class);
            startActivity(intent);
        }
        else if (Constants.IRIS_EVENT == Constants.EVENT_IMAGE)  {
            Constants.IRIS_EVENT = Constants.EVENT_NONE;
            Constants.MODEL_TYPE = Constants.IMAGE_ALL;
            Intent intent = new Intent(getActivity(), ItemGridActivity.class);
            startActivity(intent);
        }

        backgroundManager.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wallpaper));

        if (!isUpdated) {
            isUpdated = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateState();
                }
            }, DELAY_UPDATE);
        }

        IrisActionManager.getInstance().setListener(this);

        isPause = false;
        isToastShown = false;
    }

    public void onPause() {
        super.onPause();
        IrisActionManager.getInstance().setListener(null);
        isPause = true;
        isUpdated = false;
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        backgroundManager.release();
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
        getActivity().moveTaskToBack(true);
    }

    @Override
    public void onIrisClose(String message) {
        getActivity().moveTaskToBack(true);
    }

    @Override
    public void onIrisVideo(String message) {
        Constants.MODEL_TYPE = Constants.VIDEO_ALL;
        Intent intent = new Intent(getActivity(), ItemGridActivity.class);
        startActivity(intent);
    }

    @Override
    public void onIrisAudio(String message) {
        Constants.MODEL_TYPE = Constants.AUDIO_ALL;
        Intent intent = new Intent(getActivity(), ItemGridActivity.class);
        startActivity(intent);
    }

    @Override
    public void onIrisImage(String message) {
        Constants.MODEL_TYPE = Constants.IMAGE_ALL;
        Intent intent = new Intent(getActivity(), ItemGridActivity.class);
        startActivity(intent);
    }

    @Override
    public void onIrisMain(String message) {
    }

    private void setUIElements() {
        setTitle(getString(R.string.app_title));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getContext(), R.color.base_color));
        setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.search));
        backgroundManager = BackgroundManager.getInstance(getActivity());
        if (!backgroundManager.isAttached())
            backgroundManager.attach(getActivity().getWindow());

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivty.class);
                startActivity(intent);
            }
        });

        AudioTrackUpdater.getInstance().setListener(this);
        setDialog();
    }

    private void setDialog() {
        deleteDialog = new OptionDialog(getActivity(), getString(R.string.record_delete),
                new CatalogFragment.YesListener(), new CatalogFragment.NoListener());
        deleteDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK
                        || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ESCAPE
                        && deleteDialog.isShowing()) {
                    deleteDialog.dismiss();
                }
                return false;
            }
        });
    }

    private class YesListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clearAdapter(videoListAdapter);
            option = new Option();
            option.setName(getString(R.string.video_all));
            addAdapterItem(videoListAdapter, option);
            Utils.Preferences.setVideoRecord(null);

            deleteDialog.dismiss();
            Toast.makeText(getActivity(), R.string.delete_complete, Toast.LENGTH_SHORT).show();
        }
    }

    private class NoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            deleteDialog.dismiss();
        }
    }

    private void setCatalog() {
        catalogAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        listRowPrestenter = new ListRowPrestenter();

        setVideo();
        setAuido();
        setImage();
        setOther();

        setAdapter(catalogAdapter);
        setEventListener();
    }

    private void clearAdapter(final ArrayObjectAdapter adapter) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
            }
        });
    }

    private void addAdapterItem(final ArrayObjectAdapter adapter, final Object object) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(object);
            }
        });
    }

    private void setVideo() {
        Constants.isVideoRecordChanged = true;

        videoListAdapter = new ArrayObjectAdapter(listRowPrestenter);
        headerItem = new HeaderItem(0, getString(R.string.catalog_video));
        option = new Option();
        option.setName(getString(R.string.video_all));
        addAdapterItem(videoListAdapter, option);

        addAdapterItem(catalogAdapter, new ListRow(headerItem, videoListAdapter));
    }

    private void setAuido() {
        audioListAdapter = new ArrayObjectAdapter(listRowPrestenter);
        headerItem = new HeaderItem(1, getString(R.string.catalog_audio));

        String[] audioOption = new String[] {
                getString(R.string.audio_all),
                getString(R.string.audio_artists),
                getString(R.string.audio_albums),
                getString(R.string.audio_genres)
        };

        for (int i = 0; i < audioOption.length; i++) {
            option = new Option();
            option.setName(audioOption[i]);
            addAdapterItem(audioListAdapter, option);
        }

        addAdapterItem(catalogAdapter, new ListRow(headerItem, audioListAdapter));
    }

    private void setImage() {
        imageListAdapter = new ArrayObjectAdapter(listRowPrestenter);
        headerItem = new HeaderItem(2, getString(R.string.catalog_image));
        option = new Option();
        option.setName(getString(R.string.image_all));
        addAdapterItem(imageListAdapter, option);

        addAdapterItem(catalogAdapter, new ListRow(headerItem, imageListAdapter));
    }

    private void setOther() {
        listRowAdapter = new ArrayObjectAdapter(listRowPrestenter);
        headerItem = new HeaderItem(3, getString(R.string.catalog_other));
        String[] otherOption = new String[] {
                getString(R.string.other_lan),
                getString(R.string.other_internet),
                getString(R.string.video_delete),
        };

        for (int i = 0; i < otherOption.length; i++) {
            option = new Option();
            option.setName(otherOption[i]);
            addAdapterItem(listRowAdapter, option);
        }

        addAdapterItem(catalogAdapter, new ListRow(headerItem, listRowAdapter));
    }

    private void updateState() {
        if (Constants.isVideoRecordChanged) {
            Constants.isVideoRecordChanged = false;

            clearAdapter(videoListAdapter);
            option = new Option();
            option.setName(getString(R.string.video_all));
            addAdapterItem(videoListAdapter, option);

            ArrayList<Video> recordList = Utils.Preferences.getVideoRecord();
            if (recordList != null) {
                int recordCount = recordList.size();
                if (recordCount > 0) {
                    for (int i = recordCount; i > 0; i--)
                        addAdapterItem(videoListAdapter, recordList.get(i - 1));
                }
            }
        }

        if (AudioPlayerController.getInstance().getListener() != null) {
            Audio audio = Utils.Preferences.getResumeAudio();
            if (this.audio != audio) {
                this.audio = audio;

                if (audio != null) {
                    if (audioListAdapter.get(0) instanceof Audio) {
                        audioListAdapter.replace(0, audio);
                    }
                    else {
                        clearAdapter(audioListAdapter);
                        addAdapterItem(audioListAdapter, audio);
                        addAudioOption();
                    }
                }
                else {
                    if (audioListAdapter.get(0) instanceof Audio) {
                        clearAdapter(audioListAdapter);
                        addAudioOption();
                    }
                }
            }
        }
        else {
            Utils.Preferences.setResumeAudio(null);
            if (audioListAdapter.get(0) instanceof Audio) {
                clearAdapter(audioListAdapter);
                addAudioOption();
            }
        }

        clearAdapter(imageListAdapter);
        option = new Option();
        option.setName(getString(R.string.image_all));
        addAdapterItem(imageListAdapter, option);

        new setImageList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void addAudioOption() {
        String[] audioOption = new String[] {
                getString(R.string.audio_all),
                getString(R.string.audio_artists),
                getString(R.string.audio_albums),
                getString(R.string.audio_genres),
        };
        for (int i = 0; i < audioOption.length; i++) {
            option = new Option();
            option.setName(audioOption[i]);
            addAdapterItem(audioListAdapter, option);
        }
    }

    private void randomImage() {
        if (imageList != null) {
            int imageCount = imageList.size();
            if (imageCount > 0) {
                if (imageCount <= IMAGE_ITEM) {
                    for (int i = 0; i < imageCount; i++)
                        addAdapterItem(imageListAdapter, imageList.get(i));
                }
                else {
                    int imageTrack = (int) (Math.random() * (imageCount - IMAGE_ITEM));
                    for (int i = imageTrack; i < imageTrack + IMAGE_ITEM; i++)
                        addAdapterItem(imageListAdapter, imageList.get(i));
                }
            }
        }
    }

    @Override
    public void onTrackUpdate() {
        if (!isPause) {
            audio = AudioTrackUpdater.getInstance().getUpdateTrack();
            audioListAdapter.replace(0, audio);
        }
    }

    @Override
    public void onTrackListEnd() {
        if (!isPause) {
            Utils.Preferences.setResumeAudio(null);

            clearAdapter(audioListAdapter);
            addAudioOption();
            AudioPlayerController.getInstance().finish();
        }
    }

    private void scanVideo(final Video video) {
        MediaScannerConnection.scanFile(getActivity(), new String[] { video.getPath() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                video.setPath(path);

                Constants.MODEL_TYPE = Constants.VIDEO_RESUME;
                Intent intent = new Intent(getActivity(), ContentActivity.class);
                intent.putExtra(Constants.ITEM_CONTENT, video);
                startActivity(intent);
            }
        });
    }

    private void scanImage(final Image image) {
        MediaScannerConnection.scanFile(getActivity(), new String[] { image.getPath() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        image.setPath(path);

                        Constants.MODEL_TYPE = Constants.IMAGE_ALL;
                        Intent intent = new Intent(getActivity(), ContentActivity.class);
                        intent.putExtra(Constants.ITEM_CONTENT, imageList);
                        intent.putExtra(Constants.ITEM_ID, image.getId());
                        startActivity(intent);
                    }
                });
    }

    private class setImageList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            imageList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = getActivity().getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                for (int i = 0; i < cursor.getCount(); i++) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)) != null) {
                        Image image = new Image();
                        image.setId(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
                        image.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)));
                        image.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                        imageList.add(image);
                        cursor.moveToNext();
                    }
                    else
                        cursor.moveToNext();
                }
            }

            if (cursor != null) cursor.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            randomImage();
        }
    }

    private void setEventListener() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Video) {
                    scanVideo((Video) item);
                }
                else if (item instanceof Audio) {
                    Constants.MODEL_TYPE = Constants.AUDIO_RESUME;
                    Intent intent = new Intent(getActivity(), ContentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                else if (item instanceof Image) {
                    scanImage((Image) item);
                }
                else if (item instanceof Option) {
                    String name = ((Option) item).getName();

                    if (name.equals(getString(R.string.video_delete))) {
                        ArrayList<Video> recordList = Utils.Preferences.getVideoRecord();
                        if (recordList != null)
                            if (recordList.size() > 0)
                                deleteDialog.show();
                            else {
                                Utils.Preferences.setVideoRecord(null);
                                Toast.makeText(getActivity(), R.string.record_empty, Toast.LENGTH_SHORT).show();
                            }
                        else
                            Toast.makeText(getActivity(), R.string.record_empty, Toast.LENGTH_SHORT).show();
                    }
                    else if (name.equals(getString(R.string.other_lan))) {
                        Constants.MODEL_TYPE = Constants.OTHER_UPNP;
                        Intent intent = new Intent(getActivity(), UPnPActivity.class);
                        startActivity(intent);
                    }
                    else if (name.equals(getString(R.string.other_internet))) {
                        Constants.MODEL_TYPE = Constants.OTHER_INTERNET;
                        Intent intent = new Intent(getActivity(), ContentActivity.class);
                        startActivity(intent);
                    }
                    else if (name.equals(getString(R.string.other_preferences))) {
                        Constants.MODEL_TYPE = Constants.OTHER_PREFERENCES;
                    }
                    else {
                        if (name.equals(getString(R.string.video_all)))
                            Constants.MODEL_TYPE = Constants.VIDEO_ALL;
                        else if (name.equals(getString(R.string.audio_artists)))
                            Constants.MODEL_TYPE = Constants.AUDIO_ARTISTS;
                        else if (name.equals(getString(R.string.audio_albums)))
                            Constants.MODEL_TYPE = Constants.AUDIO_ALBUMS;
                        else if (name.equals(getString(R.string.audio_genres)))
                            Constants.MODEL_TYPE = Constants.AUDIO_GENRES;
                        else if (name.equals(getString(R.string.audio_all)))
                            Constants.MODEL_TYPE = Constants.AUDIO_ALL;
                        else if (name.equals(getString(R.string.image_all)))
                            Constants.MODEL_TYPE = Constants.IMAGE_ALL;

                        Intent intent = new Intent(getActivity(), ItemGridActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
