package edu.ncku.application.io.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLHandshakeException;

import edu.ncku.application.LoginDialog;
import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.service.DeleteTokenService;
import edu.ncku.application.util.DrawerListSelector;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.Preference;
import edu.ncku.application.util.PreferenceKeys;
// 舊式AES encryption
import edu.ncku.application.util.Security;

/**
 * 在背景執行登入工作
 */
public class LoginTask extends AsyncTask<String, Void, String> implements IOConstatnt{

    private static final String DEBUG_FLAG = LoginTask.class.getName();

    private static final String LOGIN_URL = "http://140.116.207.50/push/login.php";
    private static final String LOGIN_URL_SSL = "https://app.lib.ncku.edu.tw/push/login.php";
    private static final String OK = "OK";
    private static final String RESULT_LABEL = "Result";
    private static final String NAME_LABEL = "Name";

    private Context context;
    private LoginDialog loginDialog;
    private DrawerListSelector drawerListSelector;
    private SharedPreferences sharedPreferences;


    private String username;

    public LoginTask(Context context, LoginDialog loginDialog, DrawerListSelector drawerListSelector) {
        super();
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.loginDialog = loginDialog;
        this.drawerListSelector = drawerListSelector;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        String login_result = "";

        // 檢查參數值
        if (params == null || params.length != 2 || params[0].isEmpty() || params[1].isEmpty()) {
            return login_result;
        }

        username = params[0].toUpperCase();
        String password = params[1];

        try {
            if(showLogMsg){
                Log.d(DEBUG_FLAG+"1", "username : " + username);
                Log.d(DEBUG_FLAG+"2", "password : " + password);

                //**** 舊的AES encryption *****
                //String str = HttpClient.sendPost(LOGIN_URL, String.format("username=%s&password=%s", username, URLEncoder.encode((new Security()).encrypt(password)), "UTF-8"));
                //*******
                Log.d(DEBUG_FLAG+"3", "password : " + (new Security()).encrypt(password));
            }
            /*20200513 改用https protocol*/
            //String str = HttpClient.sendPost(LOGIN_URL, String.format("username=%s&password=%s&", username, URLEncoder.encode((new Security()).encrypt(password)), "UTF-8"));
            //20201126 先實例化，傳送參數context以在HttpsClient裡面可以使用alertbox
            HttpsClient httpsclient = new HttpsClient(this.context);

            String str = httpsclient.sendPost2(LOGIN_URL_SSL, String.format("username=%s&password=%s&", username, URLEncoder.encode((new Security()).encrypt(password)), "UTF-8"));
            HttpsClient.trimCache(this.context);
            if(showLogMsg){
                Log.e(DEBUG_FLAG+"4", URLEncoder.encode((new Security()).encrypt(password), "utf-8"));

                // 該登入網址如果登入成功，會回傳這樣格式的資訊
                // {"Name":"登入者姓名","Result":"OK"}
                // 姓名是以unicode的方式編碼
                // 如果失敗了，會回傳/nFail
                Log.e(DEBUG_FLAG+"5", str);
            }
            JSONObject json = new JSONObject(str);

            if (OK.equals(json.getString(RESULT_LABEL))) {
                login_result = json.getString(NAME_LABEL);
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "Name : " + login_result);

                    // 處理 deviceID 是 null 的情況
                    Log.d(DEBUG_FLAG, "did = " + Preference.getDeviceID(context));
                }
                String deviceID = sharedPreferences.getString(PreferenceKeys.DEVICE_TOKEN, "");
                if (deviceID == null || deviceID.equals("")) {
                    if(showLogMsg) {
                        Log.d(DEBUG_FLAG, "登入時發現沒有註冊 FCM，刪除現有的 token，重新註冊一個新的");
                    }
                    context.startService(new Intent(context, DeleteTokenService.class));
                }
            }

        } catch (SSLHandshakeException e){
            if(showLogMsg){
                Log.e(DEBUG_FLAG, e.toString());
                Log.w(DEBUG_FLAG, "發生SSLHandshakeException");
            }
            return "sslexception";
        }
        catch (JSONException e) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, e.toString());
                Log.w(DEBUG_FLAG, "登入資訊Json格式解析錯誤或登入失敗");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if(showLogMsg){
                Log.e(DEBUG_FLAG, e.toString());
            }
        }

        return login_result;
    }

    @Override
    protected void onPostExecute(final String name) {
        super.onPostExecute(name);
        if(showLogMsg){
            Log.e(DEBUG_FLAG, "name: "+name);
        }
        loginDialog.setLogining(false);
        //20201126 Modify login event handling, if SSLHandshakeException occurs show the alertbox
        if (!name.isEmpty()) {
            if(name.equals("sslexception")){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this.context);
                LayoutInflater inflater = LayoutInflater.from(this.context);;
                builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            } else{

                /* 存進設定值 */
                Preference.setName(context, name);
                Preference.setUsername(context, username);
                Preference.setSubscription(context, false);

                drawerListSelector.loginState(Preference.getName(context)); // 透過drawerListSelector來改變drawer狀態
                loginDialog.dismiss();      // close the login dialog fragment

                (new AlertDialog.Builder(context))
                        .setMessage(R.string.sub_hint)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SubscribeAfterLogin(true);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled to subscribe
                                SubscribeAfterLogin(false);
                            }
                        }).show();

                Toast.makeText(context, R.string.login_success, Toast.LENGTH_LONG).show();
                Preference.setLoggin(context, true);
            }
        } else {
            loginDialog.mTxtTip.setText(R.string.invalid_account_or_password);
            Preference.setAttemptLogin(context, new Date().getTime());
        }
    }

    // 這函式是用來處理當登入之後，對於訂閱所使用的判斷，不論使用者登入之後是
    // 否選擇訂閱，都該在資料庫上寫下他的帳號資訊和 device id
    private void SubscribeAfterLogin(boolean yesOrNo) {
        // User clicked OK button
        final ProgressDialog progressDialog;
        if (EnvChecker.isNetworkConnected(context)) {
            // Start IntentService to register this application with FCM.
            progressDialog = ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.handle_subscription), true); // 顯示處理中的Dialog
            SubscribeTask subscribeTask = new SubscribeTask(context);
            subscribeTask.execute(yesOrNo);
            Boolean check;
            try {
                check = subscribeTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                check = false;
            } catch (ExecutionException e) {
                e.printStackTrace();
                check = false;
            }

            if (check == null) check = false;

            final boolean transCheck = check;
            Preference.setSubscription(context, transCheck && yesOrNo);

            /* 更新UI */
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null)
                        progressDialog.dismiss(); // 關閉顯示處理中的Dialog
                    Toast.makeText(context, (transCheck) ? R.string.sub_handled : R.string.sub_fail, Toast.LENGTH_LONG).show(); // 顯示Toast
                }
            }, 1000);
        } else {
            Toast.makeText(context, R.string.network_disconnected, Toast.LENGTH_LONG).show();
        }
    }
}
