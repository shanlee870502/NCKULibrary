package edu.ncku.application.util;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by NCKU on 2016/11/9.
 *
 * 這個類別用來實做一些所有 fragment 都能使用到的功能
 * 包含：
 * hide softkeyboard
 */
public class UniversalAbility {

    // 使用傳進來的 activity 來取得 layout 的 context，然後隱藏鍵盤
    public static void HideKeyboard(Activity activity) {
        Context context = activity.getBaseContext();
        InputMethodManager imm;
        if(context == null) {
            imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        else {
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (activity.getCurrentFocus() != null )    //20210127 修正targetSdkVersion調升後產生問題 開始
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0); // 隱藏鍵盤
        //20210127 修正targetSdkVersion調升後產生問題 結束
    }
}
