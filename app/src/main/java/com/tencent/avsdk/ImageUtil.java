package com.tencent.avsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {
    private final String TAG = "ImageUtil";

    public void saveImage(Bitmap bitmap, String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }

        try {
            f.createNewFile();
        } catch (IOException e) {
            Log.w(TAG, "00" + e.toString());
            e.printStackTrace();
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "22" + e.toString());
            e.printStackTrace();
        }
        if (fout != null)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
        try {
            if (fout != null)
                fout.flush();
        } catch (IOException e) {
            Log.w(TAG, "111" + e.toString());
            e.printStackTrace();
        }
        try {
            if (fout != null)
                fout.close();
        } catch (IOException e) {
            Log.w(TAG, "222" + e.toString());
            e.printStackTrace();
        }
    }

    public int sendCoverToServer(String f, JSONObject object, String url, String json)
            throws UnsupportedEncodingException, JSONException {
        File file = new File(f);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody info = new StringBody(object.toString(), Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file, "image/jpg");
        mpEntity.addPart("image", cbFile);
        mpEntity.addPart(json, info);
        String response = Send(mpEntity, url);
        if (!response.endsWith("}")) {
            Log.e(TAG, "sendCoverToServer response is not json style" + response);
            return -1;
        }
        JSONTokener jsonTokener = new JSONTokener(response);
        JSONObject OB = (JSONObject) jsonTokener.nextValue();
        int ret = OB.getInt("code");
        Log.w(TAG, "ret = " + ret);
        Log.d(TAG, "sendCoverToServer " + response);
        return ret;

    }

    public String requestRecordList(JSONObject object, String url, String json)
            throws UnsupportedEncodingException, JSONException {
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody info = new StringBody(object.toString(), Charset.forName("UTF-8"));
        mpEntity.addPart(json, info);
        Log.d(TAG, "requestRecordList url " + url);
        Log.d(TAG, "requestRecordList object " + object);
        Log.d(TAG, "requestRecordList entity " + mpEntity);
//        String response = Send(mpEntity, url);
//        Log.i(TAG, "sendCoverToServer response is not json style"+ response);
//        if(!response.endsWith("}")){
//            Log.e(TAG, "sendCoverToServer response is not json style"+ response);
//            return "null" ;
//        }

        HttpPost httpRequest = new HttpPost(url);   //建立HTTP POST联机
        List<NameValuePair> params = new ArrayList<NameValuePair>();   //Post运作传送变量必须用NameValuePair[]数组储存
        httpRequest.setEntity(mpEntity);   //发出http请求
        HttpResponse httpResponse = null;   //取得http响应
        try {
            httpResponse = new DefaultHttpClient().execute(httpRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (httpResponse.getStatusLine().getStatusCode() == 200)
            try {
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.d(TAG, "requestRecordList strResult " + strResult);
                return strResult;
            } catch (IOException e) {
                e.printStackTrace();
            }
        return "";

    }

    public int sendHeadToServer(String s, UserInfo mSelfUserInfo)
            throws UnsupportedEncodingException, JSONException {

        File file = new File(s);
        System.out.println(s);
        MultipartEntity mpEntity = new MultipartEntity();
        String userphone = mSelfUserInfo.getUserPhone();
        int imagetype = 1;

        JSONObject object = new JSONObject();
        try {
            object.put("userphone", userphone);
            object.put("imagetype", imagetype);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ContentBody info = new StringBody(object.toString(), Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file, "image/jpg");

        mpEntity.addPart("image", cbFile);
        mpEntity.addPart("imagepostdata", info);
        String response = Send(mpEntity, HttpUtil.requestUrl);
        if (!response.endsWith("}")) {
            Log.e(TAG, "sendHeadToServer response is not json style" + response);
            return -1;
        }
        JSONTokener jsonTokener = new JSONTokener(response);
        JSONObject OB = (JSONObject) jsonTokener.nextValue();
        int ret = OB.getInt("code");
        Log.w(TAG, ret + "222");
        return ret;
    }


    public String Send(HttpEntity entity, String RequestUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httpPost = new HttpPost(RequestUrl);
        httpPost.setEntity(entity);
        System.out.println("executing request: " + httpPost.getRequestLine());
        HttpResponse response = null;
        try {
                response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }
        if (response == null)
            return "";
        HttpEntity resEntity = response.getEntity();
        int ret = response.getStatusLine().getStatusCode();
        String res = null;
        httpClient.getConnectionManager().shutdown();
        if (ret == 200)
            try {
                res = EntityUtils.toString(resEntity, "utf-8");
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }

        return "";
    }

    public Bitmap getImageFromServer(String param) {
        String url = HttpUtil.rootUrl + param;
        Log.w(TAG, "2222" + url);
        Bitmap bitmap = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream is = response.getEntity().getContent();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
