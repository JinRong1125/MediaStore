package com.tti.ttimediastore.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

import com.tti.ttimediastore.R;
import com.tti.ttimediastore.utils.Utils;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.manager.AudioPlayerController;
import com.tti.ttimediastore.model.InfoDialog;
import com.tti.ttimediastore.model.Video;

/**
 * Created by dylan_liang on 2017/4/11.
 */

public class InternetFragment extends Fragment {

    private BackgroundManager backgroundManager;

    private View view;
    private EditText urlText;
    private Button sendButton;

    private boolean isMove = false;

    private MoveHandler moveHandler;

    private AudioPlayerController audioPlayerController;

    private static final int ENABLE_MOVE = 0;
    private static final int DELAY_MOVE = 150;

    private static final int MAX_LINE = 5;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internet, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        initView();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backgroundManager = BackgroundManager.getInstance(getActivity());
        if (!backgroundManager.isAttached())
            backgroundManager.attach(getActivity().getWindow());
    }

    public void onResume() {
        super.onResume();
        backgroundManager.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wallpaper));

        String storeText = Utils.Preferences.getInternetText();
        if (!storeText.equals(urlText.getText().toString()))
            urlText.setText(storeText);
    }

    public void onPause() {
        super.onPause();
        Utils.Preferences.setInternetText(urlText.getText().toString());
    }

    public void onDestroy() {
        super.onDestroy();
        backgroundManager.release();
        if (moveHandler != null)
            moveHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        urlText = (EditText) view.findViewById(R.id.urlText);
        urlText.setText(Utils.Preferences.getInternetText());
        urlText.requestFocus();
        urlText.addTextChangedListener(new TextWatcher() {
            String lastText = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastText = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (urlText.getLineCount() > MAX_LINE) {
                    urlText.setText(lastText);
                    urlText.setSelection(urlText.getText().length());
                }
                else
                    lastText = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean isFocused) {
                if (isFocused)
                    sendButton.setBackgroundResource(R.drawable.focus_design);
                else
                    sendButton.setBackgroundResource(R.color.transparent);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (urlText.getText().toString().equals("") || urlText.getText() == null) {
                    final InfoDialog infodialog = new InfoDialog(getActivity(), getString(R.string.input_error));
                    infodialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                                    || keyCode == KeyEvent.KEYCODE_ENTER
                                    || keyCode == KeyEvent.KEYCODE_BACK
                                    || keyCode == KeyEvent.KEYCODE_ESCAPE
                                    && infodialog.isShowing()) {
                                infodialog.dismiss();
                            }
                            return false;
                        }
                    });
                    infodialog.show();
                }
                else
                    setContentFragment();
            }
        });

        moveHandler = new MoveHandler();
        audioPlayerController = AudioPlayerController.getInstance();
    }

    private void setContentFragment() {
        Video video = new Video();
        video.setTitle(URLUtil.guessFileName(urlText.getText().toString(), null, null));
        video.setPath(urlText.getText().toString());

        Constants.MODEL_TYPE = Constants.INTERNET_TO_VIDEO;

        Intent intent = new Intent(getActivity(), ContentActivity.class);
        intent.putExtra(Constants.ITEM_CONTENT, video);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    private void setMoving() {
        isMove = true;
        moveHandler.sendMessageDelayed(moveHandler.obtainMessage(ENABLE_MOVE), DELAY_MOVE);
    }

    private class MoveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_MOVE:
                    isMove = false;
                    break;
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isMove) {
            setMoving();
            if (keyCode == KeyEvent.KEYCODE_ESCAPE
                    || keyCode == KeyEvent.KEYCODE_BACK) {
                getActivity().finish();
            }
            else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                audioPlayerController.playpause();
            }
            else if (urlText.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        sendButton.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (urlText.getSelectionEnd() == urlText.getText().length())
                            sendButton.requestFocus();
                        break;
                }
            }
            else if (sendButton.isFocused()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        urlText.requestFocus();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        urlText.requestFocus();
                        break;
                }
            }
        }
        return false;
    }
}
