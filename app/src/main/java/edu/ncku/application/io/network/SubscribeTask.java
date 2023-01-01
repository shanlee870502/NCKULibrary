package edu.ncku.application.io.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.Locale;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.util.Preference;

/**
 * 此AsyncTask類別是用來讓圖書館伺服器的資料庫改變訂閱狀態，透過 php 網頁傳送參數
 */
public class SubscribeTask extends AsyncTask<Boolean, Void, Boolean> implements IOConstatnt{

    private static final String DEBUG_FLAG = SubscribeTask.class.getName();
    private static final String SUB_URL_SSL = "https://app.lib.ncku.edu.tw/push/subscription.php";

    private Context mContext;

    public SubscribeTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {
        try {
            String username = Preference.getUsername(mContext);
            String did = Preference.getDeviceID(mContext);
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "username = " + username);
                Log.d(DEBUG_FLAG, "deviceID = " + did);
            }
            if(did.equals("") || did.isEmpty()){        // 不大可能會發生就是了...
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "there is no device id");
                }
            }
            // 避免因狀態改變的自動登出導致抓不到username
            if(!username.equals("") && !did.equals("") && !username.isEmpty() && !did.isEmpty()){
                /*20200603 改用https protocol*/
                //String response = HttpClient.sendPost(SUB_URL, String.format(Locale.ENGLISH, "id=%s&did=%s&os=A&sub=%d", username, did, (params[0])?1:0));
                String response = HttpsClient.sendPost(SUB_URL_SSL, String.format(Locale.ENGLISH, "id=%s&did=%s&os=A&sub=%d", username, did, (params[0])?1:0));
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "reponse = " + response);
                }
                HttpsClient.trimCache(mContext);
                if(!response.contains("OK")) //response fail
                    throw new Exception("Posting data to server failed");
                return true;
            }else{
                throw new Exception("Username or deviceID is wrong");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
