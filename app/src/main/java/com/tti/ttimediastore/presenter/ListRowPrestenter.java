package com.tti.ttimediastore.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.model.Audio;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Option;
import com.tti.ttimediastore.model.Video;
import com.tti.ttimediastore.viewholder.CardViewHolder;

/**
 * Created by dylan_liang on 2017/4/7.
 */

public class ListRowPrestenter extends Presenter {

    private Context context;

    private static final int GRID_ITEM_WIDTH = 320;
    private static final int GRID_ITEM_HEIGHT = 240;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        context = parent.getContext();

        ImageCardView cardView = new ImageCardView(context);
        cardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER);
        cardView.setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ACTIVATED);
        cardView.setFocusable(true);
        cardView.setInfoAreaBackgroundColor(context.getResources().getColor(R.color.transparent_40));
        cardView.setMainImageDimensions(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT);
        cardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER);

        TextView textView = ((TextView) cardView.findViewById(R.id.title_text));
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(-1);

        return new CardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof Video) setVideoCard((CardViewHolder) viewHolder, item);
        else if (item instanceof Audio) setAudioCard((CardViewHolder) viewHolder, item);
        else if (item instanceof Image) setImageCard((CardViewHolder) viewHolder, item);
        else if (item instanceof Option) setOptionCard((CardViewHolder) viewHolder, item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }

    private void setOptionCard(CardViewHolder viewHolder, Object item) {
        Option option = (Option) item;
        viewHolder.setOption(option);
        String name = option.getName();

        ImageCardView imageCardView = viewHolder.imageCardView;

        imageCardView.setTitleText(name);
        imageCardView.setContentText(null);

        if (name.equals(context.getString(R.string.video_all)))
            setOptionImage(imageCardView, R.drawable.video_icon, R.color.blue);
        else if (name.equals(context.getString(R.string.audio_artists)))
            setOptionImage(imageCardView, R.drawable.artist_icon, R.color.red);
        else if (name.equals(context.getString(R.string.audio_albums)))
            setOptionImage(imageCardView, R.drawable.album_icon, R.color.red);
        else if (name.equals(context.getString(R.string.audio_genres)))
            setOptionImage(imageCardView, R.drawable.genre_icon, R.color.red);
        else if (name.equals(context.getString(R.string.audio_all)))
            setOptionImage(imageCardView, R.drawable.audio_icon, R.color.red);
        else if (name.equals(context.getString(R.string.image_all)))
            setOptionImage(imageCardView, R.drawable.image_icon, R.color.orange);
        else if (name.equals(context.getString(R.string.other_lan)))
            setOptionImage(imageCardView, R.drawable.upnp_icon, R.color.purple);
        else if (name.equals(context.getString(R.string.other_internet)))
            setOptionImage(imageCardView, R.drawable.internet_icon, R.color.purple);
        else if (name.equals(context.getString(R.string.other_preferences)))
            setOptionImage(imageCardView, R.drawable.preference_icon, R.color.purple);
        else if (name.equals(context.getString(R.string.video_delete)))
            setOptionImage(imageCardView, R.drawable.delete_icon, R.color.purple);
    }

    private void setOptionImage(ImageCardView cardView, int icon, int color) {
        Utils.setFitCenterImage(context, cardView.getMainImageView(), icon);
        cardView.setBackgroundResource(color);
    }

    private void setVideoCard(CardViewHolder viewHolder, Object item) {
        Video video = (Video) item;
        viewHolder.setVideo(video);

        viewHolder.imageCardView.setTitleText(video.getTitle());
        viewHolder.imageCardView.setContentText(
                context.getString(R.string.record_start) + Utils.generateTime(video.getRecord()));
        viewHolder.imageCardView.setBackgroundResource(R.color.blue);
        viewHolder.imageCardView.getMainImageView().setImageResource(R.color.blue);
        setRecordFrame(viewHolder.imageCardView.getMainImageView(), video);
    }

    private void setAudioCard(CardViewHolder viewHolder, Object item) {
        Audio audio = (Audio) item;
        viewHolder.setAudio(audio);

        viewHolder.imageCardView.setTitleText(audio.getTitle());

        if (audio.getIsPlaying())
            viewHolder.imageCardView.setContentText(context.getResources().getString(R.string.audio_playing));
        else
            viewHolder.imageCardView.setContentText(context.getResources().getString(R.string.audio_pause));

        viewHolder.imageCardView.setBackgroundResource(R.color.red);
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(),
                Utils.getAlbumCover(context, audio.getPath()));
    }

    private void setImageCard(CardViewHolder viewHolder, Object item) {
        Image image = (Image) item;
        viewHolder.setImage(image);

        viewHolder.imageCardView.setTitleText(image.getTitle());
        viewHolder.imageCardView.setContentText(null);
        viewHolder.imageCardView.setBackgroundResource(R.color.orange);
        Utils.setCenterCropImage(context, viewHolder.imageCardView.getMainImageView(), image.getPath());
    }

    private void setRecordFrame(ImageView imageView, Video video) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, Uri.parse(video.getPath()));
            new getRecordFrame().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    mediaMetadataRetriever, imageView, video.getRecord() * 1000);
        }
        catch (Exception e) {
            e.printStackTrace();
            Utils.setCenterCropImage(context, imageView, video.getThumbnail());
        }
    }

    private class getRecordFrame extends AsyncTask<Object, Void, Void> {
        MediaMetadataRetriever mediaMetadataRetriever;
        ImageView imageView;
        long position;

        Bitmap frameBitmap;

        @Override
        protected Void doInBackground(Object... params) {
            mediaMetadataRetriever = (MediaMetadataRetriever) params[0];
            imageView = (ImageView) params[1];
            position = (Long) params[2];
            frameBitmap = mediaMetadataRetriever.getFrameAtTime(position, MediaMetadataRetriever.OPTION_CLOSEST);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            Utils.setCenterCropImage(context, imageView, frameBitmap);
        }
    }
}
