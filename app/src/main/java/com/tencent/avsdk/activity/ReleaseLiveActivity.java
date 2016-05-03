package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tencent.TIMGroupManager;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.ImageConstant;
import com.tencent.avsdk.ImageUtil;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * 发布直播的直播
 */
public class ReleaseLiveActivity extends Activity implements TextWatcher, View.OnClickListener {
    private static final String TAG = "ReleaseLiveActivity";
    public static final int MAX_TIMEOUT = 5 * 1000;
    public static final int MSG_CREATEROOM_TIMEOUT = 1;
    public static final int MSG_CREATEROOM_SERVER_OK = 2;
    private static String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";
    private int mCreateRoomErrorCode = AVError.AV_OK;
    private Context ctx = null;
    private QavsdkControl mQavsdkControl;
    private UserInfo mSelfUserInfo;
    private Uri imageUri;
    private QavsdkApplication mQavsdkApplication;
    private EditText mEditTextLiveTitle;
    private ImageButton mImageButtonLiveCover;
    private ImageButton mImageButtonCloseLiveCover;
    private Button mButtonShow;
    private String mLiveTitleString;
    private String coverPath;
    private int roomNum;
    private String userPhone = "123";
    private String groupid;
    private String coverName = "";

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATEROOM_TIMEOUT:
                    if (mQavsdkControl != null) {
                        mQavsdkControl.setCreateRoomStatus(false);
                        mQavsdkControl.setCloseRoomStatus(false);
                        refreshWaitingDialog();
//                        Toast.makeText(ctx, R.string.notify_network_timeout, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_CREATEROOM_SERVER_OK:
                    Log.i(TAG, "CreateRoomStep 6 : jump into AvActivity ");
                    startActivityForResult(new Intent(ReleaseLiveActivity.this, AvActivity.class)
                            .putExtra(Util.EXTRA_ROOM_NUM, roomNum) //room
                            .putExtra(Util.EXTRA_SELF_IDENTIFIER, userPhone)
                            .putExtra(Util.EXTRA_GROUP_ID, groupid), 0);
                    finish();
                default:
                    break;
            }
            return false;
        }
    });

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSelfUserInfo.isCreater() == true) {
                String action = intent.getAction();
                if (action.equals(Util.ACTION_CREATE_ROOM_NUM_COMPLETE)) {
                    //本地加入创建房间
                    createRoom(roomNum);
                } else if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
                    Log.d(TAG, "CreateRoomStep 3 : AVCreateRoom Complete");
                    //加入房间的回调
                    Log.d(TAG, "create room complete");
                    mHandler.removeMessages(MSG_CREATEROOM_TIMEOUT);
                    refreshWaitingDialog();
                    mCreateRoomErrorCode = intent.getIntExtra(
                            Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
                    if (mCreateRoomErrorCode == AVError.AV_OK) {
                        createGroup();
                    } else {
                        //showDialog(DIALOG_CREATE_ROOM_ERROR);
                        Log.e(TAG, "创建房间失败");
                    }
                } else if (action.equals(Util.ACTION_CREATE_GROUP_ID_COMPLETE)) {
                    createLive();
                    //进去直播界面

                } else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_live_activity);
        mQavsdkApplication = (QavsdkApplication) getApplication();
        registerBroadcastReceiver();
        initViewUI();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_ROOM_CREATE_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
        intentFilter.addAction(Util.ACTION_CREATE_GROUP_ID_COMPLETE);
        intentFilter.addAction(Util.ACTION_CREATE_ROOM_NUM_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }

    private void initViewUI() {
        mEditTextLiveTitle = (EditText) findViewById(R.id.live_title);
        mButtonShow = (Button) findViewById(R.id.btn_show);
        mImageButtonLiveCover = (ImageButton) findViewById(R.id.live_cover);
        mImageButtonCloseLiveCover = (ImageButton) findViewById(R.id.close_live_cover);
        mButtonShow.setOnClickListener(this);
        mEditTextLiveTitle.setOnClickListener(this);
        mEditTextLiveTitle.addTextChangedListener(this);
        mImageButtonLiveCover.setOnClickListener(this);
        mImageButtonCloseLiveCover.setOnClickListener(this);

        ctx = this;
        QavsdkApplication mQavsdkApplication = (QavsdkApplication) getApplication();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        mSelfUserInfo = mQavsdkApplication.getMyselfUserInfo();
        userPhone = mSelfUserInfo.getUserPhone();
        coverPath = ImageConstant.ROOT_DIR + userPhone + "_cover.jpg";
        IMAGE_FILE_LOCATION = "file:///sdcard/" + userPhone + "_cover.jpg";
        imageUri = Uri.parse(IMAGE_FILE_LOCATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
        new ImageUtil().saveImage(bm, coverPath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_cover:
                coverName = "tempImage0.jpg";
//                takePhoto(coverName);
                showSelectPhoneForm();
                break;
            case R.id.btn_show:
                mSelfUserInfo.setIsCreater(true);
                Log.i(TAG, "CreateRoomStep 0: onClick");
                createRoomNum();
                break;
            case R.id.close_live_cover:
                mImageButtonCloseLiveCover.setVisibility(View.GONE);
                Drawable drawable = getResources().getDrawable(R.drawable.release_cover);
                mImageButtonLiveCover.setImageDrawable(drawable);
                break;
            default:
                break;
        }
    }


    /**
     * 选择获取照片方式
     */
    private void showSelectPhoneForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("拍照",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        takePhoto(coverName);
                    }
                });
        builder.setNeutralButton("照片",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getPhotoFromGallery();
                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * 加入直播房间
     *
     * @param roomNum 讨论房间号
     */
    private void createRoom(int roomNum) {
        Log.i(TAG, "CreateRoomStep 2: begin create a AVSDK Room ");
        if (Util.isNetworkAvailable(ctx)) {
            int room = roomNum;
            if (mSelfUserInfo.getEnv() == Util.ENV_TEST) {
//                room = Integer.parseInt(mSelfUserInfo.getUserPhone().substring(0, 5));
                room = 14010;
            }
            mQavsdkControl.enterRoom(room);
            mHandler.sendEmptyMessageDelayed(MSG_CREATEROOM_TIMEOUT, MAX_TIMEOUT);
            Toast.makeText(ctx, "正在创建视频房间中...", Toast.LENGTH_SHORT).show();
            refreshWaitingDialog();
        } else {
            Toast.makeText(ctx, getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * IMSDK创建聊天室
     */
    private void createGroup() {
        Log.d(TAG, "CreateRoomStep 4 : Create IMChatRoom");
        ArrayList<String> list = new ArrayList<String>();
        list.add(mSelfUserInfo.getUserName());
        TIMGroupManager.getInstance().createGroup("ChatRoom", list, mEditTextLiveTitle.getText().toString(), new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "create group failed: " + i + " :" + s);
                Toast.makeText(ctx, "创建群失败:" + i + ":" + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "create group succ: " + s);
                groupid = s;
                ctx.sendBroadcast(new Intent(Util.ACTION_CREATE_GROUP_ID_COMPLETE));
            }
        });
    }

    /**
     * 同步个人信息到后台
     */
    public void createLive() {
        Log.i(TAG, "CreateRoomStep 5: upload info to user server ");
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    object.put(Util.EXTRA_USER_PHONE, userPhone);
                    object.put(Util.EXTRA_LIVE_TITLE, mLiveTitleString);
                    object.put(Util.EXTRA_GROUP_ID, groupid);
                    object.put("imagetype", 2);
                    Log.d(TAG, "userServer testhere " + roomNum + " phone " + userPhone + " roomtitle " + mLiveTitleString);
                    System.out.println(object.toString());
                    ImageUtil tool = new ImageUtil();
                    mQavsdkApplication.setRoomName(mLiveTitleString);
                    mQavsdkApplication.setRoomCoverPath(coverPath);
                    Log.i(TAG, "CreateRoomStep 5.1: upload info to user server begin");
                    int ret = tool.sendCoverToServer(coverPath, object, HttpUtil.liveImageUrl, "livedata");
                    Log.i(TAG, "CreateRoomStep 5.1: upload info to user server  end :" + ret);
                    if (ret == 200) {
//                        ctx.sendBroadcast(new Intent(Util.ACTION_INSERT_ROOM_TO_SERVER_COMPLETE));

//                        mQavsdkApplication.setHandleMemberRoomSuccess(true);
                    } else {
                        Log.e(TAG, "testhere " + " " + ret);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        mHandler.sendEmptyMessage(MSG_CREATEROOM_SERVER_OK);
    }

    /**
     * 向后台申请房间号
     */
    public void createRoomNum() {
        Log.i(TAG, "CreateRoomStep 1 : Apply AV room num");
        new Thread() {
            @Override
            public void run() {
                super.run();
                String response = HttpUtil.PostUrl(HttpUtil.createRoomNumUrl, new ArrayList<NameValuePair>());
                Log.d(TAG, "createRoomNum response" + response);
                if (!response.endsWith("}")) {
                    Log.e(TAG, "onSmsRegisterInfo response is not json style" + response);
                    return;
                }
                JSONTokener jsonTokener = new JSONTokener(response);
                try {
                    JSONObject object = (JSONObject) jsonTokener.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    Log.d(TAG, "" + ret);
                    if (ret != HttpUtil.SUCCESS) {
                        Toast.makeText(ctx, "error:" + ret, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject dataJsonObject = object.getJSONObject(Util.JSON_KEY_DATA);
                    roomNum = dataJsonObject.getInt("num");
                    Log.d(TAG, "roomnum = " + roomNum);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //发消息表示已经创建好房间
                ctx.sendBroadcast(new Intent(Util.ACTION_CREATE_ROOM_NUM_COMPLETE));
            }
        }.start();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "return:" + requestCode + " " + resultCode);
        switch (requestCode) {
            //android 5.0 +
            case ImageConstant.SELECT_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    Log.d(TAG, "SELECT_PHOTO onActivityResult " + data.getData());
                    Uri originaluri = data.getData();
                    startPhotoZoom(originaluri);
                }
                break;
            //below android 5.0
            case ImageConstant.TAKE_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    Log.d(TAG, "TAKE_PHOTO onActivityResult " + ImageConstant.imageuri);
                    startPhotoZoom(ImageConstant.imageuri);
                }
                break;
            case ImageConstant.CROP_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    final ImageUtil tool = new ImageUtil();

                    if (imageUri != null) {
                        Bitmap bitmap = decodeUriAsBitmap(imageUri);

                        // 把解析到的位图显示出来
                        if (bitmap != null) {
                            Log.i(TAG, "bitmap:" + bitmap);
                            tool.saveImage(bitmap, coverPath);
                            mImageButtonLiveCover.setImageBitmap(bitmap);
                            mImageButtonCloseLiveCover.setVisibility(View.VISIBLE);
                        } else {
                            Log.e(TAG, "onActivityResult bundle.getParcelable() bitmap is null ");
                        }
                    }

//                    Bitmap bitmap = data.getParcelableExtra("data");


                }
                break;
            default:
                break;
        }
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
//        intent.putExtra("return-data", false);
        intent.putExtra("output", imageUri);

        startActivityForResult(intent, ImageConstant.CROP_PHOTO);
    }

    @Override
    public void afterTextChanged(Editable s) {
        mLiveTitleString = mEditTextLiveTitle.getText().toString();
        mButtonShow.setEnabled(mLiveTitleString != null && mLiveTitleString.length() > 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void refreshWaitingDialog() {

    }

    /**
     * 把Uri 转换成bitmap
     *
     * @param uri
     * @return
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    private void getPhotoFromGallery() {
        File outputImage = new File(Environment.getExternalStorageDirectory(), "output.jpg");
        File destDir = new File(ImageConstant.ROOT_DIR);
        Log.d(TAG, "getPhotoFromGallery " + destDir);
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
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageConstant.imageuri);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            startActivityForResult(intent, ImageConstant.SELECT_PHOTO);
        } else {
            startActivityForResult(intent, ImageConstant.TAKE_PHOTO);
        }

    }
}
