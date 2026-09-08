package com.samp.mobile.launcher.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.joom.paranoid.Obfuscate;
import com.samp.mobile.R;
import com.samp.mobile.launcher.config.Config;
import com.samp.mobile.launcher.util.ButtonAnimator;

@Obfuscate
public class ServerAddFragment extends Dialog {
    private final Activity activity;

    public ServerAddFragment(Activity activity, Object ignoredAdapter) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertdialog_addserver);

        ImageView close = findViewById(R.id.server_close);
        View add = findViewById(R.id.server_add);
        if (add != null) {
            add.setOnTouchListener(new ButtonAnimator(getContext(), add));
            add.setOnClickListener(view -> Toast.makeText(activity,
                    Config.SERVER_NAME + " يستخدم سيرفرًا واحدًا فقط.", Toast.LENGTH_SHORT).show());
        }
        if (close != null) {
            close.setOnTouchListener(new ButtonAnimator(getContext(), close));
            close.setOnClickListener(view -> dismiss());
        }
    }
}
