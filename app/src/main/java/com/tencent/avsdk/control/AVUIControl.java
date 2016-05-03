package com.tencent.avsdk.control;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.tencent.av.opengl.GraphicRendererMgr;
import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector;
import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector.OnMoveGestureListener;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.opengl.ui.GLViewGroup;
import com.tencent.av.opengl.utils.Utils;
import com.tencent.av.sdk.AVView;
import com.tencent.av.utils.QLog;
import com.tencent.avsdk.MemberInfo;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AVUIControl extends GLViewGroup {
    static final String TAG = "VideoLayerUI";

    boolean mIsLocalHasVideo = false;// 自己是否有视频画面

    Context mContext = null;
    GraphicRendererMgr mGraphicRenderMgr = null;

    View mRootView = null;
    int mTopOffset = 0;
    int mBottomOffset = 0;
    private int hostIndex = 0;

    GLRootView mGlRootView = null;
    GLVideoView mGlVideoView[] = null;

    int mClickTimes = 0;
    int mTargetIndex = -1;
    OnTouchListener mTouchListener = null;
    GestureDetector mGestureDetector = null;
    MoveGestureDetector mMoveDetector = null;
    ScaleGestureDetector mScaleGestureDetector = null;
    private QavsdkControl mQavsdkControl;
    private final static int DELAY_CLOSE_VIEW = 0;

    private int localViewIndex = -1;
    private int remoteViewIndex = -1;
    private String mRemoteIdentifier = "";
    private Dialog mVideoMemberInfoDialog;
    private String hostIdentifer = "";
    private boolean isSupportMultiVideo = false;

    private HashMap mapViewAndIdentifier;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder.Callback mSurfaceHolderListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mContext.sendBroadcast(new Intent(Util.ACTION_SURFACE_CREATED));
            mCameraSurfaceCreated = true;

            QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
            if (qavsdk.getRoom() != null)
            {
                qavsdk.getAVContext().setRenderMgrAndHolder(mGraphicRenderMgr, holder);
            }
            Log.e("memoryLeak", "memoryLeak surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null) {
                return;
            }
            holder.setFixedSize(width, height);
            Log.e("memoryLeak", "memoryLeak surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e("memoryLeak", "memoryLeak surfaceDestroyed");
        }
    };


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DELAY_CLOSE_VIEW:
                    int index = (int) msg.obj;
                    closeVideoView(index);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public AVUIControl(Context context, View rootView) {
        mContext = context;
        mRootView = rootView;
        mGraphicRenderMgr = new GraphicRendererMgr();
        initQQGlView();
        initCameraPreview();
        initVideoParam();
        mapViewAndIdentifier = new HashMap();
        mQavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
    }

    private void initVideoParam() {
        QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
        if (null != qavsdkControl && qavsdkControl.getIsSupportMultiView()) {
            isSupportMultiVideo = true;
        }

        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "isSupportMultiVideo: " + isSupportMultiVideo);
        }
    }

    @Override
    protected void onLayout(boolean flag, int left, int top, int right, int bottom) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "onLayout|left: " + left + ", top: " + top + ", right: " + right + ", bottom: " + bottom);
        }
        layoutVideoView3(false);
    }

    public void showGlView() {
        if (mGlRootView != null) {
            mGlRootView.setVisibility(View.VISIBLE);
        }
    }

    public void hideGlView() {
        if (mGlRootView != null) {
            mGlRootView.setVisibility(View.GONE);
        }
    }

    public void onResume() {
        if (mGlRootView != null) {
            mGlRootView.onResume();
        }

        setRotation(mCacheRotation);
    }

    public void onPause() {
        if (mGlRootView != null) {
            mGlRootView.onPause();
        }
    }

    public void onDestroy() {
        Log.i("onDestroy", " AVUIControl onDestroy");
        unInitCameraaPreview();
        mContext = null;
        mRootView = null;

        removeAllView();
        mapViewAndIdentifier.clear();
        for (int i = 0; i < mGlVideoView.length; i++) {
            mGlVideoView[i].flush();
            mGlVideoView[i].clearRender();
            mGlVideoView[i] = null;
        }
        mGlRootView.setOnTouchListener(null);
        mGlRootView.setContentPane(null);

        mTouchListener = null;
        mGestureDetector = null;
        mMoveDetector = null;
        mScaleGestureDetector = null;

        mGraphicRenderMgr = null;

        mGlRootView = null;
        mGlVideoView = null;
    }

    public void enableDefaultRender() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        qavsdk.getAVContext().setRenderFunctionPtr(mGraphicRenderMgr.getRecvDecoderFrameFunctionptr());
    }

    public boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier) {
        if (mContext == null)
            return false;

        if (Utils.getGLVersion(mContext) == 1) {
            return false;
        }


        if (isLocalHasVideo) {// 打开摄像头
            GLVideoView view = null;
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index < 0) {
                index = getIdleViewIndex(0);
                if (index >= 0) {
                    Log.d(TAG, "setLocalHasVideo   " + identifier);
//                    if (!identifier.startsWith("86-")) {
//                        identifier = "86-" + identifier;
//                    }

                    view = mGlVideoView[index];
                    view.setRender(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
                    mapViewAndIdentifier.put(identifier, index);
                    localViewIndex = index;
                }
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(false);
                view.enableLoading(false);
                // if (isFrontCamera()) {
                // view.setMirror(true);
                // } else {
                // view.setMirror(false);
                // }
                view.setVisibility(GLView.VISIBLE);
            }
            if (forceToBigView && index > 0) {
                switchVideo(0, index);
            }
        } else if (!isLocalHasVideo) {// 关闭摄像头
            int index = getViewIndexById(identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index >= 0) {
                closeVideoView(index);
                localViewIndex = -1;
            }
        }
        mIsLocalHasVideo = isLocalHasVideo;

        return true;
    }

    public void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo, boolean forceToBigView, boolean isPC) {
        boolean needForceBig = forceToBigView;
        if (mContext == null)
            return;
        if (Utils.getGLVersion(mContext) == 1) {
            isRemoteHasVideo = false;
            return;
        }
        if (!forceToBigView && !isLocalFront()) {
            forceToBigView = true;
        }

        if (isRemoteHasVideo) {// 打开对方画面
            GLVideoView view = null;
            int index = getViewIndexById(identifier, videoSrcType);
            if (index < 0) {
                index = getIdleViewIndex(0);
                if (index >= 0) {
                    view = mGlVideoView[index];
                    view.setRender(identifier, videoSrcType);
                    remoteViewIndex = index;
                    mRemoteIdentifier = identifier;
                }
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(isPC);
                view.setMirror(false);
                if (needForceBig && videoSrcType == AVView.VIDEO_SRC_TYPE_SCREEN) {
                    view.enableLoading(false);
                } else {
                    view.enableLoading(true);
                }
                view.setVisibility(GLView.VISIBLE);
            }
            if (forceToBigView && index > 0) {
                switchVideo(0, index);
            }
        } else {// 关闭对方画面
            int index = getViewIndexById(identifier, videoSrcType);
            if (index >= 0) {
                closeVideoView(index);
                remoteViewIndex = -1;
            }
        }
    }

    int mRotation = 0;
    int mCacheRotation = 0;

    public void setRotation(int rotation) {
        // 在没有画面前，ratation可能计算有误，此时的值如果设置下去会使用错误角度值，直到ratation大角度变化，新的值才能生效
        // 这种情况下把当前rotation缓存起来，等到RefreshUI时再设置
//		if (!mIsLocalHasVideo && !isRemoteHasVideo()) {
//			mCacheRotation = rotation;
//			return;
//		}
//
//		if (rotation == mRotation && rotation != mCacheRotation) {
//			return;
//		}
        if (mContext == null) {
            return;
        }

        if ((rotation % 90) != (mRotation % 90)) {
            mClickTimes = 0;
        }

        mRotation = rotation;
        mCacheRotation = rotation;

        // layoutVideoView(true);
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        if ((qavsdk != null) && (qavsdk.getAVVideoControl() != null)) {
            qavsdk.getAVVideoControl().setRotation(rotation);
        }
        switch (rotation) {
            case 0:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(0);
                }
                break;
            case 90:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(90);
                }
                break;
            case 180:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(180);
                }
                break;
            case 270:
                for (int i = 0; i < getChildCount(); i++) {
                    GLView view = getChild(i);
                    if (view != null)
                        view.setRotation(270);
                }
                break;
            default:
                break;
        }
    }

    public String getQualityTips() {
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        String audioQos = "";
        String videoQos = "";
        String roomQos = "";

        if (qavsdk != null) {
            if (qavsdk.getAVAudioControl() != null) {
                audioQos = qavsdk.getAVAudioControl().getQualityTips();
            }
            if (qavsdk.getAVVideoControl() != null) {
                videoQos = qavsdk.getAVVideoControl().getQualityTips();
            }

            if (qavsdk.getRoom() != null) {
                roomQos = qavsdk.getRoom().getQualityTips();
            }
        }

        if (audioQos != null && videoQos != null && roomQos != null) {
            return audioQos + videoQos + roomQos;
        } else {
            return "";
        }

    }

    public void setOffset(int topOffset, int bottomOffset) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setOffset topOffset: " + topOffset + ", bottomOffset: " + bottomOffset);
        }
        mTopOffset = topOffset;
        mBottomOffset = bottomOffset;
        // refreshUI();
        layoutVideoView(true);
    }

    public void setText(String identifier, int videoSrcType, String text, float textSize, int color) {
        int index = getViewIndexById(identifier, videoSrcType);
        if (index < 0) {
            index = getIdleViewIndex(0);
            if (index >= 0) {
                GLVideoView view = mGlVideoView[index];
                view.setRender(identifier, videoSrcType);
            }
        }
        if (index >= 0) {
            GLVideoView view = mGlVideoView[index];
            view.setVisibility(GLView.VISIBLE);
            view.setText(text, textSize, color);
        }
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setText identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", text: " + text + ", textSize: " + textSize + ", color: " + color + ", index: " + index);
        }
    }

    public void setBackground(String identifier, int videoSrcType, Bitmap bitmap, boolean needRenderVideo) {
        int index = getViewIndexById(identifier, videoSrcType);
        if (index < 0) {
            index = getIdleViewIndex(0);
            if (index >= 0) {
                GLVideoView view = mGlVideoView[index];
                view.setVisibility(GLView.VISIBLE);
                view.setRender(identifier, videoSrcType);
            }
        }
        if (index >= 0) {
            GLVideoView view = mGlVideoView[index];
            view.setBackground(bitmap);
            view.setNeedRenderVideo(needRenderVideo);
            if (!needRenderVideo) {
                view.enableLoading(false);
            }
        }
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setBackground identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", index: " + index + ", needRenderVideo: " + needRenderVideo);
        }
    }

    public void onVideoSrcTypeChanged(String identifier, int oldVideoSrcType, int newVideoSrcType) {
        int index = getViewIndexById(identifier, oldVideoSrcType);
        if (index >= 0) {
            GLVideoView view = mGlVideoView[index];
            view.clearRender();
            view.setRender(identifier, newVideoSrcType);
            if (index == 0 && newVideoSrcType == AVView.VIDEO_SRC_TYPE_SCREEN) {
                view.enableLoading(false);
            } else {
                view.enableLoading(true);
            }
        }
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "onVideoSrcTypeChanged identifier: " + identifier + ", oldVideoSrcType: " + oldVideoSrcType + ", newVideoSrcType: " + newVideoSrcType + ", index: " + index);
        }
    }

    boolean isLocalFront() {
        boolean isLocalFront = true;
        String selfIdentifier = "";
        GLVideoView view = mGlVideoView[0];
        if (view.getVisibility() == GLView.VISIBLE && selfIdentifier.equals(view.getIdentifier())) {
            isLocalFront = false;
        }
        return isLocalFront;
    }

    boolean isLocalHasVideo(String selfIdentifier) {
        boolean isLocalHasVideo = false;
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (view.getVisibility() == GLView.VISIBLE && selfIdentifier.equals(view.getIdentifier())) {
                isLocalHasVideo = true;
                break;
            }
        }
        return isLocalHasVideo;
    }

    boolean isRemoteHasVideo() {
        boolean isRemoteHasVideo = false;
        String selfIdentifier = "";
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (view.getVisibility() == GLView.VISIBLE && !selfIdentifier.equals(view.getIdentifier())) {
                isRemoteHasVideo = true;
                break;
            }
        }
        return isRemoteHasVideo;
    }

    int getViewCount() {
        int count = 0;
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (view.getVisibility() == GLView.VISIBLE && null != view.getIdentifier()) {
                count++;
            }
        }
        return count;
    }

    int getIdleViewIndex(int start) {
        int index = -1;
        for (int i = start; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if (null == view.getIdentifier() || view.getVisibility() == GLView.INVISIBLE) {
                index = i;
                break;
            }
        }
        return index;
    }

    int getViewIndexById(String identifier, int videoSrcType) {
        int index = -1;
        if (null == identifier) {
            return index;
        }
        for (int i = 0; i < mGlVideoView.length; i++) {
            GLVideoView view = mGlVideoView[i];
            if ((identifier.equals(view.getIdentifier()) && view.getVideoSrcType() == videoSrcType) && view.getVisibility() == GLView.VISIBLE) {
                index = i;
                break;
            }
        }
        return index;
    }

    void layoutVideoView(boolean virtical) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "layoutVideoView virtical: " + virtical);
        }
        if (mContext == null)
            return;

        int width = getWidth();
        int height = getHeight();

        Log.d(TAG, "width: " + getWidth() + "height: " + getHeight());

        mGlVideoView[0].layout(0, 0, width, height);
        mGlVideoView[0].setBackgroundColor(Color.BLACK);
        //
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = edgeX;
        if (mBottomOffset != 0) {
            edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
        }
        final int w = (width - edgeX * 2) / 4;
        final int h = w;
        //
        int left = 0;
        int right = 0;
        int top = height - h - edgeY - mBottomOffset;
        int bottom = height - edgeY - mBottomOffset;

        if (isSupportMultiVideo) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "SupportMultiVideo");
            }

            //多人画面的位置为了不与下面的关闭免提，打开麦克风等按钮重复，需要重新设计其位置，暂时置于视图中间
            top = (height - h) / 2;
            bottom = (height + h) / 2;

            if (virtical) {
                left = mGlVideoView[1].getBounds().left;
                right = mGlVideoView[1].getBounds().right;
            } else {
                left = width - w - edgeX;
                right = width - edgeX;
            }
            mGlVideoView[1].layout(left, top, right, bottom);
            if (virtical) {
                left = mGlVideoView[2].getBounds().left;
                right = mGlVideoView[2].getBounds().right;
            } else {
                right = left;
                left = right - w;
            }
            mGlVideoView[2].layout(left, top, right, bottom);
            if (virtical) {
                left = mGlVideoView[3].getBounds().left;
                right = mGlVideoView[3].getBounds().right;
            } else {
                right = left;
                left = right - w;
            }
            mGlVideoView[3].layout(left, top, right, bottom);
            if (virtical) {
                left = mGlVideoView[4].getBounds().left;
                right = mGlVideoView[4].getBounds().right;
            } else {
                right = left;
                left = right - w;
            }
            mGlVideoView[4].layout(left, top, right, bottom);
            //
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
            mGlVideoView[2].setBackgroundColor(Color.WHITE);
            mGlVideoView[3].setBackgroundColor(Color.WHITE);
            mGlVideoView[4].setBackgroundColor(Color.WHITE);
            //
            mGlVideoView[1].setPaddings(2, 3, 3, 3);
            mGlVideoView[2].setPaddings(2, 3, 2, 3);
            mGlVideoView[3].setPaddings(2, 3, 2, 3);
            mGlVideoView[4].setPaddings(3, 3, 2, 3);
        } else {
            int wRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
            int hRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
            int edgeXRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
            int edgeYRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
            left = edgeXRemote;
            right = left + wRemote;
            top = edgeYRemote + mTopOffset;
            bottom = top + hRemote;

            mGlVideoView[1].layout(left, top, right, bottom);

            //
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[2].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[3].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[4].setBackgroundColor(Color.WHITE);
            //
            //		mGlVideoView[1].setPaddings(2, 3, 3, 3);
            //		mGlVideoView[2].setPaddings(2, 3, 2, 3);
            //		mGlVideoView[3].setPaddings(2, 3, 2, 3);
            //		mGlVideoView[4].setPaddings(3, 3, 2, 3);
        }

        invalidate();
    }


    void layoutVideoView3(boolean virtical) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "layoutVideoView virtical: " + virtical);
        }
        if (mContext == null)
            return;

        int width = getWidth();
        int height = getHeight();

        Log.d(TAG, "width: " + getWidth() + "height: " + getHeight());

        mGlVideoView[0].layout(0, 0, width, height);
        mGlVideoView[0].setBackgroundColor(Color.BLACK);
        //
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);

        final int margintop = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_margin_top);
        final int marginbottom = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_margin_bottom);
        final int areaW = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_width);
        final int areaH = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_height);
        final int marginWight = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_marginright);
        final int marginbetween = mContext.getResources().getDimensionPixelSize(R.dimen.small_area_marginbetween);
        //
        int left = 0;
        int right = 0;
        int top = edgeY;
        int bottom = height - edgeY - mBottomOffset;

        if (isSupportMultiVideo) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "SupportMultiVideo");
            }

            //多人画面的位置为了不与下面的关闭免提，打开麦克风等按钮重复，需要重新设计其位置，暂时置于视图中间

            left = width - marginWight - areaW;
            right = width - marginWight;

            if (virtical) {
                left = mGlVideoView[1].getBounds().left;
                right = mGlVideoView[1].getBounds().right;
            } else {
                bottom = height - marginbottom;
                top = bottom - areaH;
            }
            mGlVideoView[1].layout(left, top, right, bottom);
            if (virtical) {
                left = mGlVideoView[2].getBounds().left;
                right = mGlVideoView[2].getBounds().right;
            } else {
                bottom = top - marginbetween;
                top = bottom - areaH;

            }
            mGlVideoView[2].layout(left, top, right, bottom);
            if (virtical) {
                left = mGlVideoView[3].getBounds().left;
                right = mGlVideoView[3].getBounds().right;
            } else {
                bottom = top - marginbetween;
                top = bottom - areaH;
            }
            mGlVideoView[3].layout(left, top, right, bottom);
