package com.tti.ttimediastore.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class AudioArtist implements Parcelable {

    private String id;
    private String artist;
    private String cover;

    public AudioArtist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.artist);
        parcel.writeString(this.cover);
    }

    protected AudioArtist(Parcel in) {
        this.id = in.readString();
        this.artist = in.readString();
        this.cover = in.readString();
    }

    public static final Parcelable.Creator<AudioArtist> CREATOR = new Parcelable.Creator<AudioArtist>() {
        public AudioArtist createFromParcel(Parcel source) {
            return new AudioArtist(source);
        }

        public AudioArtist[] newArray(int size) {
            return new AudioArtist[size];
        }
    };
}
