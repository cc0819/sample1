package com.tencent.avsdk.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.TIMCallBack;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupManager;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMGroupSystemElemType;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMTextElem;
import com.tencent.TIMValueCallBack;
import com.tencent.av.TIMAvManager;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVView;
import com.tencent.av.utils.PhoneStatusTools;
import com.tencent.avsdk.ChatEntity;
import com.tencent.avsdk.ChatMsgListAdapter;
import com.tencent.avsdk.CircularImageButton;
import com.tencent.avsdk.DemoConstants;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.ImageUtil;
import com.tencent.avsdk.MemberInfo;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 直播界面
 */
public class AvActivity extends Activity implements OnClickListener {
    private static final String TAG = "AvActivity";
    private static final String UNREAD = "0";
    private static final int PRIASE_MSG = 1;
    private static final int MEMBER_ENTER_MSG = 2;
    private static final int MEMBER_EXIT_MSG = 3;
    private static final int VIDEOCHAT_INVITE = 4;
    private static final int YES_I_JOIN = 5;
    private static final int NO_I_REFUSE = 6;
    private static final int MUTEVOICE = 7;
    private static final int UNMUTEVOICE = 8;
    private static final int MUTEVIDEO = 9;
    private static final int UNMUTEVIDEO = 10;
    private static final int CLOSEVIDEOSEND = 11;

    private static final int DIALOG_INIT = 0;
    private static final int DIALOG_AT_ON_CAMERA = DIALOG_INIT + 1;
    private static final int DIALOG_ON_CAMERA_FAILED = DIALOG_AT_ON_CAMERA + 1;
    private static final int DIALOG_AT_OFF_CAMERA = DIALOG_ON_CAMERA_FAILED + 1;
    private static final int DIALOG_OFF_CAMERA_FAILED = DIALOG_AT_OFF_CAMERA + 1;
    private static final int DIALOG_AT_SWITCH_FRONT_CAMERA = DIALOG_OFF_CAMERA_FAILED + 1;
    private static final int DIALOG_SWITCH_FRONT_CAMERA_FAILED = DIALOG_AT_SWITCH_FRONT_CAMERA + 1;
    private static final int DIALOG_AT_SWITCH_BACK_CAMERA = DIALOG_SWITCH_FRONT_CAMERA_FAILED + 1;
    private static final int DIALOG_SWITCH_BACK_CAMERA_FAILED = DIALOG_AT_SWITCH_BACK_CAMERA + 1;
    private static final int DIALOG_DESTROY = DIALOG_SWITCH_BACK_CAMERA_FAILED + 1;

    private static final int ERROR_MESSAGE_TOO_LONG = 0x1;
    private static final int ERROR_ACCOUNT_NOT_EXIT = ERROR_MESSAGE_TOO_LONG + 1;

    private static final int REFRESH_CHAT = 0x100;
    private static final int UPDAT_WALL_TIME_TIMER_TASK = REFRESH_CHAT + 1;
    private static final int REMOVE_CHAT_ITEM_TIMER_TASK = UPDAT_WALL_TIME_TIMER_TASK + 1;
    private static final int UPDAT_MEMBER = REMOVE_CHAT_ITEM_TIMER_TASK + 1;
    private static final int MEMBER_EXIT_COMPLETE = UPDAT_MEMBER + 1;
    private static final int CLOSE_VIDEO = MEMBER_EXIT_COMPLETE + 1;
    private static final int START_RECORD = CLOSE_VIDEO + 1;
    private static final int IM_HOST_LEAVE = START_RECORD + 1;
    private static final int GET_ROOM_INFO = IM_HOST_LEAVE + 1;
    private static final int REFRESH_PRAISE = GET_ROOM_INFO + 1;

    private boolean mIsPaused = false;
    private boolean mIsClicked = false;
    private boolean mIsSuccess = false;
    private boolean mpush = false;
    private boolean mRecord = false;
    private int mOnOffCameraErrorCode = AVError.AV_OK;
    private int mSwitchCameraErrorCode = AVError.AV_OK;

    private ProgressDialog mDialogInit = null;
    private ProgressDialog mDialogAtOnCamera = null;
    private ProgressDialog mDialogAtOffCamera = null;
    private ProgressDialog mDialogAtSwitchFrontCamera = null;
    private ProgressDialog mDialogAtSwitchBackCamera = null;
    private ProgressDialog mDialogAtDestroy = null;
    private String videoRecordId = "";
    private ListView mListViewMsgItems;
    private EditText mEditTextInputMsg;
    private Button mButtonSendMsg;
    private InputMethodManager mInputKeyBoard;
    private TIMConversation mConversation;
    private TIMConversation mSystemConversation, testConversation;
    private List<ChatEntity> mArrayListChatEntity;
    private ChatMsgListAdapter mChatMsgListAdapter;
    private final int MAX_PAGE_NUM = 10;
    private int mLoadMsgNum = MAX_PAGE_NUM;
    private boolean bNeverLoadMore = true;
    private boolean bMore = true;
    private boolean mIsLoading = false;
    private boolean FormalEnv = true;
    private QavsdkControl mQavsdkControl;
    private String mRecvIdentifier = "";
    private String mHostIdentifier = "";
    OrientationEventListener mOrientationEventListener = null;
    int mRotationAngle = 0;
    private PowerManager.WakeLock wakeLock;
    private Context ctx;
    private UserInfo mSelfUserInfo;
    private int roomNum;
    private String groupId;
    private boolean mChecked = false;
    private int StreamType = 2;
    private int StreamTypeCode = DemoConstants.HLS;
    private Dialog mVideoMemberInfoDialog;

    ArrayList<MemberInfo> mMemberList, mVideoMemberList, mNormalMemberList;
    private GridView mGridView;
    private static final int slength = 40;
    private float density;
    private QavsdkApplication mQavsdkApplication;
    private int groupForPush;
    private int praiseNum;
    private TextView mPraiseNum, mMemberListButton;

    private ImageButton mButtonPraise;
    private TextView mClockTextView;
    private long second = 0;
    private long time;
    private int mMemberVideoCount = 0;
    private AVView mRequestViewList[] = null;
    private String mRequestIdentifierList[] = null;
    private int stop = 1;
    private Timer mVideoTimer;
    private Timer mChatTimer;
    private Timer mHeartClickTimer;
    private VideoTimerTask mVideoTimerTask;
    private ChatTimerTask mChatTimerTask;
    private long streamChannelID;
    private static boolean LEVAE_MODE = false;
    private static boolean hasPullMemberList = false;
    //    private MemberListAdapter memberAdapter;
    private MemberListDialog mMemberListDialog;
    private ArrayAdapter spinnerAdapter;
    private static final String[] SDKtype = {"普通开发SDK业务", "普通物联网摄像头SDK业务", "滨海摄像头SDK业务"};
    private static final int MAX_REQUEST_VIEW_COUNT = 3;//当前最大支持请求画面个数
    private String selectIdentier = "";
    private FrameLayout mInviteMastk1, mInviteMastk2, mInviteMastk3;
    private ImageView mVideoHead1, mVideoHead2, mVideoHead3;
    private CircularImageButton hostHead;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private MemberInfo hostMember = new MemberInfo();
    private Boolean OpenVoice = false;
    private int mMaskViewCount = 0;
    private HashMap<String, Integer> viewIndex = new HashMap<String, Integer>();
    //正在发起请求的id
    private ArrayList<String> requestId = new ArrayList<String>();


    EditText pushfileNameInput, pushClassInput, pushInfoInput, pushCodeInput;

    private class VideoTimerTask extends TimerTask {
        public void run() {
            ++second;
            mHandler.sendEmptyMessage(UPDAT_WALL_TIME_TIMER_TASK);
        }
    }

