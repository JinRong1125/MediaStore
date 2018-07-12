package com.tti.ttimediastore.dlna.upnp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.ServiceManager;
import com.tti.ttimediastore.fragment.SpinnerFragment;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Video;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.net.URI;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by dylan_liang on 2017/7/31.
 */

public class ContentBrowseTask {

    static interface Callbacks {
        void onDisplayDevices();
        void onDisplayDirectories(boolean isGoBack);
        void onDisplayItems(ArrayList<UPnPFile> items);
        void onDisplayItemsError(String error);
        void onDeviceAdded(UPnPDevice device);
        void onDeviceRemoved(UPnPDevice device);
    }

    private SpinnerFragment spinnerFragment;

    private Callbacks mCallbacks;
    private BrowseRegistryListener mListener = new BrowseRegistryListener();
    private AndroidUpnpService mService;
    private Stack<UPnPFile> mFolders = new Stack<UPnPFile>();
    private Boolean mIsShowingDeviceList = true;
    private UPnPDevice mCurrentDevice = null;
    private Activity mActivity;

    private ArrayList<UPnPFile> itemList;

    private AudioPlayerController audioPlayerController;

    public ContentBrowseTask(Activity activity, Callbacks callbacks) {
        mActivity = activity;
        mCallbacks = callbacks;
        spinnerFragment = new SpinnerFragment();
        mActivity.getFragmentManager().beginTransaction().add(R.id.upnp_fragment, spinnerFragment).commit();
        hideProgress();
        audioPlayerController = AudioPlayerController.getInstance();
    }

    public void navigateTo(Object model, Boolean isGoBack, String currentTitle) {
        if (model instanceof UPnPDevice) {

            UPnPDevice upnpDevice = (UPnPDevice)model;
            Device device = upnpDevice.getDevice();

            if (device.isFullyHydrated()) {
                showProgress();
                Service conDir = upnpDevice.getContentDirectory();

                if (conDir != null)
                    mService.getControlPoint().execute(
                            new CustomContentBrowseActionCallback(conDir, "0", isGoBack));

//                if (mCallbacks != null)
//                    mCallbacks.onDisplayDirectories(isGoBack);

                mIsShowingDeviceList = false;

                mCurrentDevice = upnpDevice;
            } else {
                Toast.makeText(mActivity, R.string.upnp_loading, Toast.LENGTH_SHORT).show();
            }
        }

        if (model instanceof UPnPFile) {

            UPnPFile item = (UPnPFile)model;

            if (item.isContainer()) {
                showProgress();

                if (mFolders.isEmpty())
                    mFolders.push(item);
                else
                if (mFolders.peek().getId() != item.getId())
                    mFolders.push(item);

                mService.getControlPoint().execute(
                        new CustomContentBrowseActionCallback(item.getService(),
                                item.getId(), isGoBack));
            } else {
                hideProgress();
                try {
                    Uri uri = Uri.parse(item.getUrl());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getMimeTypeFromUrl(uri.toString());
                    if (type.contains(Constants.VIDEO_PREFIX)) {
                        Constants.MODEL_TYPE = Constants.INPUT_MEDIA;
                        Video video = new Video();
                        video.setTitle(item.getTitle());
                        video.setPath(uri.toString());
                        Intent intent = new Intent(mActivity, ContentActivity.class);
                        intent.putExtra(Constants.ITEM_CONTENT, video);
                        mActivity.startActivity(intent);
                    }
                    else if (type.contains(Constants.AUDIO_PREFIX)) {
                        Constants.MODEL_TYPE = Constants.INPUT_AUDIO;
                        ArrayList<Audio> audioList = new ArrayList<>();
                        for (int i = 0; i < itemList.size(); i++) {
                            Audio audio = new Audio();
                            audio.setId(String.valueOf(i));
                            audio.setTitle(itemList.get(i).getTitle());
                            audio.setAlbum(currentTitle);
                            audio.setPath(itemList.get(i).getUrl());
                            audioList.add(audio);
                        }

                        audioPlayerController.finish();
                        Intent intent = new Intent(mActivity, ContentActivity.class);
                        intent.putExtra(Constants.ITEM_CONTENT, audioList);
                        intent.putExtra(Constants.ITEM_ID, item.getId());
                        mActivity.startActivity(intent);
                    }
                    else if (type.contains(Constants.IMAGE_PREFIX)) {
                        Constants.MODEL_TYPE = Constants.INPUT_IMAGE;
                        ArrayList<Image> imageList = new ArrayList<>();
                        for (int i = 0; i < itemList.size(); i++) {
                            Image image = new Image();
                            image.setId(itemList.get(i).getId());
                            image.setTitle(itemList.get(i).getTitle());
                            image.setPath(itemList.get(i).getUrl());
                            imageList.add(image);
                        }

                        Intent intent = new Intent(mActivity, ContentActivity.class);
                        intent.putExtra(Constants.ITEM_CONTENT, imageList);
                        intent.putExtra(Constants.ITEM_ID, item.getId());
                        mActivity.startActivity(intent);
                    }
                } catch(NullPointerException ex) {
                    Log.v("upnp_error", mActivity.getString(R.string.info_could_not_start_activity));
                } catch(ActivityNotFoundException ex) {
                    Log.v("upnp_error", mActivity.getString(R.string.info_no_handler));
                }
            }
        }
    }

