package com.tencent.avsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.tencent.av.sdk.AVConstants;

public class Util {
    private static final String TAG = "Util";
    private static final String PACKAGE = "com.tencent.avsdk";
    //独立模式
    public static int APP_ID_TEXT = 1400001862;
    public static String UID_TYPE = "1019";
    //托管模式
//	public static final String APP_ID_TEXT = "1400001692";
//	public static final String UID_TYPE = "884";

    public static final String ACTION_START_CONTEXT_COMPLETE = PACKAGE
            + ".ACTION_START_CONTEXT_COMPLETE";
    public static final String ACTION_CLOSE_CONTEXT_COMPLETE = PACKAGE
            + ".ACTION_CLOSE_CONTEXT_COMPLETE";
    public static final String ACTION_ROOM_CREATE_COMPLETE = PACKAGE
            + ".ACTION_ROOM_CREATE_COMPLETE";
    public static final String ACTION_CLOSE_ROOM_COMPLETE = PACKAGE
            + ".ACTION_CLOSE_ROOM_COMPLETE";
    public static final String ACTION_SURFACE_CREATED = PACKAGE
            + ".ACTION_SURFACE_CREATED";
    public static final String ACTION_MEMBER_CHANGE = PACKAGE
            + ".ACTION_MEMBER_CHANGE";
    public static final String ACTION_SHOW_VIDEO_MEMBER_INFO=PACKAGE
            + ".ACTION_SHOW_VIDEO_MEMBER_INFO";
    public static final String ACTION_VIDEO_SHOW = PACKAGE
            + ".ACTION_VIDEO_SHOW";
    public static final String ACTION_MEMBER_VIDEO_SHOW = PACKAGE
            + ".ACTION_MEMBER_VIDEO_SHOW";
    public static final String ACTION_REQUEST_MEMBER_VIEW = PACKAGE + ".ACTION_REQUEST_MEMBER_VIEW";

    public static final String ACTION_VIDEO_CLOSE = PACKAGE
            + ".ACTION_VIDEO_CLOSE";
    public static final String ACTION_ENABLE_CAMERA_COMPLETE = PACKAGE
            + ".ACTION_ENABLE_CAMERA_COMPLETE";
    public static final String ACTION_SWITCH_CAMERA_COMPLETE = PACKAGE
            + ".ACTION_SWITCH_CAMERA_COMPLETE";
    public static final String ACTION_OUTPUT_MODE_CHANGE = PACKAGE
            + ".ACTION_OUTPUT_MODE_CHANGE";
    public static final String ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE = PACKAGE
            + ".ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE";

    public static final String ACTION_CREATE_GROUP_ID_COMPLETE = PACKAGE
            + ".ACTION_CREATE_GROUP_ID_COMPLETE";

    public static final String ACTION_CREATE_ROOM_NUM_COMPLETE = PACKAGE
            + ".ACTION_CREATE_ROOM_NUM_COMPLETE";

    public static final String ACTION_INSERT_ROOM_TO_SERVER_COMPLETE = PACKAGE + ".ACTION_INSERT_ROOM_TO_SERVER_COMPLETE";
    public static final String ACTION_INVITE_MEMBER_VIDEOCHAT = PACKAGE + ".ACTION_INVITE_MEMBER_VIDEOCHAT";
    public static final String ACTION_CLOSE_MEMBER_VIDEOCHAT = PACKAGE + ".ACTION_CLOSE_MEMBER_VIDEOCHAT";

    public static final String EXTRA_RELATION_ID = "relationId";
    public static final String EXTRA_AV_ERROR_RESULT = "av_error_result";
    public static final String EXTRA_VIDEO_SRC_TYPE = "videoSrcType";
    public static final String EXTRA_IS_ENABLE = "isEnable";
    public static final String EXTRA_IS_FRONT = "isFront";
    public static final String EXTRA_IDENTIFIER = "identifier";
    public static final String EXTRA_IS_ASKFOR_MEMVIDEO = "askfor_memvideo";
    public static final String EXTRA_SELF_IDENTIFIER = "selfIdentifier";
    public static final String EXTRA_ROOM_ID = "roomId";
    public static final String EXTRA_IS_VIDEO = "isVideo";
    public static final String EXTRA_IDENTIFIER_LIST_INDEX = "QQIdentifier";

    public static final String JSON_KEY_DATA = "data";
    public static final String JSON_KEY_CODE = "code";
    public static final String JSON_KEY_LOGIN_DATA = "logindata";
    public static final String JSON_KEY_VERSION = "version";
    public static final String JSON_KEY_FORCE = "force";
    public static final String JSON_KEY_USER_INFO = "userinfo";

