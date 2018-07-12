package com.tti.ttimediastore.constants;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class Constants {

    public static final String APP_NAME = "TTIMediaStore";

    public static int APP_STATE;
    public static final int STATE_BACKGROUND = 0;
    public static final int STATE_FOREGROUND = 1;

    public static final int PERMISSION_REQUEST = 0;
    public static final int NOTIFICATION_MUTE = 0;
    public static final int NOTIFICATION_UNMUTE = 1;

    public static String MODEL_TYPE;

    public static final String VIDEO_ALL = "ALL VIDEOS";
    public static final String VIDEO_RESUME = "RESUME VIDEO";
    public static final String AUDIO_ARTISTS = "ARTISTS";
    public static final String AUDIO_ALBUMS = "ALBUMS";
    public static final String AUDIO_GENRES = "GENRES";
    public static final String AUDIO_ALL = "ALL SONGS";
    public static final String AUDIO_RESUME = "RESUME AUDIO";
    public static final String AUDIO_SEARCH = "SEARCH AUDIO";
    public static final String IMAGE_ALL = "ALL IMAGES";
    public static final String OTHER_UPNP = "UPNP";
    public static final String OTHER_INTERNET = "INTERNET";
    public static final String OTHER_PREFERENCES = "PREFERENCES";
    public static final String INTERNET_TO_VIDEO = "INTERNET TO VIDEO";

    public static final String INPUT_MEDIA = "INPUT MEDIA";
    public static final String INPUT_AUDIO = "INPUT AUDIO";
    public static final String INPUT_IMAGE = "INPUT IMAGE";
    public static final String ACTION_MEDIA = "ACTION MEDIA";

    public static final String ITEM_CONTENT = "CONTENT";
    public static final String ITEM_ID = "ID";

    public static final int FOCUS_ITEM = 0;
    public static final int UNFOCUS_ITEM = 1;

    public static int PIP_MODE;
    public static final int PIP_OFF = 0;
    public static final int PIP_ON = 1;

    public static int IRIS_EVENT;
    public static final int EVENT_NONE = 0;
    public static final int EVENT_BACK = 1;
    public static final int EVENT_CLOSE = 2;
    public static final int EVENT_VIDEO = 3;
    public static final int EVENT_AUDIO = 4;
    public static final int EVENT_IMAGE = 5;
    public static final int EVENT_MAIN = 6;

    public static final String CONTENTDIRECTORY_SERVICE = "pref_contentDirectoryService";
    public static final String CONTENTDIRECTORY_NAME = "pref_contentDirectoryService_name";
    public static final String CONTENTDIRECTORY_VIDEO = "pref_contentDirectoryService_video";
    public static final String CONTENTDIRECTORY_AUDIO = "pref_contentDirectoryService_audio";
    public static final String CONTENTDIRECTORY_IMAGE = "pref_contentDirectoryService_image";

    public static final String VIDEO_PREFIX = "video";
    public static final String AUDIO_PREFIX = "audio";
    public static final String IMAGE_PREFIX = "image";

    public enum MEDIA_TYPE {
        AUDIO, IMAGE, VIDEO
    }

    public static boolean isVideoRecordChanged;
}
