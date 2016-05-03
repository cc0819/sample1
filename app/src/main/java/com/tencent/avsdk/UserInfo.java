package com.tencent.avsdk;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TabHost;

public class UserInfo {
    private static String TAG = "UserInfo";
	private String mPhone = "null";
    private String mName ="null";
    private int mSex = 0;
    private String mConstellation = "null";
    private String mSignature = "填写个人签名";
    private String mAddress = "保密";
    private int mLogo;
    private String mHeadImagePath="null";
    private int mViewerCount = 0;
    private int mPraiseCount = 0;
    private Boolean mIsLogin = false;
    private Boolean mIsCreater = false; //ture-直播创建者，false-直播观众
    private String mUserSig = "";
    private String groupid;
    private int Env;
    public int getEnv() {
        return Env;
    }

    public void setEnv(int env) {
        Env = env;
    }

    public UserInfo(String phone, String name) {
        this.mPhone = phone;
        this.mName = name;
    }
    public UserInfo(String phone, String name, String headImage) {
        this.mPhone = phone;
        this.mName = name;
        mHeadImagePath = headImage;
    }

    public UserInfo(String name,int viewerCount,int imageId, int praiseCount){
        this.mName = name;
        this.mViewerCount = viewerCount;
        this.mPraiseCount = praiseCount;
        this.mLogo = imageId;
    }

    public void login(Context context,String phone) {
        Log.d(TAG, "keep in local login phone " + phone);
        mIsLogin = true;
        SharedPreferences.Editor sharedata =  context.getSharedPreferences(DemoConstants.LOCAL_DATA, Context.MODE_APPEND).edit();
        sharedata.putString(DemoConstants.LOCAL_PHONE, ""+phone);
        sharedata.commit();
    }
    
    public void logout(Context context){
        SharedPreferences.Editor sharedata =  context.getSharedPreferences(DemoConstants.LOCAL_DATA, Context.MODE_APPEND).edit();
        sharedata.putString(DemoConstants.LOCAL_PHONE, "");
        sharedata.commit();
    	mIsLogin = false;
    }
    
    public Boolean isLogin() {
    	return mIsLogin == true;
    }
    public void setHeadImagePath(String headImagePath) {
        this.mHeadImagePath = headImagePath;
    }

    public String getHeadImagePath() {
        return mHeadImagePath;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public void setUsersig(String mUsersig) {

        this.mUserSig = mUsersig;
    }

    public void setUserPhone(String phone) {

    	this.mPhone = phone;
    }

    public void setUserName(String name) {
        this.mName = name;
    }

    public void setUserLogo(int imageId) {
        this.mLogo = imageId; 
    }

    public void setUserSex(int sex) {
    	mSex = sex;
    }
    
    public void setUserConstellation(String index) {
    	mConstellation = index;
    }
    
    public void setUserSignature(String signature) {
    	mSignature = signature;
    }
    
    public void setUserAddr(String addr) {
    	mAddress = addr;
    }
    
    public void setViewerCount(int viewerCount) {
        this.mViewerCount = viewerCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.mPraiseCount += praiseCount;
    }

    public String getUsersig() {
        return mUserSig;
    }

    public int getUserLogo() {
        return mLogo;
    }

    public String getUserName() {
        return mName;
    }
    
    public String getUserPhone(){
        return mPhone;
    }
    
    public int getUserSex() {
    	return mSex;
    }

    public String getUserConstellation() {
    	return mConstellation;
    }
    
    public String getUserSignature() {
    	return mSignature;
    }
    
    public String getUserAddr() {
    	return mAddress;
    }
    
    public int getViewerCount() {
        return mViewerCount;
    }

    public int getPraiseCount() {
        return mPraiseCount;
    }
    
    public Boolean isCreater() {
        return mIsCreater;
    }

    public void setIsCreater(Boolean state) {
        mIsCreater = state;
    }
}
