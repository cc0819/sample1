package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.Toast;

import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.LiveVideoInfo;
import com.tencent.avsdk.LiveVideoInfoAdapter;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kowalskixu on 2015/8/25.
 */
public class LiveActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "LiveActivity";
    private int mLoginErrorCode = AVError.AV_OK;
    private static final int DIALOG_LOGIN = 0;
    private static final int DIALOG_LOGOUT = DIALOG_LOGIN + 1;
    private static final int DIALOG_LOGIN_ERROR = DIALOG_LOGOUT + 1;
    private ProgressDialog mDialogLogin = null;
    private ProgressDialog mDialogLogout = null;

    public static final int REFRESH_ING = 0x1;
    public static final int REFRESH_COMPLETE = 0x2;
    public static final int UPDATE_LIVE_TIMER_TASK = 0x4;
    public static final int UPDATE_USER_LOGO = 0x8;

    private Context ctx = null;
    private QavsdkControl mQavsdkControl;
    private UserInfo mSelfUserInfo;
    private int roomNum;
    private String groupId;

    private ListView mListViewLiveList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LiveVideoInfoAdapter mLiveVideoInfoAdapter = null;
    private List<LiveVideoInfo> mLiveList = new ArrayList<LiveVideoInfo>();
    private LiveVideoInfo mChoseLiveVideoInfo = null;


    private long firstTime = 0;
    private final Timer mUpdateLiveListTimer = new Timer();
    private TimerTask mUpdateLiveListTimerTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(UPDATE_LIVE_TIMER_TASK);
        }
    };

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    getLiveVideoList();
                    mLiveVideoInfoAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case REFRESH_ING:
                    mLiveList.clear();
                    mLiveList.addAll((Collection<? extends LiveVideoInfo>) msg.obj);
//                    setLiveHeadImage();
                    mLiveVideoInfoAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_LIVE_TIMER_TASK:
                    getLiveVideoList();
                    break;
                case UPDATE_USER_LOGO:
                    mLiveVideoInfoAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private boolean isFirst = true;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mSelfUserInfo.isCreater() == false) {
                if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
                    Log.d(TAG, "liveAcitivity onReceive ACTION_ROOM_CREATE_COMPLETE");
                    int mCreateRoomErrorCode = intent.getIntExtra(
                            Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
                    if (isFirst) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isFirst = false;
                    }

                    if (mCreateRoomErrorCode == AVError.AV_OK) {
                        if (mChoseLiveVideoInfo == null) {
//                            Toast.makeText(LiveActivity.this, "mChoseLiveVideoInfo is null !!!! ", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "LiveActivity onReceive mChoseLiveVideoInfo is " + mChoseLiveVideoInfo);
                            return;
                        }
                        startActivity(new Intent(LiveActivity.this, AvActivity.class)
                                .putExtra(Util.EXTRA_ROOM_NUM, roomNum) //room id
                                .putExtra(Util.EXTRA_SELF_IDENTIFIER, mChoseLiveVideoInfo.getUserPhone())
                                .putExtra(Util.EXTRA_GROUP_ID, groupId) // chat converse id
                                .putExtra(Util.EXTRA_PRAISE_NUM, mChoseLiveVideoInfo.getLivePraiseCount()));
                        enterRoom();
                    }
                } else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {

                }
                Log.d(TAG, "WL_DEBUG ANR  onReceive action = " + action + " Out");
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_activity);
        ctx = this;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_ROOM_CREATE_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);

        QavsdkApplication mQavsdkApplication = (QavsdkApplication) getApplication();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        mSelfUserInfo = mQavsdkApplication.getMyselfUserInfo();
        initLiveListView();
        getLiveVideoList();
//        TIMManager.getInstance().addMessageListener(msgListener);
//        mUpdateLiveListTimer.schedule(mUpdateLiveListTimerTask, 15000, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
//        TIMManager.getInstance().removeMessageListener(msgListener);
        mUpdateLiveListTimer.cancel();
    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }

    private void initLiveListView() {
        mListViewLiveList = (ListView) findViewById(R.id.list_view_live_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_list);

        mLiveVideoInfoAdapter = new LiveVideoInfoAdapter(getBaseContext(), R.layout.live_item, mLiveList);
        mListViewLiveList.setAdapter(mLiveVideoInfoAdapter);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        mListViewLiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChoseLiveVideoInfo = mLiveList.get(position);
                if (mChoseLiveVideoInfo != null && mChoseLiveVideoInfo.getUserPhone().equals(mSelfUserInfo.getUserPhone())) {
                    Toast.makeText(LiveActivity.this, "you can't join a not exist room", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSelfUserInfo.setIsCreater(false);
                roomNum = mChoseLiveVideoInfo.getProgrammId();
                groupId = mChoseLiveVideoInfo.getLiveGroupId();

//                createRoom(224039892);
                createRoom(roomNum);
                Log.d(TAG, "you click item " + position + "roomid " + mChoseLiveVideoInfo.getProgrammId());
            }
        });
    }

    private void getLiveVideoList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response = HttpUtil.PostUrl(HttpUtil.getLiveListUrl, new ArrayList<NameValuePair>());
