package edu.ncku.application.io.network;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import edu.ncku.application.io.IOConstatnt;

public abstract class HttpsJsonReceiveTask implements Runnable, IOConstatnt {

    private static final String DEBUG_FLAG = JsonReceiveTask.class.getName();
    protected Context mContext;

    public HttpsJsonReceiveTask(Context mContext) {
        this.mContext = mContext;
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustLibHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            //return true;
            // 20210128 Fix unsafe implementation of the HostnameVerifier interface which trust every host name
            HostnameVerifier hv =
                    HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify("lib.ncku.edu.tw", session);
        }
    }

    protected final String jsonReceive (final String jsonURL) throws KeyManagementException, NoSuchAlgorithmException {

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
        /*test new X509
        try {
            sc.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        HttpsURLConnection urlConnection = null;

        try {
            URL url = new URL(jsonURL);

/*test new X509
            SSLContext sslcontext = SSLContext.getInstance("SSL","IBMJCE");
            sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
            urlConnection.setHostnameVerifier(new TrustAnyHostnameVerifier());
            urlConnection.connect();*/

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setHostnameVerifier(new TrustLibHostnameVerifier());
            urlConnection.connect();

            InputStream is = urlConnection.getInputStream();
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

        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "網頁連線逾時");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return null;
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