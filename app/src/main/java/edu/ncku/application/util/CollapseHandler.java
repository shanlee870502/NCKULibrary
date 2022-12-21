package edu.ncku.application.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.ncku.application.io.IOConstatnt;

import static android.os.Build.VERSION;

/**
 * 此類別用來處理此應用程式崩潰問題，如果發生了任一例外卻沒有處理
 * 則進入此類別的uncaughtException方法進行相關處理。
 */
public class CollapseHandler implements Thread.UncaughtExceptionHandler, IOConstatnt{

    private static final String LOGTAG = "CollapseHandler";
    private static final String LOG_FILE = "CollapseLog";

    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private Context mAppContext;

    public CollapseHandler(Context context) {
        mAppContext = context.getApplicationContext();
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try{
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            String errorReport = String.format("[%s]\nAndroidVersion=%s\nIMEI=%s\nID=%s\n%s", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()), VERSION.RELEASE, getIMEI(), Preference.getUsername(mAppContext), result.toString());

            if(showLogMsg){
                Log.e(LOGTAG, errorReport);
            }

            File logFile = new File(mAppContext.getFilesDir(), LOG_FILE);
            FileWriter fw = new FileWriter(logFile, true);

            fw.write(errorReport);

            fw.flush();
            fw.close();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    private String getIMEI() {
        try {
            TelephonyManager telManager = (TelephonyManager) mAppContext.getSystemService(Context.TELEPHONY_SERVICE);

            return telManager.getDeviceId();
        }catch(Exception e){
            e.printStackTrace();
        }

        return "Fail to Catch IMEI";
    }

}