//                Log.d(TAG, "getLiveVideoList response  " + response);
                if (HttpUtil.FAIL == response.length()) {
                    Log.e(TAG, "response's length is 0");
                    return;
                }
                if(!response.endsWith("}")){
                    Log.e(TAG, "run response is not json style"+ response);
                    return;
                }

                JSONTokener jsonTokener = new JSONTokener(response);
//                Log.d(TAG, "getLiveVideoList response jsonTokener " + jsonTokener.toString() );
                try {
                    JSONObject object = (JSONObject) jsonTokener.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    if (ret != HttpUtil.SUCCESS) {
                        Toast.makeText(ctx, "error: " + ret, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray array = object.getJSONArray(Util.JSON_KEY_DATA);
                    Message message = new Message();
                    List<LiveVideoInfo> tmplist = new ArrayList<LiveVideoInfo>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jobject = array.getJSONObject(i);
                        Log.i(TAG, "getLiveVideoList title:" + jobject.getString("subject") +
                                "name: " + jobject.getString("username") +
                                "id:" + jobject.getInt("programid") +
                                "loop:" + i);

                        LiveVideoInfo item = new LiveVideoInfo(jobject.getInt(Util.EXTRA_PROGRAM_ID),
                                jobject.getString(Util.EXTRA_SUBJECT), R.drawable.user,
                                jobject.getInt(Util.EXTRA_VIEWER_NUM), jobject.getInt(Util.EXTRA_PRAISE_NUM),
                                new UserInfo(jobject.getString(Util.EXTRA_USER_PHONE),
                                        jobject.getString(Util.EXTRA_USER_NAME),
                                        jobject.getString("headimagepath")),
                                jobject.getString(Util.EXTRA_GROUP_ID), "12345");
                        item.setCoverpath(jobject.getString("coverimagepath"));
                        Log.d(TAG, "getLiveVideoList "+ item.getUserName()+" listsize "+tmplist.size());
                        tmplist.add(item);
                    }
                    message.what = REFRESH_ING;
                    message.obj = tmplist;
                    mHandler.sendMessage(message);
//                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //有吗
    private void enterRoom() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {
//                    object.put(Util.EXTRA_ROOM_NUM, 224039892);
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    object.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    System.out.println(object.toString());
                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("viewerdata", object.toString()));
                    String ret = HttpUtil.PostUrl(HttpUtil.enterRoomUrl, list);
                    Log.d(TAG, "enter room" + ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void createRoom(int num) {
        if (Util.isNetworkAvailable(getApplicationContext())) {
            if (num != 0) {
                int nums = num;
                if (mSelfUserInfo.getEnv() == Util.ENV_TEST)
                    nums = Integer.parseInt(mSelfUserInfo.getUserPhone().substring(0, 5));
                Toast.makeText(ctx, "正在进入直播中...", Toast.LENGTH_LONG).show();
                mQavsdkControl.enterRoom(nums);
//                Toast.makeText(ctx, "正在进入直播中...", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ctx, getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    public void setLiveHeadImage() {
        mHandler.sendEmptyMessage(UPDATE_USER_LOGO);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean isChange = false;
//                ImageUtil tool = new ImageUtil();
//                for (int i = 0; i < mLiveList.size(); ++i) {
//                    if (mLiveList.get(i).getUserHeadImage() == null) {
//                        String param = "?imagepath=" + mLiveList.get(i).getHeadImagePath() + "&width=0&height=0";
//                        Bitmap headBitmap = tool.getImageFromServer(param);
//                        mLiveList.get(i).setUserHeadImage(headBitmap);
//                        isChange = true;
//                    }
//                }
//                if (isChange) {
//                    mHandler.sendEmptyMessage(UPDATE_USER_LOGO);
//                }
//            }
//        }).start();
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

    }


    private boolean isTopActivity() {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains(TAG)) {
            isTop = true;
        }
        return isTop;
    }
}
