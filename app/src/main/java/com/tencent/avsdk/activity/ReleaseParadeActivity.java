package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.avsdk.ImageConstant;
import com.tencent.avsdk.ImageUtil;
import com.tencent.avsdk.PickView;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 发布预告的界面
 */
public class ReleaseParadeActivity extends Activity implements TextWatcher, View.OnClickListener {
    private static final String TAG = "ReleaseParadeActivity";
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int today;
    private String startTime;
    private int isTomorrow = 0;
    private boolean isOnce = true;

    private LinearLayout mLinearLayout;
    private View timePickerView;
    private TextView mTextViewParadeTime;
    private TextView mTextViewParadeRemainingTime;
    private ImageButton mImageButtonParadeCover;
    private ImageButton mImageButtonCloseParadeCover;
    private String paradeTitle;
    private EditText mEditTextParadeTitle;
    private Button mButtonTimeOk;
    private Button mButtonReleaseParade;
    private String paradeCoverPath;
    private Context ctx = null;
    private QavsdkControl mQavsdkControl;
    private UserInfo mSelfUserInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_parade_activity);
        initViewUI();
    }

    private void initViewUI() {
        mLinearLayout = (LinearLayout)findViewById(R.id.parade_layout);
        mTextViewParadeTime = (TextView) findViewById(R.id.parade_time);
        mTextViewParadeRemainingTime = (TextView) findViewById(R.id.remaining_time);
        mEditTextParadeTitle = (EditText) findViewById(R.id.parade_live_title);
        mEditTextParadeTitle.addTextChangedListener(this);
        mButtonReleaseParade = (Button) findViewById(R.id.btn_release_parade);
        mImageButtonParadeCover = (ImageButton) findViewById(R.id.parade_live_cover);
        mImageButtonCloseParadeCover = (ImageButton) findViewById(R.id.close_parade_live_cover);
        mButtonTimeOk = (Button) findViewById(R.id.btn_parade_time_ok);

        mButtonTimeOk.setOnClickListener(this);
        mButtonReleaseParade.setOnClickListener(this);
        mImageButtonParadeCover.setOnClickListener(this);
        mImageButtonCloseParadeCover.setOnClickListener(this);
        mTextViewParadeTime.setOnClickListener(this);
        mTextViewParadeRemainingTime.setOnClickListener(this);

        ctx = this;
        QavsdkApplication mQavsdkApplication = (QavsdkApplication)getApplication();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        mSelfUserInfo = mQavsdkApplication.getMyselfUserInfo();
        paradeCoverPath = ImageConstant.ROOT_DIR + mSelfUserInfo.getUserPhone() + "_paradecover.jpg";

        Time time = new Time();
        time.setToNow();
        year = time.year;
        month = time.month + 1;
        today = day = time.monthDay;
        hour = time.hour;
        minute = time.minute;
    }

    @Override
    public void onClick(View v) {
        String coverName = null;
        switch (v.getId()) {
            case R.id.parade_live_cover:
                coverName = "tempImage1.jpg";
                takePhoto(coverName);
                break;
            case R.id.btn_release_parade:
                onReleaseParade();
                break;
            case R.id.parade_time:
            case R.id.remaining_time:
                if(isOnce) {
                    onTimePicker();
                    mButtonReleaseParade.setVisibility(View.GONE);
                    isOnce = false;
                }
                Log.d(TAG, "time");
                break;
            case R.id.btn_parade_time_ok:
                isOnce = true;
                settingParadeTime();
                settingRemainingTime();
                mLinearLayout.removeView(timePickerView);
                mButtonTimeOk.setVisibility(View.GONE);
                mButtonReleaseParade.setVisibility(View.VISIBLE);
                break;

            case R.id.close_parade_live_cover:
                mImageButtonCloseParadeCover.setVisibility(View.GONE);
                Drawable drawable = getResources().getDrawable(R.drawable.release_cover);
                mImageButtonParadeCover.setImageDrawable(drawable);
                break;
            default:
                break;
        }
    }

    private Dialog TimeDialog;

    public void onTimePicker() {
        mButtonTimeOk.setVisibility(View.VISIBLE);

        TimeDialog = new Dialog(this, R.style.dialog);
        TimeDialog.setContentView(R.layout.time_picker);

//        LayoutInflater layoutInflater = LayoutInflater.from(this);
//        timePickerView = layoutInflater.inflate(R.layout.time_picker, null);

        PickView hour_pv = (PickView) TimeDialog.findViewById(R.id.hour_pv);
        PickView minute_pv = (PickView) TimeDialog.findViewById(R.id.minute_pv);
        PickView day_pv = (PickView) TimeDialog.findViewById(R.id.day_pv);

        List<String> hours = new ArrayList<String>();
        List<String> minutes = new ArrayList<String>();
        List<String> days = new ArrayList<String>();

        days.add("明天");
        days.add("今天");

        for (int i = 0; i < 24; i++) {
            hours.add(i < 10 ? "0" + i : "" + i);
        }

        for (int i = 0; i < 60; i++) {
            minutes.add(i < 10 ? "0" + i : "" + i);
        }

        hour_pv.setData(hours, hour);
        hour_pv.setOnSelectListener(new PickView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                ReleaseParadeActivity.this.hour = Integer.parseInt(text);
            }
        });
        minute_pv.setData(minutes, minute);
        minute_pv.setOnSelectListener(new PickView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                ReleaseParadeActivity.this.minute = Integer.parseInt(text);
            }
        });

        day_pv.setData(days, isTomorrow == 1 ? 0 : 1);

        day_pv.setOnSelectListener(new PickView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                if (text.equals("明天")) {
                    if(today == day)
                        day++;
                    isTomorrow = 1;
                } else {
                    if(today != day)
                        day--;
                    isTomorrow = 0;
                }
            }
        });

