package com.tti.ttimediastore.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.AudioAlbum;
import com.tti.ttimediastore.model.AudioArtist;
import com.tti.ttimediastore.model.AudioGenre;
import com.tti.ttimediastore.dlna.upnp.UPnPDevice;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.dlna.upnp.UPnPFile;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.viewholder.CardViewHolder;

/**
 * Created by dylan_liang on 2017/4/10.
 */

public class CardPresenter extends Presenter {

    private Context context;

    private static int CARD_WIDTH = 320;
    private static int CARD_HEIGHT = 180;

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent) {
        context = parent.getContext();

        ImageCardView imageCardView = new ImageCardView(context);
        imageCardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER);
        imageCardView.setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
        imageCardView.setFocusable(true);
        imageCardView.setBackgroundResource(R.color.green);
        imageCardView.setInfoAreaBackgroundColor(ContextCompat.getColor(context, R.color.transparent_40));
        imageCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        imageCardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER);

        TextView textView = ((TextView) imageCardView.findViewById(R.id.title_text));
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(-1);

        return new CardViewHolder(imageCardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof Video) setVideoCard((CardViewHolder) viewHolder, item);
        else if (item instanceof AudioArtist) setAudioArtistCard((CardViewHolder) viewHolder, item);
        else if (item instanceof AudioAlbum) setAudioAlbumCard((CardViewHolder) viewHolder, item);
        else if (item instanceof AudioGenre) setAudioGenreCard((CardViewHolder) viewHolder, item);
        else if (item instanceof Audio) setAudioCard((CardViewHolder) viewHolder, item);
        else if (item instanceof Image) setImageCard((CardViewHolder) viewHolder, item);
        else if (item instanceof UPnPDevice) setUPnPDeviceCard((CardViewHolder) viewHolder, item);
        else if (item instanceof UPnPFile) setUPnPFileCard((CardViewHolder) viewHolder, item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder viewHolder) {
    }

    private void setVideoCard(final CardViewHolder viewHolder, Object item) {
        Video video = (Video) item;
        viewHolder.setVideo(video);

        viewHolder.imageCardView.setTitleText(video.getTitle());

        long duration = Utils.getMediaDuration(context, video.getPath());
        if (duration > 0)
            viewHolder.imageCardView.setContentText(Utils.generateTime(duration));

        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), video.getThumbnail());
    }

    private void setAudioArtistCard(final CardViewHolder viewHolder, Object item) {
        AudioArtist audioArtist = (AudioArtist) item;
        viewHolder.setAudioArtist(audioArtist);

        viewHolder.imageCardView.setTitleText(audioArtist.getArtist());
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), audioArtist.getCover());
    }

    private void setAudioAlbumCard(final CardViewHolder viewHolder, Object item) {
        AudioAlbum audioAlbum = (AudioAlbum) item;
        viewHolder.setAudioAlbum(audioAlbum);

        viewHolder.imageCardView.setTitleText(audioAlbum.getAlbum());
        viewHolder.imageCardView.setContentText(audioAlbum.getArtist());
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), audioAlbum.getCover());
    }

    private void setAudioGenreCard(CardViewHolder viewHolder, Object item) {
        AudioGenre audioGenre = (AudioGenre) item;
        viewHolder.setAudioGenre(audioGenre);

        viewHolder.imageCardView.setTitleText(audioGenre.getGenre());
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), audioGenre.getCover());
    }

    private void setAudioCard(CardViewHolder viewHolder, Object item) {
        Audio audio = (Audio) item;
        viewHolder.setAudio(audio);

        viewHolder.imageCardView.setTitleText(audio.getTitle());
        viewHolder.imageCardView.setContentText(audio.getArtist());
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(),
                Utils.getAlbumCover(context, audio.getPath()));
    }

    private void setImageCard(CardViewHolder viewHolder, Object item) {
        Image image = (Image) item;
        viewHolder.setImage(image);

        viewHolder.imageCardView.setTitleText(image.getTitle());
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), image.getPath());
    }

    private void setUPnPDeviceCard(CardViewHolder viewHolder, Object item) {
        UPnPDevice upnpDevice = (UPnPDevice) item;
        viewHolder.setUPnPDevice(upnpDevice);

        viewHolder.imageCardView.setTitleText(upnpDevice.getTitle());
        viewHolder.imageCardView.setContentText(upnpDevice.getDescription());
        Utils.setFitCenterImage(context, viewHolder.imageCardView.getMainImageView(), upnpDevice.getIconUrl());
    }

    private void setUPnPFileCard(CardViewHolder viewHolder, Object item) {
        UPnPFile upnpFile = (UPnPFile) item;
        viewHolder.setUPnPFile(upnpFile);

        viewHolder.imageCardView.setTitleText(upnpFile.getTitle());

        if (!upnpFile.isContainer())
            viewHolder.imageCardView.setContentText(upnpFile.getDescription2());
        else
            viewHolder.imageCardView.setContentText(upnpFile.getDescription());

        if (upnpFile.getIconUrl() != null)
            Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), upnpFile.getIconUrl());
        else
            Utils.setFitCenterImage(context, viewHolder.imageCardView.getMainImageView(), upnpFile.getIcon());
    }
}