    private class ChatTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(REMOVE_CHAT_ITEM_TIMER_TASK);
        }
    }

    private TimerTask mHeartClickTask = new TimerTask() {
        @Override
        public void run() {
            heartClick();
        }
    };


    private TIMMessageListener msgListener = new TIMMessageListener() {
        @Override
        public boolean onNewMessages(List<TIMMessage> list) {

            Log.d(TAG, "onNewMessagesGet  " + list.size());
            if (isTopActivity()) {
                //解析TIM推送消息
                if (groupId != null) {
                    refreshChat2(list);

                }

            }
            return false;
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_PRAISE:
                    if (praiseNum == 0)
                        praiseNum++;
                    mPraiseNum.setText("" + praiseNum);
                    break;
                case IM_HOST_LEAVE:
//                    onMemberExit();
                    onCloseVideo();
                    break;

                case ERROR_MESSAGE_TOO_LONG:
                    Toast.makeText(getBaseContext(), "消息太长，发送失败", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACCOUNT_NOT_EXIT:
                    Toast.makeText(getBaseContext(), "对方账号不存在或未登陆过！", Toast.LENGTH_SHORT).show();
                    break;

                case UPDAT_WALL_TIME_TIMER_TASK:
                    updateWallTime();
                    break;
                case REMOVE_CHAT_ITEM_TIMER_TASK:
                    removeChatItem();
                    break;
                case UPDAT_MEMBER:
                    updateMemberView();
//                    mChatMsgListAdapter.refresh(hostMember);
                    break;
//                case REFRESH_HOST_INFO
//                    break;
                case MEMBER_EXIT_COMPLETE:
                    sendCloseMsg();
                    break;
                case CLOSE_VIDEO:
                    onCloseVideo();
                    break;
                case START_RECORD:
                    startRecord();
                    break;
                case GET_ROOM_INFO:
                    getMemberInfo();
                    break;
                case REFRESH_CHAT:
                    showTextMessage((TIMMessage) msg.obj);
                default:
                    break;
            }
            return false;
        }
    });

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mobileInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            Log.e(TAG, "WL_DEBUG netinfo mobile = " + mobileInfo.isConnected() + ", wifi = " + wifiInfo.isConnected());

            int netType = Util.getNetWorkType(ctx);
            Log.e(TAG, "WL_DEBUG connectionReceiver getNetWorkType = " + netType);
            mQavsdkControl.setNetType(netType);

            if (!mobileInfo.isConnected() && !wifiInfo.isConnected()) {
                Log.e(TAG, "WL_DEBUG connectionReceiver no network = ");
                // unconnect network
                // 暂时不关闭
//				if (ctx instanceof Activity) {
//					Toast.makeText(getApplicationContext(), ctx.getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
//					((Activity)ctx).finish();
//				}
            } else {
                // connect network
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action = " + action);
            if (action.equals(Util.ACTION_SURFACE_CREATED)) {
                locateCameraPreview();
                wakeLock.acquire();
//                mQavsdkControl.toggleEnableCamera();
                if (mSelfUserInfo.isCreater() == true) {
                    initTIMGroup();
                    mEditTextInputMsg.setClickable(true);
                    mIsSuccess = true;
                    mVideoTimer = new Timer(true);
                    mVideoTimerTask = new VideoTimerTask();
                    mVideoTimer.schedule(mVideoTimerTask, 1000, 1000);
                    mQavsdkControl.toggleEnableCamera();
                    boolean isEnable = mQavsdkControl.getIsEnableCamera();
                    refreshCameraUI();
                    if (mOnOffCameraErrorCode != AVError.AV_OK) {
                        showDialog(isEnable ? DIALOG_OFF_CAMERA_FAILED : DIALOG_ON_CAMERA_FAILED);
                        mQavsdkControl.setIsInOnOffCamera(false);
                        refreshCameraUI();
                    }
//                    Log.d(TAG, "getMemberInfo isHandleMemberRoomSuccess " + mQavsdkApplication.isHandleMemberRoomSuccess());

//                    if (mQavsdkApplication.isHandleMemberRoomSuccess()) {
//                        Log.d(TAG, "getMemberInfo isHandleMemberRoomSuccess " + mQavsdkApplication.isHandleMemberRoomSuccess() + " yes  do it normally ");
                    mHandler.sendEmptyMessageDelayed(GET_ROOM_INFO, 0);
//                    } else {
//                        Log.w(TAG, "getMemberInfo isHandleMemberRoomSuccess " + mQavsdkApplication.isHandleMemberRoomSuccess() + " no wait for call");
//                    }
                    mQavsdkControl.setRequestCount(0);
                    //上报主播心跳
                    mHeartClickTimer.schedule(mHeartClickTask, 1000, 10000);
                } else {

                    hostRequestView(mHostIdentifier);
                }
            } else if (action.equals(Util.ACTION_VIDEO_CLOSE)) {
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                if (!TextUtils.isEmpty(mRecvIdentifier)) {
                    mQavsdkControl.setRemoteHasVideo(false, mRecvIdentifier, 0);
                }

                mRecvIdentifier = identifier;
            } else if (action.equals(Util.ACTION_VIDEO_SHOW)) {
                //成员模式加入视频聊天室
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                Log.d(TAG, "onReceive ACTION_VIDEO_SHOW  id " + identifier);
                mRecvIdentifier = identifier;
                mQavsdkControl.setRemoteHasVideo(true, mRecvIdentifier, 0);
                //IMSDk 加入聊天室
                joinGroup();
                initTIMGroup();
                mIsSuccess = true;
                mEditTextInputMsg.setClickable(true);
                //获取群组成员信息
                getMemberInfo();
                //发消息通知大家 自己上线了
                onMemberEnter();
                Util.switchWaitingDialog(ctx, mDialogInit, DIALOG_INIT, false);
            } else if (action.equals(Util.ACTION_ENABLE_CAMERA_COMPLETE)) {
                Log.d(TAG, "onClick ACTION_ENABLE_CAMERA_COMPLETE   " + " status " + mQavsdkControl.getIsEnableCamera());
                //自己是主播才本地渲染摄像头界面

                boolean isbeauty = mQavsdkControl.getAVContext().getVideoCtrl().enableBeauty(true);
                //如果具备美颜能力 显示美颜接口
                if (isbeauty == true)
                    mButtonBeauty.setVisibility(View.VISIBLE);

                if (mSelfUserInfo.isCreater() == true) {
                    refreshCameraUI();
                    mOnOffCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
                    boolean isEnable = intent.getBooleanExtra(Util.EXTRA_IS_ENABLE, false);

                    if (mOnOffCameraErrorCode == AVError.AV_OK) {
                        if (!mIsPaused) {
                            Log.d(TAG, "ACTION_ENABLE_CAMERA_COMPLETE mHostIdentifier " + mHostIdentifier);
                            if (!mHostIdentifier.startsWith("86-")) {
                                mHostIdentifier = "86-" + mHostIdentifier;
                            }
                            mQavsdkControl.setSelfId(mHostIdentifier);
                            mQavsdkControl.setLocalHasVideo(isEnable, mHostIdentifier);
//                            startDefaultRecord();
                        }
                    } else {
                        showDialog(isEnable ? DIALOG_ON_CAMERA_FAILED : DIALOG_OFF_CAMERA_FAILED);
                    }

                    if (currentCameraIsFront == false) {
                        Log.d(TAG, " onSwitchCamera!!ACTION_ENABLE_CAMERA_COMPLETE and lastTime is backCamera :  " + mQavsdkControl.getIsInOnOffCamera());
                        onSwitchCamera();
                    }
                }
            } else if (action.equals(Util.ACTION_SWITCH_CAMERA_COMPLETE)) {
                Log.d(TAG, " onSwitchCamera!! ACTION_SWITCH_CAMERA_COMPLETE  " + mQavsdkControl.getIsInOnOffCamera());
                refreshCameraUI();

                mSwitchCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
                boolean isFront = intent.getBooleanExtra(Util.EXTRA_IS_FRONT, false);
                if (mSwitchCameraErrorCode != AVError.AV_OK) {
                    showDialog(isFront ? DIALOG_SWITCH_FRONT_CAMERA_FAILED : DIALOG_SWITCH_BACK_CAMERA_FAILED);
                } else {
                    currentCameraIsFront = mQavsdkControl.getIsFrontCamera();
                    Log.d(TAG, "onSwitchCamera  " + currentCameraIsFront);
                }
            } else if (action.equals(Util.ACTION_MEMBER_CHANGE)) {

            } else if (action.equals(Util.ACTION_INSERT_ROOM_TO_SERVER_COMPLETE)) {
                Log.w(TAG, "getMemberInfo isHandleMemberRoomSuccess " + mQavsdkApplication.isHandleMemberRoomSuccess() + " now is time ");
                mHandler.sendEmptyMessageDelayed(GET_ROOM_INFO, 0);
            } else if (action.equals(Util.ACTION_INVITE_MEMBER_VIDEOCHAT)) {
                //发起邀请消息
                selectIdentier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                Log.d(TAG, "onReceive inviteVC selectIdentier " + selectIdentier);

                if (viewIndex != null) {
                    String id;
                    if (selectIdentier.startsWith("86-")) {
                        id = selectIdentier.substring(3);

                    } else {
                        id = selectIdentier;
                    }
                    if (viewIndex.containsKey(id)) {
                        Toast.makeText(AvActivity.this, "you can't allowed to invite the same people", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                //开始邀请信息
                sendMaskViewStatus(selectIdentier);
                sendVCInvitation(selectIdentier);

            } else if (action.equals(Util.ACTION_MEMBER_VIDEO_SHOW)) {
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                mRecvIdentifier = identifier;
                //不在这个位置
//                int viewindex = viewIndex.get(identifier.substring(3));
                //第一个位置
                int locactionIndex = mQavsdkControl.getSmallVideoView();
                mMemberVideoCount = locactionIndex;
                Log.d(TAG, "onReceive ACTION_VIDEO_SHOW  id " + identifier + " viewindex " + locactionIndex);
                mQavsdkControl.setRemoteHasVideo(true, mRecvIdentifier, locactionIndex);

            } else if (action.equals(Util.ACTION_SHOW_VIDEO_MEMBER_INFO)) {
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                showVideoMemberInfo(identifier);
            } else if (action.equals(Util.ACTION_CLOSE_MEMBER_VIDEOCHAT)) {
                String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
                closeVideoMemberByHost(identifier);
            } else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
                closeActivity();
            }


        }
    };


    private void closeActivity() {
        destroyTIM();
        if (wakeLock.isHeld())
            wakeLock.release();
        if (mSelfUserInfo.isCreater() == true) {
            closeLive();
            setResult(Util.SHOW_RESULT_CODE);
            Util.switchWaitingDialog(ctx, mDialogAtDestroy, DIALOG_DESTROY, true);
//            startActivity(new Intent(AvActivity.this, GameOverActivity.class)
//                    .putExtra(Util.EXTRA_ROOM_NUM, roomNum)
//                    .putExtra(Util.EXTRA_LEAVE_MODE,false)
//                    );
        } else {
            leaveLive();
            setResult(Util.VIEW_RESULT_CODE);
        }
        startActivity(new Intent(AvActivity.this, GameOverActivity.class)
                        .putExtra(Util.EXTRA_ROOM_NUM, roomNum)
                        .putExtra(Util.EXTRA_LEAVE_MODE, LEVAE_MODE)
        );
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "WL_DEBUG onCreate start");
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.av_activity);
        registerBroadcastReceiver();

        showDialog(DIALOG_INIT);
        Util.switchWaitingDialog(ctx, mDialogInit, DIALOG_INIT, true);
        mQavsdkApplication = (QavsdkApplication) getApplication();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        int netType = Util.getNetWorkType(ctx);
        Log.e(TAG, "WL_DEBUG connectionReceiver onCreate = " + netType);
        if (netType != AVConstants.NETTYPE_NONE) {
            mQavsdkControl.setNetType(Util.getNetWorkType(ctx));
        }

        if (mQavsdkControl.getAVContext() != null) {
            mQavsdkControl.onCreate((QavsdkApplication) getApplication(), findViewById(android.R.id.content));
        } else {
            finish();
        }
        mQavsdkControl.setRequestCount(0);
        mSelfUserInfo = mQavsdkApplication.getMyselfUserInfo();
        mMemberList = mQavsdkControl.getMemberList();
        mNormalMemberList = copyToNormalMember();
        mVideoMemberList = new ArrayList<MemberInfo>();
        roomNum = getIntent().getExtras().getInt(Util.EXTRA_ROOM_NUM);
        groupForPush = roomNum;
        if (mSelfUserInfo.getEnv() == Util.ENV_TEST) {
//			groupForPush = Integer.parseInt(mSelfUserInfo.getUserPhone().substring(0, 5));
            groupForPush = 14010;
        }
        mRecvIdentifier = "" + roomNum;
        mHostIdentifier = getIntent().getExtras().getString(Util.EXTRA_SELF_IDENTIFIER);
        Log.d(TAG, "onCreate mHostIdentifier" + mHostIdentifier);

        groupId = getIntent().getExtras().getString(Util.EXTRA_GROUP_ID);
        if (!mSelfUserInfo.isCreater()) {
            praiseNum = getIntent().getExtras().getInt(Util.EXTRA_PRAISE_NUM);
        }
        mIsSuccess = false;
        mRequestIdentifierList = new String[MAX_REQUEST_VIEW_COUNT];
        mRequestViewList = new AVView[MAX_REQUEST_VIEW_COUNT];
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        initView();
        initGridView();

