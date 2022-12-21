package edu.ncku.application.io.network;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.ncku.application.io.IOConstatnt;

/**
 * 此抽象類別是父工具類別，將jsonRecieve與saveFile方法抽出而成
 * 提供多個ReceiveTask類別去繼承並使用這兩個方法，並實作run方法 *
 */
public abstract class JsonReceiveTask implements Runnable, IOConstatnt {

    private static final String DEBUG_FLAG = JsonReceiveTask.class.getName();
    protected Context mContext;

    public JsonReceiveTask(Context mContext) {
        this.mContext = mContext;
    }

    protected final String jsonRecieve(final String jsonURL) {
        HttpURLConnection urlConnection = null;
        StringBuilder responseStrBuilder = null;

        try {
            URL url = new URL(jsonURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader streamReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "網頁連線逾時");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            if (responseStrBuilder == null) responseStrBuilder = new StringBuilder();
        }

        return responseStrBuilder.toString();
    }

    protected void saveFile(final Object data, final String fileName) throws IOException, NullPointerException {
        if (data == null) {
            throw new NullPointerException("argument data is null.");
        }

        /* Get internal storage directory */
        File dir = mContext.getFilesDir();
        File activityFile = new File(dir, fileName);

        ObjectOutputStream oos = null;

        oos = new ObjectOutputStream(new FileOutputStream(activityFile));
        oos.writeObject(data);
        oos.flush();
        oos.close();
    }
}