//            if (virtical) {
//                left = mGlVideoView[4].getBounds().left;
//                right = mGlVideoView[4].getBounds().right;
//            } else {
//                top = bottom;
//                bottom =top+areaH;
//            }
            mGlVideoView[4].layout(0, 0, 0, 0);
            //
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
            mGlVideoView[2].setBackgroundColor(Color.WHITE);
            mGlVideoView[3].setBackgroundColor(Color.WHITE);
            mGlVideoView[4].setBackgroundColor(Color.WHITE);
            //
            mGlVideoView[1].setPaddings(2, 3, 3, 3);
            mGlVideoView[2].setPaddings(2, 3, 2, 3);
            mGlVideoView[3].setPaddings(2, 3, 2, 3);
            mGlVideoView[4].setPaddings(3, 3, 2, 3);
        } else {
            int wRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
            int hRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
            int edgeXRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
            int edgeYRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
            left = edgeXRemote;
            right = left + wRemote;
            top = edgeYRemote + mTopOffset;
            bottom = top + hRemote;

            mGlVideoView[1].layout(left, top, right, bottom);

            //
            mGlVideoView[1].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[2].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[3].setBackgroundColor(Color.WHITE);
            //		mGlVideoView[4].setBackgroundColor(Color.WHITE);
            //
            //		mGlVideoView[1].setPaddings(2, 3, 3, 3);
            //		mGlVideoView[2].setPaddings(2, 3, 2, 3);
            //		mGlVideoView[3].setPaddings(2, 3, 2, 3);
            //		mGlVideoView[4].setPaddings(3, 3, 2, 3);
        }

        invalidate();
    }


    void closeVideoView(int index) {
        Log.d(TAG, " closeMemberVideoView " + index + " getclose " + getViewCount());
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "closeVideoView index: " + index);
        }
        //删除index
        int deleteindex = index;
        //当前界面有4个
        if (getViewCount() == 4) {
            switch (index) {
                case 3:
                    deleteindex = 3;
                    break;
                case 2:
                    switchIdentifer(2, 3);
                    switchVideo(2, 3);
                    deleteindex = 3;
                case 1:
                    switchVideo(3, 1);
                    switchIdentifer(3, 1);
                    deleteindex = 3;
            }

        } else if (getViewCount() == 3) {
            switch (index) {
                case 2:
                    deleteindex = 2;
                    break;
                case 1:
                    switchVideo(1, 2);
                    switchIdentifer(1, 2);
                    deleteindex = 2;
            }

        }
        Object ob = getKeyFromValue(mapViewAndIdentifier, deleteindex);
        mapViewAndIdentifier.remove(ob);
        GLVideoView view = mGlVideoView[deleteindex];
        view.setVisibility(GLView.INVISIBLE);
        view.setNeedRenderVideo(true);
        view.enableLoading(false);
        view.setIsPC(false);
        view.clearRender();

