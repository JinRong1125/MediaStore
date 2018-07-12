package com.tti.ttimediastore.viewholder;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;

import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.AudioAlbum;
import com.tti.ttimediastore.model.AudioArtist;
import com.tti.ttimediastore.model.AudioGenre;
import com.tti.ttimediastore.dlna.upnp.UPnPDevice;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.dlna.upnp.UPnPFile;
import com.tti.ttimediastore.model.Option;
import com.tti.ttimediastore.model.Video;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class CardViewHolder extends Presenter.ViewHolder {

    public Option option;
    public Video video;
    public Audio audio;
    public Image image;
    public AudioArtist audioArtist;
    public AudioAlbum audioAlbum;
    public AudioGenre audioGenre;
    public UPnPDevice upnpDevice;
    public UPnPFile upnpFile;

    public ImageCardView imageCardView;
    public Drawable cardImage;

    public CardViewHolder(View view) {
        super(view);
        imageCardView = (ImageCardView) view;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public Option getOption() {
        return option;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Video getVideo() {
        return video;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setAudioArtist(AudioArtist audioArtist) {
        this.audioArtist = audioArtist;
    }

    public AudioArtist getAudioArtist() {
        return audioArtist;
    }

    public void setAudioAlbum(AudioAlbum audioAlbum) {
        this.audioAlbum = audioAlbum;
    }

    public AudioAlbum getAudioAlbum() {
        return audioAlbum;
    }

    public void setAudioGenre(AudioGenre audioGenre) {
        this.audioGenre = audioGenre;
    }

    public AudioGenre getAudioGenre() {
        return audioGenre;
    }

    public void setUPnPDevice(UPnPDevice upnpDevice) {
        this.upnpDevice = upnpDevice;
    }

    public UPnPDevice getUPnPDevice() {
        return upnpDevice;
    }

    public void setUPnPFile(UPnPFile upnpFile) {
        this.upnpFile = upnpFile;
    }

    public UPnPFile getUPnPFile() {
        return upnpFile;
    }

    public ImageCardView getImageCardView() {
        return imageCardView;
    }

    public Drawable getCardImage() {
        return cardImage;
    }
}
