package edu.ncku.application.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import edu.ncku.application.R;
import edu.ncku.application.io.network.DetectLoginStateTask;
import edu.ncku.application.util.Security;


/**
 * 本機端資料管理類別
 */
public class Preference {

    private static final String DEBUG_FLAG = Preference.class.getName();

    /**
     * 確認手機是否已訂閱推播訊息(跟伺服器可能會不一致)
     *
     */
    public static boolean isSub(Context context, String notifyUsername){
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        // 20201112 Decrypt the ACCOUNT from shared_prefs
        String username = null;
        try {
            username = (new Security()).decrypt(SP.getString(PreferenceKeys.ACCOUNT, ""));
        }catch (Exception e) {
        }
        boolean sub = SP.getBoolean(PreferenceKeys.SUBSCRIPTION, false);

        if (username.isEmpty() || !username.equals(notifyUsername)) { // 如果沒登入或者帳號與notifyUsername不一致則一律回傳false
            return false;
        } else {
            return sub;
        }
    }

    /**
     * 取得使用者姓名(MASK_NAME)
     */
    public static String getName(Context context){
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (SP.contains(PreferenceKeys.MASK_NAME) == false){
            Log.d("not setting name",PreferenceKeys.NAME);
            // 20220707 更新: 因資安檢測問題，如果沒有maskname 使用者重新設定maskname並加密原始姓名
            setName(context, PreferenceKeys.NAME);
        }
        return SP.getString(PreferenceKeys.MASK_NAME, "");
    }

    /**
     * 取得使用者帳號
     */
    public static String getUsername(Context context) {
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        // 20201112 Decrypt the ACCOUNT from shared_prefs
        String username = null;
        try {
            username = (new Security()).decrypt(SP.getString(PreferenceKeys.ACCOUNT, ""));
        }catch (Exception e) {
        }
        return username;
    }

    /**
     * 取得GCM Device ID
     */
    public static String getDeviceID(Context context) {
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        return SP.getString(PreferenceKeys.DEVICE_TOKEN, "");
    }

    /**
     * 取得備份總館人數
     */
    public static String getMainVisitor(Context context) {
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        return SP.getString(PreferenceKeys.MAINVISITOR, "");
    }

    /**
     * 取得備份 K 館人數
     */
    public static String getKVisitor(Context context) {
        final SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(context);
        return SP.getString(PreferenceKeys.KVISITOR, "");
    }

    /**
     * 儲存使用者姓名
     * 儲存兩種形式的姓名: 1. 全姓名加密 2. 遮罩姓名 (例如: 王小明 -> 王**)
     */
    public static void setName(Context context, String name) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String encrypt_name = "";
        String mask_name = "";
        try{
            // 20220707 encrypte name
            encrypt_name = (new Security()).encrypt(name);
            // 20220707 mask name
            for (String retval: name.split(" ")){
                String mask = String.join("", Collections.nCopies(retval.length()-1, "*"));;
                mask_name += Character.toString(retval.charAt(0)) + mask + "\t";
            }

        }catch(Exception e){

        }
        Log.d("setName> mask_name", mask_name);
        Log.d("setName> encrypt_name", encrypt_name);
        sp.edit().putString(PreferenceKeys.NAME, encrypt_name).apply();
        sp.edit().putString(PreferenceKeys.MASK_NAME, mask_name).apply();
    }

    /**
     * 儲存帳號，同時表示已登入
     */
    public static void setUsername(Context context, String username) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // 20201112 Encrypt the ACCOUNT and write in shared_prefs
        String encrypt_username = null;
        try{
            encrypt_username = (new Security()).encrypt(username);
        }catch (Exception e){

        }
        sp.edit().putString(PreferenceKeys.ACCOUNT, encrypt_username).apply();
    }

    /**
     * 儲存訂閱狀態
     */
    public static void setSubscription(Context context, boolean sub) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PreferenceKeys.SUBSCRIPTION, sub).apply();
    }

    /**
     * 儲存總館人數
     */
    public static void setMainVisitor(Context context, String visitor) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PreferenceKeys.MAINVISITOR, visitor).apply();
    }

    /**
     * 儲存 K 館人數
     */
    public static void setKVisitor(Context context, String visitor) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PreferenceKeys.KVISITOR, visitor).apply();
    }

    /**
     * 儲存 K 館 2樓人數
     */
    public static void setK2Visitor(Context context, String visitor) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PreferenceKeys.K2VISITOR, visitor).apply();
    }

    /**
     * 儲存 Device ID
     */
    public static void setDeviceID(Context context, String deviceID) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PreferenceKeys.DEVICE_TOKEN, deviceID).apply();
    }

    /**
     * 確認是否已登入(以判斷帳號是否存在為依據)
     */
    public static boolean isLoggin(final Context context){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PreferenceKeys.LOGGIN_STATE, false);
    }

    /**
     * 確認是否已登入(以判斷帳號是否存在為依據)
     */
    public static void setLoggin(final Context context, boolean login_state){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PreferenceKeys.LOGGIN_STATE, login_state).apply();
    }

    /**
     *  檢查登入狀態，以使用者帳號和本身APP的device id
     */
    public static void DetectLoggin(final Context context) {
        /* 判斷網路是否有連線 */
        if (!EnvChecker.isNetworkConnected(context)) {
            String error_message = context.getString(R.string.login_detect_error);
            Toast.makeText(context, error_message, Toast.LENGTH_LONG)
                    .show();
            setLoggin(context, false);
        }

        // 檢查登入狀態
        DetectLoginStateTask detectLoginTask = new DetectLoginStateTask(context);
        detectLoginTask.execute();
        Boolean check;
        try {
            check = detectLoginTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            check = false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            check = false;
        }
        final boolean loginState = check;

        if(loginState){
            Toast.makeText(context, (loginState) ? R.string.approval : R.string.disapproval, Toast.LENGTH_LONG).show(); // 顯示Toast
        }

        setLoggin(context, loginState);
    }

    /**
     *  儲存上次登入時間以及嘗試次數
     */
    public static void setAttemptLogin(Context context, long now_time){
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        Integer login_num = sp.getInt(PreferenceKeys.ATTEMPT_LOGIN_NUM, 0);
        Long last_login_time = sp.getLong(PreferenceKeys.ATTEMPT_LOGIN_TIME, 0);

        // 若上次登入時間為3分鐘前，且嘗試次數少於3次，則重設嘗試次數為1次
        // 若上次登入時間為15分鐘前，則重設嘗試次數為1次
        if( (now_time - last_login_time > 3 * 60 * 1000 && login_num <= 3) || (now_time - last_login_time > 15 * 60 * 1000 )){
            sp.edit().putInt(PreferenceKeys.ATTEMPT_LOGIN_NUM, 1).apply();
        }
        // 其餘則計算為3分鐘內嘗試次數
        else {
            sp.edit().putInt(PreferenceKeys.ATTEMPT_LOGIN_NUM, login_num+1).apply();
        }

        sp.edit().putLong(PreferenceKeys.ATTEMPT_LOGIN_TIME, now_time).apply();
//        Log.d("setAttemptLogin", String.valueOf(sp.getInt(PreferenceKeys.ATTEMPT_LOGIN_NUM, 0)));
//        Log.d("setAttemptLogin", String.valueOf(sp.getLong(PreferenceKeys.ATTEMPT_LOGIN_TIME, 0)));
    }
}
