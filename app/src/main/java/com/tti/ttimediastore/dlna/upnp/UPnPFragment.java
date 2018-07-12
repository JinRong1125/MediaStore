package com.tti.ttimediastore.dlna.upnp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.tti.ttimediastore.fragment.NoNetworkFragment;

import com.tti.ttimediastore.presenter.CardPresenter;

import java.util.ArrayList;

/**
 * Created by dylan_liang on 2017/7/31.
 */

public class UPnPFragment extends VerticalGridFragment implements ContentBrowseTask.Callbacks {

    private VerticalGridPresenter gridPresenter;
    private ArrayObjectAdapter deviceAdapter, itemAdapter;

    private BackgroundManager backgroundManager;
    private NoNetworkFragment noNetworkFragment;

    private Activity activity;

    private UPnPDevice currentDevice;

    private ArrayList<String> titleList;
    private String currentTitle;

    private boolean isPause = false;
    private boolean isGoBack = false;
    private boolean isNeedReload = false;

    private ContentBrowseTask contentBrowseTask;

    private static final int NUM_COLUMNS = 5;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity == null) return;

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFragment();
        connectUPnP();
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

        if (isNeedReload && isAdded()) {
            clearItem();
            setDevice();
            isNeedReload = false;
        }
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        isPause = true;
    }

    private void setupFragment() {
        setTitle(getString(R.string.other_lan));
        noNetworkFragment = new NoNetworkFragment();

        gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        deviceAdapter = new ArrayObjectAdapter(new CardPresenter());
        itemAdapter = new ArrayObjectAdapter(new CardPresenter());
        setAdapter(deviceAdapter);
    }

    private void connectUPnP() {
        contentBrowseTask = new ContentBrowseTask(activity, this);
        contentBrowseTask.startUPnPService();
        titleList = new ArrayList<>();
    }

    public boolean goPreviousStack() {
        if (contentBrowseTask.goBack(true))
            return true;
        else
            return false;
    }

    private boolean isNetworkEnable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isAvailable())
                return true;
            else
                return false;
        }
        else
            return false;
    }

    private void setEventListener() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {

            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof UPnPDevice) {
                    UPnPDevice upnpDevice = (UPnPDevice) item;
                    currentDevice = upnpDevice;
                    currentTitle = upnpDevice.getTitle();
                }
                else if (item instanceof UPnPFile) {
                    UPnPFile upnpFile = (UPnPFile) item;
                    if (upnpFile.isContainer())
                        currentTitle = upnpFile.getTitle();
                }

                contentBrowseTask.navigateTo(item, isGoBack, currentTitle);
            }
        });
    }

    private void clearItem() {
        titleList.clear();
        itemAdapter.clear();
        deviceAdapter.clear();
        currentDevice = null;
    }

    private void setDevice() {
        setTitle(getString(R.string.other_lan));
        setAdapter(deviceAdapter);
        contentBrowseTask.clearStack();
        contentBrowseTask.refreshDevices();
    }

    @Override
    public void onDisplayDevices() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(getString(R.string.other_lan));
                titleList.clear();
                setAdapter(deviceAdapter);
                currentDevice = null;
            }
        });
    }

    @Override
    public void onDisplayDirectories(boolean isGoBack) {
        if (!isGoBack)
            titleList.add(currentTitle);
        else {
            titleList.remove(titleList.size() - 1);
            currentTitle = titleList.get(titleList.size() - 1);
        }

        setTitle(currentTitle);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemAdapter.clear();
                setAdapter(itemAdapter);
            }
        });
    }

    @Override
    public void onDisplayItems(final ArrayList<UPnPFile> items) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemAdapter.clear();
                itemAdapter.addAll(0, items);
            }
        });
    }

    @Override
    public void onDisplayItemsError(final String error) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentBrowseTask.goBack(true);
                Toast.makeText(activity, R.string.error_folder, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeviceAdded(final UPnPDevice device) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int position = deviceAdapter.indexOf(device);
                if (position >= 0)
                    deviceAdapter.replace(position, device);
                else
                    deviceAdapter.add(device);
            }
        });
    }

    @Override
    public void onDeviceRemoved(final UPnPDevice device) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceAdapter.remove(device);
                if (currentDevice != null) {
                    if (currentDevice.getDevice() == device.getDevice()) {
                        if (!isPause) {
                            clearItem();
                            setDevice();
                        }
                        else
                            isNeedReload = true;
                    }
                }
            }
        });
    }

    private void showNoNetwork() {
        if (!noNetworkFragment.isAdded() && !isPause)
            getActivity().getFragmentManager().beginTransaction()
                    .add(R.id.upnp_fragment, noNetworkFragment).commitAllowingStateLoss();
    }

    private void hideNoNetwork() {
        if (noNetworkFragment.isAdded() && !isPause)
            getActivity().getFragmentManager().beginTransaction().remove(noNetworkFragment).commitAllowingStateLoss();
    }
}
