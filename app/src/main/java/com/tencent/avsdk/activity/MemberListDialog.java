package com.tencent.avsdk.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.avsdk.CircularImageButton;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.MemberInfo;
import com.tencent.avsdk.MemberListAdapter;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import java.util.List;

/**
 * Created by admin on 15/11/7.
 */
public class MemberListDialog extends Dialog {
    private static final String TAG = MemberListDialog.class.getSimpleName();
    private Context context;
    private GridView normalMember = null;
    private GridView videoMember = null;
    private String str;
    private List<MemberInfo> normalMemberList, videoMemberList;
    private int position;
    private MemberListAdapter normalMemberAdapter, videoMemberAdapter;
    private Dialog memberInfoDialog, VideoMemberInfoDialog;
    private CircularImageButton memberHead;
    private TextView memberName, memberTips, backMsg;
    private TextView btnClose, btnInviteVC, btnCloseVC;
    private TextView normalCount, videoCount,chatRoomName;
    private String memberSelectPhone;
    private boolean isHost = false;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private int requestCount = 0;
    private QavsdkApplication mQavsdkApplication;
    private QavsdkControl mQavsdkControl;


    public MemberListDialog(Context context, int theme, List<MemberInfo> normalMemberList, List<MemberInfo> videoMemberList, boolean isHost) {
        super(context, theme);
        this.context = context;
        this.normalMemberList = normalMemberList;
        this.videoMemberList = videoMemberList;
        this.isHost = isHost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置对话框使用的布局文件
        this.setContentView(R.layout.memberlist_dialog_);
        mQavsdkApplication = (QavsdkApplication) context.getApplicationContext();
        mQavsdkControl = mQavsdkApplication.getQavsdkControl();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        normalMember = (GridView) findViewById(R.id.normal_members);
        videoMember = (GridView) findViewById(R.id.video_members);
        normalCount = (TextView) findViewById(R.id.normalCounts);
        videoCount = (TextView) findViewById(R.id.videoCounts);
        chatRoomName = (TextView) findViewById(R.id.chatRoomName);
        normalCount.setText("普通观众(" + normalMemberList.size() + ")");
        videoCount.setText("互动直播(" + videoMemberList.size() + ")");
        videoCount.setVisibility(View.GONE);
        chatRoomName.setText(mQavsdkApplication.getRoomName());
        backMsg = (TextView) findViewById(R.id.backMsg);
        backMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        // 设置NormalMember的数据源
        normalMemberAdapter = new MemberListAdapter(context, normalMemberList);
        normalMember.setAdapter(normalMemberAdapter);


        //设置VideoMember的数据源
        videoMemberAdapter = new MemberListAdapter(context, videoMemberList);
        videoMember.setAdapter(videoMemberAdapter);

        // 为GridView设置监听器
        normalMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
//
                memberInfoDialog = new Dialog(context, R.style.dialog);
                View view = LayoutInflater.from(context).inflate(R.layout.member_info_dialog, null);
                //取得头像和名字
                memberHead = (CircularImageButton) view.findViewById(R.id.memberinfo_head);
                memberName = (TextView) view.findViewById(R.id.memberinfo_name);
                memberTips = (TextView) view.findViewById(R.id.invite_info);
                btnClose = (TextView) view.findViewById(R.id.close_memberInfo);
                btnInviteVC = (TextView) view.findViewById(R.id.invite_videochat);
                requestCount = mQavsdkControl.getSmallVideoView();
                if (requestCount >= 3) {
                    btnInviteVC.setBackgroundResource(R.drawable.btn_cannot);
                    btnInviteVC.setClickable(false);
                    btnInviteVC.setTextColor(context.getResources().getColor(R.color.mygray));
                    btnInviteVC.setOnClickListener(null);

                }
                String url = HttpUtil.rootUrl + "?imagepath=" + normalMemberList.get(position).getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url, memberHead);
//                memberHead.setImageBitmap(normalMemberList.get(position).getHeadImage());
                Log.d(TAG, "MemberListDialog onItemClick +position: " + position + " name " + normalMemberList.get(position).getUserName());
                memberName.setText(normalMemberList.get(position).getUserName());
                memberSelectPhone = normalMemberList.get(position).getUserPhone();
                btnInviteVC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (requestCount >= 3) {
                            Toast.makeText(context, "you can't invite more ", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        inviteFriendVC();
                        memberInfoDialog.dismiss();
                    }
                });
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        memberInfoDialog.dismiss();
                    }
                });
                if (isHost) {

                } else {
                    //如果非主播没有邀请权利
                    btnInviteVC.setVisibility(View.INVISIBLE);
                    memberTips.setVisibility(View.INVISIBLE);
                }
                memberInfoDialog.setCanceledOnTouchOutside(true);
                memberInfoDialog.setContentView(view);
                memberInfoDialog.show();
            }
        });


        videoMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
