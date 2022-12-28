package edu.ncku.application.io.network;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

public class HttpsClient {
    //20201126 Add context variable to add alert box
    private Context context;
    public HttpsClient(Context context) {
        super();
        this.context = context;
    }
    //20201126 Get SSL Context(Using PinnedSSLContextFactory class)
    private SSLContext getPinnedSSLContext() throws IOException {
        InputStream input = null;
        try {
            //D:\AndroidStudioProjects\Jeff__new_version\MyApplication03_GCM2FCM\app\src\main\assets
            input = this.context.getAssets().open("server.cer");
            return PinnedSSLContextFactory.getSSLContext(input);
        } finally {
            if (null != input) {
                input.close();
            }
        }
    }

    private static class TrustLibHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            // 20210128 Fix unsafe implementation of the HostnameVerifier interface which trust every host name
            HostnameVerifier hv =
                    HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify("lib.ncku.edu.tw", session);
            //return true;
        }
    }

    /**
     * post(https protocol)
     *
     * @param url     請求地址
     * @param content 請求参數，請求参數應該是name1=value1&name2=value2的形式
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    public static String sendPost(String url, String content) throws  IOException {

        URL console = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty("connection", "Keep-Alive");

        conn.setHostnameVerifier(new TrustLibHostnameVerifier());
        conn.connect();
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(content.getBytes("utf-8"));
        out.flush();
        out.close();
        InputStream is = conn.getInputStream();
        if (is != null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            if (outStream != null) {
                return new String(outStream.toByteArray(), "utf-8");
            }
        }
        return null;
    }
    //20201126 Method used to deal with login
    public String sendPost2(String url, String content) throws IOException {
        SSLContext sslContext = null;
        sslContext = getPinnedSSLContext();
        URL console = new URL(url);
        URLConnection connection = console.openConnection();
        if (null != sslContext && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        // 20210128 Remove redundant code, use inner class TrustLibHostnameVerifier
        /*HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hv =
                        HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("lib.ncku.edu.tw", session);
            }
        };*/
        ((HttpsURLConnection) connection).setHostnameVerifier(new TrustLibHostnameVerifier());


        connection.setDoOutput(true);
        connection.addRequestProperty("connection", "Keep-Alive");
        connection.connect();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(content.getBytes("utf-8"));
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        if (is != null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            if (outStream != null) {
                return new String(outStream.toByteArray(), "utf-8");
            }
        }
        return null;
    }

    public static void trimCache(Context context) {
        File dir = context.getCacheDir();
        if(dir!= null && dir.isDirectory()){
            File[] children = dir.listFiles();
            if (children == null) {
                // Either dir does not exist or is not a directory
            } else {
                File temp;
                for (int i = 0; i < children.length; i++) {
                    temp = children[i];
                    if (!temp.toString().contains("uil-images")){
                        temp.delete();
                    }
                }
            }

        }
    }
}
