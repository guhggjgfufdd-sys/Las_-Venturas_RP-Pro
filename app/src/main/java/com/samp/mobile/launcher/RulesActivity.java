package com.samp.mobile.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.samp.mobile.R;
import com.samp.mobile.game.SAMP;
import com.samp.mobile.launcher.config.Config;

public class RulesActivity extends AppCompatActivity {
    private CountDownTimer timer;
    private boolean launched;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView countdown = findViewById(R.id.rules_countdown);
        Button continueButton = findViewById(R.id.rules_continue);
        continueButton.setEnabled(false);

        timer = new CountDownTimer(10000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999L) / 1000L;
                countdown.setText("سيتم الدخول إلى " + Config.SERVER_NAME + " خلال " + seconds + " ثوانٍ");
            }

            @Override
            public void onFinish() {
                countdown.setText("تمت قراءة القوانين — أهلاً بك");
                continueButton.setText("الدخول إلى " + Config.SERVER_NAME);
                continueButton.setEnabled(true);
                launchGame();
            }
        }.start();

        continueButton.setOnClickListener(view -> launchGame());
    }

    private void launchGame() {
        if (launched) return;
        launched = true;
        startActivity(new Intent(this, SAMP.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
