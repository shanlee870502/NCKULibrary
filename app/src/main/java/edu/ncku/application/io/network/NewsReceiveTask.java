package edu.ncku.application.io.network;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.News;
import edu.ncku.application.util.PreferenceKeys;

/**
 * 此類別用來在背景接收最新消息的JSON資料，一樣將其存進檔案之中
 */
//2020527繼承使用https的JsonReceiveTask
//public class NewsReceiveTask extends JsonReceiveTask implements IOConstatnt {
public class NewsReceiveTask extends HttpsJsonReceiveTask implements IOConstatnt {

    private static final String DEBUG_FLAG = NewsReceiveTask.class.getName();
    private static final String FILE_NAME = NEWS_FILE;
    //20200527更改成https的網址
    //private static final String NEWS_JSON_URL = NEWS_URL;
    private static final String NEWS_JSON_URL = NEWS_URL_SSL;
    private static final Object LOCKER = new Object();

    private static NetworkInfo currentNetworkInfo;

    private boolean isOnce = false; // 判斷是否為使用者刷新最新消息頁面
    private Context mContext;
    private Intent mIntent = new Intent();

    public NewsReceiveTask(Context mContext, boolean isOnce) {
        super(mContext);
        this.mContext = mContext;
        this.isOnce = isOnce;
        this.mIntent.setAction("android.intent.action.MY_RECEIVER");
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (mContext != null) {
            ConnectivityManager connectivityManager = ((ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE));
            currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }

        /* 避免因為網路不通而導致最新消息頁面卡住 */
        Thread protector = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000); // 等待三秒後強制更新頁面
                    mIntent.putExtra("flag", "FinishFlushFlag");
                    mContext.sendBroadcast(mIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        protector.start();

        // 判斷網路是否連線
        if (currentNetworkInfo != null && currentNetworkInfo.isConnected()) {

            receiveNewsFromNetwork("cht");
            receiveNewsFromNetwork("eng");

        } else {
            /* 廣播給NewsFragment告知網路目前無法連線 */
            if (isOnce) {
                mIntent.putExtra("flag", mContext.getString(R.string.messenger_network_disconnected));
                mContext.sendBroadcast(mIntent);
            }
        }
    }

