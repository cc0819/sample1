package com.tencent.avsdk.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

import com.tencent.avsdk.R;

/**
 * 发布界面
 */
public class ReleaseActivity extends TabActivity implements View.OnClickListener{
    private TabHost tabHost;
    private TextView mTextViewReleaseLive;
    private TextView mTextViewReleaseParade;
    private ImageButton mImageButtonCloseRelease;
    private View mViewLine0;
    private View mViewLine1;
    private int selectedColor;
    private int unselectedColor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_activity);
        mTextViewReleaseLive = (TextView)findViewById(R.id.release_live);
        mTextViewReleaseParade = (TextView)findViewById(R.id.release_parade);
        mViewLine0 = findViewById(R.id.line0);
        mViewLine1 = findViewById(R.id.line1);
        mImageButtonCloseRelease = (ImageButton)findViewById(R.id.close_release);
        mTextViewReleaseLive.setOnClickListener(this);
        mTextViewReleaseParade.setOnClickListener(this);
        mImageButtonCloseRelease.setOnClickListener(this);
        selectedColor = getResources().getColor(R.color.indicators_color);
        unselectedColor = getResources().getColor(R.color.main_gray);
        initTabHost();
    }

    private void initTabHost(){
        tabHost = getTabHost();
        Intent showIntent = new Intent(this, ReleaseLiveActivity.class);
        Intent paradeIntent = new Intent(this, ReleaseParadeActivity.class);
        tabHost.addTab(tabHost.newTabSpec("show")
                .setIndicator("发布直播")
                .setContent(showIntent));

        tabHost.addTab(tabHost.newTabSpec("parade")
                .setIndicator("发布预告")
                .setContent(paradeIntent));
        tabHost.setCurrentTabByTag("show");
        updateTab();
        tabHost.setOnTabChangedListener(new OnTabChangedListener());
    }

    class OnTabChangedListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            updateTab();
        }
    }

    private void updateTab() {
        if(tabHost.getCurrentTab() == 0) {
            mTextViewReleaseLive.setTextColor(selectedColor);
            mTextViewReleaseParade.setTextColor(unselectedColor);
            mViewLine0.setVisibility(View.VISIBLE);
            mViewLine1.setVisibility(View.INVISIBLE);
        } else {
            mTextViewReleaseLive.setTextColor(unselectedColor);
            mTextViewReleaseParade.setTextColor(selectedColor);
            mViewLine0.setVisibility(View.INVISIBLE);
            mViewLine1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.release_live:
                tabHost.setCurrentTabByTag("show");
                break;
            case R.id.release_parade:
                tabHost.setCurrentTabByTag("parade");
                break;
            case R.id.close_release:
                finish();
                break;
        }
    }
}