//        LinearLayout.LayoutParams timePickerViewParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, 460);
//        timePickerViewParams.leftMargin = 140;
//        TimeDialog.setLayoutParams(timePickerViewParams);
        TimeDialog.show();
//        mLinearLayout.addView(timePickerView);
    }

    public void swapDays() {
        if (isTomorrow == 1) {
            int dayOfMonth[] = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if (isleepyear()) {
                dayOfMonth[2] = 29;
            }

            //处理日月变换
            if(day == 0) {
                month--;
                day = dayOfMonth[month];
            } else if (day  == dayOfMonth[month] + 1) {
                day = 1;
                month++;
            }

            //处理月年变换（元旦）
            if(month == 0) {
                year--;
                month = 12;
            }else if (month == 13) {
                month = 1;
                year++;
            }
        }
    }

    private boolean isleepyear() {
        if ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0))
            return true;
        return false;
    }

    private void settingParadeTime() {
        String showMonth = null;
        String showDay = null;
        String showHour = null;
        String showMinute = null;
        swapDays();
        if (month < 10) {
            showMonth = "0" + month;
        } else {
            showMonth = "" + month;
        }

        if (day < 10) {
            showDay = "0" + day;
        } else {
            showDay = "" + day;
        }

        if (hour < 10) {
            showHour = "0" + hour;
        } else {
            showHour = "" + hour;
        }

        if (minute < 10) {
            showMinute = "0" + minute;
        } else {
            showMinute = "" + minute;
        }

        //startTime = "" + year + "-"
        startTime = "" + showMonth + "-"
                + showDay + " "
                + showHour + ":"
                + showMinute;
        mTextViewParadeTime.setText(startTime);

    }

    private void settingRemainingTime() {
        int remainingHour;
        int remainingMinute;
        String remainingTime = "";
        Time time = new Time();
        time.setToNow();

        if(isTomorrow == 1) {
            remainingHour = 24 - time.hour + hour;
            remainingMinute = (60 - time.minute + minute) % 60;
        } else {
            remainingHour = hour - time.hour;
            if(remainingHour > 0)
                remainingMinute = (60 - time.minute + minute) % 60;
            else
                remainingMinute = minute - time.minute;
        }

        if(remainingHour > 0) {
            remainingTime = "" + remainingHour + "小时";
            if(remainingMinute > 0) {
                remainingTime += remainingMinute + "分钟后";
            } else {
                remainingTime += "后";
            }
        } else if(remainingHour == 0){
            if(remainingMinute > 0) {
                remainingTime += remainingMinute + "分钟后";
            } else if(remainingMinute == 0) {
                remainingTime = "立即直播";
            } else {
                remainingTime = "时间已过，请重设";
            }
        } else {
            remainingTime = "时间已过!请重设";
        }

        mTextViewParadeRemainingTime.setText(remainingTime);
    }

    public void onReleaseParade() {
        releaseParade();
        finish();
    }

    public void releaseParade() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {
                    object.put(Util.EXTRA_LIVE_TITLE, paradeTitle);
                    object.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    object.put("starttime", "" + year + "-" + startTime + ":00");
                    object.put("imagetype", 2);
                    System.out.println(object.toString());
                    ImageUtil tool = new ImageUtil();
                    String url = "http://203.195.167.34/live_forcastcreate.php";
                    int ret = tool.sendCoverToServer(paradeCoverPath, object, url, "forcastdata");
                    Log.d(TAG, "testhere" + " " + ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "return:" + requestCode + " " + resultCode);
        switch (requestCode) {
            case ImageConstant.SELECT_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    Uri originaluri = data.getData();
                    startPhotoZoom(originaluri);
                }
                break;
            case ImageConstant.TAKE_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    startPhotoZoom(ImageConstant.imageuri);
                }
                break;
            case ImageConstant.CROP_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    final ImageUtil tool = new ImageUtil();
                    Bitmap bitmap = data.getParcelableExtra("data");
                    tool.saveImage(bitmap, paradeCoverPath);
                    mImageButtonParadeCover.setImageBitmap(bitmap);
                    mImageButtonCloseParadeCover.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    public void takePhoto(String name) {
        File outputImage = new File(Environment.getExternalStorageDirectory(), name);
        ImageConstant.PhotoClassflag = ImageConstant.COVER;
        File destDir = new File(ImageConstant.ROOT_DIR);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageConstant.imageuri = Uri.fromFile(outputImage);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageConstant.imageuri);
        startActivityForResult(intent, ImageConstant.TAKE_PHOTO);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, ImageConstant.CROP_PHOTO);
    }

    @Override
    public void afterTextChanged(Editable s) {
        paradeTitle = mEditTextParadeTitle.getText().toString();
        mButtonReleaseParade.setEnabled(paradeTitle != null && paradeTitle.length() > 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
