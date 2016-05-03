package com.tencent.avsdk;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.TIMElemType;
import com.tencent.TIMTextElem;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ChatMsgListAdapter extends BaseAdapter {

    private static String TAG = ChatMsgListAdapter.class.getSimpleName();
    private static final int ITEMCOUNT = 9;
    private List<ChatEntity> listMessage = null;
    private LayoutInflater inflater;
    private LinearLayout layout;
    public static final int TYPE_TEXT_SEND = 0;
    public static final int TYPE_TEXT_RECV = 1;
    private Context context;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private ArrayList<MemberInfo> mMemberList;
    private UserInfo myselfInfo;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ViewHolder holder;
    private MemberInfo host;
    private QavsdkApplication mQavsdkApplication;
    private DisplayImageOptions option;

    public ChatMsgListAdapter(Context context, List<ChatEntity> objects, ArrayList<MemberInfo> memberList, UserInfo myself) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listMessage = objects;
        mMemberList = memberList;
        myselfInfo = myself;
        option = new DisplayImageOptions.Builder()
                .cacheInMemory(false)   //设置图片不缓存于内存中
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)    //设置图片的质量
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)    //设置图片的缩放类型，该方法可以有效减少内存的占用
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        host = new MemberInfo();
    }


    @Override
    public int getCount() {
        return listMessage.size();
    }

    @Override
    public Object getItem(int position) {
        return listMessage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        ChatEntity entity = listMessage.get(position);
        if (entity.getElem().getType() == TIMElemType.Text) {
            return entity.getIsSelf() ? TYPE_TEXT_SEND : TYPE_TEXT_RECV;
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatEntity entity = listMessage.get(position);
        TIMTextElem elem = (TIMTextElem) entity.getElem();

        Log.d(TAG, "ChatMsgListAdapter senderName " + entity.getSenderName());


        if (convertView == null) {
            convertView = (LinearLayout) inflater.inflate(R.layout.chat_item_left, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            holder.icon = (CircularImageButton) convertView.findViewById(R.id.tv_chat_head_image);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        mQavsdkApplication = (QavsdkApplication) context.getApplicationContext();
        host = mQavsdkApplication.getHostInfo();
        String phone = entity.getSenderName();
        phone = phone.substring(3);

        if (host != null && host.getUserPhone().equals(phone)) {
            //是主播
            String url = HttpUtil.rootUrl + "?imagepath=" + host.getHeadImagePath() + "&width=0&height=0";
            imageLoader.displayImage(url, holder.icon, option);
            holder.text.setText(elem.getText().toString());
//       }else if (entity.getIsSelf() == true) {
            //是自己发消息
//            String url = HttpUtil.rootUrl + "?imagepath=" +   myselfInfo.getHeadImagePath() + "&width=0&height=0";
//            imageLoader.displayImage(url, holder.icon);
//            holder.text .setText(elem.getText().toString());
        } else {
            for (int i = 0; i < mMemberList.size(); ++i) {
                String IDPhone;
                if (!mMemberList.get(i).getUserPhone().startsWith("86-")) {
                    IDPhone = "86-" + mMemberList.get(i).getUserPhone();
                } else {
                    IDPhone = mMemberList.get(i).getUserPhone();
                }

                if (IDPhone.equals(entity.getSenderName())) {
                    String url = HttpUtil.rootUrl + "?imagepath=" + mMemberList.get(i).getHeadImagePath() + "&width=0&height=0";
                    imageLoader.displayImage(url, holder.icon, option);
                }
            }
            holder.text.setText(elem.getText().toString());
        }

        return convertView;
    }

    static class ViewHolder {
        public TextView text;
        public CircularImageButton icon;

    }

    public void refresh(MemberInfo memberInfo) {
        host = memberInfo;
    }

}