//
                VideoMemberInfoDialog = new Dialog(context, R.style.dialog);
                View view = LayoutInflater.from(context).inflate(R.layout.videomember_info_dialog, null);

                //取得头像和名字
                memberHead = (CircularImageButton) view.findViewById(R.id.memberinfo_head);
                memberName = (TextView) view.findViewById(R.id.memberinfo_name);

                memberTips = (TextView) view.findViewById(R.id.invite_info);
                btnClose = (TextView) view.findViewById(R.id.close_memberInfo);
                btnCloseVC = (TextView) view.findViewById(R.id.close_videochat);
                String url = HttpUtil.rootUrl + "?imagepath=" + videoMemberList.get(position).getHeadImagePath() + "&width=0&height=0";
                imageLoader.displayImage(url, memberHead);
                Log.d(TAG, "MemberListDialog onItemClick +position: " + position + " name " + videoMemberList.get(position).getUserName());
                memberName.setText(videoMemberList.get(position).getUserName());
                memberSelectPhone = videoMemberList.get(position).getUserPhone();
                btnCloseVC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeFriendVC();
                        VideoMemberInfoDialog.dismiss();
                    }
                });
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VideoMemberInfoDialog.dismiss();
                    }
                });
                VideoMemberInfoDialog.setCanceledOnTouchOutside(true);
                VideoMemberInfoDialog.setContentView(view);
                VideoMemberInfoDialog.show();
            }
        });


    }

    public void refreshMemberData(List<MemberInfo> romalMemberLsit, List<MemberInfo> videoMemberList) {
        this.normalMemberList = romalMemberLsit;
        this.videoMemberList = videoMemberList;
        if (videoMemberList.size() == 0) {
            videoCount.setVisibility(View.GONE);
        } else {
            videoCount.setVisibility(View.VISIBLE);
        }
        normalMemberAdapter.refreshMemberData(romalMemberLsit);
        normalMemberAdapter.notifyDataSetChanged();
        videoMemberAdapter.refreshMemberData(videoMemberList);
        videoMemberAdapter.notifyDataSetChanged();
        normalCount.setText("普通观众(" + normalMemberList.size() + ")");
        videoCount.setText("互动直播(" + videoMemberList.size() + ")");
    }


    //发起视频聊天
    public void inviteFriendVC() {
        Log.d(TAG, "inviteFriendVC phone : " + memberSelectPhone);
        context.sendBroadcast(new Intent(Util.ACTION_INVITE_MEMBER_VIDEOCHAT)
                        .putExtra(Util.EXTRA_IDENTIFIER, memberSelectPhone)
        );
    }

    public void closeFriendVC() {
        Log.d(TAG, "inviteFriendVC phone : " + memberSelectPhone);
        context.sendBroadcast(new Intent(Util.ACTION_CLOSE_MEMBER_VIDEOCHAT)
                        .putExtra(Util.EXTRA_IDENTIFIER, memberSelectPhone)
        );
    }

    public void freshRequestCount(int RequestCount) {
        requestCount = RequestCount;
    }


}