//        initShowTips();
        registerOrientationListener();

    }


    @Override
    protected void onStart() {
        super.onStart();
//        if (mSelfUserInfo.isCreater() == true)
//            if (mQavsdkControl.getIsEnableCamera() == false) {
////                mQavsdkControl.toggleEnableCamera();
//                OpenVideo = true;
//
//            }
        AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
        avAudioCtrl.enableMic(true);
        OpenVoice = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsPaused = false;
        LEVAE_MODE = false;
        mQavsdkControl.onResume();
        Log.i(TAG, "onResume switchCamera!! ");
        refreshCameraUI();

        if (mOnOffCameraErrorCode != AVError.AV_OK) {
            showDialog(DIALOG_ON_CAMERA_FAILED);
        }
        startOrientationListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
        mQavsdkControl.onPause();
        Log.i(TAG, "onPause switchCamera!! ");
        refreshCameraUI();
        if (mOnOffCameraErrorCode != AVError.AV_OK) {
            showDialog(DIALOG_OFF_CAMERA_FAILED);
        }
        stopOrientationListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭摄像头
//        if (mQavsdkControl.getIsEnableCamera() == true) {
////            mQavsdkControl.toggleEnableCamera();
////            OpenVideo = false;
//        }
        //关闭Mic
//        mQavsdkControl.setRequestCount(0);
        AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
        avAudioCtrl.enableMic(false);
        OpenVoice = false;
        mQavsdkApplication.setHandleMemberRoomSuccess(false);
        hasPullMemberList = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMaskViewCount = 0;
        mQavsdkControl.onDestroy();
        // 注销广播
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
        }

        Log.d(TAG, "WL_DEBUG onDestroy");
        Util.switchWaitingDialog(ctx, mDialogAtDestroy, DIALOG_DESTROY, false);
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_SURFACE_CREATED);
        intentFilter.addAction(Util.ACTION_VIDEO_SHOW);
        intentFilter.addAction(Util.ACTION_MEMBER_VIDEO_SHOW);
        intentFilter.addAction(Util.ACTION_VIDEO_CLOSE);
        intentFilter.addAction(Util.ACTION_ENABLE_CAMERA_COMPLETE);
        intentFilter.addAction(Util.ACTION_SWITCH_CAMERA_COMPLETE);
        intentFilter.addAction(Util.ACTION_INSERT_ROOM_TO_SERVER_COMPLETE);
        intentFilter.addAction(Util.ACTION_INVITE_MEMBER_VIDEOCHAT);
        intentFilter.addAction(Util.ACTION_MEMBER_CHANGE);
        intentFilter.addAction(Util.ACTION_SHOW_VIDEO_MEMBER_INFO);
        intentFilter.addAction(Util.ACTION_CLOSE_MEMBER_VIDEOCHAT);
        intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);

        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, netIntentFilter);
    }

    private LinearLayout praiseLayout, mBeautySettings;
    private ImageButton mButtonMute;
    private TextView mButtonBeauty;
    private int mBeautyRate;
    private SeekBar mBeautyBar;
    private FrameLayout mBottomBar;
    private TextView mBeautyConfirm;

    private void initView() {
        ImageButton mButtonSwitchCamera;
        hostHead = (CircularImageButton) findViewById(R.id.host_head);
        mButtonMute = (ImageButton) findViewById(R.id.mic_btn);
        mButtonBeauty = (TextView) findViewById(R.id.beauty_btn);
        mButtonBeauty.setOnClickListener(this);
        mButtonSwitchCamera = (ImageButton) findViewById(R.id.qav_topbar_switchcamera);
        mListViewMsgItems = (ListView) findViewById(R.id.im_msg_items);
        mEditTextInputMsg = (EditText) findViewById(R.id.qav_bottombar_msg_input);
        mBeautySettings = (LinearLayout) findViewById(R.id.qav_beauty_setting);
        mBeautyConfirm = (TextView) findViewById(R.id.qav_beauty_setting_finish);
        mBeautyConfirm.setOnClickListener(this);
        mBottomBar = (FrameLayout) findViewById(R.id.qav_bottom_bar);
        mBeautyBar = (SeekBar) (findViewById(R.id.qav_beauty_progress));
        mBeautyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                Log.d("SeekBar", "onStopTrackingTouch");
                Toast.makeText(AvActivity.this, "beauty " + mBeautyRate + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                Log.d("SeekBar", "onStartTrackingTouch");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                mBeautyRate = progress;
                mQavsdkControl.getAVContext().getVideoCtrl().inputBeautyParam(getBeautyProgress(progress));

            }
        });


        mInviteMastk1 = (FrameLayout) findViewById(R.id.inviteMaskItem1);
        mInviteMastk2 = (FrameLayout) findViewById(R.id.inviteMaskItem2);
        mInviteMastk3 = (FrameLayout) findViewById(R.id.inviteMaskItem3);
        mVideoHead1 = (ImageView) findViewById(R.id.inviteMaskHead1);
        mVideoHead2 = (ImageView) findViewById(R.id.inviteMaskHead2);
        mVideoHead3 = (ImageView) findViewById(R.id.inviteMaskHead3);
        mInviteMastk1.setVisibility(View.GONE);
        mInviteMastk2.setVisibility(View.GONE);
        mInviteMastk3.setVisibility(View.GONE);

        mEditTextInputMsg.setOnClickListener(this);

        findViewById(R.id.qav_topbar_hangup).setOnClickListener(this);
        findViewById(R.id.qav_topbar_push).setOnClickListener(this);
        findViewById(R.id.qav_topbar_record).setOnClickListener(this);
        findViewById(R.id.qav_topbar_streamtype).setOnClickListener(this);
        if (!mSelfUserInfo.isCreater()) {
            findViewById(R.id.qav_topbar_push).setVisibility(View.GONE);
            findViewById(R.id.qav_topbar_streamtype).setVisibility(View.GONE);
            findViewById(R.id.qav_topbar_record).setVisibility(View.GONE);
        }
        praiseLayout = (LinearLayout) findViewById(R.id.praise_layout);
        mButtonSendMsg = (Button) findViewById(R.id.qav_bottombar_send_msg);
        mButtonSendMsg.setOnClickListener(this);
        mClockTextView = (TextView) findViewById(R.id.qav_timer);
        mPraiseNum = (TextView) findViewById(R.id.text_view_live_praise);
        mMemberListButton = (TextView) findViewById(R.id.btn_member_list);
        mMemberListButton.setOnClickListener(this);
        mButtonPraise = (ImageButton) findViewById(R.id.image_btn_praise);
        mButtonPraise.setOnClickListener(this);

        if (mSelfUserInfo.isCreater()) {
            mButtonMute.setOnClickListener(this);
            mButtonSwitchCamera.setOnClickListener(this);
            AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
//            avAudioCtrl.enableMic(false);
            avAudioCtrl.enableMic(true);
            mButtonPraise.setEnabled(false);
        } else {
            mButtonSwitchCamera.setOnClickListener(this);
            mQavsdkControl.getAVContext().getAudioCtrl().enableMic(false);
            mPraiseNum.setText("" + praiseNum);
//            mButtonMute.setVisibility(View.GONE);
//            mButtonSwitchCamera.setVisibility(View.GONE);
        }

        //不熄屏
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TAG");

        //默认不显示键盘
        mInputKeyBoard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        findViewById(R.id.av_screen_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideMsgIputKeyboard();
                mEditTextInputMsg.setVisibility(View.VISIBLE);
                return false;
            }
        });

        mVideoTimer = new Timer(true);
        mHeartClickTimer = new Timer(true);


        tvTipsMsg = (TextView) findViewById(R.id.qav_tips_msg);
        tvTipsMsg.setTextColor(Color.GREEN);
        tvShowTips = (TextView) findViewById(R.id.param_video);
        tvShowTips.setOnClickListener(this);
        timer.schedule(task, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    private void initTIMGroup() {
        Log.d(TAG, "initTIMGroup groupId" + groupId);
        if (groupId != null) {
            mConversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, groupId);

            Log.d(TAG, "initTIMGroup mConversation" + mConversation);
        } else {

        }
        mSystemConversation = TIMManager.getInstance().getConversation(TIMConversationType.System, "");
//        testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, "18602833226");
        mArrayListChatEntity = new ArrayList<ChatEntity>();
        mChatMsgListAdapter = new ChatMsgListAdapter(this, mArrayListChatEntity, mMemberList, mSelfUserInfo);
        mListViewMsgItems.setAdapter(mChatMsgListAdapter);
        if (mListViewMsgItems.getCount() > 1)
            mListViewMsgItems.setSelection(mListViewMsgItems.getCount() - 1);
        mListViewMsgItems.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideMsgIputKeyboard();
                mEditTextInputMsg.setVisibility(View.VISIBLE);
                return false;
            }
        });

        mListViewMsgItems.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getFirstVisiblePosition() == 0 && !mIsLoading && bMore) {
                            bNeverLoadMore = false;
                            mIsLoading = true;
                            mLoadMsgNum += MAX_PAGE_NUM;
//							getMessage();
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
//        getMessage();

        TIMManager.getInstance().addMessageListener(msgListener);

        mChatTimer = new Timer(true);
        time = System.currentTimeMillis() / 1000;
        mChatTimerTask = new ChatTimerTask();
        mChatTimer.schedule(mChatTimerTask, 8000, 2000);
    }

    private void destroyTIM() {
        TIMManager.getInstance().removeMessageListener(msgListener);
        Log.d(TAG, "WL_DEBUG onDestroy");
        if (groupId != null && mIsSuccess) {
            if (mSelfUserInfo.isCreater()) {
                TIMGroupManager.getInstance().deleteGroup(groupId, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Log.e(TAG, "quit group error " + i + " " + s);
                    }

                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "delete group success");
                        Log.d(TAG, "WL_DEBUG onDestroy");
                    }
                });
            } else {
                TIMGroupManager.getInstance().quitGroup(groupId, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Log.e(TAG, "quit group error " + i + " " + s);
                    }

                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "delete group success");
                        Log.i(TAG, "WL_DEBUG onDestroy");
                    }
                });
            }
            TIMManager.getInstance().deleteConversation(TIMConversationType.Group, groupId);
        }
    }

    private void joinGroup() {
        TIMGroupManager.getInstance().applyJoinGroup(groupId, "申请加入" + groupId, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
//                TIMManager.getInstance().logout();
                Toast.makeText(ctx, "加群失败,失败原因：" + i + ":" + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "applyJpoinGroup success");
            }
        });
    }


    private void refreshChat2(List<TIMMessage> msg) {
        Log.d(TAG, "refreshChat 0000 " + msg);
        List<TIMMessage> tlist = msg;


        if (tlist.size() > 0) {
            mConversation.setReadMessage(tlist.get(0));
            Log.d(TAG, "refreshChat readMessage " + tlist.get(0).timestamp());
        }
        if (!bNeverLoadMore && (tlist.size() < mLoadMsgNum))
            bMore = false;

        for (int i = tlist.size() - 1; i >= 0; i--) {
            Log.d(TAG, "refreshChat 2222curMsg");
            TIMMessage currMsg = tlist.get(i);


            for (int j = 0; j < currMsg.getElementCount(); j++) {


                if (currMsg.getElement(j) == null)
                    continue;
                TIMElem elem = currMsg.getElement(j);
                TIMElemType type = elem.getType();
                Log.i(TAG, "refreshChat2 type " + type);


                //系统消息
                if (type == TIMElemType.GroupSystem) {
                    Log.d(TAG, "getSysMessage !!!! cuonNewMessagesrMsg    " + ((TIMGroupSystemElem) elem).getSubtype());
                    if (TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE == ((TIMGroupSystemElem) elem).getSubtype()) {
                        mHandler.sendEmptyMessage(IM_HOST_LEAVE);
                        LEVAE_MODE = true;
                    }

                }

                //定制消息
                if (type == TIMElemType.Custom) {
                    handleCustomMsg(elem);
                    continue;
                }

                //其他群消息过滤
                if (!groupId.equals(currMsg.getConversation().getPeer())) {
                    continue;
                }


                ChatEntity entity = new ChatEntity();
                entity.setElem(elem);
                entity.setIsSelf(currMsg.isSelf());
                Log.d(TAG, "refreshChat2 " + currMsg.isSelf());
                entity.setTime(currMsg.timestamp());
                Log.e(TAG, "" + currMsg.timestamp());
                entity.setType(currMsg.getConversation().getType());
                entity.setSenderName(currMsg.getSender());
                entity.setStatus(currMsg.status());
                mArrayListChatEntity.add(entity);
            }
        }

        mChatMsgListAdapter.notifyDataSetChanged();
        mListViewMsgItems.setVisibility(View.VISIBLE);
        if (mListViewMsgItems.getCount() > 1) {
            if (mIsLoading)
                mListViewMsgItems.setSelection(0);
            else
                mListViewMsgItems.setSelection(mListViewMsgItems.getCount() - 1);
//                mListViewMsgItems.setClickable(false);
        }
        mIsLoading = false;
    }

    public boolean hideMsgIputKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) {
                mInputKeyBoard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return true;
            }
        }

        return false;
    }


    private void locateCameraPreview() {
        if (mDialogInit != null && mDialogInit.isShowing()) {
            mDialogInit.dismiss();
        }
    }

