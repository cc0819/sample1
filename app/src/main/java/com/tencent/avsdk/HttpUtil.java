package com.tencent.avsdk;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class HttpUtil {
    private static String TAG = "HttpUtil";
    public static final String SERVER_URL = "http://203.195.167.34/";
    public static final String SERVER_URL_TEST ="http://203.195.167.34:7707";
    public static final String rootUrl = SERVER_URL + "image_get.php";
    public static final String requestUrl = SERVER_URL + "image_post.php";
    public static final String loginUrl = SERVER_URL + "login.php";
    public static final String UserInfoUrl = SERVER_URL + "user_getinfo.php";
    public static final String registerUrl = SERVER_URL + "register.php";
    public static final String replaycreateUrl = SERVER_URL + "replay_create.php";
    public static final String liveImageUrl = SERVER_URL + "live_create.php";
    public static final String testliveImageUrl = SERVER_URL + "test_live_create.php";
    public static final String createRoomNumUrl = SERVER_URL + "create_room_id.php";
    public static final String liveCloseUrl = SERVER_URL + "live_close.php";
    public static final String closeLiveUrl = SERVER_URL + "room_withdraw.php";
    public static final String enterRoomUrl = SERVER_URL + "enter_room.php";
    public static final String getLiveListUrl = SERVER_URL + "live_listget.php";
    public static final String getRecordListUrl= SERVER_URL+"replay_getbytime.php";
    public static final String saveUserInfoUrl = SERVER_URL + "saveuserinfo.php";
    public static final String liveAddPraiseUrl = SERVER_URL + "live_addpraise.php";
    public static final String modifyFieldUrl = SERVER_URL + "user_modifyfields.php";
    public static final String heartClickUrl = SERVER_URL + "update_heart.php";


    public static final int SUCCESS = 200;
    public static final int FAIL = 500;

    public static String PostUrl(String Url, List<NameValuePair> pairlist) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Url);

        List<NameValuePair> params = pairlist;
        UrlEncodedFormEntity entity;
        int ret = 0;
        try {
            entity = new UrlEncodedFormEntity(params, "utf-8");
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            ret = httpResponse.getStatusLine().getStatusCode();

            if (ret > 0) {
                HttpEntity getEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(getEntity, "utf-8");
                return response;
            } else {
                Log.e("error", "Httpresponse error");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("error", e.toString());
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e("error", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("error", e.toString());
            e.printStackTrace();
        }
        return "";
    }
}
