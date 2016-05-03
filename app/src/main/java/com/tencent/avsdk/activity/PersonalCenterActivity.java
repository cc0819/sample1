package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.avsdk.CircularImageButton;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.ImageConstant;
import com.tencent.avsdk.ImageUtil;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kowalskixu on 2015/8/21.
 */
public class PersonalCenterActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "PersonalCenterActivity";
    private static final int ERROR_INTNET = -1;
    private static final int UPDATE_USER_NAME = 0;
    private static final int UPDATE_USER_SEX = UPDATE_USER_NAME + 1;
    private static final int UPDATE_USER_ADDRESS = UPDATE_USER_SEX + 1;
    private static final int UPDATE_USER_SIGNATURE = UPDATE_USER_ADDRESS + 1;
    private static final int SAVE_DATA_SUCCESS = UPDATE_USER_SIGNATURE + 1;
    private static final int SAVE_HEAD_TO_SERVER = SAVE_DATA_SUCCESS + 1;
    private static final int SAVE_USR_NAME_SUCESS = SAVE_HEAD_TO_SERVER + 1;
    private static final int ERROR_SAVE_NAME = SAVE_USR_NAME_SUCESS + 1;
    private static String IMAGE_FILE_LOCATION = "file:///sdcard/head.jpg";
    private Uri imageUri;
    private Context ctx;
    private ImageButton btnImage;
    private String mHeadPath;
    private Dialog dialog;
    private UserInfo mSelfUserInfo;
    private String userPhone;
    private TextView mUserNameTextView;
    private TextView mUserSexTextView;
    private TextView mUserAddressTextView;
    private TextView mUserSignatureTextView;
    private String newName = "";

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR_INTNET:
                    Toast.makeText(PersonalCenterActivity.this, "上传不成功！！！", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_SAVE_NAME:
                    Toast.makeText(PersonalCenterActivity.this, "保存名字不成功，可能含有非法字符", Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_USER_NAME:
                    String newUserName = (String) msg.obj;
                    if (!newUserName.equals(mSelfUserInfo.getUserName())) {
//                        mUserNameTextView.setText(newUserName);
//                        mSelfUserInfo.setUserName(newUserName);
                        saveUserName(newUserName);
                    }
                    break;
                case UPDATE_USER_SEX:
                    if (mSelfUserInfo.getUserSex() != userSex) {
                        mSelfUserInfo.setUserSex(userSex);
                        setUserSex();
                        saveUserSex();
                    }
                    break;
                case UPDATE_USER_ADDRESS:
                    String newUserAddress = (String) msg.obj;
                    if (!newUserAddress.equals(mSelfUserInfo.getUserAddr())) {
                        mUserAddressTextView.setText(newUserAddress);
                        mSelfUserInfo.setUserAddr(newUserAddress);
                        saveUserAddress(newUserAddress);
                    }
                    break;
                case UPDATE_USER_SIGNATURE:
                    String newUserSignature = (String) msg.obj;
                    if (!newUserSignature.equals(mSelfUserInfo.getUserSignature())) {
                        mUserSignatureTextView.setText(newUserSignature);
                        mSelfUserInfo.setUserSignature(newUserSignature);
                        saveUserSignature(newUserSignature);
                    }
                    break;
                case SAVE_DATA_SUCCESS:
                    Toast.makeText(ctx, "修改成功", Toast.LENGTH_SHORT).show();
                    break;
                case SAVE_HEAD_TO_SERVER:
                    saveHeadTOServer();
                case SAVE_USR_NAME_SUCESS:
                    Toast.makeText(ctx, "修改成功", Toast.LENGTH_SHORT).show();
                    mUserNameTextView.setText(newName);
                    mSelfUserInfo.setUserName(newName);
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_center_activity);
        ctx = this;
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        Button mExitButton = (Button) findViewById(R.id.btn_exit);
        mUserNameTextView = (TextView) findViewById(R.id.user_name);
        mUserSexTextView = (TextView) findViewById(R.id.user_sex);
        mUserAddressTextView = (TextView) findViewById(R.id.user_address);
        mUserSignatureTextView = (TextView) findViewById(R.id.user_signature);
        ImageButton mReturnImageButton = (ImageButton) findViewById(R.id.btn_person_return);
        btnImage = (ImageButton) findViewById(R.id.userimage);
        mSelfUserInfo = ((QavsdkApplication) getApplication()).getMyselfUserInfo();
        mHeadPath = ImageConstant.ROOT_DIR + mSelfUserInfo.getUserPhone() + "_head.jpg";
        userPhone = mSelfUserInfo.getUserPhone();
        IMAGE_FILE_LOCATION = "file:///sdcard/" + userPhone + "_head.jpg";
        imageUri = Uri.parse(IMAGE_FILE_LOCATION);

        setUserHeadImage();
        mUserNameTextView.setText(mSelfUserInfo.getUserName());
        mUserAddressTextView.setText(mSelfUserInfo.getUserAddr().length() > 0 ? mSelfUserInfo.getUserAddr() : "未知");
        mUserSignatureTextView.setText(mSelfUserInfo.getUserSignature());
        setUserSex();

        findViewById(R.id.btn_about).setOnClickListener(this);
        findViewById(R.id.btn_about_label).setOnClickListener(this);
        findViewById(R.id.user_image_relativelayout).setOnClickListener(this);
        findViewById(R.id.user_name_relativelayout).setOnClickListener(this);
        findViewById(R.id.user_sex_relativelayout).setOnClickListener(this);
        findViewById(R.id.user_address_relativelayout).setOnClickListener(this);
        findViewById(R.id.user_signature_relativelayout).setOnClickListener(this);

        mReturnImageButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
        mUserSignatureTextView.setOnClickListener(this);
        btnImage.setOnClickListener(this);
    }

    private void setUserHeadImage() {
        File file = new File(mHeadPath);
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                btnImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void takePhoto() {
        ImageConstant.PhotoClassflag = ImageConstant.HEAD;
        File outputImage = new File(Environment.getExternalStorageDirectory(), "tempImage.jpg");
        File destDir = new File(ImageConstant.ROOT_DIR);
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                Log.d(TAG, "DEBUG mkdirs fail");
            }
        }
        try {
            if (outputImage.exists() && outputImage.delete()) {
                Log.d(TAG, "DEBUG deletedirs success");
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

    private void getUserHeadImage() {
        File outputImage = new File(Environment.getExternalStorageDirectory(), "output.jpg");
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
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageConstant.imageuri);
        startActivityForResult(intent, ImageConstant.SELECT_PHOTO);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
//        intent.putExtra("return-data", true);
        intent.putExtra("return-data", false);
        intent.putExtra("output", imageUri);
        startActivityForResult(intent, ImageConstant.CROP_PHOTO);
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

                    if (imageUri != null) {
                        Bitmap bitmap = decodeUriAsBitmap(imageUri);

                        // 把解析到的位图显示出来
                        if (bitmap != null) {
                            Log.i(TAG, "bitmap:" + bitmap);
                            tool.saveImage(bitmap, mHeadPath);
//
//                            mSelfUserInfo.setHeadImagePath(mHeadPath);
                            mHandler.sendEmptyMessage(SAVE_HEAD_TO_SERVER);

                            btnImage.setImageBitmap(bitmap);
                            btnImage.setVisibility(View.VISIBLE);
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

    private void setUserSex() {
        int sex = mSelfUserInfo.getUserSex();
        switch (sex) {
            case 0:
                mUserSexTextView.setText("未填写");
                break;
            case 1:
                mUserSexTextView.setText("男");
                break;
            case 2:
                mUserSexTextView.setText("女");
                break;
            default:
                break;
        }
    }


    private void saveHeadTOServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    new ImageUtil().sendHeadToServer(mHeadPath, mSelfUserInfo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_person_return:
                finish();
                break;
            case R.id.btn_exit:
                onExit();
                break;
            case R.id.btn_about:
            case R.id.btn_about_label:
                onAbout();
                break;
            case R.id.userimage:
                onUserImageClick();
                break;
            case R.id.user_image_relativelayout:
                onUserImageClick();
                break;
            case R.id.user_name_relativelayout:
                onUserName();
                break;
            case R.id.user_sex_relativelayout:
                onUserSex();
                break;
            case R.id.user_address_relativelayout:
                onUserAddress();
                break;
            case R.id.user_signature_relativelayout:
                onUserSignature();
                break;
            default:
                break;
        }
    }

    private void onUserImageClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("拍照",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        takePhoto();
                    }
                });
        builder.setNeutralButton("照片",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getUserHeadImage();
                    }
                });
        builder.create();
        builder.show();
    }

    private void onUserName() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.input_dialog);
        ((TextView) dialog.findViewById(R.id.input_title)).setText("请编辑用户昵称");
        final EditText inputEditText = (EditText) dialog.findViewById(R.id.input);
        inputEditText.setText(mSelfUserInfo.getUserName());
        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = UPDATE_USER_NAME;
                message.obj = inputEditText.getText().toString();
                newName = inputEditText.getText().toString();
                mHandler.sendMessage(message);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private int userSex;

    private void onUserSex() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.sex_dialog);
        RadioButton femaleBtn = (RadioButton) dialog.findViewById(R.id.female);
        femaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSex = 2;
            }
        });

        RadioButton maleBtn = (RadioButton) dialog.findViewById(R.id.male);
        maleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSex = 1;
            }
        });

        userSex = mSelfUserInfo.getUserSex();
        if (userSex == 2) {
            femaleBtn.setChecked(true);
        } else if (userSex == 1) {
            maleBtn.setChecked(true);
        }

        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(UPDATE_USER_SEX);
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void onUserAddress() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.input_dialog);
        ((TextView) dialog.findViewById(R.id.input_title)).setText("请编辑个人地址");
        final EditText inputEditText = (EditText) dialog.findViewById(R.id.input);
        inputEditText.setText(mSelfUserInfo.getUserAddr());
        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = UPDATE_USER_ADDRESS;
                message.obj = inputEditText.getText().toString();
                mHandler.sendMessage(message);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void onUserSignature() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.input_dialog);
        ((TextView) dialog.findViewById(R.id.input_title)).setText("请编辑个人签名");
        final EditText inputEditText = (EditText) dialog.findViewById(R.id.input);
        inputEditText.setText(mSelfUserInfo.getUserSignature());
        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = UPDATE_USER_SIGNATURE;
                message.obj = inputEditText.getText().toString();
                mHandler.sendMessage(message);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void onExit() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.exit_dialog);
        Button exitOk = (Button) dialog.findViewById(R.id.btn_exit_ok);
        Button exitCancel = (Button) dialog.findViewById(R.id.btn_exit_cancel);

        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelfUserInfo.logout(getApplicationContext());
                System.exit(0);
            }
        });
        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void onAbout() {
        String version = "产品名称：随心播 | 版本号：" + getVersion();
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.about_dialog);

        Button closeAbout = (Button) dialog.findViewById(R.id.close_about);
        ((TextView) dialog.findViewById(R.id.about_message)).setText(version);
        closeAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void saveUserName(final String newUserName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject sendobj = new JSONObject();
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                try {
                    sendobj.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    sendobj.put(Util.EXTRA_USER_NAME, newUserName);

                    list.add(new BasicNameValuePair(Util.JSON_KEY_DATA, sendobj.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save" + sendobj.toString());
                String response = HttpUtil.PostUrl(HttpUtil.modifyFieldUrl, list);
                Log.d(TAG, "save" + response);
                if (response.length() == 0) {
                    mHandler.sendEmptyMessage(ERROR_SAVE_NAME);
                    return;
                }

//                if (!response.startsWith("{")) {
//                    Log.e(TAG, "saveUserName  not start { response is not json style ！！！！ " + response);
//                    mHandler.sendEmptyMessage(ERROR_SAVE_NAME);
//                    return;
//                }
//                if (!response.endsWith("}")) {
//                    Log.e(TAG, "saveUserName response is not json style" + response);
//                    mHandler.sendEmptyMessage(ERROR_SAVE_NAME);
//                    return;
//                }
                JSONTokener json = new JSONTokener(response);

                try {
                    Object oj = json.nextValue();
                    if ((oj) instanceof JSONObject) {
                        JSONObject object = (JSONObject) oj;

                        int ret = object.getInt(Util.JSON_KEY_CODE);
                        if (ret == HttpUtil.SUCCESS || ret == 260) {
                            mHandler.sendEmptyMessage(SAVE_USR_NAME_SUCESS);
                            mSelfUserInfo.setUserName(newUserName);
                            return;
                        }

                    } else {
                        mHandler.sendEmptyMessage(ERROR_SAVE_NAME);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void saveUserSex() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject sendobj = new JSONObject();
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                try {
                    sendobj.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    sendobj.put("sex", userSex);
                    list.add(new BasicNameValuePair(Util.JSON_KEY_DATA, sendobj.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save" + sendobj.toString());
                String response = HttpUtil.PostUrl(HttpUtil.modifyFieldUrl, list);
                Log.d(TAG, "save" + response);
                if (response.length() == 0) {
                    mHandler.sendEmptyMessage(ERROR_INTNET);
                    return;
                }

                if (!response.endsWith("}")) {
                    Log.e(TAG, "saveUserSex response is not json style" + response);
                    return;
                }
                JSONTokener json = new JSONTokener(response);
                try {
                    JSONObject object = (JSONObject) json.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    if (ret == HttpUtil.SUCCESS || ret == 260) {
                        mHandler.sendEmptyMessage(SAVE_DATA_SUCCESS);
                        mSelfUserInfo.setUserSex(userSex);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void saveUserAddress(final String newUserAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject sendobj = new JSONObject();
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                try {
                    sendobj.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    sendobj.put("address", newUserAddress);
                    list.add(new BasicNameValuePair("data", sendobj.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save" + sendobj.toString());
                String response = HttpUtil.PostUrl(HttpUtil.modifyFieldUrl, list);
                Log.d(TAG, "save" + response);
                if (response.length() == 0) {
                    mHandler.sendEmptyMessage(ERROR_INTNET);
                    return;
                }

                if (!response.endsWith("}")) {
                    Log.e(TAG, "saveUserAddress response is not json style" + response);
                    return;
                }
                JSONTokener json = new JSONTokener(response);
                try {
                    JSONObject object = (JSONObject) json.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    if (ret == HttpUtil.SUCCESS || ret == 260) {
                        mHandler.sendEmptyMessage(SAVE_DATA_SUCCESS);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void saveUserSignature(final String newUserSignature) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject sendobj = new JSONObject();
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                try {
                    sendobj.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    sendobj.put("signature", newUserSignature);
                    list.add(new BasicNameValuePair("data", sendobj.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save" + sendobj.toString());
                String response = HttpUtil.PostUrl(HttpUtil.modifyFieldUrl, list);
                Log.d(TAG, "save" + response);
                if (response.length() == 0) {
                    mHandler.sendEmptyMessage(ERROR_INTNET);
                    return;
                }

                if (!response.endsWith("}")) {
                    Log.e(TAG, "saveUserSignature response is not json style" + response);
                    return;
                }
                JSONTokener json = new JSONTokener(response);
                try {
                    JSONObject object = (JSONObject) json.nextValue();
                    int ret = object.getInt(Util.JSON_KEY_CODE);
                    if (ret == HttpUtil.SUCCESS || ret == 260) {
                        mHandler.sendEmptyMessage(SAVE_DATA_SUCCESS);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            int versionCode = info.versionCode;
            return version + ". " + versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}