//    private AVEndpoint.RequestViewCompleteCallback mRequestViewCompleteCallback = new AVEndpoint.RequestViewCompleteCallback() {
//        protected void OnComplete(String identifier, int result) {
//            // TODO
//            Log.d(TAG, "RequestViewCompleteCallback.OnComplete");
//        }
//    };
//
//    private AVEndpoint.CancelViewCompleteCallback mCancelViewCompleteCallback = new AVEndpoint.CancelViewCompleteCallback() {
//        protected void OnComplete(String identifier, int result) {
//            // TODO
//            Log.d(TAG, "CancelViewCompleteCallback.OnComplete");
//        }
//    };

    public void hostRequestView(String identifier) {
        Log.d(TAG, "request " + identifier);
        identifier = "86-" + identifier;
        AVEndpoint endpoint = null;
        if (mQavsdkControl != null && mQavsdkControl.getAVContext() != null && mQavsdkControl.getAVContext().getRoom() != null) {
            endpoint = ((AVRoomMulti) mQavsdkControl.getAVContext().getRoom()).getEndpointById(identifier);
        }
        Log.d(TAG, "hostRequestView identifier " + identifier + " endpoint " + endpoint);
        if (endpoint != null) {
            mVideoTimer = new Timer(true);
            mVideoTimerTask = new VideoTimerTask();
            mVideoTimer.schedule(mVideoTimerTask, 1000, 1000);
            AVView view = new AVView();
            view.videoSrcType = AVView.VIDEO_SRC_TYPE_CAMERA;//SDK1.2版本只支持摄像头视频源，所以当前只能设置为VIDEO_SRC_TYPE_CAMERA。
            view.viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;


            //界面数
            mRequestViewList[0] = view;
            mRequestIdentifierList[0] = identifier;
            mRequestViewList[0].viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;
            AVEndpoint.requestViewList(mRequestIdentifierList, mRequestViewList, 1, mRequestViewListCompleteCallback);
            //成员模式请求界面
            ctx.sendBroadcast(new Intent(Util.ACTION_VIDEO_SHOW)
                    .putExtra(Util.EXTRA_IDENTIFIER, identifier)
                    .putExtra(Util.EXTRA_VIDEO_SRC_TYPE, view.videoSrcType));

        } else {
            mEditTextInputMsg.setVisibility(View.GONE);
            mButtonSendMsg.setVisibility(View.GONE);
            mPraiseNum.setVisibility(View.GONE);
            mButtonPraise.setVisibility(View.GONE);

            dialog = new Dialog(this, R.style.dialog);
            dialog.setContentView(R.layout.alert_dialog);
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText("温馨提示");
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText("此直播已结束，请观看其他直播！");
            ((Button) dialog.findViewById(R.id.close_dialog)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCloseVideo();
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    public void requestMultiView(String identifier) {
        Log.d(TAG, "requestMultiView " + identifier + "  mMemberVideoCount  ");
        int mMemberVideoCount = mQavsdkControl.getSmallVideoView();
        identifier = "86-" + identifier;
        AVEndpoint endpoint = ((AVRoomMulti) mQavsdkControl.getAVContext().getRoom()).getEndpointById(identifier);
        if (endpoint != null) {
            //界面参数
            AVView view = new AVView();
            view.videoSrcType = AVView.VIDEO_SRC_TYPE_CAMERA;
            view.viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;

//            Log.d(TAG, "requestMultiView  " + identifier + "  mMemberVideoCount  " + mMemberVideoCount);
//            for (int i = 0; i < mMemberVideoCount; i++) {
//                mRequestViewList[i].viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;
//            }

            //界面参数
            mRequestViewList[mMemberVideoCount] = view;
            mRequestIdentifierList[mMemberVideoCount] = identifier;

            //请求次数
            mMemberVideoCount++;
            if (mMemberVideoCount > 3) {
                Toast.makeText(this, "requestCount cannot pass  4", Toast.LENGTH_LONG);
                return;
            }
            mQavsdkControl.setRequestCount(mMemberVideoCount);


            Log.d(TAG, "requestMultiView identifier " + identifier + " mMemberVideoCount " + mMemberVideoCount);
            AVEndpoint.requestViewList(mRequestIdentifierList, mRequestViewList, mMemberVideoCount, mRequestViewListCompleteCallback);


//            endpoint.hostRequestView(view, mRequestViewCompleteCallback);
            //成员模式请求界面
            ctx.sendBroadcast(new Intent(Util.ACTION_MEMBER_VIDEO_SHOW)
                    .putExtra(Util.EXTRA_IDENTIFIER, identifier)
                    .putExtra(Util.EXTRA_VIDEO_SRC_TYPE, view.videoSrcType));
        } else {
            mEditTextInputMsg.setVisibility(View.GONE);
            mButtonSendMsg.setVisibility(View.GONE);
            mPraiseNum.setVisibility(View.GONE);
            mButtonPraise.setVisibility(View.GONE);

            dialog = new Dialog(this, R.style.dialog);
            dialog.setContentView(R.layout.alert_dialog);
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText("温馨提示");
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText("此直播已结束，请观看其他直播！");
            ((Button) dialog.findViewById(R.id.close_dialog)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCloseVideo();
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mic_btn:
                onCheckedChanged(mChecked);
                mChecked = !mChecked;
                break;
            case R.id.qav_topbar_hangup:
                if (mIsSuccess && mSelfUserInfo.isCreater() == false) {
                    memberCloseAlertDialog();
//                    onMemberExit();
//                    onCloseVideo();
                } else
                    hostCloseAlertDialog();
                break;
            case R.id.beauty_btn:
                if (mBeautySettings != null) {
                    if (mBeautySettings.getVisibility() == View.GONE) {
                        mBeautySettings.setVisibility(View.VISIBLE);
                        mBottomBar.setVisibility(View.INVISIBLE);
                        mMemberListButton.setVisibility(View.INVISIBLE);
                    } else {
                        mBeautySettings.setVisibility(View.GONE);
                        mBottomBar.setVisibility(View.VISIBLE);
                        mMemberListButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.i(TAG, "beauty_btn mTopBar  is null ");
                }
                break;

            case R.id.qav_beauty_setting_finish:
                mBeautySettings.setVisibility(View.GONE);
                mBottomBar.setVisibility(View.VISIBLE);
                mMemberListButton.setVisibility(View.VISIBLE);
                break;
            case R.id.qav_topbar_switchcamera:
                onSwitchCamera();
                break;
            case R.id.qav_bottombar_send_msg:
                onSendMsg();
                break;
            case R.id.qav_bottombar_msg_input:
                mIsClicked = true;
                break;
            case R.id.image_btn_praise:
                onSendPraise();
                break;
            case R.id.param_video:
                showTips = !showTips;
//                if (showTips) {
//                    tvShowTips.setText("CloseTips");
//                    tvShowTips.setTextColor(Color.RED);
//                } else {
//                    tvShowTips.setText("OpenTips");
//                    tvShowTips.setTextColor(Color.GREEN);
//                }
                break;
            case R.id.qav_topbar_push:
                Push();
//                Push();
                break;
            case R.id.qav_topbar_streamtype:
                switch (StreamType) {
//                    case 1:
//                        ((Button) findViewById(R.id.qav_topbar_streamtype)).setText("FLV");
//                        StreamType = 2;
//                        StreamTypeCode = DemoConstants.FLV;
//                        break;
                    case 2:
                        ((Button) findViewById(R.id.qav_topbar_streamtype)).setText("RTMP");
                        StreamType = 5;
                        StreamTypeCode = DemoConstants.RTMP;
                        break;
                    case 5:
                        ((Button) findViewById(R.id.qav_topbar_streamtype)).setText("HLS");
                        StreamTypeCode = DemoConstants.HLS;
                        StreamType = 2;
                        break;
                }
                break;
            case R.id.qav_topbar_record:
//                Record();
                if (!mRecord) {
                    setRecordParam();
//                    startDefaultRecord();
                } else {
                    stopRecord();
                }
                break;
            case R.id.btn_member_list:
                showMemberList();
                break;
            default:
                break;
        }
    }


    public void showMemberList() {
        if (mSelfUserInfo.isCreater() == true) {
            mMemberListDialog = new MemberListDialog(this, R.style.dialog, mNormalMemberList, mVideoMemberList, true);
            mMemberListDialog.freshRequestCount(mMemberVideoCount);
            mMemberListDialog.show();
        } else {
            mMemberListDialog = new MemberListDialog(this, R.style.dialog, mNormalMemberList, mVideoMemberList, false);
            mMemberListDialog.show();
        }

    }

    public void Push() {
        int roomid = (int) mQavsdkControl.getAVContext().getRoom().getRoomId();
        Log.i(TAG, "Push roomid: " + roomid);
        Log.d(TAG, "Push groupid: " + groupForPush);
        Log.d(TAG, "Push mpush: " + mpush);
        Log.d(TAG, "Push enviroment: " + mSelfUserInfo.getEnv());

        TIMAvManager.RoomInfo roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRoomId(roomid);
        roomInfo.setRelationId(groupForPush);

        if (!mpush) {
            setParamAndPush(roomInfo);

        } else {
            stopPushAction(roomInfo);
        }
    }

    private void pushAction(TIMAvManager.RoomInfo roominfo) {
        //推流的接口
        TIMAvManager.getInstance().requestMultiVideoStreamerStart(roominfo, mStreamParam, new TIMValueCallBack<TIMAvManager.StreamRes>() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "url error " + i + " : " + s);
                Toast.makeText(getApplicationContext(), "start stream error,try again " + i + " : " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMAvManager.StreamRes streamRes) {
                List<TIMAvManager.LiveUrl> liveUrls = streamRes.getUrls();
                streamChannelID = streamRes.getChnlId();
                mpush = true;
                Log.e(TAG, liveUrls.toString());
                ((Button) findViewById(R.id.qav_topbar_push)).setTextColor(getResources().getColor(R.color.red));
                ((Button) findViewById(R.id.qav_topbar_push)).setText("停推");
                int length = liveUrls.size();
                Log.e(TAG, "url success size " + " : " + length);
                String url = null;
                String url2 = null;
                if (length == 1) {
                    TIMAvManager.LiveUrl avUrl = liveUrls.get(0);
                    url = avUrl.getUrl();
                } else if (length == 2) {
                    TIMAvManager.LiveUrl avUrl = liveUrls.get(0);
                    url = avUrl.getUrl();
                    TIMAvManager.LiveUrl avUrl2 = liveUrls.get(1);
                    url2 = avUrl2.getUrl();
                }
//                for (int i = 0; i < length; i++) {
//                    TIMAvManager.LiveUrl avUrl = liveUrls.get(i);
//                    url = avUrl.getUrl();
//                    Log.e(TAG, "url success " + " : " + url);
//                }
//                final String finalUrl = url;
//                dialog = new Dialog(AvActivity.this, R.style.dialog);
//                dialog.setContentView(R.layout.alert_dialog);
//                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(url);
//                if (url2 == null) {
//                    ((TextView) dialog.findViewById(R.id.dialog_message2)).setVisibility(View.GONE);
//
//                } else {
//                    ((TextView) dialog.findViewById(R.id.dialog_message2)).setText(url2);
//                }
//                ((Button) dialog.findViewById(R.id.close_dialog)).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        ClipboardManager clip = (ClipboardManager) getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
//                        clip.setText(finalUrl);
//                        dialog.dismiss();
//                        //Toast.makeText(getApplicationContext(), "copy success", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                dialog.setCanceledOnTouchOutside(false);
//                dialog.show();

                ClipToBoard(url, url2);

            }
        });
    }


    private void ClipToBoard(final String url, final String url2) {
        if (url == null) return;
        dialog = new Dialog(AvActivity.this, R.style.dialog);
        dialog.setContentView(R.layout.alert_dialog);
        TextView urlText = ((TextView) dialog.findViewById(R.id.dialog_message));
        TextView urlText2 = ((TextView) dialog.findViewById(R.id.dialog_message2));
        urlText.setText(url);
        urlText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clip = (ClipboardManager) getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                clip.setText(url);
                Toast.makeText(AvActivity.this, "粘贴地址1到粘贴版了", Toast.LENGTH_SHORT).show();
            }
        });
        if (url2 == null) {
            urlText2.setVisibility(View.GONE);
        } else {
            urlText2.setText(url2);
            urlText2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clip = (ClipboardManager) getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                    clip.setText(url2);
                    Toast.makeText(AvActivity.this, "粘贴地址2到粘贴版了", Toast.LENGTH_SHORT).show();
                }
            });
        }
        ((Button) dialog.findViewById(R.id.close_dialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ClipboardManager clip = (ClipboardManager) getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
//                clip.setText(finalUrl);
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    private void stopPushAction(TIMAvManager.RoomInfo roomInfo) {
        Log.d(TAG, "Push stop Id " + streamChannelID);
        List<Long> myList = new ArrayList<Long>();
        myList.add(streamChannelID);
        TIMAvManager.getInstance().requestMultiVideoStreamerStop(roomInfo, myList, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {

                Log.e(TAG, "url stop error " + i + " : " + s);
                Toast.makeText(getApplicationContext(), "stop stream error,try again " + i + " : " + s, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess() {
                mpush = false;
                ((Button) findViewById(R.id.qav_topbar_push)).setTextColor(getResources().getColor(R.color.white));
                ((Button) findViewById(R.id.qav_topbar_push)).setText("推流");
            }
        });
    }

    private EditText filenameEditText;
    private EditText tagEditText;
    private EditText classEditText;
    private CheckBox trancodeCheckBox;
    private CheckBox screenshotCheckBox;
    private CheckBox watermarkCheckBox;
    private String filename = "";
    private String tags = "";
    private String classId = "";
    TIMAvManager.RecordParam mRecordParam;
    TIMAvManager.StreamParam mStreamParam;

    private void setRecordParam() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.record_param);
        mRecordParam = TIMAvManager.getInstance().new RecordParam();

        filenameEditText = (EditText) dialog.findViewById(R.id.record_filename);
        tagEditText = (EditText) dialog.findViewById(R.id.record_tag);
        classEditText = (EditText) dialog.findViewById(R.id.record_class);
        trancodeCheckBox = (CheckBox) dialog.findViewById(R.id.record_tran_code);
        screenshotCheckBox = (CheckBox) dialog.findViewById(R.id.record_screen_shot);
        watermarkCheckBox = (CheckBox) dialog.findViewById(R.id.record_water_mark);

        if (filename.length() > 0) {
            filenameEditText.setText(filename);
        }
        filenameEditText.setText(mQavsdkApplication.getRoomName());

        if (tags.length() > 0) {
            tagEditText.setText(tags);
        }

        if (classId.length() > 0) {
            classEditText.setText(classId);
        }
        Button recordOk = (Button) dialog.findViewById(R.id.btn_record_ok);
        recordOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filename = filenameEditText.getText().toString();
                mRecordParam.setFilename(filename);
                tags = tagEditText.getText().toString();
                classId = classEditText.getText().toString();
                Log.d(TAG, "onClick classId " + classId);
                if (classId.equals("")) {
                    Toast.makeText(getApplicationContext(), "classID can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                mRecordParam.setClassId(Integer.parseInt(classId));
                mRecordParam.setTransCode(trancodeCheckBox.isChecked());
                mRecordParam.setSreenShot(screenshotCheckBox.isChecked());
                mRecordParam.setWaterMark(watermarkCheckBox.isChecked());
                mHandler.sendEmptyMessage(START_RECORD);
                startOrientationListener();
                dialog.dismiss();
            }
        });
        Button recordCancel = (Button) dialog.findViewById(R.id.btn_record_cancel);
        recordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrientationListener();
                dialog.dismiss();
            }
        });
        stopOrientationListener();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void startDefaultRecord() {
        Log.d(TAG, "setDefaultRecordParam roomName" + mQavsdkApplication.getRoomName());
        mRecordParam = TIMAvManager.getInstance().new RecordParam();
        mRecordParam.setFilename(mQavsdkApplication.getRoomName());
        mRecordParam.setClassId(0);
        mRecordParam.setTransCode(false);
        mRecordParam.setSreenShot(false);
        mRecordParam.setWaterMark(false);
        mHandler.sendEmptyMessage(START_RECORD);
    }

    public void startRecord() {
        int roomid = (int) mQavsdkControl.getAVContext().getRoom().getRoomId();
        Log.i(TAG, "roomid: " + roomid);
        Log.i(TAG, "groupid: " + groupForPush);

        TIMAvManager.RoomInfo roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRelationId(groupForPush);
        roomInfo.setRoomId(roomid);

        TIMAvManager.getInstance().requestMultiVideoRecorderStart(roomInfo, mRecordParam, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "Record error" + i + " : " + s);
                Toast.makeText(getApplicationContext(), "start record error,try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
                mRecord = true;
                ((Button) findViewById(R.id.qav_topbar_record)).setTextColor(getResources().getColor(R.color.red));
                ((Button) findViewById(R.id.qav_topbar_record)).setText("停录");
                Log.i(TAG, "begin to record");
                Toast.makeText(getApplicationContext(), "start record now ", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void setParamAndPush(final TIMAvManager.RoomInfo roominfo) {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.push_param);
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SDKtype);

        mStreamParam = TIMAvManager.getInstance().new StreamParam();
        pushfileNameInput = (EditText) dialog.findViewById(R.id.push_filename);
        pushfileNameInput.setText("" + mQavsdkApplication.getRoomName());
        pushInfoInput = (EditText) dialog.findViewById(R.id.push_info);
//        pushCodeInput = (EditText) dialog.findViewById(R.id.push_password);
        Button recordOk = (Button) dialog.findViewById(R.id.btn_record_ok);
//        Spinner spinner = (Spinner) dialog.findViewById(R.id.SdkType);
//        spinner.setAdapter(spinnerAdapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(TAG, "onItemSelected " + position);
//                switch (position) {
//                    case 0:
//                        mStreamParam.setSdkType(TIMAvManager.SDKType.Normal);
//                        break;
//                    case 1:
//                        mStreamParam.setSdkType(TIMAvManager.SDKType.IOTCamara);
//                        break;
//                    case 2:
//                        mStreamParam.setSdkType(TIMAvManager.SDKType.CoastCamara);
//                        break;
//                    default:
//                        mStreamParam.setSdkType(TIMAvManager.SDKType.Normal);
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        //设置默认值


        recordOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pushfileNameInput.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "ChannelId can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                mStreamParam.setChannelName(pushfileNameInput.getText().toString());
//                mStreamParam.setChannelPasswd(pushCodeInput.getText().toString());
                Log.d(TAG, "setParamAndPush " + StreamTypeCode);
                switch (StreamTypeCode) {
                    case DemoConstants.FLV:
//                        mStreamParam.setEncode(TIMAvManager.StreamEncode.FLV);
                        break;
                    case DemoConstants.HLS:
                        mStreamParam.setEncode(TIMAvManager.StreamEncode.HLS);
                        break;
                    case DemoConstants.RTMP:
                        mStreamParam.setEncode(TIMAvManager.StreamEncode.RTMP);
                        break;
                    default:
                        mStreamParam.setEncode(TIMAvManager.StreamEncode.HLS);
                        break;
                }
                mStreamParam.setChannelDescr(pushInfoInput.getText().toString());
//                mStreamParam.setSdkType();


                pushAction(roominfo);
                startOrientationListener();
                dialog.dismiss();
            }
        });
        Button recordCancel = (Button) dialog.findViewById(R.id.btn_record_cancel);
        recordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrientationListener();
                dialog.dismiss();
            }
        });
        stopOrientationListener();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void stopRecord() {
        int roomid = (int) mQavsdkControl.getAVContext().getRoom().getRoomId();

        TIMAvManager.RoomInfo roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRelationId(groupForPush);
        roomInfo.setRoomId(roomid);
        TIMAvManager.getInstance().requestMultiVideoRecorderStop(roomInfo, new TIMValueCallBack<List<String>>() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "stop record error " + i + " : " + s);
                Toast.makeText(getApplicationContext(), "stop record error,try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(List<String> files) {
                mRecord = false;
                for (String file : files) {
                    Log.d(TAG, "stopRecord onSuccess file  " + file);
                }
                videoRecordId = files.get(0);
//                uploadRecordToServer();
                ((Button) findViewById(R.id.qav_topbar_record)).setTextColor(getResources().getColor(R.color.white));
                ((Button) findViewById(R.id.qav_topbar_record)).setText("录制");
                Toast.makeText(getApplicationContext(), "stop record success", Toast.LENGTH_SHORT).show();


            }
        });
        Log.d(TAG, "success");
    }

    protected void onCheckedChanged(boolean checked) {
        AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
        if (checked) {
            Log.d(TAG, "audio Mic set true ");
            mButtonMute.setBackgroundResource(R.drawable.mic1);
            avAudioCtrl.enableMic(true);
        } else {
            Log.d(TAG, "audio Mic set false ");
            avAudioCtrl.enableMic(false);
            mButtonMute.setBackgroundResource(R.drawable.mic3);
        }
    }

    private boolean currentCameraIsFront = true;

    private void onSwitchCamera() {
        boolean isFront = mQavsdkControl.getIsFrontCamera();
        Log.d(TAG, "onSwitchCamera 111111  " + isFront);

        mSwitchCameraErrorCode = mQavsdkControl.toggleSwitchCamera();
        Log.d(TAG, "onSwitchCamera() switchCamera!!  " + mSwitchCameraErrorCode);
        refreshCameraUI();
        if (mSwitchCameraErrorCode != AVError.AV_OK) {
            showDialog(isFront ? DIALOG_SWITCH_BACK_CAMERA_FAILED : DIALOG_SWITCH_FRONT_CAMERA_FAILED);
            mQavsdkControl.setIsInSwitchCamera(false);
            refreshCameraUI();
        } else {

//            Log.d(TAG, "onSwitchCamera " + currentCameraIsFront);
        }
    }

    private void onCloseVideo() {
//        if (mSelfUserInfo.isCreater() == true)
//            stopRecord();
        stopOrientationListener();
//		showDialog(DIALOG_DESTROY);
        if (mSelfUserInfo.isCreater() != true) {
            Util.switchWaitingDialog(ctx, mDialogAtDestroy, DIALOG_DESTROY, true);
        }
        if (mIsSuccess) {
            mChatTimer.cancel();
            mVideoTimer.cancel();
            timer.cancel();
            mHeartClickTimer.cancel();
        }
        //退出IM聊天室
//        destroyTIM();
        //退出AV那边群
        mQavsdkControl.exitRoom();

//        if (wakeLock.isHeld())
//            wakeLock.release();
//
//        if (mSelfUserInfo.isCreater() == true) {
//            closeLive();
//            setResult(Util.SHOW_RESULT_CODE);
//            Util.switchWaitingDialog(ctx, mDialogAtDestroy, DIALOG_DESTROY, true);
////            startActivity(new Intent(AvActivity.this, GameOverActivity.class)
////                    .putExtra(Util.EXTRA_ROOM_NUM, roomNum)
////                    .putExtra(Util.EXTRA_LEAVE_MODE,false)
////                    );
//        } else {
//            leaveLive();
//            setResult(Util.VIEW_RESULT_CODE);
//        }
//
//        startActivity(new Intent(AvActivity.this, GameOverActivity.class)
//                        .putExtra(Util.EXTRA_ROOM_NUM, roomNum)
//                        .putExtra(Util.EXTRA_LEAVE_MODE, LEVAE_MODE)
//        );
//        finish();
    }

    private void closeLive() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {
                    Log.d(TAG, "DEBUG " + mRecvIdentifier);
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    System.out.println(object.toString());
                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("closedata", object.toString()));
                    String ret = HttpUtil.PostUrl(HttpUtil.liveCloseUrl, list);
                    Log.d(TAG, "close room" + ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void leaveLive() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    object.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    System.out.println(object.toString());
                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("viewerout", object.toString()));
                    String ret = HttpUtil.PostUrl(HttpUtil.closeLiveUrl, list);
                    Log.d(TAG, "leave room" + ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void onSendMsg() {
        final String msg = mEditTextInputMsg.getText().toString();
        mEditTextInputMsg.setText("");
        if (msg.length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendText(msg);
                }
            }).start();
        }
    }

    private void sendText(String msg) {
        if (msg.length() == 0)
            return;
        try {
            byte[] byte_num = msg.getBytes("utf8");
            if (byte_num.length > 160) {
                mHandler.sendEmptyMessage(ERROR_MESSAGE_TOO_LONG);
                return;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        TIMMessage Nmsg = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(msg);
        if (Nmsg.addElement(elem) != 0) {
            return;
        }
        mConversation.sendMessage(Nmsg, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                if (i == 85) {
                    mHandler.sendEmptyMessage(ERROR_MESSAGE_TOO_LONG);
                } else if (i == 6011) {
                    mHandler.sendEmptyMessage(ERROR_ACCOUNT_NOT_EXIT);
                }
                Log.e(TAG, "send message failed. code: " + i + " errmsg: " + s);
//                getMessage();
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                Log.i(TAG, "Send text Msg ok");
                Message msg = new Message();
                msg.what = REFRESH_CHAT;
                msg.obj = timMessage;
                mHandler.sendMessage(msg);

//                showTextMessage(timMessage);
//                getMessage();
            }
        });
    }

    private void showTextMessage(TIMMessage timMessage) {
        Log.w(TAG, "showTextMessage ");
        TIMElem elem = timMessage.getElement(0);
        ChatEntity entity = new ChatEntity();
        entity.setElem(elem);
        entity.setIsSelf(timMessage.isSelf());
        Log.d(TAG, "showTextMessage  isSelf " + timMessage.isSelf());
        entity.setTime(timMessage.timestamp());
        entity.setType(timMessage.getConversation().getType());
        entity.setSenderName(timMessage.getSender());
        entity.setStatus(timMessage.status());
        mArrayListChatEntity.add(entity);
        mChatMsgListAdapter.notifyDataSetChanged();
        mListViewMsgItems.setVisibility(View.VISIBLE);
        if (mListViewMsgItems.getCount() > 1) {
            if (mIsLoading)
                mListViewMsgItems.setSelection(0);
            else
                mListViewMsgItems.setSelection(mListViewMsgItems.getCount() - 1);
//                mListViewMsgItems.setClickable(false);
        }
    }


    private void handleCustomMsg(TIMElem elem) {
        Log.i(TAG, " inviteVC handleCustomMsg  ");
        try {
            String customText = new String(((TIMCustomElem) elem).getData(), "UTF-8");
            Log.i(TAG, " inviteVC handleCustomMsg  :" + customText);
            String splitItems[] = customText.split("&");
//            if(splitItems.length<=3){
//                splitItems[3]="";
//            }
            int cmd = Integer.parseInt(splitItems[1]);
            for (int i = 0; i < splitItems.length; ++i) {
                Log.d(TAG, " splitItems :" + splitItems[i] + " loop " + i);
            }
            switch (cmd) {
                case PRIASE_MSG:
                    int num = Integer.parseInt(splitItems[2]);
                    praiseNum += num;
                    mPraiseNum.setText("" + praiseNum);
                    break;
                //用户登录消息
                case MEMBER_ENTER_MSG:
                    boolean isExist = false;
                    //判断是否已经群组存在
                    for (int i = 0; i < mMemberList.size(); ++i) {

                        String userPhone = mMemberList.get(i).getUserPhone();
                        if (userPhone.equals(splitItems[0])) {
                            isExist = true;
                            Log.d(TAG, " willguo handleCustomMsg isExist = true  ");
                            break;
                        }
                    }
                    //不存在增加
                    if (!isExist) {
                        Log.d(TAG, "willguo handleCustomMsg  isExist = false");
                        MemberInfo member = null;
                        //包含完整信息
                        if (splitItems.length <= 3) {
                            member = new MemberInfo(splitItems[0], splitItems[2], "");
                        } else {
                            member = new MemberInfo(splitItems[0], splitItems[2], splitItems[3]);
                        }
                        if (!member.getUserPhone().equals(mSelfUserInfo.getUserPhone())) {
                            mMemberList.add(member);
                            mNormalMemberList.add(member);
                            mMemberListButton.setText("" + mMemberList.size());
                        }
//                        updateMemberHeadImage();
                        mHandler.sendEmptyMessage(UPDAT_MEMBER);
                    }
                    break;
                //用户登出消息
                case MEMBER_EXIT_MSG:
                    for (int i = 0; i < mMemberList.size(); ++i) {
                        String userPhone = mMemberList.get(i).getUserPhone();
                        if (userPhone.equals(splitItems[0])) {
                            Log.d(TAG, "handleCustomMsg member leave userPhone " + userPhone);

                            mQavsdkControl.closeMemberView(userPhone);
                            mMemberList.remove(i);
                            viewIndexRemove(userPhone);
                            MemberInfo member = findMemberInfo(mVideoMemberList, userPhone);
                            if (member != null) {
                                Log.d(TAG, "before  mVideoMemberList remove   " + mVideoMemberList.size());
                                mVideoMemberList.remove(member);
                                Log.d(TAG, "after mVideoMemberList remove " + mVideoMemberList.size());

                            } else {
                                MemberInfo normalMember = findMemberInfo(mNormalMemberList, userPhone);
                                mNormalMemberList.remove(normalMember);
                            }
                            mMemberListButton.setText("" + mMemberList.size());

                        }
                    }
                    updateMemberView();
                    break;
                case VIDEOCHAT_INVITE:
                    showInviteDialog();
                    break;
                case YES_I_JOIN:
                    //对方答应加入
                    String memberIdentifier = splitItems[0];
                    Log.i(TAG, "handleCustomMsg YES_I_JOIN+ " + memberIdentifier);
                    upMemberLevel(memberIdentifier);
//                    acceptHideMaskView(memberIdentifier);
                    requestMultiView(memberIdentifier);
                    acceptHideMaskView(memberIdentifier);
                    break;
                case NO_I_REFUSE:
                    String memberIdentifier2 = splitItems[0];
                    //
                    refuseHideMaskView(memberIdentifier2);
                    Toast.makeText(AvActivity.this, memberIdentifier2 + "memberIdentifier2 refuese !", Toast.LENGTH_SHORT).show();
                    break;
                case MUTEVOICE:
                    AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
                    if (!OpenVoice) {
                        Toast.makeText(AvActivity.this, "host open your voice ", Toast.LENGTH_SHORT).show();
                        avAudioCtrl.enableMic(true);
                        OpenVoice = true;
                    } else {
                        Toast.makeText(AvActivity.this, "host close your voice ", Toast.LENGTH_SHORT).show();
                        avAudioCtrl.enableMic(false);
                        OpenVoice = false;
                    }
                    //关闭Mic
                    break;
                case UNMUTEVOICE:
                    Toast.makeText(AvActivity.this, "host allow your voice again ", Toast.LENGTH_SHORT).show();
                    //开启Mic
                    AVAudioCtrl avAudioCtrl2 = mQavsdkControl.getAVContext().getAudioCtrl();
                    avAudioCtrl2.enableMic(true);
                    break;
                case MUTEVIDEO:
                    if (!mQavsdkControl.getIsEnableCamera()) {
                        //打开你的视频
                        Toast.makeText(AvActivity.this, "host open your camera  ", Toast.LENGTH_SHORT).show();
                        mQavsdkControl.toggleEnableCamera();
                    } else {
                        //关闭你的视频
                        Toast.makeText(AvActivity.this, "host close your camera ", Toast.LENGTH_SHORT).show();
                        mQavsdkControl.toggleEnableCamera();
                    }
                    break;
                case UNMUTEVIDEO:
                    Toast.makeText(AvActivity.this, "host allow your video again ", Toast.LENGTH_SHORT).show();
                    //开启Video
//                    if (mQavsdkControl.getIsInOnOffCamera() == false) {
                    mQavsdkControl.toggleEnableCamera();
//                    }
                    break;
                case CLOSEVIDEOSEND:
                    if (inviteDialog != null && inviteDialog.isShowing())
                        inviteDialog.dismiss();
                    Toast.makeText(AvActivity.this, "host close your video  ", Toast.LENGTH_SHORT).show();

                    if (mQavsdkControl.getIsEnableCamera() == true) {
                        mQavsdkControl.toggleEnableCamera();
                    }
                    AVAudioCtrl avAudioCtrl3 = mQavsdkControl.getAVContext().getAudioCtrl();
                    avAudioCtrl3.enableMic(false);
                    OpenVoice = false;

                default:
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, " inviteVC handleCustomMsg  " + e.toString());
        }
    }

