package com.tencent.avsdk.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播结束界面
 */
public class GameOverActivity extends Activity {
    public static String TAG = "GameOverActivity";
    public final static int ERROR_INTERNET = 0;
    public final static int SHOW_LIVE_INFO = ERROR_INTERNET + 1;
    private TextView mPraiseCountTextView;
    private TextView mViewerCountTextView;
    private int roomNum;
    private int praisecount = 0;
    private int viewercount = 0;
    private boolean hostleave = false;
    private Context ctx;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR_INTERNET:
                    Toast.makeText(ctx, "网络链接错误", Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_LIVE_INFO:
                    mPraiseCountTextView.setText("" + praisecount);
                    mViewerCountTextView.setText("" + viewercount);
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
        setContentView(R.layout.game_over_activity);

        ctx = this;
        mViewerCountTextView = (TextView) findViewById(R.id.viewercount);
        mPraiseCountTextView = (TextView) findViewById(R.id.praisecount);
        TextView title = (TextView) findViewById(R.id.title);
        roomNum = getIntent().getExtras().getInt(Util.EXTRA_ROOM_NUM);
        hostleave = getIntent().getExtras().getBoolean(Util.EXTRA_LEAVE_MODE);
        if (hostleave == true) {
            title.setText("主播已离开");
        }
        getLiveInfo();
        ((Button) findViewById(R.id.return_main)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((QavsdkApplication) getApplication() != null) {
                    if (((QavsdkApplication) getApplication()).getAppTabHost() != null)
                        ((QavsdkApplication) getApplication()).getAppTabHost()
                                .setCurrentTabByTag("live");
                    finish();
                }
            }
        });
    }

    public void getLiveInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                JSONObject object = new JSONObject();
                try {
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    params.add(new BasicNameValuePair("liveinfo", object.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String url = HttpUtil.SERVER_URL + "live_infoget.php";
                String response = HttpUtil.PostUrl(url, params);
                if (0 == response.length()) {
                    mHandler.sendEmptyMessage(ERROR_INTERNET);
                    return;
                }
                Log.e(TAG, response);
                if (!response.endsWith("}")) {
                    Log.e(TAG, "getLiveInfo response is not json style" + response);
                    return;
                }
                JSONTokener jsonTokener = new JSONTokener(response);
                try {
                    JSONObject jsonResult = (JSONObject) jsonTokener.nextValue();
                    int ret = jsonResult.getInt("code");
                    Log.d(TAG, "" + jsonResult.getInt("code") + jsonResult.getString("data"));

                    if (ret == 200) {
                        JSONObject jsonObject = jsonResult.getJSONObject("data");
                        viewercount = jsonObject.getInt("totalnum");
                        praisecount = jsonObject.getInt("praisenum");
                        mHandler.sendEmptyMessage(SHOW_LIVE_INFO);
                    } else {
                        Log.d(TAG, "get live message fail!ret = " + ret);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
