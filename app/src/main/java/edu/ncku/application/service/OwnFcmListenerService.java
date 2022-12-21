package edu.ncku.application.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.Executors;

import edu.ncku.application.MainActivity;
import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.MsgReceiveTask;
import edu.ncku.application.util.Preference;
import edu.ncku.application.util.PreferenceKeys;

/**
 * 此類別是繼承自 FirebaseMessagingService，用來實現 FCM 與 APP 的接口
 * 當 FCM 發送訊息給此 APP 時，將會呼叫 onMessageReceived 來處理訊息
 * 而 sendNotification 將會發出通知給使用者。
 */
public class OwnFcmListenerService extends FirebaseMessagingService implements IOConstatnt{

    private static final String DEBUG_FLAG = "OwnFcmListenerService";
    private static final String GLOBAL = "/topics/global";
    private static final String LOGOUT_CTRL = "logout";

    /**
     * Called when message is received.
     *
     * @param message an object of the message
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {


        //if(message.getData().size() > 0) {
            String from = message.getFrom();
            Map data = message.getData();
            String username = (String)data.get("user");
            String title = (String)data.get("title");
            String time = (String)data.get("time");
            String msgNo = (String)data.get("msgNo");
            String control = (String)data.get("control");
            String click_action = (String)data.get("click_action");

            if(showLogMsg){
                Log.d(DEBUG_FLAG, "data: " + data);
                Log.d(DEBUG_FLAG, "from : " + from);
                Log.d(DEBUG_FLAG, "Time : " + time);
                Log.d(DEBUG_FLAG, "msgNo : " + msgNo);
                Log.d(DEBUG_FLAG, "username : " + username);
                Log.d(DEBUG_FLAG, "click_action : " + click_action);
            }


            if(GLOBAL.equals(from) && title != null && !title.isEmpty()){ /* 確認是否為緊急廣播資料*/
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "Handle Global Notification");
                }
                String body = (String)data.get("body");
                sendGlobalNotification(title,body);
                return;
            } else { /* 接收個人推播 */
                /* 確認是否有控制指令 */

                if (control != null) {
                    if(showLogMsg){
                        Log.d(DEBUG_FLAG, "Control: " + control);
                    }
                    if (control.equals(LOGOUT_CTRL)) logout(); // 收到登出指令，清除登入資料
                    return;

                } else if (time == null || time.isEmpty() || msgNo == null || msgNo.isEmpty()) {
                    /* 確認推播訊息的完整性 */
                    if(showLogMsg){
                        Log.e(DEBUG_FLAG, "推播資料缺失");
                    }
                    return;

                } else if(!username.equals(Preference.getUsername(getApplicationContext()))) {/* 如果沒有登入則不顯示推播通知 */
                    return;

                }

                Executors.newSingleThreadExecutor().submit(new MsgReceiveTask(this.getApplicationContext(), username, msgNo, Integer.valueOf(time)));
                // 目前用 MsgReceiveTask 從圖書館伺服器撈

            }
        //}
        //else if (message.getNotification() != null) {
        if (message.getNotification() != null) {
            //改成單純String 用 (" ") 空白分開符號來連接
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "Message Notification Body: " + message.getNotification().getBody());
            }
            //Notification 的 data payload 會變成extra intent被推到launcher activity
        }// Check if message contains a notification payload.
    }
    // [END receive_message]

    private void sendGlobalNotification(String title, String body){
        /* 設置通知點擊會啟動App Intent */
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(PreferenceKeys.GLOBAL_NEWS, true); // 告訴Activity要開啟最新消息頁面
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse("custom://" + System.currentTimeMillis())); // 時間差
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //.setContentTitle(getString(R.string.app_name))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
//                .setSound(defaultSoundUri)  通知聲音關閉
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body)); //big vision style(new)


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(showLogMsg){
                Log.d("test icon", "lollipop~");
            }
            notificationBuilder.setColor(Color.rgb(216,72,48));
            notificationBuilder.setSmallIcon(R.drawable.ic_notification_red);
        }else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_notification_color);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(title.hashCode() /* ID of notification */, notificationBuilder.build());
    }

    /**
     * 清除登入資訊來表示登出
     */
    private void logout(){
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SP.edit().remove(PreferenceKeys.ACCOUNT).apply();
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "logout");
        }
    }
}
