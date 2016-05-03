package com.tencent.avsdk.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMManager;
import com.tencent.TIMUserStatusListener;
import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

/**
 * Created by kowalskixu on 2015/8/21.
 */
public class ProgramListActivity extends TabActivity implements View.OnClickListener {
    private static final String TAG = "ProgramListActivity";
    private int mLoginErrorCode = AVError.AV_OK;
    private static final int DIALOG_LOGIN = 0;
    private static final int DIALOG_LOGOUT = DIALOG_LOGIN + 1;
    private static final int DIALOG_LOGIN_ERROR = DIALOG_LOGOUT + 1;
    private ProgressDialog mDialogLogin = null;
    private ProgressDialog mDialogLogout = null;

    private Context ctx = null;
    private TabHost tabHost;
    private QavsdkControl mQavsdkControl;
    private UserInfo mSelfUserInfo;

    private TextView mTextViewLiveTab;
    private TextView mTextViewParadeTab;
    private View mViewLine0;
    private View mViewLine1;
    private int selectedColor;
    private int unselectedColor;
    private final static String LIVE_TAB = "livelist";
    private final static String PARADE_TAB = "paradelist";


    private long firstTime = 0;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {
                mLoginErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
                refreshWaitingDialog();
                if (mLoginErrorCode != AVError.AV_OK) {
                    Log.e(TAG, "登录失败");
                    showDialog(DIALOG_LOGIN_ERROR);
                }
                Log.d(TAG, "start context complete");
            } else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {
                mQavsdkControl.setIsInStopContext(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "ProgramListActivity onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.program_list_activity);
        ctx = this;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);

        QavsdkApplication mQavsdkApplication = (QavsdkApplication) getApplication();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        mSelfUserInfo = mQavsdkApplication.getMyselfUserInfo();
        //注册AvSDK信息
        //
        //超级重要的注册
        //

        startContext();

        mTextViewLiveTab = (TextView) findViewById(R.id.live_list);
        mTextViewParadeTab = (TextView) findViewById(R.id.parade_list);
//        mTextViewRecordTab = (TextView) findViewById(R.id.record_list);
        mViewLine0 = findViewById(R.id.line0);
        mViewLine1 = findViewById(R.id.line1);
//        mViewLine2 = findViewById(R.id.line2);
        mTextViewLiveTab.setOnClickListener(this);
        mTextViewParadeTab.setOnClickListener(this);
//        mTextViewRecordTab.setOnClickListener(this);
        selectedColor = getResources().getColor(R.color.indicators_color);
        unselectedColor = getResources().getColor(R.color.main_gray);
        initTabHost();



        TIMManager.getInstance().setUserStatusListener(new TIMUserStatusListener() {
            @Override
            public void onForceOffline() {
                //被踢下线
                Toast.makeText(ProgramListActivity.this, "被同一个账号踢下限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        mQavsdkControl.stopContext();
        mQavsdkControl.setIsInStopContext(false);
    }


    private void initTabHost() {
        tabHost = getTabHost();
        Intent liveIntent = new Intent(this, LiveActivity.class);
        Intent padadeIntent = new Intent(this, ParadeActivity.class);

        TabHost.TabSpec spec1 = tabHost.newTabSpec(LIVE_TAB);
        spec1.setIndicator("最新直播");
        spec1.setContent(liveIntent);

        TabHost.TabSpec spec2 = tabHost.newTabSpec(PARADE_TAB);
        spec2.setIndicator("直播预告");
        spec2.setContent(padadeIntent);


        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.setCurrentTabByTag("livelist");
        updateTab();
        tabHost.setOnTabChangedListener(new OnTabChangedListener()); // 选择监听器
    }

    class OnTabChangedListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            updateTab();
        }
    }

    private void updateTab() {
        if (tabHost.getCurrentTab() == 0) {
            mTextViewLiveTab.setTextColor(selectedColor);
            mTextViewParadeTab.setTextColor(unselectedColor);
            mViewLine0.setVisibility(View.VISIBLE);
            mViewLine1.setVisibility(View.INVISIBLE);
        } else if (tabHost.getCurrentTab() == 1) {
            mTextViewLiveTab.setTextColor(unselectedColor);
            mTextViewParadeTab.setTextColor(selectedColor);
            mViewLine0.setVisibility(View.INVISIBLE);
            mViewLine1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_list:
                tabHost.setCurrentTabByTag(LIVE_TAB);
                break;
            case R.id.parade_list:
                tabHost.setCurrentTabByTag(PARADE_TAB);
                break;
            default:
                break;
        }
    }

    /**
     * qavsdk 注册信息
     */
    private void startContext() {
        Log.i(TAG, "initAVSDKStep -1 : ProgramListActivity " + mQavsdkControl.hasAVContext());
        if (mQavsdkControl.hasAVContext() == false) {
            String phone = mSelfUserInfo.getUserPhone();
            //if (mSelfUserInfo.getLoginType() == Util.TRUSTEESHIP)
            phone = "86-" + phone;
            if (mSelfUserInfo.getUsersig().equals("")) {
//                Toast.makeText(ProgramListActivity.this, "Shit Crash!!!!  ", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Log.e(TAG, "import phone: " + phone + "  Usersig   " + mSelfUserInfo.getUsersig());
            mLoginErrorCode = mQavsdkControl.startContext(
                    phone, mSelfUserInfo.getUsersig());
            Log.e(TAG, "startContext mLoginErrorCode   " + mLoginErrorCode);
            if (mLoginErrorCode != AVError.AV_OK) {
                Log.e(TAG, "startContext mLoginErrorCode   " + mLoginErrorCode);
                showDialog(DIALOG_LOGIN_ERROR);
            }
            refreshWaitingDialog();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_LOGIN:
                dialog = mDialogLogin = Util.newProgressDialog(this,
                        R.string.at_login);
                break;

            case DIALOG_LOGOUT:
                dialog = mDialogLogout = Util.newProgressDialog(this,
                        R.string.at_logout);
                break;

            case DIALOG_LOGIN_ERROR:
                dialog = Util.newErrorDialog(this, R.string.login_failed);
                break;

            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_LOGIN_ERROR:
                ((AlertDialog) dialog)
                        .setMessage(getString(R.string.error_code_prefix)
                                + mLoginErrorCode);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();

                if (secondTime - firstTime > 2000) {
                    firstTime = secondTime;
                    Toast.makeText(this, "再点击一次退出程序", Toast.LENGTH_SHORT).show();
                    return true;
                } else
                    finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void refreshWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.switchWaitingDialog(ctx, mDialogLogin, DIALOG_LOGIN,
                        mQavsdkControl.getIsInStartContext());
                Util.switchWaitingDialog(ctx, mDialogLogout, DIALOG_LOGOUT,
                        mQavsdkControl.getIsInStopContext());
            }
        });
    }
}
