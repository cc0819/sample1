package com.tencent.avsdk.control;

import android.content.Context;
import android.content.Intent;

import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVAudioCtrl.Delegate;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.Util;

public class AVAudioControl {
	private Context mContext = null;
	
	private Delegate mDelegate = new Delegate() {
		@Override
		protected void onOutputModeChange(int outputMode) {
            super.onOutputModeChange(outputMode);
            mContext.sendBroadcast(new Intent(Util.ACTION_OUTPUT_MODE_CHANGE));
        }
	};
	
	AVAudioControl(Context context) {
		mContext = context;
	}
	
	void initAVAudio() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext)
				.getQavsdkControl();
		qavsdk.getAVContext().getAudioCtrl().setDelegate(mDelegate);
	}
	
	boolean getHandfreeChecked() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext)
				.getQavsdkControl();
		return qavsdk.getAVContext().getAudioCtrl().getAudioOutputMode() == AVAudioCtrl.OUTPUT_MODE_HEADSET;
	}
	
	String getQualityTips() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVAudioCtrl avAudioCtrl;
		if (qavsdk != null) {
			avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
			return avAudioCtrl.getQualityTips();
		}
		
		return "";
	}	
}