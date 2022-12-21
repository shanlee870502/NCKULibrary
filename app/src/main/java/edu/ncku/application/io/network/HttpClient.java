package edu.ncku.application.io.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 工具靜態類別，處理Http Request
 */
public class HttpClient {

    /**
     * 向指定URL發送POST方法的請求
     * @param urlStr 發送請求的URL
     * @param parmas 請求参數，請求参數應該是name1=value1&name2=value2的形式
     * @return 網頁回傳的字串
     */
    public static String sendPost(String urlStr, String parmas) throws Exception {

        String result = "";
        BufferedReader bufferedReader = null;
        DataOutputStream dos = null;

        URL url = new URL(urlStr);

        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.setRequestMethod("POST");

        // Send post request
        urlConnection.setDoOutput(true);
        dos = new DataOutputStream(
                urlConnection.getOutputStream());

        // Set parameters ID and Password with url encode format
        dos.writeBytes(parmas);

        dos.flush();
        dos.close();

        bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        for (; (line = bufferedReader.readLine()) != null; ) {
            result += "\n" + line;
        }

        if (null != bufferedReader) bufferedReader.close();
        if (null != dos) dos.close();

        return result;

    }
}
