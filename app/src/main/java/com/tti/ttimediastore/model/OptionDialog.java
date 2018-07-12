package com.tti.ttimediastore.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.tti.ttimediastore.R;

/**
 * Created by allen_kuo on 2016/6/30.
 */
public class OptionDialog extends Dialog {

    private TextView txtMessage;
    private Button yesButton;
    private Button noButton;

    private Context context;

    private String message;
    private View.OnClickListener clickYes;
    private View.OnClickListener clickNo;

    public OptionDialog(Context context, String message, View.OnClickListener clickYes, View.OnClickListener clickNo) {
        super(context);
        this.context = context;
        this.message = message;
        this.clickYes = clickYes;
        this.clickNo = clickNo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_option);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        txtMessage = (TextView) findViewById(R.id.dialog_message);
        txtMessage.setText(message);

        yesButton = (Button) findViewById(R.id.yes_button);
        yesButton.setOnClickListener(clickYes);
        yesButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused)
                    yesButton.setBackgroundResource(R.drawable.focus_design);
                else
                    yesButton.setBackgroundResource(R.color.transparent);
            }
        });

        noButton = (Button) findViewById(R.id.no_button);
        noButton.setOnClickListener(clickNo);
        noButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused)
                    noButton.setBackgroundResource(R.drawable.focus_design);
                else
                    noButton.setBackgroundResource(R.color.transparent);
            }
        });

        yesButton.requestFocus();
    }

    public Button getYesButton() {
        return yesButton;
    }

    public Button getNoButton() {
        return noButton;
    }
}