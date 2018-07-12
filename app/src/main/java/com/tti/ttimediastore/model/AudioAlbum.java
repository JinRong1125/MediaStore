package com.tti.ttimediastore.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class AudioAlbum implements Parcelable {

    private String id;
    private String album;
    private String artist;
    private String cover;

    public AudioAlbum() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
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
        parcel.writeString(this.album);
        parcel.writeString(this.artist);
        parcel.writeString(this.cover);
    }

    protected AudioAlbum(Parcel in) {
        this.id = in.readString();
        this.album = in.readString();
        this.artist = in.readString();
        this.cover = in.readString();
    }

    public static final Parcelable.Creator<AudioAlbum> CREATOR = new Parcelable.Creator<AudioAlbum>() {
        public AudioAlbum createFromParcel(Parcel source) {
            return new AudioAlbum(source);
        }

        public AudioAlbum[] newArray(int size) {
            return new AudioAlbum[size];
        }
    };
}
