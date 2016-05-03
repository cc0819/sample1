package com.tencent.avsdk;


import android.graphics.Bitmap;

public class LiveVideoInfo {
    private UserInfo mUserInfo;
    private int mProgrammId;
    private String mLiveSubject;
    private int mLiveConverImage;
    private int mLivePraiseCount;
    private int mLiveViewerCount;
    private int mLiveTotalviewer;
    private String mLiveGroupId;
    private String mShareUrl;
    private String coverpath;
    private int mpraisenum;
    private Bitmap userHeadImage = null;
    private String recordId = "";
    private String recordTime = "";

    public LiveVideoInfo(int liveId, String subject, int cover,
                         int viewerCount, int praiseCount, UserInfo userInfo, String groupId, String Url) {
        mProgrammId = liveId;
        mLiveSubject = subject;
        mLiveConverImage = cover;
        mLivePraiseCount = praiseCount;
        mLiveViewerCount = viewerCount;
        mUserInfo = userInfo;
        mLiveGroupId = groupId;
        mShareUrl = Url;
    }

    public void setUserHeadImage(Bitmap bt) {
        userHeadImage = bt;
    }

    public Bitmap getUserHeadImage() {
        return userHeadImage;
    }

    public String getHeadImagePath() {
        return mUserInfo.getHeadImagePath();
    }

    public void setmLiveTotalviewer(int mLiveTotalviewer) {
        this.mLiveTotalviewer = mLiveTotalviewer;
    }

    public int getmLiveTotalviewer() {
        return mLiveTotalviewer;
    }

    public void setCoverpath(String coverpath) {
        this.coverpath = coverpath;
    }

    public String getCoverpath() {
        return coverpath;
    }

    public void setUserName(String name) {
        mUserInfo.setUserName(name);
    }

    public void setUserPhone(String phone) {
        mUserInfo.setUserPhone(phone);
    }

    public void setLivePraiseCount(int praiseCount) {
        mLivePraiseCount = praiseCount;
        mUserInfo.setPraiseCount(praiseCount);
    }

    public void setLiveViewerCount(int viewerCount) {
        mLiveViewerCount = viewerCount;
    }

    public void setGroupId(String groupId) {
        mLiveGroupId = groupId;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public String getUserName() {
        return mUserInfo.getUserName();
    }

    public String getUserPhone() {
        return mUserInfo.getUserPhone();
    }

    public String getLiveTitle() {
        return mLiveSubject;
    }

    public int getLiveConverImage() {
        return mLiveConverImage;
    }

    public int getProgrammId() {
        return mProgrammId;
    }

    public int getLivePraiseCount() {
        return mLivePraiseCount;
    }

    public int getLiveViewerCount() {
        return mLiveViewerCount;
    }

    public String getLiveGroupId() {
        return mLiveGroupId;
    }

    public void setmShareUrl(String mShareUrl) {
        this.mShareUrl = mShareUrl;
    }

    public String getmShareUrl() {
        return mShareUrl;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }
}
