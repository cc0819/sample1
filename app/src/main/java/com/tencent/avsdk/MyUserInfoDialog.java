package com.tencent.avsdk;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by lyriezheng on 2015/8/7.
 */
public class MyUserInfoDialog {
    private Context context;
    private Dialog dialog;
    private Button button;
    private TextView username;
    private TextView signature;
    private TextView praisenum;
    private ImageButton HeadImage;
    public MyUserInfoDialog(Context con){
        this.context = con;
        dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.userinfo);
        username = (TextView) dialog.findViewById(R.id.username);
        signature = (TextView) dialog.findViewById(R.id.usersignature);
        praisenum = (TextView) dialog.findViewById(R.id.praisecount);
        button = (Button) dialog.findViewById(R.id.dismiss);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        HeadImage = (ImageButton) dialog.findViewById(R.id.userimage);
    }

    public void setPraisenum(String num) {
        praisenum.setText(num);
    }

    public void setUsername(String name) {
        username.setText(name);
    }

    public void setHeadImage(Bitmap bitmap) {
        if(bitmap != null)
            HeadImage.setImageBitmap(bitmap);
    }

    public void setSignature(String sig) {
        signature.setText(sig);
    }
    public void show(){
        dialog.show();
    }
    public void hide(){
        dialog.hide();
    }
    public void dismiss(){
        dialog.dismiss();
    }
}
