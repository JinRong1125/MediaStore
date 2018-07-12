package com.tti.ttimediastore.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.tti.ttimediastore.R;

/**
 * Created by allen_kuo on 2016/6/30.
 */
public class InfoDialog extends Dialog {

    private TextView txtMessage;
    private Button button;

    private String message;

    public InfoDialog(Context context, String message) {
        super(context);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_info);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        txtMessage = (TextView) findViewById(R.id.dialog_message);
        txtMessage.setText(message);

        button = (Button) findViewById(R.id.dialog_button);
        button.requestFocus();
    }
}