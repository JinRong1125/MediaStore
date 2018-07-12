package com.tti.ttimediastore.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tti.ttimediastore.R;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.Video;

import org.fourthline.cling.model.types.UDN;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class Utils {

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(TTIMediaStore.getInstance());
    }

    public static class Preferences {
        private static String PLAY_MODE = "PlayMode";
        private static String SUBTITLES_MODE = "SubtitlesMode";
        private static String VIDEO_RECORD = "VideoRecord";
        private static String RESUME_AUDIO = "ResumeAudio";
        private static String INTERNET_TEXT = "InternetText";
        private static String DLNA_DMS_UDN = "DlnaDmsUDN";
        private static String DLNA_DMR_UDN = "DlnaDmrUDN";
        private static String ITEM_VIEW_STATE = "ItemViewState";
        private static String ITEM_VIEW_LIFE = "ItemViewLife";

        public static void setPlayMode(int PlayMode) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(PLAY_MODE, PlayMode);
            editor.apply();
        }

        public static int getPlayMode() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getInt(PLAY_MODE, 0);
        }

        public static void setSubtitlesMode(int subtitlesMode) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(SUBTITLES_MODE, subtitlesMode);
            editor.apply();
        }

        public static int getSubtitlesMode() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getInt(SUBTITLES_MODE, 0);
        }

        public static void setVideoRecord(ArrayList<Video> recordList) {
            Constants.isVideoRecordChanged = true;

            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(VIDEO_RECORD, new Gson().toJson(recordList));
            editor.apply();
        }

        public static ArrayList<Video> getVideoRecord() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(VIDEO_RECORD, "");
            Type type = new TypeToken<List<Video>>(){}.getType();

            return new Gson().fromJson(json, type);
        }

        public static void setResumeAudio(Audio audio) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(RESUME_AUDIO, new Gson().toJson(audio));
            editor.apply();
        }

        public static Audio getResumeAudio() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(RESUME_AUDIO, "");

            return new Gson().fromJson(json, Audio.class);
        }

        public static void setInternetText(String text) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(INTERNET_TEXT, text);
            editor.apply();
        }

        public static String getInternetText() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getString(INTERNET_TEXT, "");
        }

        private static void setDmsUDN(UDN dmsUDN) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(DLNA_DMS_UDN, new Gson().toJson(dmsUDN));
            editor.apply();
        }

        public static UDN getDmsUDN() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(DLNA_DMS_UDN, "");

            UDN dmsUDN = new Gson().fromJson(json, UDN.class);
            if (dmsUDN != null)
                return dmsUDN;
            dmsUDN = new UDN(UUID.randomUUID());
            setDmsUDN(dmsUDN);
            return dmsUDN;
        }

        private static void setDmrUDN(UDN dmrUDR) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(DLNA_DMR_UDN, new Gson().toJson(dmrUDR));
            editor.apply();
        }

        public static UDN getDmrUDN() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(DLNA_DMR_UDN, "");

            UDN dmrUDN = new Gson().fromJson(json, UDN.class);
            if (dmrUDN != null)
                return dmrUDN;
            dmrUDN = new UDN(UUID.randomUUID());
            setDmrUDN(dmrUDN);
            return dmrUDN;
        }

        public static void setItemViewState(int ItemViewState) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(ITEM_VIEW_STATE, ItemViewState);
            editor.apply();
        }

        public static int getItemViewState() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getInt(ITEM_VIEW_STATE, 0);
        }

        public static void setItemViewLife(int ItemViewLife) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(ITEM_VIEW_LIFE, ItemViewLife);
            editor.apply();
        }

        public static int getItemViewLife() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getInt(ITEM_VIEW_LIFE, 0);
        }
    }

    public static void updateMediaStore(Activity activity) {
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }

    public static boolean isPermissionGranted(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public static void requestMultiplePermissions(Activity activity) {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };
        activity.requestPermissions(permissions, Constants.PERMISSION_REQUEST);
    }

    public static long getMediaDuration(Context context, String videoPath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, Uri.parse(videoPath));
            return Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            mediaMetadataRetriever.release();
        }
    }

    public static Bitmap getAlbumCover(Context context, String audioPath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, Uri.parse(audioPath));
            byte [] data = mediaMetadataRetriever.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            mediaMetadataRetriever.release();
        }
    }

    public static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        String timeText = null;

        if (hours > 0)
            timeText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
        else
            timeText = String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();

        return timeText;
    }

    public static Bitmap readCoverBitmap(Context context, String path, int width) {
        if (path == null)
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.tti_logo);
        if (path.startsWith("file"))
            path = path.substring(7);
        Bitmap cover = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        /* Get the resolution of the bitmap without allocating the memory */
        options.inJustDecodeBounds = true;

        if (options.outWidth > 0 && options.outHeight > 0) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;

            // Find the best decoding scale for the bitmap
            if (width > 0) {
                while (options.outWidth / options.inSampleSize > width)
                    options.inSampleSize = options.inSampleSize * 2;
            }

            // Decode the file (with memory allocation this time)
            try {
                cover = BitmapFactory.decodeFile(path, options);
            }
            catch (Exception ex) {
                cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.tti_logo);
            }
        }
        return cover;
    }

    public static void adjustVolume(AudioManager audioManager, int direction) {
        if (audioManager != null)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
    }

    public static void setFitCenterImage(Context context, ImageView imageView, Object object) {
        Glide.with(context)
                .load(object)
                .apply(new RequestOptions().fitCenter().error(R.drawable.tti_logo))
                .into(imageView);
    }

    public static void setCenterCropImage(Context context, ImageView imageView, Object object) {
        Glide.with(context)
                .load(object)
                .apply(new RequestOptions().centerCrop().error(R.drawable.tti_logo))
                .into(imageView);
    }

    public static String getSettingContentDirectoryName(Context ctx)
    {
        String value = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(Constants.CONTENTDIRECTORY_NAME, "");
        return (!value.equals("")) ? value : android.os.Build.MODEL;
    }

    private static InetAddress getLocalIpAdressFromIntf(String intfName)
    {
        try
        {
            NetworkInterface intf = NetworkInterface.getByName(intfName);
            if(intf.isUp())
            {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        return inetAddress;
                }
            }
        } catch (Exception e) {
            Log.v("MainActivity", "Unable to get ip adress for interface " + intfName);
        }
        return null;
    }

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                 enumeration.hasMoreElements();) {
                NetworkInterface networkInterface = enumeration.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }


    public static InetAddress getLocalIpAddress(Context ctx) throws UnknownHostException
    {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress!=0)
            return InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));

        Log.v("MainActivity", "No ip adress available throught wifi manager, try to get it manually");

        InetAddress inetAddress;

        inetAddress = getLocalIpAddress();
        if(inetAddress!=null)
        {
            Log.v("MainActivity", "Got an ip for interfarce ethernet");
            return inetAddress;
        }

        inetAddress = getLocalIpAdressFromIntf("wlan0");
        if(inetAddress!=null)
        {
            Log.v("MainActivity", "Got an ip for interfarce wlan0");
            return inetAddress;
        }

        inetAddress = getLocalIpAdressFromIntf("usb0");
        if(inetAddress!=null)
        {
            Log.v("MainActivity", "Got an ip for interfarce usb0");
            return inetAddress;
        }

        return InetAddress.getByName("0.0.0.0");
    }
}
