package edu.ncku.application.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.ncku.application.R;


/**
 * 此頁面是用來顯示最新消息或最新訊息的內容，以WebView元件顯示
 */
public class NewsViewerFragment extends Fragment {

    private static final String DEBUG_FLAG = NewsViewerFragment.class.getName();
    private RelativeLayout msgViewer;
    private WebView msgContents;
    private TextView msgTitle, msgDate;

    public static NewsViewerFragment newInstance() {
        return new NewsViewerFragment();
    }

    public NewsViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_viewer, container,
                false);

        msgViewer = (RelativeLayout) rootView.findViewById(R.id.msgViewer);
        msgViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 覆蓋掉推播訊息的長按事件，避免進入刪除模式
            }
        });

        String title = "", date = "", contents = "";
        try {
            title = getArguments().getString("title");
            date = getArguments().getString("date");
            contents = getArguments().getString("contents");
        }catch (Exception e){
            e.printStackTrace();
        }

        msgTitle = (TextView) rootView.findViewById(R.id.txtMsgTitle);
        msgTitle.setText((title != null)?title:"title");

        msgDate = (TextView) rootView.findViewById(R.id.txtMsgDate);
        msgDate.setText((date != null)?date:"2038/1/19");

        msgContents = (WebView) rootView.findViewById(R.id.webContesViewer);
        WebSettings settings = msgContents.getSettings();
        //settings.setJavaScriptEnabled(true);  // notice: javascript enable for iframe(XSS attack)
        msgContents.loadDataWithBaseURL("file:///android_asset/",
                (contents != null)?contents:"", "text/html",
                "utf-8", null);
        msgContents.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                msgContents.loadUrl("file:///android_asset/error_path.html");
            }
        });
        return rootView;
    }

}