    public static final String EXTRA_LIVE_VIDEO_INFO = "LiveVideoInfo";
    public static final String EXTRA_USER_PHONE = "userphone";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_USER_NAME = "username";
    public static final String EXTRA_USER_SIG = "usersig";
    public static final String EXTRA_SEX = "sex";
    public static final String EXTRA_CONSTELLATION = "constellation";
    public static final String EXTRA_PRAISE_NUM = "praisenum";
    public static final String EXTRA_VIEWER_NUM = "viewernum";
    public static final String EXTRA_SIGNATURE = "signature";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_HEAD_IMAGE_PATH = "headimagepath";
    public static final String EXTRA_GROUP_ID = "groupid";
    public static final String EXTRA_PROGRAM_ID = "programid";
    public static final String EXTRA_ROOM_NUM = "roomnum";
    public static final String EXTRA_LIVE_TITLE = "livetitle";
    public static final String EXTRA_SUBJECT = "subject";
    public static final String EXTRA_LIVEPHONE = "livephone";
    public static final String EXTRA_LEAVE_MODE = "leave_mode";
    public static final String EXTRA_REPLAYID="replayid";
    public static final String EXTRA_RECORDTIME="duration";

    public static final int SHOW_RESULT_CODE = 10000;
    public static final int EDIT_RESULT_CODE = 20000;
    public static final int VIEW_RESULT_CODE = 30000;

    public static final int AUDIO_VOICE_CHAT_MODE = 0;
    public static final int AUDIO_MEDIA_PLAY_RECORD = AUDIO_VOICE_CHAT_MODE + 1;
    public static final int AUDIO_MEDIA_PLAYBACK = AUDIO_MEDIA_PLAY_RECORD + 1;

    public static final int TRUSTEESHIP = 1;
    public static final int INTEGERATE = TRUSTEESHIP + 1;

    public static final int ENV_FORMAL = 0;
    public static final int ENV_TEST = ENV_FORMAL + 1;

    public static ProgressDialog newProgressDialog(Context context, int titleId) {
        ProgressDialog result = new ProgressDialog(context);
        result.setTitle(titleId);
        result.setIndeterminate(true);
        result.setCancelable(false);


        return result;
    }

    public static AlertDialog newErrorDialog(Context context, int titleId) {
        return new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(R.string.error_code_prefix)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }).create();
    }

    public static void switchWaitingDialog(Context ctx,
                                           ProgressDialog waitingDialog, int dialogId, boolean isToShow) {
        if (isToShow) {
            if (waitingDialog == null || !waitingDialog.isShowing()) {
                if (ctx instanceof Activity) {
                    Activity ctx2 = (Activity) ctx;
                    if (ctx2.isFinishing() == true) return;
                    ((Activity) ctx).showDialog(dialogId);
                }
            }
        } else {
            if (waitingDialog != null && waitingDialog.isShowing()) {
                waitingDialog.dismiss();
            }
        }
    }

    /**
     * 网络是否正常
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static int getNetWorkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();

            if (type.equalsIgnoreCase("WIFI")) {
                return AVConstants.NETTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobileInfo != null) {
                    switch (mobileInfo.getType()) {
                        case ConnectivityManager.TYPE_MOBILE:// 手机网络
                            switch (mobileInfo.getSubtype()) {
                                case TelephonyManager.NETWORK_TYPE_UMTS:
                                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                                case TelephonyManager.NETWORK_TYPE_HSDPA:
                                case TelephonyManager.NETWORK_TYPE_HSUPA:
                                case TelephonyManager.NETWORK_TYPE_HSPA:
                                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                case TelephonyManager.NETWORK_TYPE_EHRPD:
                                case TelephonyManager.NETWORK_TYPE_HSPAP:
                                    return AVConstants.NETTYPE_3G;
                                case TelephonyManager.NETWORK_TYPE_CDMA:
                                case TelephonyManager.NETWORK_TYPE_GPRS:
                                case TelephonyManager.NETWORK_TYPE_EDGE:
                                case TelephonyManager.NETWORK_TYPE_1xRTT:
                                case TelephonyManager.NETWORK_TYPE_IDEN:
                                    return AVConstants.NETTYPE_2G;
                                case TelephonyManager.NETWORK_TYPE_LTE:
                                    return AVConstants.NETTYPE_4G;
                                default:
                                    return AVConstants.NETTYPE_NONE;
                            }
                    }
                }
            }
        }

        return AVConstants.NETTYPE_NONE;
    }

    /*
     * 获取网络类型
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的  
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用  
                    return true;
                }
            }
        }
        return false;
    }
}