    public Boolean goBack(Boolean isGoBack) {
        if (mFolders.empty()) {
            hideProgress();
            if (!mIsShowingDeviceList) {
                mIsShowingDeviceList = true;
                if (mCallbacks != null)
                    mCallbacks.onDisplayDevices();
            } else {
                return true;
            }
        } else {
            showProgress();
            UPnPFile item = mFolders.pop();

            mService.getControlPoint().execute(
                    new CustomContentBrowseActionCallback(item.getService(),
                            item.getContainer().getParentID(), isGoBack));
        }

        return false;
    }

    public void refreshDevices() {
        hideProgress();
        if (mService == null)
            return;

        mService.getRegistry().removeAllRemoteDevices();

        for (Device device : mService.getRegistry().getDevices())
            mListener.deviceAdded(device);

        mService.getControlPoint().search();
    }

    public void refreshCurrent(Boolean isGoBack) {
        showProgress();

        if (mService == null) {
            hideProgress();
            return;
        }

        if (mIsShowingDeviceList != null && mIsShowingDeviceList) {
            hideProgress();
            if (mCallbacks != null)
                mCallbacks.onDisplayDevices();

            mService.getRegistry().removeAllRemoteDevices();

            for (Device device : mService.getRegistry().getDevices())
                mListener.deviceAdded(device);

            mService.getControlPoint().search();
        } else {
            if (!mFolders.empty()) {
                UPnPFile item = mFolders.peek();
                if (item == null) {
                    hideProgress();
                    return;
                }

                mService.getControlPoint().execute(
                        new CustomContentBrowseActionCallback(item.getService(),
                                item.getId(), isGoBack));
            } else {
                if (mCurrentDevice != null) {
                    Service service = mCurrentDevice.getContentDirectory();
                    if (service != null)
                        mService.getControlPoint().execute(
                                new CustomContentBrowseActionCallback(service, "0", isGoBack));
                }
            }
        }
    }

