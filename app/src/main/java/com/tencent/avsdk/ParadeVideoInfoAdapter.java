package com.tencent.avsdk;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParadeVideoInfoAdapter extends ArrayAdapter<ParadeVideoInfo> {
    private int resourceId;
    private Context context;

    private ParadeVideoInfo paradeVideoInfo;
    private View view;
    private ImageView imageViewCoverImage;
    private ImageButton imageButtonUserLogo;
    private TextView textViewUserName;
    private TextView textViewLiveTitle;
    private TextView textViewStartTime;
    private ClipboardManager clip;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;

    public ParadeVideoInfoAdapter(Context context, int textViewResourceId, List<ParadeVideoInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }

    private void initUI() {
        view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        imageViewCoverImage = (ImageView) view.findViewById(R.id.image_view_parade_cover_image);
        imageButtonUserLogo = (ImageButton) view.findViewById(R.id.image_btn_parade_user_logo);
        textViewUserName = (TextView) view.findViewById(R.id.text_view_parade_user_name);
        textViewLiveTitle = (TextView) view.findViewById(R.id.text_view_parade_title);
        textViewStartTime = (TextView) view.findViewById(R.id.start_time);
    }

    private void setUI() {
        imageButtonUserLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), paradeVideoInfo.getUserName(), Toast.LENGTH_SHORT).show();
            }
        });
        textViewLiveTitle.setText(paradeVideoInfo.getLiveSubject());
        textViewUserName.setText("@" + paradeVideoInfo.getUserName());
        textViewStartTime.setText(paradeVideoInfo.getRemainingTimeString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        paradeVideoInfo = getItem(position);
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.user)
                .showImageForEmptyUri(R.drawable.user)
                .showImageOnFail(R.drawable.user)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        initUI();
        getCover();
        setUI();
        return view;
    }

    public void getCover() {
        String param = paradeVideoInfo.getCoverImagePath();
//        String root = "http://203.195.167.34/upload/";
        String root = HttpUtil.SERVER_URL+"upload/";
        String url = root + param;
        if (param.length() > 0) {
            imageLoader.displayImage(url,imageViewCoverImage,options,animateFirstListener);
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
