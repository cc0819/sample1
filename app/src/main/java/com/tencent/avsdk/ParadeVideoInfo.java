package com.tencent.avsdk;

public class ParadeVideoInfo {
    private UserInfo mUserInfo;
    private int mProgrammId;
    private String mLiveSubject;
    private String mCoverImagePath;
    private String mStartTime;
    private int remainingSecond;
    private int remainingHour;
    private int remainingMinute;

    public ParadeVideoInfo(String userphone, String username, String headimagepath,
                           int roomnum, String subject, String coverpath, String starttime) {
        mUserInfo = new UserInfo(userphone, username);
        mUserInfo.setHeadImagePath(headimagepath);
        mProgrammId = roomnum;
        mLiveSubject = subject;
        mCoverImagePath = coverpath;
        mStartTime = starttime;
    }

    public String getLiveSubject() {
        return  mLiveSubject;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getCoverImagePath() {
        return mCoverImagePath;
    }

    public String getUserName() {
        return mUserInfo.getUserName();
    }

    public String getUserPhone() {
        return mUserInfo.getUserPhone();
    }

    public void decRemainingTime() {
        --remainingSecond;
        if(remainingSecond == -1) {
            remainingSecond = 59;
            --remainingMinute;
        }
        if(remainingMinute == -1) {
            remainingMinute = 59;
            --remainingHour;
        }
    }


    public String getRemainingTimeString() {
        String remainingTime = "";

        if(remainingHour > 0) {
            remainingTime += remainingHour + "时";
        }
        if(remainingMinute >= 0) {
            remainingTime += remainingMinute + "分";
        }
        if(remainingSecond >= 0) {
            remainingTime += remainingSecond + "秒";
        }
        return remainingTime;
    }

    public String getRemainingTimeDigitString() {
        String remainingTime = "";
        if(remainingHour == 0) {
            remainingTime += "00";
        }else if(remainingHour > 0 && remainingHour < 10) {
            remainingTime += "0" + remainingHour;
        } else {
            remainingTime +=  remainingHour;
        }
        remainingTime += ":";
        if(remainingMinute == 0) {
            remainingTime += "00";
        }else if(remainingMinute > 0 && remainingMinute < 10) {
            remainingTime += "0" + remainingMinute;
        } else {
            remainingTime +=  remainingMinute;
        }
        remainingTime += ":";
        if(remainingSecond == 0) {
            remainingTime += "00";
        }else if(remainingSecond > 0 && remainingSecond < 10) {
            remainingTime += "0" + remainingSecond;
        } else {
            remainingTime +=  remainingSecond;
        }
        return remainingTime;
    }
    public void setRemainingTime(int hour, int minute, int second) {
        remainingHour = hour;
        remainingMinute = minute;
        remainingSecond = second;
    }
}