//		for (int i = 0; i < mGlVideoView.length - 1; i++) {
//			GLVideoView view1 = mGlVideoView[i];
//			for (int j = i + 1; j < mGlVideoView.length; j++) {
//				GLVideoView view2 = mGlVideoView[j];
//				if (view1.getVisibility() == GLView.INVISIBLE && view2.getVisibility() == GLView.VISIBLE) {
//					String openId = view2.getOpenId();
//					int videoSrcType = view2.getVideoSrcType();
//					boolean isPC = view2.isPC();
//					boolean isMirror = view2.isMirror();
//					boolean isLoading = view2.isLoading();
//					view1.setRender(openId, videoSrcType);
//					view1.setIsPC(isPC);
//					view1.setMirror(isMirror);
//					view1.enableLoading(isLoading);
//					view1.setVisibility(GLView.VISIBLE);
//					view2.setVisibility(GLView.INVISIBLE);
//				}
//			}
//		}

//        layoutVideoView(false);
        layoutVideoView3(false);
    }

    void initQQGlView() {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "initQQGlView");
        }
        mGlRootView = (GLRootView) mRootView.findViewById(R.id.av_video_glview);
        mGlVideoView = new GLVideoView[5];
        // for (int i = 0; i < mGlVideoView.length; i++) {
        // mGlVideoView[i] = new GLVideoView(mVideoController, mContext.getApplicationContext());
        // mGlVideoView[i].setVisibility(GLView.INVISIBLE);
        // addView(mGlVideoView[i]);
        // }
        mGlVideoView[0] = new GLVideoView(mContext.getApplicationContext(), mGraphicRenderMgr);
        mGlVideoView[0].setVisibility(GLView.INVISIBLE);
        addView(mGlVideoView[0]);
        for (int i = 4; i >= 1; i--) {
            mGlVideoView[i] = new GLVideoView(mContext.getApplicationContext(), mGraphicRenderMgr);
            mGlVideoView[i].setVisibility(GLView.INVISIBLE);
            addView(mGlVideoView[i]);
        }
        mGlRootView.setContentPane(this);
        // set bitmap ,reuse the backgroud BitmapDrawable,mlzhong
        // setBackground(UITools.getBitmapFromResourceId(mContext, R.drawable.qav_gaudio_bg));

        mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureListener());
        mGestureDetector = new GestureDetector(mContext, new GestureListener());
        mMoveDetector = new MoveGestureDetector(mContext, new MoveListener());
        mTouchListener = new TouchListener();
        setOnTouchListener(mTouchListener);
    }

    boolean mCameraSurfaceCreated = false;

    void initCameraPreview() {

//		SurfaceView localVideo = (SurfaceView) mRootView.findViewById(R.id.av_video_surfaceView);
//		SurfaceHolder holder = localVideo.getHolder();
//		holder.addCallback(mSurfaceHolderListener);
//		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 3.0以下必须在初始化时调用，否则不能启动预览
//		localVideo.setZOrderMediaOverlay(true);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // layoutParams.flags |= LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.windowAnimations = 0;// android.R.style.Animation_Toast;
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //layoutParams.setTitle("Toast");
        try {
            mSurfaceView = new SurfaceView(mContext);
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(mSurfaceHolderListener);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 3.0以下必须在初始化时调用，否则不能启动预览
            mSurfaceView.setZOrderMediaOverlay(true);
            windowManager.addView(mSurfaceView, layoutParams);
        } catch (IllegalStateException e) {
            windowManager.updateViewLayout(mSurfaceView, layoutParams);
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail: IllegalStateException." + e);
            }
        } catch (Exception e) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail." + e);
            }
        }
        Log.i(TAG, "initCameraPreview");
    }

    void unInitCameraaPreview() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        try {
            windowManager.removeView(mSurfaceView);
            mSurfaceView = null;
        } catch (Exception e) {
            if (QLog.isColorLevel()) {
                QLog.e(TAG, QLog.CLR, "remove camera view fail.", e);
            }
        }
    }

    void switchVideo(int index1, int index2) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "switchVideo index1: " + index1 + ", index2: " + index2);
        }
        if (index1 == index2 || index1 < 0 || index1 >= mGlVideoView.length || index2 < 0 || index2 >= mGlVideoView.length) {
            return;
        }

        if (GLView.INVISIBLE == mGlVideoView[index1].getVisibility() || GLView.INVISIBLE == mGlVideoView[index2].getVisibility()) {
            Log.d("switchVideo", "can not switchVideo");
            return;
        }

        String identifier1 = mGlVideoView[index1].getIdentifier();
        int videoSrcType1 = mGlVideoView[index1].getVideoSrcType();
        boolean isPC1 = mGlVideoView[index1].isPC();
        boolean isMirror1 = mGlVideoView[index1].isMirror();
        boolean isLoading1 = mGlVideoView[index1].isLoading();
        String identifier2 = mGlVideoView[index2].getIdentifier();
        int videoSrcType2 = mGlVideoView[index2].getVideoSrcType();
        boolean isPC2 = mGlVideoView[index2].isPC();
        boolean isMirror2 = mGlVideoView[index2].isMirror();
        boolean isLoading2 = mGlVideoView[index2].isLoading();

        mGlVideoView[index1].setRender(identifier2, videoSrcType2);
        mGlVideoView[index1].setIsPC(isPC2);
        mGlVideoView[index1].setMirror(isMirror2);
        mGlVideoView[index1].enableLoading(isLoading2);
        mGlVideoView[index2].setRender(identifier1, videoSrcType1);
        mGlVideoView[index2].setIsPC(isPC1);
        mGlVideoView[index2].setMirror(isMirror1);
        mGlVideoView[index2].enableLoading(isLoading1);

        int temp = localViewIndex;
        localViewIndex = remoteViewIndex;
        remoteViewIndex = temp;
        Log.d(TAG, "closeMemberVideoView switch back done +");
    }

    class Position {
        final static int CENTER = 0;
        final static int LEFT_TOP = 1;
        final static int RIGHT_TOP = 2;
        final static int RIGHT_BOTTOM = 3;
        final static int LEFT_BOTTOM = 4;
    }

    public void setSmallVideoViewLayout(boolean isRemoteHasVideo, String remoteIdentifier, int Viewindex) {
        if (QLog.isColorLevel()) {
            QLog.d(TAG, QLog.CLR, "setSmallVideoViewLayout position: " + mPosition);
        }
        if (mContext == null) {
            return;
        }
        Log.i(TAG, "setSmallVideoViewLayout " + remoteIdentifier + " isRemoteHasVideo " + isRemoteHasVideo);
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int width = getWidth();
        int height = getHeight();
        int w = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int h = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
        if (mBottomOffset == 0) {
            edgeY = edgeX;
        }

        switch (mPosition) {
            case Position.LEFT_TOP:
                left = edgeX;
                right = left + w;
                // if (mBottomOffset != 0) {
                // top = height - h - edgeY - mBottomOffset;
                // bottom = top + h;
                // } else {
                top = edgeY + mTopOffset;
                bottom = top + h;
                // }
                break;
            case Position.RIGHT_TOP:
                left = width - w - edgeX;
                right = left + w;
                // if (mBottomOffset != 0) {
                // top = height - h - edgeY - mBottomOffset;
                // bottom = top + h;
                // } else {
                top = edgeY + mTopOffset;
                bottom = top + h;
                // }
                break;
            case Position.LEFT_BOTTOM:
                left = edgeX;
                right = left + w;
                top = height - h - edgeY - mBottomOffset;
                bottom = top + h;
                break;
            case Position.RIGHT_BOTTOM:
                left = width - w - edgeX;
                top = height - h - edgeY - mBottomOffset;
                right = left + w;
                bottom = top + h;
                break;
        }


        if (isRemoteHasVideo) {// 打开摄像头
            GLVideoView view = null;
            mRemoteIdentifier = remoteIdentifier;
            int index = getViewIndexById(remoteIdentifier, AVView.VIDEO_SRC_TYPE_CAMERA);

            //请求多路画面用这个测试
//			if (remoteViewIndex != -1 && !mRemoteIdentifier.equals("") && !mRemoteIdentifier.equals(remoteIdentifier)) {
//				closeVideoView(remoteViewIndex);
//			}

            if (!isSupportMultiVideo) {
                if (remoteViewIndex != -1) {
                    closeVideoView(remoteViewIndex);
                }
            }
            if (index < 0) {
                index = getIdleViewIndex(0);
                if (index >= 0) {
                    view = mGlVideoView[index];
                    Log.i(TAG, "setSmallVideoViewLayout put remoteId  " + remoteIdentifier + " indexView " + index);
                    if (Viewindex > 0) {
                        mapViewAndIdentifier.put(remoteIdentifier, Viewindex);
                    } else {
                        mapViewAndIdentifier.put(remoteIdentifier, index);
                    }
                    view.setRender(remoteIdentifier, AVView.VIDEO_SRC_TYPE_CAMERA);
                    remoteViewIndex = index;
                }
            } else {
                view = mGlVideoView[index];
            }
            if (view != null) {
                view.setIsPC(false);
                view.enableLoading(false);
                view.setVisibility(GLView.VISIBLE);
            }

        } else {// 关闭摄像头
            int index = getViewIndexById(remoteIdentifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index >= 0) {
                closeVideoView(index);
                remoteViewIndex = -1;
            }
        }


//		if (null != mGlVideoView[1].getOpenId()) {
//			mGlVideoView[1].clearRender();
//		}
//		
//				
//		mGlVideoView[1].layout(left, top, right, bottom);
//		mGlVideoView[1].setRender(remoteOpenid, AVConstants.VIDEO_SRC_CAMERA);
//		mGlVideoView[1].setIsPC(false);
//		mGlVideoView[1].enableLoading(false);	
//		mGlVideoView[1].setVisibility(View.VISIBLE);
    }

    int mPosition = Position.LEFT_TOP;
    boolean mDragMoving = false;

    public int getPosition() {
        return mPosition;
    }

    void checkAndChangeMargin(int deltaX, int deltaY) {
        if (mContext == null) {
            return;
        }
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);

        Rect outRect = getBounds();
        int minOffsetX = 0;
        int minOffsetY = 0;
        int maxOffsetX = outRect.width() - width;
        int maxOffsetY = outRect.height() - height;

        int left = mGlVideoView[1].getBounds().left + deltaX;
        int top = mGlVideoView[1].getBounds().top + deltaY;
        if (left < minOffsetX) {
            left = minOffsetX;
        } else if (left > maxOffsetX) {
            left = maxOffsetX;
        }
        if (top < minOffsetY) {
            top = minOffsetY;
        } else if (top > maxOffsetY) {
            top = maxOffsetY;
        }
        int right = left + width;
        int bottom = top + height;
        mGlVideoView[1].layout(left, top, right, bottom);
    }

    int getSmallViewPosition() {
        int position = Position.CENTER;
        Rect visableRect = getBounds();
        int screenCenterX = visableRect.centerX();
        int screenCenterY = visableRect.centerY();
        int viewCenterX = mGlVideoView[1].getBounds().centerX();
        int viewCenterY = mGlVideoView[1].getBounds().centerY();
        if (viewCenterX < screenCenterX && viewCenterY < screenCenterY) {
            position = Position.LEFT_TOP;
        } else if (viewCenterX < screenCenterX && viewCenterY > screenCenterY) {
            position = Position.LEFT_BOTTOM;
        } else if (viewCenterX > screenCenterX && viewCenterY < screenCenterY) {
            position = Position.RIGHT_TOP;
        } else if (viewCenterX > screenCenterX && viewCenterY > screenCenterY) {
            position = Position.RIGHT_BOTTOM;
        }

        return position;
    }

    class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(GLView view, MotionEvent event) {
            Log.i(TAG, "onTouch view" + view);
            if (view == mGlVideoView[0]) {
                mTargetIndex = 0;
            } else if (view == mGlVideoView[1]) {
                mTargetIndex = 1;
            } else if (view == mGlVideoView[2]) {
                mTargetIndex = 2;
            } else if (view == mGlVideoView[3]) {
                mTargetIndex = 3;
            } else if (view == mGlVideoView[4]) {
                mTargetIndex = 4;
            } else {
                mTargetIndex = -1;
            }
            if (mGestureDetector != null) {
                mGestureDetector.onTouchEvent(event);
            }
//            if (mTargetIndex == 1 && mMoveDetector != null) {
//                mMoveDetector.onTouchEvent(event);
//            } else if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVConstants.VIDEO_SRC_SHARESCREEN) {
//                if (mScaleGestureDetector != null) {
//                    mScaleGestureDetector.onTouchEvent(event);
//                }
//                if (mMoveDetector != null) {
//                    mMoveDetector.onTouchEvent(event);
//                }
//            }
            return true;
        }
    }

    ;

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (QLog.isColorLevel())
                QLog.d(TAG, QLog.CLR, "GestureListener-->mTargetIndex=" + mTargetIndex);
            if (mTargetIndex <= 0) {
                // 显示控制层
            } else {
                showVideoMemberInfo(mTargetIndex);
//                switchVideo(0, mTargetIndex);
            }
            return true;
        }

