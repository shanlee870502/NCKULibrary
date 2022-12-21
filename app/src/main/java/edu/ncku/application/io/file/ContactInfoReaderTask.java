package edu.ncku.application.io.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.ContactInfo;
import edu.ncku.application.util.EnvChecker;

/**
 * 此AsyncTask類別將會在聯絡資訊頁面開啟時被執行，進行頁面資料讀取的工作
 */
public class ContactInfoReaderTask extends AsyncTask<Void, Void, ArrayList<ContactInfo>> implements IOConstatnt {

    private static final String DEBUG_FLAG = ContactInfoReaderTask.class.getName();
    private static final String FILE_NAME = CONTACT_FILE;

    private Context mContext;

    public ContactInfoReaderTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected ArrayList<ContactInfo> doInBackground(Void... params) {
        File inputFile = null;
        ObjectInputStream ois = null;
        ArrayList<ContactInfo> contactInfos = null;

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
                contactInfos = (ArrayList<ContactInfo>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactInfos;
    }
}