    /**
     * 將最新消息資料覆蓋至檔案
     *
     * @param newsList 來自網路的最新消息資料
     * @return 新增的最新消息數量
     */
    private int rewriteNewsFile(LinkedList<News> newsList, String locale) {

        if(newsList == null || newsList.isEmpty()) return 0; // 當資料異常或為空時，不寫入檔案

		/* Get internal storage directory */
        File dir = mContext.getFilesDir();
        File newsFile = new File(dir, FILE_NAME + "_" + locale);

        ObjectInputStream ois;
        ObjectOutputStream oos;
        LinkedList<News> readNews;
        HashSet<News> newsIntersection;

        int updateNum = 0;

        synchronized (LOCKER) { // 為避免有可能的race condition 以同步化區塊框之
            try {
                // read news data from file
                if (newsFile.exists()) {
                    ois = new ObjectInputStream(new FileInputStream(newsFile));
                    readNews = (LinkedList<News>) ois.readObject();
                    if (ois != null)
                        ois.close();
                } else {
                    readNews = new LinkedList<News>();
                }

                newsIntersection = new HashSet<News>(newsList);
                newsIntersection.retainAll(readNews);

                // overwrite the news data to the file
                oos = new ObjectOutputStream(new FileOutputStream(newsFile));
                oos.writeObject(newsList);
                oos.flush();
                if (oos != null)
                    oos.close();

                updateNum = newsList.size() - newsIntersection.size();

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                if(showLogMsg){
                    Log.e(DEBUG_FLAG, "The read object can't be found.");
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }

        }

        return updateNum;
    }

    /**
     * 從網路接收最新消息
     */
    private void receiveNewsFromNetwork(String locale) {
        LinkedList<News> news;

        int numNews = 0;

        try {
            Resources resources = mContext.getResources();

            /*20200527 For the use of https protocol*/
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonReceive(NEWS_JSON_URL + locale));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            String noDataMsg = jsonObject.getString("noDataMsg");
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "noDataMsg : " + noDataMsg);
            }
            if (!noDataMsg.isEmpty()) {
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PreferenceKeys.NO_DATA_MSGS, noDataMsg).apply();
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "noDataMsg saving...");
                }
            }
            JSONArray arr = jsonObject.getJSONArray("NewsList");

            news = new LinkedList<News>();
            for (int i = 0; i < arr.length(); i++) {

                JSONObject json = arr.getJSONObject(i);
                String title = json.getString("news_title");
                String publish_dept = json.getString("publish_dept");
                String relatedLink = json.getString("related_url");
                String att_file_1 = json.getString("att_file_1");
                String att_file_1_des = json.getString("att_file_1_des");
                String att_file_2 = json.getString("att_file_2");
                String att_file_2_des = json.getString("att_file_2_des");
                String att_file_3 = json.getString("att_file_3");
                String att_file_3_des = json.getString("att_file_3_des");
                String content = json.getString("news_text");
                String contact_unit = json.getString("contact_unit");
                String contact_tel = json.getString("contact_tel");
                String contact_email = json.getString("contact_email");
                 // 20210203
                if (!content.contains("https"))
                    content = content.replace("http","https");

                int publish_time = json.getInt("publish_time");
                int end_time = json.getInt("end_time");

                if (relatedLink != null && relatedLink.length() > 0) {
                    /*20191225 <a href的地方加上"">*/
                    content += String.format("<br><tr><td class=\"newslink\"><img src=\"link.png\" height=\"20\" width=\"20\"><a href=\"%s\" target=\"_blank\" class=\"ui-link\">%s</a></td></tr><br>", relatedLink, resources.getString(R.string.link));
                }

                if (att_file_1 != null && att_file_1.length() > 0) {
                    /*20191225 連上圖書館資料庫，附件網址加上資料夾名稱*/
                    att_file_1 = "https://www.lib.ncku.edu.tw/news/admin_news/" + att_file_1;
                    content += String.format("<br><tr><td class=\"newsfile\"><img src=\"file.png\" height=\"20\" width=\"20\"><a href=\"%s\" target=\"_blank\" class=\"ui-link\">%s</a></td></tr><br>", att_file_1, ((att_file_1_des.length() > 0) ? att_file_1_des
                            : resources.getString(R.string.additional) + "1"));
                }

                if (att_file_2 != null && att_file_2.length() > 0) {
                    /*20191225 連上圖書館資料庫，附件網址加上資料夾名稱*/
                    att_file_2 = "https://www.lib.ncku.edu.tw/news/admin_news/" + att_file_2;
                    content += String.format("<br><tr><td class=\"newsfile\"><img src=\"file.png\" height=\"20\" width=\"20\"><a href=\"%s\" target=\"_blank\" class=\"ui-link\">%s</a></td></tr><br>", att_file_2, ((att_file_2_des.length() > 0) ? att_file_2_des
                            : resources.getString(R.string.additional) + "2"));
                }

                if (att_file_3 != null && att_file_3.length() > 0) {
                    /*20191225 連上圖書館資料庫，附件網址加上資料夾名稱*/
                    att_file_3 = "https://www.lib.ncku.edu.tw/news/admin_news/" + att_file_3;
                    content += String.format("<br><tr><td class=\"newsfile\"><img src=\"file.png\" height=\"20\" width=\"20\"><a href=\"%s\" target=\"_blank\" class=\"ui-link\">%s</a></td></tr><br>", att_file_3, ((att_file_3_des.length() > 0) ? att_file_3_des
                            : resources.getString(R.string.additional) + "3"));
                }

                if (contact_unit != null && contact_unit.length() > 0) {
                    content += "<br>"
                            + contact_unit;
                }

                if (contact_tel != null && contact_tel.length() > 0) {
                    content += "&nbsp;"
                            + contact_tel
                            + "<br>";
                }

                if (contact_email != null && contact_email.length() > 0) {
                    content += "<br>"
                            + contact_email
                            + "<br>";
                }

                news.add(new News(title, publish_dept, publish_time, end_time, content));
            }

            if(showLogMsg){
                Log.d(DEBUG_FLAG, "get " + locale + " news from network : " + news.size());
            }

            numNews = rewriteNewsFile(news, locale);

        } catch (JSONException e) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "最新消息Json格式解析錯誤或沒有資料");
            }
            rewriteNewsFile(new LinkedList<News>(), locale);
        }

		/* 當使用者刷新最新消息頁面時，通知其更新頁面 */
        if (isOnce) {
            mIntent.putExtra("numNews", numNews);
            mIntent.putExtra("flag", "FinishFlushFlag");
            mContext.sendBroadcast(mIntent);
        }
    }
}