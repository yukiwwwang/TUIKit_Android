package com.tencent.uikit.app.main.conference;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.cloud.tuikit.roomkit.ConferenceSession;
import com.tencent.cloud.tuikit.roomkit.R;
import com.tencent.cloud.tuikit.roomkit.view.basic.PrepareView;

public class ConferenceActivity extends AppCompatActivity {
    public static final String INTENT_ENABLE_PREVIEW = "enablePreview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuiroomkit_activity_prepare);
        ViewGroup root = findViewById(R.id.ll_root);
        boolean enablePreview = getIntent().getBooleanExtra(INTENT_ENABLE_PREVIEW, true);
        PrepareView prepareView = new PrepareView(this, enablePreview);
        root.addView(prepareView);
        configureConferenceUI();
    }

    private void configureConferenceUI() {
        ConferenceSession.sharedInstance().enableWaterMark();
    }
}