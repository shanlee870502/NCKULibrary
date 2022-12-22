package edu.ncku.application.io.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.util.Preference;

public class OccupancyReceiveTask implements Runnable, IOConstatnt{
    private static final String Mainlib_API = Main_LIB_URL_SSL;
    private static final String KnowLEDGE_API = KnowLEDGE_URL_SSL;
    private static final String Medlib_API = Medlib_URL_SSL;
    private static final String  D24_API = D24_URL_SSL;
    private static final String Xcollege_API = Xcollege_URL_SSL;

    private Context mContext;
    private boolean isBackground;
    private boolean isOnce; // 判斷是否為使用者刷新席位使用頁面

    public OccupancyReceiveTask(Context context, boolean isBackground, boolean isOnce){
        this.mContext = context;
        this.isBackground = isBackground;
        this.isOnce = isOnce;
    }

    @Override
    public void run() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo currentNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if(currentNetworkInfo!=null && currentNetworkInfo.isConnected()) {
                Map<String,String> occupancy = new HashMap<String,String>();
                occupancy.put("mainlib","");
                occupancy.put("knowledge","");
                occupancy.put("medlib","");
                occupancy.put("d24","");
                occupancy.put("xcollege","");

                try{
                    occupancy.put("mainlib", HttpsClient.sendPost(Mainlib_API,""));
                    occupancy.put("knowledge",HttpsClient.sendPost(KnowLEDGE_API, "").trim());
                    occupancy.put("medlib",HttpsClient.sendPost(Medlib_API,"").trim());
                    occupancy.put("d24", HttpsClient.sendPost(D24_API,"").trim());
                    occupancy.put("xcollege",HttpsClient.sendPost(Xcollege_API,"").trim());
                }catch (Exception e){
                    Log.i("Occupancy","Https send post failed");
                }
                for(Map.Entry<String, String> entry: occupancy.entrySet()) {
                    if(!TextUtils.isDigitsOnly(entry.getValue())){
                        occupancy.put(entry.getKey(),"");
                    }
                }
                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.OCCUPANCY_RECEIVER");
                for(Map.Entry<String, String> entry: occupancy.entrySet()) {
                    mIntent.putExtra(entry.getKey(),entry.getValue());
                }
                mContext.sendBroadcast(mIntent);

                /* 註冊 10 秒後的在館人數請求工作 */
                if(isBackground){
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                    executor.schedule(new OccupancyReceiveTask(mContext, true, false), 10, TimeUnit.SECONDS);
                }
            }else{ //network not connected
                if(isBackground){
                    Intent mIntent = new Intent();
                    mIntent.setAction("android.intent.action.OCCUPANCY_RECEIVER");
                    mContext.sendBroadcast(mIntent);
                    Log.i("Occupancy","網路斷線 取消註冊1秒後的在館人數請求工作");
                }
            }
        }catch(Exception e) {
            /* 當發生例外時，一律對前景發出網路不通的訊息 */
            if(!isBackground) {
                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.OCCUPANCY_RECEIVER");
                mContext.sendBroadcast(mIntent);
            }
            e.printStackTrace();
        }
    }
}
