package com.tti.ttimediastore.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tti.ttimediastore.R;

/**
 * Created by dylan_liang on 2017/6/5.
 */

public class NoNetworkFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_network, container, false);
        return view;
    }
}
