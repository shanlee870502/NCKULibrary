package edu.ncku.application.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.ContactInfoReaderTask;
import edu.ncku.application.model.ContactInfo;

/**
 * 將聯絡資訊利用網頁(Webview)的形式顯示，並實現點擊電話的超連結撥打電話(Javascript)
 */
public class LibContactFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = LibContactFragment.class.getName();

    public final static String CSS_STYLE ="<link rel=\"stylesheet\" type=\"text/css\" href=\"https://app.lib.ncku.edu.tw/css/mobile.css\" /><style>* {font-size:20px;line-height:20px;} p {color:#333;} a {text-decoration:none;color:#3E62A6;} img {max-width:310px;}pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;}</style>";
//    private static final String CONTACT_PHONE = "聯絡電話";
    private static final String CONTACT_PHONE_SUPER_LINK = "<a href=\"#\" onClick=\"window.PhoneCall.telext('%s');\">%s</a><br /><br />";
    private static final String CONTACT_EMAIL_SUPER_LINK = "<a href=\"mailto:%s\">%s</a><br /><br /><hr><br />";

    private PhoneCall phoneCall = new PhoneCall();

    private String html = "";

    /**
     * 實現Javascript的類別，使用@JavascriptInterface的這個Annotation，呼叫電話撥打的Intent
     */
    public class PhoneCall{

        //After API 17, you will have to annotate each method with @JavascriptInterface within your class that you'd like to access from Javascript.
        @JavascriptInterface
        public void telext(String telStr) {
            if(showLogMsg){
                Log.d(DEBUG_FLAG, telStr);
            }
            Uri uri = Uri.parse("tel:"+telStr);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            startActivity(intent);
        }

    }

    public static LibContactFragment newInstance() {
        LibContactFragment fragment = new LibContactFragment();
        return fragment;
    }

    public LibContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String CONTACT_PHONE = getString(R.string.contact_phone);

        try {
            /* 從檔案之中讀取聯絡資訊 */
            ContactInfoReaderTask contactInfoReaderTask = new ContactInfoReaderTask(getActivity().getApplicationContext());
            contactInfoReaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            ArrayList<ContactInfo> contactInfos = contactInfoReaderTask.get(3, TimeUnit.SECONDS);
            if(contactInfos == null){
                html = null;
            }else{
                String centralPhone = "";
                for(ContactInfo contactInfo : contactInfos){
                    if(contactInfo.getDivision().equals(CONTACT_PHONE)){
//                        html += CONTACT_IMG;
                        html += CONTACT_PHONE;
                        centralPhone = convert2Telext(contactInfo.getPhone().split("#")[0]); // 將分機號碼進行處理
                        html += String.format(CONTACT_PHONE_SUPER_LINK, convert2Telext(contactInfo.getPhone()), contactInfo.getPhone());
                    }else{
//                        html += DEPT_IMG;
                        html += contactInfo.getDivision() + "<span class=\"hourscolor\">";
                        html += String.format(CONTACT_PHONE_SUPER_LINK, centralPhone + convert2Telext(contactInfo.getPhone()), contactInfo.getPhone());
                        if(contactInfo.getEmail()!=null && !contactInfo.getEmail().isEmpty()){
                            html += String.format(CONTACT_EMAIL_SUPER_LINK, contactInfo.getEmail(), contactInfo.getEmail());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lib_contact, container,
                false);

        WebView webView = (WebView) rootView.findViewById(R.id.lib_contact_webView);
        TextView networkHint = (TextView) rootView.findViewById(R.id.networkHint);

        if(html != null) {

            webView.setVisibility(View.VISIBLE);
            networkHint.setVisibility(View.INVISIBLE);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(phoneCall, "PhoneCall");
            webView.loadDataWithBaseURL("file:///android_asset/", CSS_STYLE + html, "text/html",
                    "utf-8", null); // 網頁圖片資源取得(本地)
        }else{
            webView.setVisibility(View.INVISIBLE);
            networkHint.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    /**
     * 將電話號碼裡有分機的進行轉換以便撥打電話，將#取代為,，以方便Javascript使用
     *
     * @param s
     * @return
     */
    private String convert2Telext(String s){
        return Pattern.compile("[^0-9#]").matcher(s).replaceAll("").replace('#', ',');
    }

}
