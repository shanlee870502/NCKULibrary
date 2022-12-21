package edu.ncku.application.io.network;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.ncku.application.io.IOConstatnt;

/**
 * Created by NCKU on 2016/6/14.
 */
public class CollapseLogSendTask implements Runnable, IOConstatnt {

    private static final String DEBUG_FLAG = CollapseLogSendTask.class.getName();
    //20200603 改成https網址
    //private static final String LOGS_URL = "http://140.116.207.50/push/android_crash_logs.php";
    private static final String LOGS_URL_SSL = "https://140.116.207.50/push/android_crash_logs.php";
    private static final String LOG_FILE = "CollapseLog";

    private Context mContext;

    public CollapseLogSendTask(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        File logFile = new File(mContext.getFilesDir(), LOG_FILE);
        if(!logFile.exists()) return;

        String log = "crashLog=", line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFile));

            while ((line = reader.readLine()) != null){
                log += line + "\n";
            }
            //20200603 改成https protocol
            //HttpClient.sendPost(LOGS_URL, log);
            HttpsClient.sendPost(LOGS_URL_SSL, log);
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "send log...");
            }

            logFile.delete();
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "delete log...");
            }
        } catch (FileNotFoundException e) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "Collapse log file is not found.", e);
            }
        }  catch (Exception e) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "Some exceptions", e);
            }
        } finally {
            if(reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
