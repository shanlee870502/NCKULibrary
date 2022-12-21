package edu.ncku.application.io.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.ActivityInfo;
import edu.ncku.application.util.EnvChecker;

/**
 * 此AsyncTask類別將會在近期活動頁面開啟時被執行，進行頁面資料讀取的工作
 */
public class UpcomingEventsReaderTask extends AsyncTask<Void, Void, List<ActivityInfo>> implements IOConstatnt{

    private static final String DEBUG_FLAG = UpcomingEventsReaderTask.class.getName();
    private static final String FILE_NAME = IOConstatnt.UPCOMING_EVENT_FILE;

    private Context mContext;

    public UpcomingEventsReaderTask(Context context){
        this.mContext = context;
    }

    @Override
    protected List<ActivityInfo> doInBackground(Void... params) {
        File inputFile = null;
        ObjectInputStream ois = null;
        List<ActivityInfo> imgSuperLink = null;

        try {
            inputFile = new File(mContext
                    .getFilesDir(), FILE_NAME + ((EnvChecker.isLunarSetting())?"_cht":"_eng"));

            if (!inputFile.exists()) {
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "file is not exist.");
                }
                return null;
            } else {
                ois = new ObjectInputStream(new FileInputStream(inputFile));
                imgSuperLink = (List<ActivityInfo>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgSuperLink;
    }
}
