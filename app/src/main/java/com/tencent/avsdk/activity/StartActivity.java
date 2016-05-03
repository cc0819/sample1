package com.tencent.avsdk.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;

import com.tencent.TIMManager;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;


/**
 * 应用主界面
 */
public class StartActivity extends TabActivity implements View.OnClickListener {
    private static final String TAG = StartActivity.class.getSimpleName();
    private TabHost tabHost;
    private ImageButton mImageButtonShow;
    private ImageView mImageViewMenuLive;
    private ImageView mImageViewMenuMe;
    private Drawable mDrawableMenuLiveSelected;
    private Drawable mDrawableMenuLiveUnselected;
    private Drawable mDrawableMenuMeSelected;
    private Drawable mDrawableMenuMeUnselected;
    private UserInfo mSelfUserInfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        mImageViewMenuLive = (ImageView) findViewById(R.id.menu_live);
        mImageViewMenuMe = (ImageView) findViewById(R.id.menu_me);
        mImageButtonShow = (ImageButton) findViewById(R.id.image_btn_show);
        mImageViewMenuLive.setOnClickListener(this);
        mImageViewMenuMe.setOnClickListener(this);
        mImageButtonShow.setOnClickListener(this);

        mDrawableMenuLiveSelected = getResources().getDrawable(R.drawable.menu_live_selected);
        mDrawableMenuLiveUnselected = getResources().getDrawable(R.drawable.menu_live_unselected);
        mDrawableMenuMeSelected = getResources().getDrawable(R.drawable.menu_me_selected);
        mDrawableMenuMeUnselected = getResources().getDrawable(R.drawable.menu_me_unselected);
        mSelfUserInfo = ((QavsdkApplication)getApplication()).getMyselfUserInfo();
        initTabHost();
        ((QavsdkApplication)getApplication()).setAppTabHost(tabHost);
    }

    class OnTabChangedListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            updateTab();
        }
    }

    private void initTabHost() {
        tabHost = getTabHost();
        Intent liveIntent = new Intent(this, ProgramListActivity.class);
        Intent centerIntent = new Intent(this, PersonalCenterActivity.class);

        tabHost.addTab(tabHost.newTabSpec("live")
                .setIndicator("看直播")
                .setContent(liveIntent));
        tabHost.addTab(tabHost.newTabSpec("center")
                .setIndicator("个人中心")
                .setContent(centerIntent));
        tabHost.setCurrentTabByTag("live");
        updateTab();
        tabHost.setOnTabChangedListener(new OnTabChangedListener()); // 选择监听器
    }

    private void updateTab() {
        if(tabHost.getCurrentTab() == 0) {
            mImageViewMenuLive.setImageDrawable(mDrawableMenuLiveSelected);
            mImageViewMenuMe.setImageDrawable(mDrawableMenuMeUnselected);
        } else {
            mImageViewMenuLive.setImageDrawable(mDrawableMenuLiveUnselected);
            mImageViewMenuMe.setImageDrawable(mDrawableMenuMeSelected);
        }
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume ");
        TIMManager.getInstance().init(getApplicationContext());
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_live:
                tabHost.setCurrentTabByTag("live");
                break;
            case R.id.menu_me:
                tabHost.setCurrentTabByTag("center");
                break;
            case R.id.image_btn_show:
                startActivity(new Intent(StartActivity.this, ReleaseActivity.class));
//                CrashReport.testJavaCrash();
//                startActivity(new Intent(StartActivity.this, null));
                break;
            default:
                break;
        }
    }
}
