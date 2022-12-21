package edu.ncku.application.io.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.util.Preference;

/**
 * 此AsyncTask類別是用來讓圖書館伺服器的資料庫改變訂閱狀態，透過 php 網頁傳送參數
 */
public class DetectLoginStateTask extends AsyncTask<Boolean, Void, Boolean> implements IOConstatnt{

    private static final String DEBUG_FLAG = DetectLoginStateTask.class.getName();

    //20200603改成https網址
    //private static final String Detect_URL = "http://140.116.207.50/push/detect_login_state.php";
    private static final String Detect_URL_SSL = "https://app.lib.ncku.edu.tw/push/detect_login_state.php";
    private Context mContext;

    public DetectLoginStateTask(Context mContext) {
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

            //20200603 改成https protocol
            //String httpResult = HttpClient.sendPost(Detect_URL, String.format(Locale.ENGLISH, "id=%s&did=%s", username, did));
            //JSONObject jsonResult = new JSONObject(httpResult);
            String httpsResult = HttpsClient.sendPost(Detect_URL_SSL, String.format(Locale.ENGLISH, "id=%s&did=%s", username, did));
            HttpsClient.trimCache(mContext);;
            JSONObject jsonResult = new JSONObject(httpsResult);
            return jsonResult.getBoolean("Result");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
