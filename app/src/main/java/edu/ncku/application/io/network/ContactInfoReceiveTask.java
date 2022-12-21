package edu.ncku.application.io.network;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.ContactInfo;

/**
 * 此類別繼承JsonReceiveTask，用來處理聯絡資訊JSON資料的接收
 * 並將其存進SD卡之中(覆蓋)。
 */
//2020525繼承使用https的JsonReceiveTask
//public class ContactInfoReceiveTask extends JsonReceiveTask implements IOConstatnt {
public class ContactInfoReceiveTask extends HttpsJsonReceiveTask implements IOConstatnt {

    private static final String DEBUG_FLAG = ContactInfoReceiveTask.class.getName();
    //2020525更改成https的網址
    //private static final String JSON_URL = CONTACT_URL;
    private static final String JSON_URL = CONTACT_URL_SSL;
    private static final String FILE_NAME = CONTACT_FILE;

    public ContactInfoReceiveTask(Context mContext) {
        super(mContext);
    }

    @Override
    public void run(){
        try {
            saveFile(decodeJson(JSON_URL + "cht"), FILE_NAME + "_cht"); // 存進檔案之中(中文)
            saveFile(decodeJson(JSON_URL + "eng"), FILE_NAME + "_eng"); // 存進檔案之中(英文)
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解碼JSON
     *
     * @param url JSON網站URL
     * @return 解碼後的聯絡資料
     * @throws IOException
     * @throws JSONException
     */
    private ArrayList<ContactInfo> decodeJson(String url) throws  JSONException {
        ArrayList<ContactInfo> contactInfos = new ArrayList<ContactInfo>();

        /*20200527 For the use of https protocol*/
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonReceive(url));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        /* 將資料從JSON物件當中取出 */
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);

            contactInfos.add(new ContactInfo(
                    json.getString("Division"),
                    json.getString("Phone"),
                    json.getString("Email")
            ));
        }

        return contactInfos;
    }

}