    public Boolean bindServiceConnection() {
        Context context = mActivity.getApplicationContext();
        if (context == null)
            return false;

        context.bindService(
                new Intent(mActivity, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE
        );

        return true;
    }

    public Boolean unbindServiceConnection() {
        if (mService != null)
            mService.getRegistry().removeListener(mListener);

        Context context = mActivity.getApplicationContext();
        if (context == null)
            return false;

        context.unbindService(serviceConnection);
        return true;
    }

    public void startUPnPService() {
        if (ServiceManager.androidUpnpService != null) {
            mService = ServiceManager.androidUpnpService;
            mService.getRegistry().addListener(mListener);

            for (Device device : mService.getRegistry().getDevices())
                mListener.deviceAdded(device);

            mService.getControlPoint().search();
        }
    }

    public void clearStack() {
        mFolders.clear();
        mIsShowingDeviceList = true;
    }

    private void showProgress() {
        mActivity.getFragmentManager().beginTransaction().show(spinnerFragment).commit();
    }

    private void hideProgress() {
        mActivity.getFragmentManager().beginTransaction().hide(spinnerFragment).commit();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = (AndroidUpnpService) service;
            mService.getRegistry().addListener(mListener);

            for (Device device : mService.getRegistry().getDevices())
                mListener.deviceAdded(device);

            mService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private class BrowseRegistryListener extends DefaultRegistryListener {
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            deviceRemoved(device);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(Device device) {

            UPnPDevice upnpDevice = new UPnPDevice(R.drawable.ic_device, device);

            Service conDir = upnpDevice.getContentDirectory();
            if (conDir != null) {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mActivity);
                if (prefs.getBoolean("settings_validate_devices", false)) {
                    if (device.isFullyHydrated())
                        mService.getControlPoint().execute(
                                new CustomContentBrowseTestCallback(device, conDir));
                } else {
                    if (mCallbacks != null)
                        mCallbacks.onDeviceAdded(upnpDevice);
                }
            }
        }

        public void deviceRemoved(Device device) {
            if (mCallbacks != null)
                mCallbacks.onDeviceRemoved(new UPnPDevice(R.drawable.ic_device, device));
        }
    }

    private class CustomContentBrowseActionCallback extends Browse {
        private Service service;

        public CustomContentBrowseActionCallback(Service service, String id, Boolean isGoBack) {
            super(service, id, BrowseFlag.DIRECT_CHILDREN, "*", 0, 99999l,
                    new SortCriterion(true, "dc:title"));

            this.service = service;

            if (mCallbacks != null)
                mCallbacks.onDisplayDirectories(isGoBack);
        }

        private UPnPFile createUPnPFile(DIDLObject item) {

            UPnPFile upnpFile = new UPnPFile(mActivity.getResources(),
                    R.drawable.ic_folder, service, item);

            URI usableIcon = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ICON.class);
            if (usableIcon == null || usableIcon.toString().isEmpty()) {
                usableIcon = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
            }
            if (usableIcon != null)
                upnpFile.setIconUrl(usableIcon.toString());

            if (item instanceof Item) {
                upnpFile.setIcon(R.drawable.ic_file);

                SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(mActivity);

                if (prefs.getBoolean("settings_hide_file_icons", false))
                    upnpFile.setHideIcon(true);

                if (prefs.getBoolean("settings_show_extensions", false))
                    upnpFile.setShowExtension(true);
            }

            return upnpFile;
        }

        @Override
        public void received(final ActionInvocation actionInvocation, final DIDLContent didl) {
            hideProgress();

            ArrayList<UPnPFile> items = new ArrayList<UPnPFile>();

            try {
                for (Container childContainer : didl.getContainers())
                    items.add(createUPnPFile(childContainer));

                for (Item childItem : didl.getItems())
                    items.add(createUPnPFile(childItem));

                if (mCallbacks != null) {
                    mCallbacks.onDisplayItems(items);
                    itemList = items;
                }

            } catch (Exception ex) {
                actionInvocation.setFailure(new ActionException(
                        ErrorCode.ACTION_FAILED,
                        "Can't create list childs: " + ex, ex));
                failure(actionInvocation, null, ex.getMessage());
            }
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse response, String s) {
            hideProgress();
            if (mCallbacks != null)
                mCallbacks.onDisplayItemsError(createDefaultFailureMessage(invocation, response));
        }
    }

    private class CustomContentBrowseTestCallback extends Browse {
        private Device device;
        private Service service;

        public CustomContentBrowseTestCallback(Device device, Service service) {
            super(service, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 99999l,
                    new SortCriterion(true, "dc:title"));

            this.device = device;
            this.service = service;
        }

        @Override
        public void received(final ActionInvocation actionInvocation, final DIDLContent didl) {
            if (mCallbacks != null)
                mCallbacks.onDeviceAdded(new UPnPDevice(R.drawable.ic_device, device));
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

        }
    }
}