//    public void updateMemberHeadImage() {
//        mHandler.sendEmptyMessage(UPDAT_MEMBER);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ImageUtil tool = new ImageUtil();
//                for (int i = 0; i < mMemberList.size(); ++i) {
//                    if (mMemberList.get(i).getHeadImage() == null) {
//                        //Log.d(TAG, "xujinguang" + mMemberList.get(i).getHeadImagePath());
//                        String param = "?imagepath=" + mMemberList.get(i).getHeadImagePath() + "&width=0&height=0";
//                        Bitmap headBitmap = tool.getImageFromServer(param);
//                        if (i < mMemberList.size())
//                            mMemberList.get(i).setHeadImage(headBitmap);
//                    }
//                }
//                Log.d(TAG, "willguo IMGroupSystem updateMemberHeadImage ");
//                mHandler.sendEmptyMessage(UPDAT_MEMBER);
//            }
//        }).start();
//    }

    /**
     * 邀请Dialog
     */

    private Dialog inviteDialog;

    private void showInviteDialog() {
        inviteDialog = new Dialog(this, R.style.dialog);
        inviteDialog.setContentView(R.layout.vc_invitedialog);
        inviteDialog.setCancelable(false);
        Button exitOk = (Button) inviteDialog.findViewById(R.id.btn_exit_cancel);
        Button exitCancel = (Button) inviteDialog.findViewById(R.id.btn_exit_ok);
        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //接受邀请打开自己摄像头
                if (mQavsdkControl.getIsEnableCamera() == false) {
                    mQavsdkControl.toggleEnableCamera();

                }
                //打开Mic
                AVAudioCtrl avAudioCtrl = mQavsdkControl.getAVContext().getAudioCtrl();
                avAudioCtrl.enableMic(true);
                OpenVoice = true;
                //回应消息
                anwserVCInvitation(mHostIdentifier, true);
                inviteDialog.dismiss();
            }
        });

        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrientationListener();
                //回应拒绝消息
                anwserVCInvitation(mHostIdentifier, false);
                inviteDialog.dismiss();
            }
        });
        stopOrientationListener();
        inviteDialog.show();
    }

    private void initGridView() {
        mGridView = (GridView) findViewById(R.id.grid);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        int itemWidth = (int) (slength * density);
        mGridView.setColumnWidth(itemWidth);
        mGridView.setHorizontalSpacing(0);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

    }


    private void updateMemberView() {
        Log.d(TAG, "IMGroupSystem updateMemberView memberNum " + mVideoMemberList.size());
        if (mMemberListDialog != null)
            mMemberListDialog.refreshMemberData(mNormalMemberList, mVideoMemberList);
        mMemberListButton.setText("" + mMemberList.size());
        if (hostMember != null) {
            String headurl = HttpUtil.rootUrl + "?imagepath=" + hostMember.getHeadImagePath() + "&width=0&height=0";
            imageLoader.displayImage(headurl, hostHead);
        }

    }

