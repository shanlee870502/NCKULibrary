package edu.ncku.application.io.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.util.EnvChecker;

/**
 * 此AsyncTask類別將會在樓層資訊頁面開啟時被執行，進行頁面資料讀取的工作
 */
public class FloorInfoReaderTask extends AsyncTask<Void, Void, Map<String, String>> implements IOConstatnt {

    private static final String DEBUG_FLAG = FloorInfoReaderTask.class.getName();
    private static final String FILE_NAME = FLOOR_INFO_FILE;

    private Context mContext;

    public FloorInfoReaderTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Map<String, String> doInBackground(Void... params) {
        File inputFile = null;
        ObjectInputStream ois = null;
        Map<String, String> serviceMap = null;

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
                serviceMap = (Map<String, String>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serviceMap;
    }
}

