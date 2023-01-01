package edu.ncku.application.io.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.util.Preference;

/**
 * 在館人數更新背景工作
 */
public class VisitorRecieveTask implements Runnable, IOConstatnt {
    private static final String DEBUG_FLAG = VisitorRecieveTask.class.getName();
    private static final String Main_LIB_VISITORS_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_main.php";
    private static final String K_VISITORS_URL_SSL = "https://app.lib.ncku.edu.tw/push/functions/getVisitorNumber_Knowledge.php";


    private Context mContext;
    private boolean isBackground;
    private boolean isOnce;


    public VisitorRecieveTask(Context context, boolean isBackground, boolean isOnce) {
        this.mContext = context;
        this.isBackground = isBackground;
        this.isOnce = isOnce;
    }

    @Override
    public void run() {
        try {
            ConnectivityManager connectivityManager = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
            NetworkInfo currentNetworkInfo = connectivityManager.getActiveNetworkInfo();

            /* 再次確認網路狀態 */
            if (currentNetworkInfo != null && currentNetworkInfo.isConnected()) {
                String visitors_main = "";
                String visitors_k = "";
                String visitors_k2 = "";
                try {
                    visitors_main = HttpsClient.sendPost(Main_LIB_VISITORS_URL_SSL, "").trim();
                    visitors_k = HttpsClient.sendPost(K_VISITORS_URL_SSL, "").trim();

                }catch (Exception e){
                    e.printStackTrace();
                    visitors_main = "";
                    visitors_k = "";
                }

                /* 如果 main visitors 回傳結果包含非數字則清空 */
                if(!TextUtils.isDigitsOnly(visitors_main)){
                    visitors_main = "";
                }

                /* 如果 k visitors 回傳結果包含非數字則清空 */
                if(!TextUtils.isDigitsOnly(visitors_k)){
                    visitors_k = "";
                }

//                /* 如果 k2 visitors 回傳結果包含非數字則清空 */
//                if(!TextUtils.isDigitsOnly(visitors_k2)){
//                    visitors_k2 = "";
//                }

                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.VISITORS_RECEIVER");
                mIntent.putExtra("visitors_main", visitors_main);
                mIntent.putExtra("visitors_k", visitors_k);
//                mIntent.putExtra("visitors_k2", visitors_k2);
                Preference.setMainVisitor(mContext, visitors_main); // 這是避免App剛開啟時，在一分鐘的空窗期內不會立即顯示的解決方法
                Preference.setKVisitor(mContext, visitors_k);
//                Preference.setK2Visitor(mContext, visitors_k2);
                mContext.sendBroadcast(mIntent);

                /* 註冊一分鐘後的在館人數請求工作 */
                if(isBackground && !isOnce) {
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                    executor.schedule(new VisitorRecieveTask(mContext, true, false), 1, TimeUnit.MINUTES);
                    executor.shutdown();
                }else{
                    if(showLogMsg){
                        Log.v(DEBUG_FLAG, "點擊刷新");
                    }
                }
            }else{ // 網路斷線
                if(isBackground) {
                    if(showLogMsg){
                        Log.d(DEBUG_FLAG, "網路斷線，取消註冊一分鐘後的在館人數請求工作");
                    }
                    //20200407 就算網路斷線還是傳送廣播給mainpage，讓mainpage那邊處理
                    //Preference.setMainVisitor(mContext, ""); // 斷線時清空
                    Intent mIntent = new Intent();
                    mIntent.setAction("android.intent.action.VISITORS_RECEIVER");
                    mContext.sendBroadcast(mIntent);
                }else{
                    Intent mIntent = new Intent();
                    mIntent.setAction("android.intent.action.VISITORS_RECEIVER");
                    mContext.sendBroadcast(mIntent);
                }
            }
        } catch (Exception e) {
            /* 當發生例外時，一律對前景發出網路不通的訊息 */
            if(!isBackground) {
                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.VISITORS_RECEIVER");
                mContext.sendBroadcast(mIntent);
            }
            e.printStackTrace();
        }
    }
}