//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVConstants.VIDEO_SRC_SHARESCREEN) {
//                mClickTimes++;
//                if (mClickTimes % 2 == 1) {
//                    mGlVideoView[0].setScale(GLVideoView.MAX_SCALE + 1, 0, 0, true);
//                } else {
//                    mGlVideoView[0].setScale(GLVideoView.MIN_SCALE, 0, 0, true);
//                }
//                return true;
//            }
//            return super.onDoubleTap(e);
//        }
    }

    ;

    class MoveListener implements OnMoveGestureListener {
        int startX = 0;
        int startY = 0;
        int endX = 0;
        int endY = 0;
        int startPosition = 0;

        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {
            if (mTargetIndex == 0) {

            } else if (mTargetIndex == 1) {
                startX = (int) detector.getFocusX();
                startY = (int) detector.getFocusY();
                startPosition = getSmallViewPosition();
            }
            return true;
        }

        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF delta = detector.getFocusDelta();
            int deltaX = (int) delta.x;
            int deltaY = (int) delta.y;
            if (mTargetIndex == 0) {
                mGlVideoView[0].setOffset(deltaX, deltaY, false);
            } else if (mTargetIndex == 1) {
                if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                    mDragMoving = true;
                }
                // 修改拖动窗口的位置
                checkAndChangeMargin(deltaX, deltaY);
            }
            return true;
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
            PointF delta = detector.getFocusDelta();
            int deltaX = (int) delta.x;
            int deltaY = (int) delta.y;
            if (mTargetIndex == 0) {
                mGlVideoView[0].setOffset(deltaX, deltaY, true);
            } else if (mTargetIndex == 1) {
                // 修改拖动窗口的位置
                checkAndChangeMargin(deltaX, deltaY);
                endX = (int) detector.getFocusX();
                endY = (int) detector.getFocusY();
                mPosition = getSmallViewDstPosition(startPosition, startX, startY, endX, endY);
                afterDrag(mPosition);
            }
        }
    }

    ;

    class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float scale = detector.getScaleFactor();
            float curScale = mGlVideoView[0].getScale();
            mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, false);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float scale = detector.getScaleFactor();
            float curScale = mGlVideoView[0].getScale();
            mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, true);
        }

    }

    enum MoveDistanceLevel {
        e_MoveDistance_Min, e_MoveDistance_Positive, e_MoveDistance_Negative
    }

    ;

    int getSmallViewDstPosition(int startPosition, int nStartX, int nStartY, int nEndX, int nEndY) {
        int thresholdX = mContext.getApplicationContext().getResources().getDimensionPixelSize(R.dimen.video_smallview_move_thresholdX);
        int thresholdY = mContext.getApplicationContext().getResources().getDimensionPixelSize(R.dimen.video_smallview_move_thresholdY);
        int xMoveDistanceLevelStandard = thresholdX;
        int yMoveDistanceLevelStandard = thresholdY;

        MoveDistanceLevel eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        MoveDistanceLevel eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;

        if (nEndX - nStartX > xMoveDistanceLevelStandard) {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
        } else if (nEndX - nStartX < -xMoveDistanceLevelStandard) {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
        } else {
            eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        }

        if (nEndY - nStartY > yMoveDistanceLevelStandard) {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
        } else if (nEndY - nStartY < -yMoveDistanceLevelStandard) {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
        } else {
            eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
        }

        int eBeginPosition = startPosition;
        int eEndPosition = Position.LEFT_TOP;
        int eDstPosition = Position.LEFT_TOP;
        eEndPosition = getSmallViewPosition();

        if (eEndPosition == Position.RIGHT_BOTTOM) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.RIGHT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.LEFT_TOP;
                    } else {
                        eDstPosition = Position.LEFT_BOTTOM;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.RIGHT_TOP;
                    } else {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    }
                }
            }
        } else if (eEndPosition == Position.RIGHT_TOP) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.RIGHT_TOP;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.LEFT_BOTTOM;
                    } else {
                        eDstPosition = Position.LEFT_TOP;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    } else {
                        eDstPosition = Position.RIGHT_TOP;
                    }
                }
            }
        } else if (eEndPosition == Position.LEFT_TOP) {
            if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                eDstPosition = Position.LEFT_TOP;
            } else if (eBeginPosition == Position.LEFT_TOP) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    } else {
                        eDstPosition = Position.RIGHT_TOP;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                        eDstPosition = Position.LEFT_BOTTOM;
                    } else {
                        eDstPosition = Position.LEFT_TOP;
                    }
                }
            }
        } else if (eEndPosition == Position.LEFT_BOTTOM) {
            if (eBeginPosition == Position.LEFT_TOP) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_TOP) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.RIGHT_BOTTOM) {
                eDstPosition = Position.LEFT_BOTTOM;
            } else if (eBeginPosition == Position.LEFT_BOTTOM) {
                if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.RIGHT_TOP;
                    } else {
                        eDstPosition = Position.RIGHT_BOTTOM;
                    }
                } else {
                    if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
                        eDstPosition = Position.LEFT_TOP;
                    } else {
                        eDstPosition = Position.LEFT_BOTTOM;
                    }
                }
            }
        }
        return eDstPosition;
    }

    void afterDrag(int position) {
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
        int edgeX = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
        int edgeY = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
        if (mBottomOffset == 0) {
            edgeY = edgeX;
        }
        Rect visableRect = getBounds();

        int fromX = mGlVideoView[1].getBounds().left;
        int fromY = mGlVideoView[1].getBounds().top;
        int toX = 0;
        int toY = 0;

        switch (position) {
            case Position.LEFT_TOP:
                toX = edgeX;
                toY = edgeY;
                break;
            case Position.RIGHT_TOP:
                toX = visableRect.width() - edgeX - width;
                toY = edgeY;
                break;
            case Position.RIGHT_BOTTOM:
                toX = visableRect.width() - edgeX - width;
                toY = visableRect.height() - edgeY - height;
                break;
            case Position.LEFT_BOTTOM:
                toX = edgeX;
                toY = visableRect.height() - edgeY - height;
                break;
            default:
                break;
        }
    }

    public void setSelfId(String key) {
        if (mGraphicRenderMgr != null) {
            mGraphicRenderMgr.setSelfId(key + "_" + AVView.VIDEO_SRC_TYPE_CAMERA);
        }
    }

    void onMemberChange() {
        Log.d(TAG, "WL_DEBUG onMemberChange start");
        QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
        ArrayList<MemberInfo> memberList = qavsdk.getMemberList();

        for (MemberInfo memberInfo : memberList) {
            int index = getViewIndexById(memberInfo.identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
            if (index >= 0) {
                Log.d(TAG, "WL_DEBUG onMemberChange memberInfo.isVideoIn = " + memberInfo.isVideoIn);

                if (!memberInfo.isVideoIn && !memberInfo.isSpeaking) {
                    closeVideoView(index);
                }
            }
        }

        // 去掉已经不再memberlist中的view
        if (!memberList.isEmpty()) {
            for (int i = 0; i < mGlVideoView.length; i++) {
                GLVideoView view = mGlVideoView[i];
                if (view == null)
                    continue;
                String viewIdentifier = view.getIdentifier();
                if (TextUtils.isEmpty(viewIdentifier))
                    continue;


                boolean memberExist = false;
                for (int j = 0; j < memberList.size(); j++) {
                    if (!TextUtils.isEmpty(memberList.get(j).identifier)) {
                        if (viewIdentifier.equals(memberList.get(j).identifier)) {
                            memberExist = true;
                            break;
                        }
                    }
                }

                if (!memberExist) {
                    mQavsdkControl = ((QavsdkApplication) mContext.getApplicationContext()).getQavsdkControl();
                    if (null != mQavsdkControl) {
                        String selfIdentifier = mQavsdkControl.getSelfIdentifier();
                        Log.d(TAG, "self identifier : " + selfIdentifier);
                        if (selfIdentifier != null && selfIdentifier.equals(viewIdentifier)) {
                            return;
                        }
                    }

                    closeVideoView(i);
                }
            }
        } else {
            for (int i = 0; i < mGlVideoView.length; i++) {
                closeVideoView(i);
            }
        }

        Log.d(TAG, "WL_DEBUG onMemberChange end");
    }

    public void closeMemberVideoView(String identifier) {
        Log.d(TAG, "closeMemberVideoView +" + identifier);
        if (!identifier.startsWith("86-")) {
            identifier = "86-" + identifier;
        }
        if (mapViewAndIdentifier.containsKey(identifier)) {
            int index = (int) mapViewAndIdentifier.get(identifier);
            Log.d(TAG, "closeMemberVideoView iscontain index +" + index);

            //请求次数
            int request = mQavsdkControl.getSmallVideoView();
            request--;
            Log.d(TAG, "closeMemberVideoView request " + request);
            mQavsdkControl.setRequestCount(request);
//            if(request==1){
//                request--;
//                mQavsdkControl.setRequestCount(0);
//            }

            //如果要关闭界面在大界面，则先交换再关闭

            if (index == 0) {
                //交换
                switchVideoGroudBack();

                //延时关闭
//                Message msg = new Message();
//                msg.what = DELAY_CLOSE_VIEW;
//                msg.obj = index;
//                mHandler.sendMessageDelayed(msg, 1000);

                closeVideoView(hostIndex);
            } else {
                closeVideoView(index);
            }
        }
    }

    public void showVideoMemberInfo(int indexview) {
        String identifier = (String) getKeyFromValue(mapViewAndIdentifier, indexview);
        Log.d(TAG, "showVideoMemberInfo " + identifier);
        if (identifier == null) return;
        mContext.sendBroadcast(new Intent(
                Util.ACTION_SHOW_VIDEO_MEMBER_INFO).putExtra(
                Util.EXTRA_IDENTIFIER, identifier));
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public void switchVideoView(String identifierBG, String identifier) {
        if (mapViewAndIdentifier != null && mapViewAndIdentifier.containsKey(identifier)) {
            int index = (int) mapViewAndIdentifier.get(identifier);
            if (index >= 0) {
                switchVideo(0, index);
                if (!identifierBG.startsWith("86-")) {
                    identifierBG = "86-" + identifierBG;
                }
                mapViewAndIdentifier.put(identifier, 0);
                mapViewAndIdentifier.put(identifierBG, index);
            }
        }
    }

    public void switchVideowithBackground(String memberIdentifier) {
        //如果发现底部不是自己，先把自己交换回来
        String downIdentifer = (String) getKeyFromValue(mapViewAndIdentifier, 0);
        if (!downIdentifer.equals(hostIdentifer)) {
            switchVideoGroudBack();
        }


        if (mapViewAndIdentifier != null && mapViewAndIdentifier.containsKey(memberIdentifier)) {
            int memberIndex = (int) mapViewAndIdentifier.get(memberIdentifier);

            if (memberIndex >= 0) {

                switchVideo(0, memberIndex);
                hostIndex = memberIndex;
//                String hostIdentifier = (String) getKeyFromValue(mapViewAndIdentifier, 0);
                mapViewAndIdentifier.put(memberIdentifier, 0);
                mapViewAndIdentifier.put(hostIdentifer, memberIndex);
            }
        }
    }


    //把自己从小界面交换回来
    public void switchVideoGroudBack() {
        String memberidentifier = (String) getKeyFromValue(mapViewAndIdentifier, 0);
        switchVideo(0, hostIndex);
        mapViewAndIdentifier.put(memberidentifier, hostIndex);
        mapViewAndIdentifier.put(hostIdentifer, 0);
    }

    public void getHostId(String id) {
        hostIdentifer = id;
    }


    public void switchIdentifer(int id1, int id2) {
        String memberidentifier1 = (String) getKeyFromValue(mapViewAndIdentifier, id1);
        String memberidentifier2 = (String) getKeyFromValue(mapViewAndIdentifier, id2);
        mapViewAndIdentifier.put(memberidentifier1, id2);
        mapViewAndIdentifier.put(memberidentifier2, id1);
    }
}
