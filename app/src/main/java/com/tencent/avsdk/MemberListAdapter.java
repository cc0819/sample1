package com.tencent.avsdk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

/**
 * Created by admin on 15/11/7.
 */
public class MemberListAdapter extends BaseAdapter {
    private static final String TAG = MemberListAdapter.class.getSimpleName();

    Context context;
    List<MemberInfo> list;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public MemberListAdapter(Context context, List<MemberInfo> list) {
        this.list = list;
        this.context = context;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.layout_member_item, null);
            holder.icon = (CircularImageButton) convertView.findViewById(R.id.head_image);
            holder.text = (TextView) convertView.findViewById(R.id.member_name);
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

//        headImage.setImageBitmap(item.getHeadImage());
        MemberInfo item = list.get(position);
        holder.icon.setClickable(false);
        holder.icon.setFocusable(false);
        TextView textview = (TextView) convertView.findViewById(R.id.member_name);

        textview.setText("" + item.getUserName());
        String url = HttpUtil.rootUrl + "?imagepath=" + list.get(position).getHeadImagePath() + "&width=0&height=0";
        imageLoader.displayImage(url, holder.icon);
        return convertView;
    }

    public void refreshMemberData(List<MemberInfo> list) {
        this.list = list;

    }

    static class ViewHolder {
        public TextView text;
        public CircularImageButton icon;

    }

}
