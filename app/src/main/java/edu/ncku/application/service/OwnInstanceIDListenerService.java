package edu.ncku.application.service;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.SubscribeTask;
import edu.ncku.application.util.Preference;

/**
 * 此類別是官網範例中的一個Service，當DeviceID(token)
 * 發生改變時，在背景向 FCM 重新註冊
 */
public class OwnInstanceIDListenerService extends FirebaseInstanceIdService implements IOConstatnt{

    private static final String DEBUG_FLAG = "InstanceIDGetter";
    private static final String[] TOPICS = {"global", "test"};

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "Refreshed token: " + refreshedToken);
        }
        Preference.setDeviceID(getBaseContext(), refreshedToken);
        // Subscribe to topics
        for (String topic:TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + topic);
        }
        // 實作註冊到伺服器端的功能
        sendRegistrationToServer(getApplicationContext());
    }

    private void sendRegistrationToServer(Context context) {
        String userName = Preference.getUsername(context);
        if(userName.equals("")){
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "sendRegistrationToServer: hihihihihihihi");
            }
            return;
        }

        // 到伺服器註冊訂閱狀態
        SubscribeTask subscribeTask = new SubscribeTask(context);
        subscribeTask.execute(true);
        Boolean check;
        try {
            check = subscribeTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            check = false;
        }

        if (check == null) check = false;
        Preference.setSubscription(context, check);
    }
}
