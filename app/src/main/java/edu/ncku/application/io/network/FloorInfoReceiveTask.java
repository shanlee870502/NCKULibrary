package edu.ncku.application.io.network;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

import edu.ncku.application.io.IOConstatnt;

/**
 * 此類別繼承JsonReceiveTask，用來處理樓層資訊JSON資料的接收
 * 並將其存進SD卡之中(覆蓋)。
 */
//2020527繼承使用https的JsonReceiveTask
//public class FloorInfoReceiveTask extends JsonReceiveTask implements IOConstatnt{
public class FloorInfoReceiveTask extends HttpsJsonReceiveTask implements IOConstatnt{
    private static final String DEBUG_FLAG = FloorInfoReceiveTask.class.getName();
    //2020525更改成https的網址
    //private static final String JSON_URL = FLOOR_INFO_URL;
    private static final String JSON_URL = FLOOR_INFO_URL_SSL;
    private static final String FILE_NAME = FLOOR_INFO_FILE;

    public FloorInfoReceiveTask(Context mContext) {
        super(mContext);
    }

    @Override
    public void run() {
        try {
            saveFile(decodeJson(JSON_URL + "cht"), FILE_NAME + "_cht");
            saveFile(decodeJson(JSON_URL + "eng"), FILE_NAME + "_eng");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private LinkedHashMap<String, String> decodeJson(String url) throws IOException, JSONException {

        LinkedHashMap<String, String> floorInfo = new LinkedHashMap<>();

        JSONArray jsonArray = null;
        /*20200527 For the use of https protocol*/
        try {
            jsonArray = new JSONArray(jsonReceive(url));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            String floor = json.getString("Floor");
            String introduction = json.getString("Introduction");

            floorInfo.put(floor, introduction);
        }

        return floorInfo;
    }
}
