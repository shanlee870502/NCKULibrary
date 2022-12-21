package edu.ncku.application.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import edu.ncku.application.util.Preference;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * 因為 FCM 不用手動進行註冊等等，所以如果要取得新的 token，方法是手動把現在的 token 刪除
 * FCM 就會自動產生新的 token 了
 */
public class DeleteTokenService extends IntentService
{
    public static final String TAG = DeleteTokenService.class.getSimpleName();

    public DeleteTokenService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            // Check for current token
            String originalToken = Preference.getDeviceID(this);;
            Log.d(TAG, "Token before deletion: " + originalToken);

            // Resets Instance ID and revokes all tokens.
            FirebaseInstanceId.getInstance().deleteInstanceId();

            // Clear current saved token
            Preference.setDeviceID(this, "");

            // Check for success of empty token
            String tokenCheck = Preference.getDeviceID(this);;
            Log.d(TAG, "Token deleted. Proof: " + tokenCheck);

            // Now manually call onTokenRefresh()
            FirebaseInstanceId id = FirebaseInstanceId.getInstance();
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "Getting new token: " + token);
            Preference.setDeviceID(this, token);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
