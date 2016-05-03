package com.tencent.avsdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.avsdk.DemoConstants;
import com.tencent.avsdk.HttpUtil;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.UserInfo;
import com.tencent.avsdk.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import tencent.tls.platform.TLSAccountHelper;
import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSSmsRegListener;
import tencent.tls.platform.TLSUserInfo;

public class RegisterActivity extends Activity implements TextWatcher, View.OnClickListener {
	public static String TAG = "RegisterActivity";

	public final static int ERROR_CLEAN = 0;
	public final static int ERROR_ACCOUNT_EXIT = ERROR_CLEAN - 1;
	public final static int ERROR_ACCOUNT_IILEGAL = ERROR_ACCOUNT_EXIT - 1;
	public final static int ERROR_USERNAME_EXIT = ERROR_ACCOUNT_IILEGAL - 1;
	public final static int ERROR_CODE_TOO_SHORT = ERROR_USERNAME_EXIT - 1;
	public final static int ERROR_CODE_TOO_LONG = ERROR_CODE_TOO_SHORT - 1;
	public final static int ERROR_CODE_IILEGAL = ERROR_CODE_TOO_LONG - 1;
	public final static int ERROR_CODE_NOT_EQUAL = ERROR_CODE_IILEGAL - 1;
	public final static int ERROR_EMPTY_INPUT = ERROR_CODE_NOT_EQUAL - 1;
	public final static int ERROR_INTERNET= ERROR_EMPTY_INPUT - 1;
	public final static int ERROR_REGISTER_FAIL =  ERROR_INTERNET - 1;
	public final static String DEFAULT_PASSWORD = "@23458";
	private EditText mUserPhoneEditText;
	private EditText mPasswordEditText;
	private TextView mRegisterTipsTextView;
	private String userphone = "";
	private String username = "";
	private String password = "";
	private int accType = Integer.parseInt(DemoConstants.ACCOUNTTYPE);
	private int sdkAppid = DemoConstants.APPID;
	private UserInfo mSelfUserInfo;
	private String appVer = "1.0";
	private MyCount mc;
	private TLSAccountHelper accountHelper;
	private TLSSmsRegListener smsRegListener;
	private Handler mErrorHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg){
			switch(msg.what){
				case ERROR_CLEAN:
					mRegisterTipsTextView.setText("");
					break;
				case ERROR_ACCOUNT_EXIT:
					mRegisterTipsTextView.setText("此号码已注册");
					break;
				case ERROR_ACCOUNT_IILEGAL:
					mRegisterTipsTextView.setText("输入电话号码有误");
					break;
				case ERROR_USERNAME_EXIT:
					mRegisterTipsTextView.setText("用户名已经注册，换个名吧");
					break;
				case ERROR_CODE_TOO_SHORT:
					mRegisterTipsTextView.setText("输入密码太短！提示：密码不能少于6位");
					break;
				case ERROR_CODE_TOO_LONG:
					mRegisterTipsTextView.setText("输入密码太长！提示：密码不能超过18位");
					break;
				case ERROR_CODE_IILEGAL:
					mRegisterTipsTextView.setText("密码有误！提示：必须是数字，字母或下划线");
					break;
				case ERROR_CODE_NOT_EQUAL:
					mRegisterTipsTextView.setText("两次输入的密码不一致");
					break;
				case ERROR_EMPTY_INPUT:
					mRegisterTipsTextView.setText("输入不能为空");
					break;
				case ERROR_INTERNET:
					mRegisterTipsTextView.setText("网络链接错误");
					break;
				case ERROR_REGISTER_FAIL:
					mRegisterTipsTextView.setText("注册失败");
					break;
				default:
					break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		mUserPhoneEditText = (EditText) findViewById(R.id.et_userphone);
		mPasswordEditText = (EditText) findViewById(R.id.et_password);
		mRegisterTipsTextView = (TextView) findViewById(R.id.register_tips);
		mSelfUserInfo = ((QavsdkApplication)getApplication()).getMyselfUserInfo();
		findViewById(R.id.btn_register).setOnClickListener(this);
		mUserPhoneEditText.addTextChangedListener(this);
		mPasswordEditText.addTextChangedListener(this);
		//mSelfUserInfo.setLoginType(Util.INTEGERATE);
		((ImageButton) findViewById(R.id.btn_back_login)).setOnClickListener(this);
	}

	public void initRegListener() {
		smsRegListener = new TLSSmsRegListener() {
			@Override
			public void OnSmsRegAskCodeSuccess(int i, int i1) {
				mc = new MyCount(i * 1000, 1000);
				mc.start();
				//mSelfUserInfo.setLoginType(Util.TRUSTEESHIP);

				((EditText) findViewById(R.id.et_password)).setInputType(InputType.TYPE_CLASS_NUMBER);
				Toast.makeText(RegisterActivity.this, "OnSmsRegAskCodeSuccess" + i + " " + i1, Toast.LENGTH_LONG).show();
			}

			@Override
			public void OnSmsRegReaskCodeSuccess(int i, int i1) {

			}

			@Override
			public void OnSmsRegVerifyCodeSuccess() {
				accountHelper.TLSSmsRegCommit(smsRegListener);
			}

			@Override
			public void OnSmsRegCommitSuccess(TLSUserInfo tlsUserInfo) {
				Toast.makeText(RegisterActivity.this, "OnSmsRegCommitSuccess" + tlsUserInfo.identifier, Toast.LENGTH_LONG);
				userphone = mUserPhoneEditText.getText().toString().trim();
				username = "";
				//mSelfUserInfo.setLoginType(Util.INTEGERATE);
//				if(0 == username.length())
				username = userphone;
				password = DEFAULT_PASSWORD;
				onSmsRegisterInfo();
			}

			@Override
			public void OnSmsRegFail(TLSErrInfo tlsErrInfo) {
				if(tlsErrInfo.ErrCode==2){
					Log.d(TAG, "OnSmsRegFail if Register local didn't happened ,do it again ");
//					onSmsRegisterInfo();
				}
				Toast.makeText(RegisterActivity.this, "fail "+tlsErrInfo.ErrCode + ":" + tlsErrInfo.Title + "  " + tlsErrInfo.Msg, Toast.LENGTH_LONG).show();
			}

			@Override
			public void OnSmsRegTimeout(TLSErrInfo tlsErrInfo) {
				Toast.makeText(RegisterActivity.this, tlsErrInfo.ErrCode + ":" + tlsErrInfo.Title + "  " + tlsErrInfo.Msg, Toast.LENGTH_LONG).show();
			}
		};
	}

	/**
	 * 获取验证码
	 * @param view
	 */
	public void onSmsRegister(View view) {
		String telephone = "86-" + mUserPhoneEditText.getText().toString().trim();
//		setAppId(DemoConstants.APPID, DemoConstants.ACCOUNTTYPE);
		accType = Integer.parseInt(DemoConstants.ACCOUNTTYPE);
		sdkAppid = DemoConstants.APPID;
		accountHelper = TLSAccountHelper.getInstance().init(getApplicationContext(), sdkAppid, accType, appVer);
		initRegListener();
		accountHelper.TLSSmsRegAskCode(telephone, smsRegListener);
	}

//	public void setAppId(int id, String type){
//		Util.APP_ID_TEXT = id;
//		Util.UID_TYPE = type;
//	}

	public void sendSmsSig() {
		String sig = ((EditText) findViewById(R.id.et_password)).getText().toString().trim();
		accountHelper.TLSSmsRegVerifyCode(sig, smsRegListener);
	}

	public void onRegister() {
			sendSmsSig();
			if (checkInput() == false)
				return;

//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					JSONObject object = new JSONObject();
//					try {
//						object.put(Util.EXTRA_USER_PHONE, mUserPhoneEditText.getText().toString().trim());
//						object.put(Util.EXTRA_USER_NAME, "未填写");
//						object.put(Util.EXTRA_PASSWORD, "null");
//
//						System.out.println(object.toString());
//						List<NameValuePair> list = new ArrayList<NameValuePair>();
//						list.add(new BasicNameValuePair(Util.JSON_KEY_DATA, object.toString()));
//						Log.d(TAG, "run register in my own serv ");
//						String response = HttpUtil.PostUrl(HttpUtil.registerUrl, list);
//						Log.d(TAG, "run response" + response);
//						System.out.println(response);
//						if (0 == response.length()) {
//							mErrorHandler.sendEmptyMessage(ERROR_INTERNET);
//							return;
//						}
//						JSONTokener jsonTokener = new JSONTokener(response);
//						JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//						int ret = jsonObject.getInt(Util.JSON_KEY_CODE);
//						if (260 == ret) {
//							Intent intent = new Intent();
//							intent.putExtra(Util.EXTRA_USER_PHONE, userphone);
//							setResult(RESULT_OK, intent);
//							finish();
//						} else if (562 == ret) {
//							mErrorHandler.sendEmptyMessage(ERROR_ACCOUNT_EXIT);
//						} else if (561 == ret) {
//							mErrorHandler.sendEmptyMessage(ERROR_USERNAME_EXIT);
//						} else {
//							mErrorHandler.sendEmptyMessage(ERROR_REGISTER_FAIL);
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();
	}


	public void onSmsRegisterInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject object = new JSONObject();
				try {
					String phone = mUserPhoneEditText.getText().toString().trim();

					Log.d(TAG, "onSmsRegisterInfo phone "+phone+"  username "+ phone);
					object.put(Util.EXTRA_USER_PHONE, phone);
					object.put(Util.EXTRA_USER_NAME, phone);
					object.put(Util.EXTRA_PASSWORD, "null");
					System.out.println(object.toString());
					List<NameValuePair> list = new ArrayList<NameValuePair>();
					list.add(new BasicNameValuePair(Util.JSON_KEY_DATA, object.toString()));
					Log.d(TAG, "run register in my own ");
					String response = HttpUtil.PostUrl(HttpUtil.registerUrl, list);
					Log.d(TAG, "run response" + response);
					response.indexOf("{");
					String substring = response.substring(response.indexOf("{"), response.length());
					Log.d(TAG, "run response222 " + substring);
					System.out.println(response);
					if (0 == substring.length()) {
						mErrorHandler.sendEmptyMessage(ERROR_INTERNET);
						return;
					}


					if(!response.endsWith("}")){
						Log.e(TAG, "onSmsRegisterInfo response is not json style" + response);
						return ;
					}
					JSONTokener jsonTokener = new JSONTokener(substring);
					JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
					int ret = jsonObject.getInt(Util.JSON_KEY_CODE);
					if (260 == ret) {
						Intent intent = new Intent();
						intent.putExtra(Util.EXTRA_USER_PHONE, userphone);
						setResult(RESULT_OK, intent);
						finish();
					} else if (562 == ret) {
						mErrorHandler.sendEmptyMessage(ERROR_ACCOUNT_EXIT);
					} else if (561 == ret) {
						mErrorHandler.sendEmptyMessage(ERROR_USERNAME_EXIT);
					} else {
						mErrorHandler.sendEmptyMessage(ERROR_REGISTER_FAIL);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private Boolean checkInput() {
		int len = userphone.length();
		if(len != 11) {
			mErrorHandler.sendEmptyMessage(ERROR_ACCOUNT_IILEGAL);
			return false;
		}

		char ch = userphone.charAt(1);
		if(userphone.charAt(0) != '1'){
			mErrorHandler.sendEmptyMessage(ERROR_ACCOUNT_IILEGAL);
			return false;
		}

		return true;
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if(s.length() > 0)
			mErrorHandler.sendEmptyMessage(ERROR_CLEAN);
		userphone = mUserPhoneEditText.getText().toString().trim();
		password = mPasswordEditText.getText().toString();
		findViewById(R.id.btn_register).setEnabled(userphone.length() > 0 && password.length() > 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_register:
				onRegister();
				break;
			case R.id.btn_back_login:
				finish();
				break;
		}
	}

	class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			((Button) findViewById(R.id.btn_smsregister)).setClickable(true);
			((Button) findViewById(R.id.btn_smsregister)).setText("获取验证码");
		}

		@Override
		public void onTick(long millisUntilFinished) {
			((Button) findViewById(R.id.btn_smsregister)).setClickable(false);
			((Button) findViewById(R.id.btn_smsregister)).setText( millisUntilFinished / 1000 + "s");
		}
	}
}
