package edu.ncku.application.io.network;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.ncku.application.io.IOConstatnt;

/**
 * 此類別用來接收開館時間的JSON資料，一樣儲存進檔案
 */
//2020527繼承使用https的JsonReceiveTask
//public class LibOpenTimeReceiveTask extends JsonReceiveTask  implements IOConstatnt {
public class LibOpenTimeReceiveTask extends HttpsJsonReceiveTask  implements IOConstatnt {
    private static final String DEBUG_FLAG = LibOpenTimeReceiveTask.class.getName();

    //20200527更改成https的網址
    //private static final String JSON_LIB_INFO_URL = LIB_OPEN_TIME_URL;
    private static final String JSON_LIB_INFO_URL = LIB_OPEN_TIME_URL_SSL;

    private static final String FILE_NAME = LIB_OPEN_TIME_FILE;
    private static final String TITLES_CHT = "titles_cht";
    private static final String CONTENT_CHT = "content_cht";
    private static final String TITLES_ENG = "titles_eng";
    private static final String CONTENT_ENG = "content_eng";


    public LibOpenTimeReceiveTask(Context mContext) {
        super(mContext);
    }

    @Override
    public void run() {

        try {

            parsingAllJson();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsingAllJson() throws JSONException, IOException {
        Map<String, ArrayList<String>> serviceChtMap = new LinkedHashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> serviceEngMap = new LinkedHashMap<String, ArrayList<String>>();

        /*20200527 For the use of https protocol*/
        JSONObject json = null;
        try {
            json = new JSONObject(jsonReceive(JSON_LIB_INFO_URL));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        JSONArray titles_cht = json.getJSONArray(TITLES_CHT);
        JSONObject content_cht = json.getJSONObject(CONTENT_CHT);
        for(int titleIndex = 0; titleIndex < titles_cht.length(); titleIndex++){
            String title = titles_cht.getString(titleIndex);

            JSONArray services = null;
            try{
                services = content_cht.getJSONArray(title);
            }catch(JSONException e){ // 避免標題抓不到
                e.printStackTrace();
            }

            if(services == null) continue;

            ArrayList<String> contentList = new ArrayList<String>();
            String content = "";

            for(int contentIndex = 0;contentIndex < services.length(); contentIndex++){
                content += services.getString(contentIndex) + "\n";
            }
            contentList.add(content);
            serviceChtMap.put(title, contentList);
        }

        JSONArray titles_eng = json.getJSONArray(TITLES_ENG);
        JSONObject content_eng = json.getJSONObject(CONTENT_ENG);
        for(int titleIndex = 0; titleIndex < titles_eng.length(); titleIndex++){
            String title = titles_eng.getString(titleIndex);
            JSONArray services = null;
            try{
                services = content_eng.getJSONArray(title);
            }catch(JSONException e){
                e.printStackTrace();
            }

            if(services == null) continue;

            ArrayList<String> contentList = new ArrayList<String>();
            String content = "";

            for(int contentIndex = 0;contentIndex < services.length(); contentIndex++){
                content += services.getString(contentIndex) + "\n";
            }
            contentList.add(content);
            serviceEngMap.put(title, contentList);
        }

        saveFile(serviceChtMap, FILE_NAME + "_cht");
        saveFile(serviceEngMap, FILE_NAME + "_eng");
    }

}
