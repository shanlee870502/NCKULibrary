package edu.ncku.application.io.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ncku.application.io.IOConstatnt;

public class OccupancyLimitTask implements Runnable,IOConstatnt {
    private static final String OCC_LIMIT_API = OCC_LIMIT_URL_SSL;
    private Context mContext;
    private List<String> list = new ArrayList<String>();
    public OccupancyLimitTask(Context context){
        this.mContext = context;
    }
    @Override
    public void run() {
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo curreuntNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if(curreuntNetworkInfo!=null && curreuntNetworkInfo.isConnected()){
                try{
                    String result = HttpsClient.sendPost(OCC_LIMIT_API,"");
                    JSONArray jsonArr = new JSONArray(result);
                    for(int i=0;i<jsonArr.length();i++) {
                        list.add(jsonArr.getJSONObject(i).getString("num"));
                    }
                }catch (Exception e){
                    Log.i("OCCUPANCY RECEIEVE ERR","wrong");
                }
                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.OCCUPANCY_LIMIT_RECEIVER");
                mIntent.putStringArrayListExtra("limit_arr",new ArrayList<>(list));
                mContext.sendBroadcast(mIntent);
            }
        }catch (Exception e){

        }
    }
}
