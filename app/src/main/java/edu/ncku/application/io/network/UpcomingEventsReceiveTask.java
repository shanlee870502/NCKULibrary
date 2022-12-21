package edu.ncku.application.io.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.ActivityInfo;

/**
 * 此類別用來接收近期活動的JSON資料，一樣儲存進檔案
 */
//2020527繼承使用https的JsonReceiveTask
//public class UpcomingEventsReceiveTask extends JsonReceiveTask implements IOConstatnt {
public class UpcomingEventsReceiveTask extends HttpsJsonReceiveTask implements IOConstatnt {
    private static final String DEBUG_FLAG = UpcomingEventsReceiveTask.class.getName();
    //20200527更改成https的網址
    //private static final String JSON_URL = UPCOMING_EVENT_URL;
    private static final String JSON_URL = UPCOMING_EVENT_URL_SSL;
    private static final String FILE_NAME = UPCOMING_EVENT_FILE;

    public UpcomingEventsReceiveTask(Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
//            Map<String, String> upcomingEventData;
            List<ActivityInfo> upcomingEventData;
            upcomingEventData = decodeJson(JSON_URL + "cht");

            if(upcomingEventData != null && !upcomingEventData.isEmpty()){
                saveFile(upcomingEventData, FILE_NAME + "_cht");
            }

            upcomingEventData = decodeJson(JSON_URL + "eng");

            if(upcomingEventData != null && !upcomingEventData.isEmpty()){
                saveFile(upcomingEventData, FILE_NAME + "_eng");
            }
        } catch (Exception e){
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "最近活動Json格式解析錯誤或沒有資料", e);
            }
        }
    }

    private List<ActivityInfo> decodeJson(String url) throws IOException, JSONException {
        Map<String, String> imgSuperLink = new HashMap<String, String>();
        List<ActivityInfo> activityList = new ArrayList<ActivityInfo>();

        /*20200527 For the use of https protocol*/
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonReceive(url));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        /******20191218 json 標籤名稱變更 時間格式有做更改
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         ******/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            /*******20191218 json 標籤名稱變更
            //String imgUrl = json.getString("ImgUrl");
            //String activityURL = json.getString("ActivityURL");
            //String startTime = json.getString("ActivityStartTime");
//            startTime = startTime.replace('-','/');
            //String endTime = json.getString("ActivityEndTime");
//            endTime = endTime.replace('-','/');
             ******/
            String imgUrl = json.getString("image_url");
            String activityURL = json.getString("activity_url");
            String startTime = json.getString("start_time");
            String endTime = json.getString("end_time");
            //startTime = startTime.replace('-','/');
            //endTime = endTime.replace('-','/');
            ActivityInfo activityInfo = null;
            try{
                activityInfo = new ActivityInfo(sdf.parse(startTime),
                        sdf.parse(endTime), imgUrl, activityURL);
            }catch (Exception e){
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "date time parse error");
                }
                e.printStackTrace();
            }
            activityList.add(activityInfo);
            imgSuperLink.put(imgUrl, activityURL);
        }

        return activityList;
    }
}
