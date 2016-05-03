package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.avsdk.CircularImageButton;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.ParadeVideoInfo;
import com.tencent.avsdk.ParadeVideoInfoAdapter;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import org.apache.http.NameValuePair;
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
public class ParadeActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "ParadeActivity";
    public  static final int REFRESH_ING = 0x1;
    public  static final int REFRESH_COMPLETE = 0x2;
    public  static final int UPDATE_REMAINING_TIMER_TASK = 0x4;
    private Context ctx = null;
    private ListView mListViewParadeList;
    private SwipeRefreshLayout mSwipeRefreshLayoutParade;
    private CircularImageButton mCircularImageBtnUserLogo;
    private ParadeVideoInfoAdapter mParadeVideoInfoAdapter = null;
    private List<ParadeVideoInfo> mParadeList = new ArrayList<ParadeVideoInfo>();
    private ParadeVideoInfo mChoseParadeVideoInfo;
    private Dialog dialog;

    private final Timer mUpdateParadeRemainingTimer = new Timer();
    private TimerTask mUpdateParadeRemainingTimerTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(UPDATE_REMAINING_TIMER_TASK);
        }
    };

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    getParadeVideoInfo();
                    //mParadeVideoInfoAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayoutParade.setRefreshing(false);
                    break;
                case REFRESH_ING:
                    mParadeList.clear();
                    mParadeList.addAll((Collection<? extends ParadeVideoInfo>) msg.obj);
                    setRemainingTime();
                    mParadeVideoInfoAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_REMAINING_TIMER_TASK:
                    refreshRemainingTime();
                    mParadeVideoInfoAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parade_activity);
        ctx = this;
        initParadeListView();
        mUpdateParadeRemainingTimer.schedule(mUpdateParadeRemainingTimerTask, 1000, 2000);
        getParadeVideoInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUpdateParadeRemainingTimer.cancel();
    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
    }

    private void initParadeListView() {
        mListViewParadeList = (ListView)findViewById(R.id.list_view_progarm_parade_list);
        mSwipeRefreshLayoutParade = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_parade);
        mCircularImageBtnUserLogo = (CircularImageButton) findViewById(R.id.image_btn_parade_user_logo);
        mParadeVideoInfoAdapter = new ParadeVideoInfoAdapter(getBaseContext(), R.layout.parade_item, mParadeList);
        mListViewParadeList.setAdapter(mParadeVideoInfoAdapter);
        mSwipeRefreshLayoutParade.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayoutParade.setOnRefreshListener(this);

        mListViewParadeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChoseParadeVideoInfo = mParadeList.get(position);
                showRemainingTimeDialog();
                Log.d(TAG, "you click item " + position + "time " + mChoseParadeVideoInfo.getStartTime());
            }
        });
    }

    private void getParadeVideoInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://203.195.167.34/live_forcastlist.php";
                String response = HttpUtil.PostUrl(url, new ArrayList<NameValuePair>());
                Log.d(TAG, "response" + response);
                if (HttpUtil.FAIL == response.length()) {
                    Log.e(TAG, "response's length is 0");
                    return;
                }
                if(!response.endsWith("}")){
                    Log.e(TAG, "getParadeVideoInfo response is not json style"+ response);
                    return ;
                }
                JSONTokener jsonTokener = new JSONTokener(response);
                try {
                    JSONObject object = (JSONObject) jsonTokener.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    if (ret != HttpUtil.SUCCESS) {
                        Toast.makeText(ctx, "error:" + ret, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray array = object.getJSONArray(Util.JSON_KEY_DATA);
                    Message message = new Message();
                    List<ParadeVideoInfo> tmplist = new ArrayList<ParadeVideoInfo>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jobject = array.getJSONObject(i);

                        ParadeVideoInfo item = new ParadeVideoInfo(jobject.getString(Util.EXTRA_USER_PHONE),
                                jobject.getString(Util.EXTRA_USER_NAME),
                                jobject.getString("headimagepath"),
                                jobject.getInt("roomnum"),
                                jobject.getString(Util.EXTRA_SUBJECT),
                                jobject.getString("coverimagepath"),
                                jobject.getString("starttime"));
                        tmplist.add(item);
                    }
                    message.what = REFRESH_ING;
                    message.obj = tmplist;
                    mHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refreshRemainingTime() {
        for(int i = 0; i < mParadeList.size(); ++i) {
            mParadeList.get(i).decRemainingTime();
        }
    }

    private void setRemainingTime() {
        for(int i = 0; i < mParadeList.size(); ++i) {
          computRemainingTime(mParadeList.get(i));
        }
    }

    private void computRemainingTime(ParadeVideoInfo mParadeVideoInfo) {
        int remainingHour;
        int remainingMinute;
        int remainingSecond;
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        String startTime = mParadeVideoInfo.getStartTime();
        //2016-08-27 15:14:00
        year = Integer.parseInt(startTime.substring(0, 4));
        month = Integer.parseInt(startTime.substring(5, 7));
        day = Integer.parseInt(startTime.substring(8, 10));
        hour = Integer.parseInt(startTime.substring(11, 13));
        minute = Integer.parseInt(startTime.substring(14, 16));
        second = Integer.parseInt(startTime.substring(17, 19));

        Time time = new Time();
        time.setToNow();
        Log.d(TAG, "remainintimeyear" + time.year + "/"+ time.month + "/"+ time.monthDay + "/" + time.hour+ "/" + time.minute + "/" + time.second);
        Log.d(TAG, "remaininyear" + year + "/" + month + "/" + day + "/" + hour + "/" + minute + "/");

        if(year > time.year || month > time.month + 1 || day > time.monthDay) {
            remainingHour = 24 - time.hour + hour;
            remainingMinute = (60 - time.minute + minute) % 60;
        } else {
            remainingHour = hour - time.hour;
            if(remainingHour > 0)
                remainingMinute = (60 - time.minute + minute) % 60;
            else
                remainingMinute = minute - time.minute;
        }
        remainingSecond = 60 - time.second + second;

        remainingMinute += remainingSecond / 60;
        remainingSecond %= 60;
        mParadeVideoInfo.setRemainingTime(remainingHour, remainingMinute, remainingSecond);
    }

    private void showRemainingTimeDialog() {
        dialog = new Dialog(getParent().getParent(), R.style.dialog);
        dialog.setContentView(R.layout.remaining_time_dialog);
        ((TextView)dialog.findViewById(R.id.user_name)).setText(mChoseParadeVideoInfo.getUserName());
        ((TextView)dialog.findViewById(R.id.remaining_timer)).setText(mChoseParadeVideoInfo.getRemainingTimeDigitString());
        QavsdkApplication mQavsdkApplication = (QavsdkApplication)getApplication();
        ((TextView) dialog.findViewById(R.id.praise_count)).setText("" + mQavsdkApplication.getMyselfUserInfo().getPraiseCount());
        ImageButton btnCloseDialog = (ImageButton) dialog.findViewById(R.id.close_remaining_time_dialog);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog .setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private long firstTime = 0;
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
}
