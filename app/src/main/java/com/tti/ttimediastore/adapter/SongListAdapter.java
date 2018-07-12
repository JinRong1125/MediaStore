package com.tti.ttimediastore.adapter;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.model.Audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/4/12.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {

    private ArrayList<Audio> audioList;

    private FocusHandler focusHandler;

    public SongListAdapter(ArrayList<Audio> audioList) {
        this.audioList = audioList;
        focusHandler = new FocusHandler();
    }

    @Override
    public SongListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SongListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongListViewHolder holder, int position) {
        holder.itemView.setBackgroundResource(R.color.transparent);
        holder.title.setText(audioList.get(position).getTitle());
        holder.artist.setText(audioList.get(position).getArtist());
    }

    @Override
    public void onBindViewHolder(SongListViewHolder holder, int position, List<Object> payloads) {
        if(!payloads.isEmpty()) {
            focusHandler.sendMessage(focusHandler.obtainMessage((Integer) payloads.get(0), holder));
        }
        else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public class SongListViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public TextView title;
        public TextView artist;

        public SongListViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
        }
    }

    private class FocusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SongListViewHolder holder = (SongListViewHolder) msg.obj;
            switch (msg.what) {
                case Constants.FOCUS_ITEM:
                    holder.itemView.setBackgroundResource(R.drawable.focus_design);
                    break;
                case Constants.UNFOCUS_ITEM:
                    holder.itemView.setBackgroundResource(R.color.transparent);
                    break;
            }
        }
    }
}
