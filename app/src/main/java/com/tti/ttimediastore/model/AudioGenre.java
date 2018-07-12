package com.tti.ttimediastore.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class AudioGenre implements Parcelable {

    private String id;
    private String genre;
    private String cover;

    public AudioGenre() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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
        parcel.writeString(this.genre);
        parcel.writeString(this.cover);
    }

    protected AudioGenre(Parcel in) {
        this.id = in.readString();
        this.genre = in.readString();
        this.cover = in.readString();
    }

    public static final Parcelable.Creator<AudioGenre> CREATOR = new Parcelable.Creator<AudioGenre>() {
        public AudioGenre createFromParcel(Parcel source) {
            return new AudioGenre(source);
        }

        public AudioGenre[] newArray(int size) {
            return new AudioGenre[size];
        }
    };
}