//    public class GridViewAdapter extends BaseAdapter {
//        Context context;
//        List<MemberInfo> list;
//
//        public GridViewAdapter(Context context, List<MemberInfo> list) {
//            this.list = list;
//            this.context = context;
//        }
//
//        @Override
//        public int getCount() {
//            return list.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return list.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d(TAG, "Adapter getView  " + position);
//            LayoutInflater layoutInflater = LayoutInflater.from(context);
//            convertView = layoutInflater.inflate(R.layout.member_head_image, null);
//            CircularImageButton headImage = (CircularImageButton) convertView.findViewById(R.id.head_image);
//            headImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            MemberInfo item = list.get(position);
//            headImage.setImageBitmap(item.getHeadImage());
//            return convertView;
//        }
//    }

    private void onSendPraise() {
        String message = mSelfUserInfo.getUserPhone() + "&" + PRIASE_MSG + "&" + 1;
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "priase error", Toast.LENGTH_SHORT).show();
        else {
            mConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "send praise error " + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    mHandler.sendEmptyMessage(REFRESH_PRAISE);
                    Log.i(TAG, "send priase success");
                }
            });
        }
//        getMessage();
        sendPraiseToServer();
    }

    private void sendPraiseToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject object = new JSONObject();
                try {
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    object.put("addnum", 1);
                    System.out.println(object.toString());
                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("praisedata", object.toString()));
                    String ret = HttpUtil.PostUrl(HttpUtil.liveAddPraiseUrl, list);
                    Log.e(TAG, "send praise to server return: " + ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 发一条消息告诉大家 自己上线了
     */
    private void onMemberEnter() {
        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message = mSelfUserInfo.getUserPhone() + "&"
                + MEMBER_ENTER_MSG + "&"
                + mSelfUserInfo.getUserName() + "&"
                + mSelfUserInfo.getHeadImagePath() + "&" + "inedex: " + mQavsdkApplication.getEnterIndex() + "&";


        Log.d(TAG, "onMemberEnter " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {

            mConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "msgSystem send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        final String msg = "轻轻地“" + mSelfUserInfo.getUserName() + "”来了";
        if (msg.length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendText(msg);
                }
            }).start();
        }
    }


    /**
     * 发起邀请消息
     */
    private void sendVCInvitation(String inviteIdentifier) {

        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message = mSelfUserInfo.getUserPhone() + "&"
                + VIDEOCHAT_INVITE + "&"
                + mSelfUserInfo.getUserName() + "&"
                + inviteIdentifier + "&";


        Log.d(TAG, "inviteVC sendVCInvitation " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {
            inviteIdentifier = "86-" + inviteIdentifier;
            testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, inviteIdentifier);
            testConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "inviteVC sendVCInvitation send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 主播发送成员消息VOICE命令
     */
    private void sendVoiceACK(boolean isMute, String inviteIdentifier) {

        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message;
        if (isMute) {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + MUTEVOICE + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + inviteIdentifier + "&";
        } else {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + UNMUTEVOICE + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + inviteIdentifier + "&";
        }

        Log.d(TAG, "inviteVC sendVCInvitation " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {
            if (!inviteIdentifier.startsWith("86-"))
                inviteIdentifier = "86-" + inviteIdentifier;
            testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, inviteIdentifier);
            testConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "inviteVC sendVCInvitation send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 主播发送成员消息VIDEO命令
     */
    private void sendVideoACK(boolean isMute, String inviteIdentifier) {

        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message;
        if (isMute) {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + MUTEVIDEO + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + inviteIdentifier + "&";
        } else {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + UNMUTEVIDEO + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + inviteIdentifier + "&";
        }

        Log.d(TAG, "inviteVC sendVCInvitation " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {
            if (!inviteIdentifier.startsWith("86-"))
                inviteIdentifier = "86-" + inviteIdentifier;
            testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, inviteIdentifier);
            testConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "inviteVC sendVCInvitation send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void sendCloseVideoMsg(String inviteIdentifier) {

        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message = mSelfUserInfo.getUserPhone() + "&"
                + CLOSEVIDEOSEND + "&"
                + mSelfUserInfo.getUserName() + "&"
                + inviteIdentifier + "&";


        Log.d(TAG, "inviteVC sendVCInvitation " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {
            if (!inviteIdentifier.startsWith("86-"))
                inviteIdentifier = "86-" + inviteIdentifier;
            testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, inviteIdentifier);
            testConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "inviteVC sendVCInvitation send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void anwserVCInvitation(String hostidentifier, boolean anwser) {
        mQavsdkApplication.enterPlusPlus();
        if (mSelfUserInfo.getUserName() == null) {
            mSelfUserInfo.setUserName("null");
        }
        if (mSelfUserInfo.getHeadImagePath().equals("")) {
            mSelfUserInfo.setHeadImagePath("null");
        }
        String message;
        if (anwser) {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + YES_I_JOIN + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + hostidentifier + "&";
        } else {
            message = mSelfUserInfo.getUserPhone() + "&"
                    + NO_I_REFUSE + "&"
                    + mSelfUserInfo.getUserName() + "&"
                    + hostidentifier + "&";
        }


        Log.d(TAG, "anwserVCInvitation " + message);
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "enter error", Toast.LENGTH_SHORT).show();
        else {
            hostidentifier = "86-" + hostidentifier;
            testConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, hostidentifier);
            testConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "enter error" + i + ": " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "anwserVCInvitation send groupmsg enter  success :" + text);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }


    /**
     * 发一条消息通知大家 自己下线了
     */
    private void onMemberExit() {
        mQavsdkApplication.exitPlusPlus();
        String message = mSelfUserInfo.getUserPhone() + "&"
                + MEMBER_EXIT_MSG + "&" + "inedex: " + mQavsdkApplication.getExitIndex() + "&";
        ;
        TIMMessage Tim = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(message.getBytes());
        elem.setDesc(UNREAD);
        if (1 == Tim.addElement(elem))
            Toast.makeText(getApplicationContext(), "exit error", Toast.LENGTH_SHORT).show();
        else {
            mConversation.sendMessage(Tim, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "exit error" + i + ": " + s);

                    mHandler.sendEmptyMessage(MEMBER_EXIT_COMPLETE);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {

                    TIMCustomElem elem = (TIMCustomElem) (timMessage.getElement(0));
                    try {
                        String text = new String(elem.getData(), "utf-8");
                        Log.i(TAG, "msgSystem send groupmsg exit  success :" + text);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(MEMBER_EXIT_COMPLETE);
                }
            });
        }
        mMemberList.clear();
    }

    private void sendCloseMsg() {
        final String msg = "轻轻地“" + mSelfUserInfo.getUserName() + "”离开了";
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (msg.length() == 0)
                    return;
                try {
                    byte[] byte_num = msg.getBytes("utf8");
                    if (byte_num.length > 160) {
                        mHandler.sendEmptyMessage(ERROR_MESSAGE_TOO_LONG);
                        return;
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                TIMMessage Nmsg = new TIMMessage();
                TIMTextElem elem = new TIMTextElem();
                elem.setText(msg);
                if (Nmsg.addElement(elem) != 0) {
                    return;
                }
                mConversation.sendMessage(Nmsg, new TIMValueCallBack<TIMMessage>() {
                    @Override
                    public void onError(int i, String s) {
                        if (i == 85) {
                            mHandler.sendEmptyMessage(ERROR_MESSAGE_TOO_LONG);
                        } else if (i == 6011) {
                            mHandler.sendEmptyMessage(ERROR_ACCOUNT_NOT_EXIT);
                        }
                        Log.e(TAG, "send message failed. code: " + i + " errmsg: " + s);
                        mHandler.sendEmptyMessage(CLOSE_VIDEO);
                    }

                    @Override
                    public void onSuccess(TIMMessage timMessage) {
                        Log.e(TAG, "Send text Msg ok");
                        mHandler.sendEmptyMessage(CLOSE_VIDEO);
                    }
                });
            }
        }).start();

    }

    //向用户服务器获取成员信息
    private void getMemberInfo() {
        Log.d(TAG, "userServer getMemberInfo hasPullMemberList ? " + hasPullMemberList);
        //值拉一次
//        if (hasPullMemberList == true) {
//            return;
//        }
//        hasPullMemberList = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                JSONObject object = new JSONObject();
                String url = "http://203.195.167.34/test_user_getinfobatch.php";
                String response = "";
                try {
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    Log.d(TAG, object.toString());
                    params.add(new BasicNameValuePair("data", object.toString()));
                    response = HttpUtil.PostUrl(url, params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, TAG + response);
                if (0 == response.length()) {
                    return;
                }
                if (!response.endsWith("}")) {
                    Log.e(TAG, "getMemberInfo response is not json style" + response);
                    return;
                }
                JSONTokener jsonTokener = new JSONTokener(response);
                try {
                    object = (JSONObject) jsonTokener.nextValue();
                    int ret = object.getInt("code");
                    if (ret != 200) {
                        Log.e(TAG, "getMemberInfo error: " + ret);
                        return;
                    }

                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jobject = array.getJSONObject(i);
                        MemberInfo member = new MemberInfo(jobject.getString(Util.EXTRA_USER_PHONE),
                                jobject.getString(Util.EXTRA_USER_NAME),
                                jobject.getString(Util.EXTRA_HEAD_IMAGE_PATH));

                        Log.d(TAG, "getMemberInfo mSelfUserInfo " + mSelfUserInfo.getUserPhone() + "  member UserPhone  " + member.getUserPhone());
                        if (member.getUserPhone().equals(mSelfUserInfo.getUserPhone())) {
                            mSelfUserInfo.setHeadImagePath(member.getHeadImagePath());
                        }


                        //如果自己是主播，拉下的成员列表过滤自己
                        Log.d(TAG, "getMemberInfo mHostIdentifier " + mHostIdentifier);
                        String hostphone;
                        if (mHostIdentifier.startsWith("86-")) {
                            hostphone = mHostIdentifier.substring(3);
                        } else {
                            hostphone = mHostIdentifier;
                        }
                        if (member.getUserPhone().equals(hostphone)) {
                            Log.d(TAG, "getMemberInfo eliminate mHostIdentifier ");
                            hostMember = new MemberInfo(jobject.getString(Util.EXTRA_USER_PHONE),
                                    jobject.getString(Util.EXTRA_USER_NAME),
                                    jobject.getString(Util.EXTRA_HEAD_IMAGE_PATH));
                            mQavsdkApplication.setHostInfo(hostMember);

                            continue;
                        }
//                        Log.d(TAG, "getMemberInfo mSelfUserInfo "+mSelfUserInfo.getUserPhone()+"  member UserPhone  "+member.getUserPhone());
//                        if(member.getUserPhone().equals(mSelfUserInfo.getUserPhone())){
//                            mSelfUserInfo.setHeadImagePath(member.getHeadImagePath());
//                        }

                        mMemberList.add(member);
                        mNormalMemberList = copyToNormalMember();
                    }

                    mHandler.sendEmptyMessage(UPDAT_MEMBER);
//                    updateMemberHeadImage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case DIALOG_INIT:
                dialog = mDialogInit = Util.newProgressDialog(this, R.string.interface_initialization);
                break;
            case DIALOG_AT_ON_CAMERA:
                dialog = mDialogAtOnCamera = Util.newProgressDialog(this, R.string.at_on_camera);
                break;
            case DIALOG_ON_CAMERA_FAILED:
                dialog = Util.newErrorDialog(this, R.string.on_camera_failed);
                break;
            case DIALOG_AT_OFF_CAMERA:
                dialog = mDialogAtOffCamera = Util.newProgressDialog(this, R.string.at_off_camera);
                break;
            case DIALOG_OFF_CAMERA_FAILED:
                dialog = Util.newErrorDialog(this, R.string.off_camera_failed);
                break;

            case DIALOG_AT_SWITCH_FRONT_CAMERA:
                dialog = mDialogAtSwitchFrontCamera = Util.newProgressDialog(this, R.string.at_switch_front_camera);
                break;
            case DIALOG_SWITCH_FRONT_CAMERA_FAILED:
                dialog = Util.newErrorDialog(this, R.string.switch_front_camera_failed);
                break;
            case DIALOG_AT_SWITCH_BACK_CAMERA:
                dialog = mDialogAtSwitchBackCamera = Util.newProgressDialog(this, R.string.at_switch_back_camera);
                break;
            case DIALOG_SWITCH_BACK_CAMERA_FAILED:
                dialog = Util.newErrorDialog(this, R.string.switch_back_camera_failed);
                break;
            case DIALOG_DESTROY:
                dialog = mDialogAtDestroy = Util.newProgressDialog(this, R.string.at_close_room);
                break;
            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_ON_CAMERA_FAILED:
            case DIALOG_OFF_CAMERA_FAILED:
                ((AlertDialog) dialog).setMessage(getString(R.string.error_code_prefix) + mOnOffCameraErrorCode);
                break;
            case DIALOG_SWITCH_FRONT_CAMERA_FAILED:
            case DIALOG_SWITCH_BACK_CAMERA_FAILED:
                ((AlertDialog) dialog).setMessage(getString(R.string.error_code_prefix) + mSwitchCameraErrorCode);
                break;
            default:
                break;
        }
    }

    private void refreshCameraUI() {
        boolean isEnable = mQavsdkControl.getIsEnableCamera();
        boolean isFront = mQavsdkControl.getIsFrontCamera();
        boolean isInOnOffCamera = mQavsdkControl.getIsInOnOffCamera();
        boolean isInSwitchCamera = mQavsdkControl.getIsInSwitchCamera();


        if (isInOnOffCamera) {
            if (isEnable) {
                Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, true);
                Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, false);
            } else {
                Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, false);
//                Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, true);
                Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, false);

            }
        } else {
            Util.switchWaitingDialog(this, mDialogAtOffCamera, DIALOG_AT_OFF_CAMERA, false);
            Util.switchWaitingDialog(this, mDialogAtOnCamera, DIALOG_AT_ON_CAMERA, false);
        }

        if (isInSwitchCamera) {
            if (isFront) {
                Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, true);
                Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, false);
            } else {
                Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, false);
                Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, true);
            }
        } else {
            Util.switchWaitingDialog(this, mDialogAtSwitchBackCamera, DIALOG_AT_SWITCH_BACK_CAMERA, false);
            Util.switchWaitingDialog(this, mDialogAtSwitchFrontCamera, DIALOG_AT_SWITCH_FRONT_CAMERA, false);
        }
    }

    class VideoOrientationEventListener extends OrientationEventListener {
        boolean mbIsTablet = false;

        public VideoOrientationEventListener(Context context, int rate) {
            super(context, rate);
            mbIsTablet = PhoneStatusTools.isTablet(context);
        }

        int mLastOrientation = -25;

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                mLastOrientation = orientation;
                return;
            }

            if (mLastOrientation < 0) {
                mLastOrientation = 0;
            }

            if (((orientation - mLastOrientation) < 20)
                    && ((orientation - mLastOrientation) > -20)) {
                return;
            }

            if (mbIsTablet) {
                orientation -= 90;
                if (orientation < 0) {
                    orientation += 360;
                }
            }
            mLastOrientation = orientation;

            if (orientation > 314 || orientation < 45) {
                if (mQavsdkControl != null) {
                    mQavsdkControl.setRotation(0);
                }
                mRotationAngle = 0;
            } else if (orientation > 44 && orientation < 135) {
                if (mQavsdkControl != null) {
                    mQavsdkControl.setRotation(90);
                }
                mRotationAngle = 90;
            } else if (orientation > 134 && orientation < 225) {
                if (mQavsdkControl != null) {
                    mQavsdkControl.setRotation(180);
                }
                mRotationAngle = 180;
            } else {
                if (mQavsdkControl != null) {
                    mQavsdkControl.setRotation(270);
                }
                mRotationAngle = 270;
            }
        }
    }

    void registerOrientationListener() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new VideoOrientationEventListener(super.getApplicationContext(), SensorManager.SENSOR_DELAY_UI);
        }
    }

    void startOrientationListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.enable();
        }
    }

    void stopOrientationListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    private void updateWallTime() {
        String formatTime;
        String hs, ms, ss;

        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }

        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }

        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }

        formatTime = hs + ":" + ms + ":" + ss;
        mClockTextView.setText(formatTime);
    }

    private void removeChatItem() {
        time += 2;
        int num = mListViewMsgItems.getCount();
//        Log.e(TAG, "lvCount:" + num);
        if (num > 0) {
            for (int i = num - 1; i >= 0; i--) {
                if (mArrayListChatEntity.size() == 0) return;
                if (time - mArrayListChatEntity.get(i).getTime() > 10) {
//                    Log.e(TAG, "remove");
                    mArrayListChatEntity.remove(i);
                }
            }
            mChatMsgListAdapter.notifyDataSetChanged();
            mListViewMsgItems.setVisibility(View.VISIBLE);
        }
    }

    private Dialog dialog;

    private void hostCloseAlertDialog() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.exit_dialog);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.message);
        Button exitOk = (Button) dialog.findViewById(R.id.btn_exit_ok);
        Button exitCancel = (Button) dialog.findViewById(R.id.btn_exit_cancel);
        messageTextView.setText("有" + mMemberList.size() + "人正在看您的直播\n确定结束直播吗？");
        exitOk.setText("结束直播");
        exitCancel.setText("继续直播");
        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onCloseVideo();
                dialog.dismiss();
            }
        });

        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrientationListener();
                dialog.dismiss();
            }
        });
        stopOrientationListener();
        dialog.show();
    }

    private void memberCloseAlertDialog() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.exit_dialog);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.message);
        Button exitOk = (Button) dialog.findViewById(R.id.btn_exit_ok);
        Button exitCancel = (Button) dialog.findViewById(R.id.btn_exit_cancel);
        messageTextView.setText("确认退出吗？");
        exitOk.setText("结束观看");
        exitCancel.setText("继续观看");
        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onMemberExit();
                dialog.dismiss();
            }
        });

        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrientationListener();
                dialog.dismiss();
            }
        });
        stopOrientationListener();
        dialog.show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mIsClicked) {
                    mIsClicked = false;
                    break;
                }
                if (mSelfUserInfo.isCreater())
                    hostCloseAlertDialog();
                else {
                    memberCloseAlertDialog();
//                    onCloseVideo();
                }
                break;
        }

        return false; //这一句很关键
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


    private static final int TIMER_INTERVAL = 1000;
    private TextView tvTipsMsg;
    private boolean showTips = false;
    private TextView tvShowTips;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (showTips) {
                        if (tvTipsMsg != null) {
                            String strTips = mQavsdkControl.getQualityTips();
                            strTips = praseString(strTips);
                            if (!TextUtils.isEmpty(strTips)) {
                                tvTipsMsg.setText(strTips);
                            }
                        }
                    } else {
                        tvTipsMsg.setText("");
                    }
                }
            });
        }
    };

    private String praseString(String video) {
        if (video.length() == 0) {
            return "";
        }
        String result = "";
        String splitItems[];
        String tokens[];
        splitItems = video.split("\\n");
        for (int i = 0; i < splitItems.length; ++i) {
            if (splitItems[i].length() < 2)
                continue;

            tokens = splitItems[i].split(":");
            if (tokens[0].length() == "mainVideoSendSmallViewQua".length()) {
                continue;
            }
            if (tokens[0].endsWith("BigViewQua")) {
                tokens[0] = "mainVideoSendViewQua";
            }
            if (tokens[0].endsWith("BigViewQos")) {
                tokens[0] = "mainVideoSendViewQos";
            }
            result += tokens[0] + ":\n" + "\t\t";
            for (int j = 1; j < tokens.length; ++j)
                result += tokens[j];
            result += "\n\n";
            //Log.d(TAG, "test:" + result);
        }
        //Log.d(TAG, "test:" + result);
        return result;
    }


    private void heartClick() {
        Log.d(TAG, "heartClick click ");
        JSONObject object = new JSONObject();
        try {
            object.put(Util.EXTRA_LIVEPHONE, mSelfUserInfo.getUserPhone());
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("heartTime", object.toString()));
            String ret = HttpUtil.PostUrl(HttpUtil.heartClickUrl, list);
            Log.d(TAG, "hear click" + ret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AVEndpoint.RequestViewListCompleteCallback mRequestViewListCompleteCallback = new AVEndpoint.RequestViewListCompleteCallback() {
        protected void OnComplete(String identifierList[], int count, int result) {
            // TODO
            Log.d(TAG, "RequestViewListCompleteCallback.OnComplete");
        }
    };

    public void showVideoMemberInfo(final String identifer) {
        Log.d(TAG, "showVideoMemberInfo " + identifer);
        String videoIdentifier;
        if (identifer.startsWith("86-")) {
            videoIdentifier = identifer.substring(3);
        } else {
            videoIdentifier = identifer;
        }

        mVideoMemberInfoDialog = new Dialog(this, R.style.dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.video_member_info_dialog, null);


        MemberInfo videoMember;

        if (videoIdentifier.equals(hostMember.getUserPhone())) {
            videoMember = new MemberInfo();
            videoMember.setUserPhone(hostMember.getUserPhone());
            videoMember.setHeadImagePath(hostMember.getHeadImagePath());
            videoMember.setUserName(hostMember.getUserName());

        } else {
            videoMember = findMemberInfo(mMemberList, identifer);
        }

        if (videoMember == null) {
            Toast.makeText(AvActivity.this, "videoMember don't exist ", Toast.LENGTH_SHORT).show();
            return;
        }
        //取得头像和名字
        CircularImageButton memberHead = (CircularImageButton) view.findViewById(R.id.memberinfo_head);
        TextView memberName = (TextView) view.findViewById(R.id.memberinfo_name);
        Button toggleVoice = (Button) view.findViewById(R.id.mute_voice);
        Button toggleVideo = (Button) view.findViewById(R.id.mute_video);
        Button switchVideoBT = (Button) view.findViewById(R.id.switch_video);
        Button hungUp = (Button) view.findViewById(R.id.hung_up);
        TextView line1 = (TextView) view.findViewById(R.id.line_1);
        TextView line2 = (TextView) view.findViewById(R.id.line_2);
        TextView line3 = (TextView) view.findViewById(R.id.line_3);
        memberName.setText("" + videoMember.getUserName());
        String url = HttpUtil.rootUrl + "?imagepath=" + videoMember.getHeadImagePath() + "&width=0&height=0";
        imageLoader.displayImage(url, memberHead);

        TextView cancel = (TextView) view.findViewById(R.id.close_memberInfo);
        toggleVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVoiceACK(true, identifer);
                mVideoMemberInfoDialog.dismiss();
            }
        });

        toggleVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVideoACK(true, identifer);
                mVideoMemberInfoDialog.dismiss();
            }
        });

        hungUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                closeVideoMemberByHost(identifer);
                mVideoMemberInfoDialog.dismiss();

            }
        });


        switchVideoBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQavsdkControl.switchViewwithBG(identifer);
                mVideoMemberInfoDialog.dismiss();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoMemberInfoDialog.dismiss();
            }
        });

        if (videoMember.getUserPhone().equals(hostMember.getUserPhone())) {
            hungUp.setVisibility(View.INVISIBLE);
            toggleVoice.setVisibility(View.INVISIBLE);
            toggleVideo.setVisibility(View.INVISIBLE);
            line1.setVisibility(View.INVISIBLE);
            line2.setVisibility(View.INVISIBLE);
            line3.setVisibility(View.INVISIBLE);
        }
        mVideoMemberInfoDialog.setCanceledOnTouchOutside(true);
        mVideoMemberInfoDialog.setContentView(view);
        mVideoMemberInfoDialog.show();
    }

    //上传wen
    public void uploadRecordToServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject object = new JSONObject();
                try {

                    String time = getCurrentTime();
                    object.put(Util.EXTRA_ROOM_NUM, roomNum);
                    object.put(Util.EXTRA_USER_PHONE, mSelfUserInfo.getUserPhone());
                    object.put(Util.EXTRA_LIVE_TITLE, mQavsdkApplication.getRoomName());
                    object.put(Util.EXTRA_GROUP_ID, groupId);
                    object.put(Util.EXTRA_REPLAYID, videoRecordId);
                    object.put(Util.EXTRA_RECORDTIME, time);

//                    object.put("imagetype", 2);
                    Log.d(TAG, "uploadRecordToServer json: " + object);
                    Log.d(TAG, "uploadRecordToServer " + roomNum + " phone " + mSelfUserInfo.getUserPhone() + " roomtitle " + mQavsdkApplication.getRoomName() + " time " + time);
                    Log.d(TAG, "uploadRecordToServer coverPath" + mQavsdkApplication.getRoomCoverPath());
                    ImageUtil tool = new ImageUtil();
                    int ret = tool.sendCoverToServer(mQavsdkApplication.getRoomCoverPath(), object, HttpUtil.replaycreateUrl, "replaydata");
                    Log.i(TAG, "userServer upload roomInfo to server " + " " + ret);
                    if (ret == 200) {
                        Log.i(TAG, "uploadRecordToServer roomInfo to server success " + " " + ret);
                    } else {
                        Log.e(TAG, "uploadRecordToServer  " + " " + ret);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }

    private void acceptHideMaskView(String memberIdentifier) {
        Log.d(TAG, " viewIndex" + memberIdentifier);
        requestId.remove(memberIdentifier);
        Integer indexI = (Integer) viewIndex.get(memberIdentifier);

        //以当前
        int location = mQavsdkControl.getSmallVideoView();
//        int location = indexI.intValue();
        if (location == 1) {
            //只有一个阴影
            mInviteMastk1.setVisibility(View.GONE);
            //剩余1个人 重新布局mask
            if (requestId.size() == 1) {
                String remainId2 = requestId.get(0);
                MemberInfo inviteMemberInfo2 = findMemberInfo(mMemberList, remainId2);
                String url2 = HttpUtil.rootUrl + "?imagepath=" + inviteMemberInfo2.getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url2, mVideoHead2);
                mInviteMastk2.setVisibility(View.VISIBLE);
            }
            //剩余1个人 重新布局mask
            else if (requestId.size() == 2) {
                //取第一个
                String remainId2 = requestId.get(0);
                MemberInfo inviteMemberInfo2 = findMemberInfo(mMemberList, remainId2);
                String url2 = HttpUtil.rootUrl + "?imagepath=" + inviteMemberInfo2.getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url2, mVideoHead2);
                mInviteMastk2.setVisibility(View.VISIBLE);

                //取第二个
                String remainId3 = requestId.get(1);
                MemberInfo inviteMemberInfo3 = findMemberInfo(mMemberList, remainId3);
                String url3 = HttpUtil.rootUrl + "?imagepath=" + inviteMemberInfo3.getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url3, mVideoHead3);
                mInviteMastk3.setVisibility(View.VISIBLE);

            }
        } else if (location == 2) {
            mInviteMastk1.setVisibility(View.GONE);
            mInviteMastk2.setVisibility(View.GONE);
            if (requestId.size() == 1) {
                String remainId3 = requestId.get(1);
                MemberInfo inviteMemberInfo3 = findMemberInfo(mMemberList, remainId3);
                String url3 = HttpUtil.rootUrl + "?imagepath=" + inviteMemberInfo3.getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url3, mVideoHead3);
                mInviteMastk3.setVisibility(View.VISIBLE);
            }
        } else if (location == 3) {
            mInviteMastk1.setVisibility(View.GONE);
            mInviteMastk2.setVisibility(View.GONE);
            mInviteMastk3.setVisibility(View.GONE);
        }

        mMaskViewCount--;

        Log.d(TAG, "acceptHideMaskView viewIndex" + mMaskViewCount);
    }

    private void refuseHideMaskView(String memberIdentifier) {
        Log.d(TAG, " viewIndex" + memberIdentifier);
        Integer indexI = (Integer) viewIndex.get(memberIdentifier);

        int index = indexI.intValue();
        if (index == 1) {
            mInviteMastk1.setVisibility(View.GONE);
        } else if (index == 2) {
            mInviteMastk1.setVisibility(View.GONE);
            mInviteMastk2.setVisibility(View.GONE);
        } else if (index == 3) {
            mInviteMastk1.setVisibility(View.GONE);
            mInviteMastk2.setVisibility(View.GONE);
            mInviteMastk3.setVisibility(View.GONE);
        }
        mMaskViewCount--;
        requestId.remove(memberIdentifier);
        viewIndex.remove(memberIdentifier);
    }

    private void sendMaskViewStatus(String id) {

        //已存在视频数量
        int nowVideoView = mQavsdkControl.getSmallVideoView();
        //已存在阴影数量
        int nowMaskViewCount = mMaskViewCount;
        //阴影位置
        int masklocation = nowMaskViewCount + nowVideoView + 1;
        //阴影加1
        mMaskViewCount++;

        Log.d(TAG, "requestMultiView  count  " + mQavsdkControl.getSmallVideoView());

        MemberInfo inviteMemberInfo = findMemberInfo(mMemberList, id);
        requestId.add(id);
        if (inviteMemberInfo != null) {
            Log.d(TAG, "sendMaskViewStatus videopath " + inviteMemberInfo.getHeadImagePath());
            String url = HttpUtil.rootUrl + "?imagepath=" + inviteMemberInfo.getHeadImagePath() + "&width=0&height=0";
            if (masklocation == 1) {
                viewIndex.put(id, 1);
                imageLoader.displayImage(url, mVideoHead1);
                mInviteMastk1.setVisibility(View.VISIBLE);
            } else if (masklocation == 2) {
                viewIndex.put(id, 2);
                imageLoader.displayImage(url, mVideoHead2);
                mInviteMastk2.setVisibility(View.VISIBLE);
            } else if (masklocation == 3) {
                viewIndex.put(id, 3);
                imageLoader.displayImage(url, mVideoHead3);
                mInviteMastk3.setVisibility(View.VISIBLE);
            }
        }
    }


    public MemberInfo findMemberInfo(ArrayList<MemberInfo> list, String id) {
        Log.d(TAG, "findMemberInfo id" + id);
        String identifier = "";
        if (id.startsWith("86-")) {
            identifier = id.substring(3);
        } else {
            identifier = id;
        }
        Log.d(TAG, "findMemberInfo identifier " + identifier);
        for (MemberInfo member : list) {

            if (member.getUserPhone().equals(identifier))
                return member;
        }
        return null;
    }

    public void upMemberLevel(String identifier) {
        MemberInfo upMember = findMemberInfo(mNormalMemberList, identifier);
        mVideoMemberList.add(upMember);
        mNormalMemberList.remove(upMember);
        mMemberListDialog.refreshMemberData(mNormalMemberList, mVideoMemberList);
    }

    public void downMemberLevel(String identifier) {
        MemberInfo upMember = findMemberInfo(mVideoMemberList, identifier);
        mNormalMemberList.add(upMember);
        mVideoMemberList.remove(upMember);
        mMemberListDialog.refreshMemberData(mNormalMemberList, mVideoMemberList);
    }

    public ArrayList<MemberInfo> copyToNormalMember() {
        mNormalMemberList = new ArrayList<MemberInfo>();
        for (MemberInfo member : mMemberList) {
            mNormalMemberList.add(member);
        }
        return mNormalMemberList;
    }

    private void closeVideoMemberByHost(String identifer) {
        viewIndexRemove(identifer);
        sendCloseVideoMsg(identifer);
        mQavsdkControl.closeMemberView(identifer);
        downMemberLevel(identifer);
    }

    private void viewIndexRemove(String identifer) {
        if (viewIndex != null) {
            String id;
            if (identifer.startsWith("86-")) {
                id = identifer.substring(3);

            } else {
                id = identifer;
            }
            if (viewIndex.containsKey(id)) {
                viewIndex.remove(id);
            }

        }

    }


    private float getBeautyProgress(int progress) {
        Log.d("shixu", "progress: " + progress);
        return (9.0f * progress / 100.0f);
    }


}