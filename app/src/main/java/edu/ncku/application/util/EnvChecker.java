package edu.ncku.application.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

/**
 * Created by NCKU on 2016/5/3.
 * 工具靜態類別，用來取得一些環境參數
 */
public class EnvChecker {

    /**
     * 判斷是否為(簡繁)中文環境
     *
     * @return
     */
    public static boolean isLunarSetting() {
        String language = getLanguageEnv();

        if (language != null
                && (language.trim().equals("zh-CN") || language.trim().equals("zh-TW")))
            return true;
        else
            return false;
    }

    /**
     * 確認手機是否連線(只能知道wifi或3, 4G已經連上AP或基地台不代表真的連上網路)
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager CM = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
        *  以兩秒為上限，對Google DNS進行Ping的動作，以確認真正有連上網路
        *  去ping google的原因是為了避免進入成大的認證網頁，避免有連上網路
        *  ，但沒有連線功能的情況，而開啟成大的認證頁面
        * @return
        */
    public static boolean pingGoogleDNS(Context context) {
        if(Build.FINGERPRINT.contains("generic")){
            return isNetworkConnected(context);
        }
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 -W 2 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    return false;
    }

    /**
     * 取得語言環境參數
     *
     * @return 語言環境
     */
    private static String getLanguageEnv() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry().toLowerCase();

        if ("zh".equals(language)) {
            if ("cn".equals(country)) {
                language = "zh-CN";
            } else if ("tw".equals(country)) {
                language = "zh-TW";
            }
        } else if ("pt".equals(language)) {
            if ("br".equals(country)) {
                language = "pt-BR";
            } else if ("pt".equals(country)) {
                language = "pt-PT";
            }
        }

        return language;
    }

}
