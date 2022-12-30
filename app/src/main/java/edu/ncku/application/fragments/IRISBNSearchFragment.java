package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.PinnedSSLContextFactory;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.WebViewChecker;

/**
 * 使用ISBN參數來向IR搜尋網頁取得相關資訊
 */
public class IRISBNSearchFragment extends Fragment implements IOConstatnt{
    // TODO: Rename parameter arguments, choose names that match
    private static final String DEBUG_FLAG = IRISBNSearchFragment.class.getName();

    private static final String ISBN = "ISBN";

    //private static final String ISBN_SEARCH_URL = "http://m.lib.ncku.edu.tw/catalogs/ISBNBibSearch.php?lan=%s&ISBN=%s";
    //20201123 Add titlechangerListener
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    // TODO: Rename and change types of parameters
    private String isbn;

    private InputStream input;
    private SSLContext sslContext;
    private WebViewClient webViewClient;

    public static IRISBNSearchFragment newInstance(String isbn) {
        IRISBNSearchFragment fragment = new IRISBNSearchFragment();
        Bundle args = new Bundle();
        args.putString(ISBN, isbn);
        fragment.setArguments(args);
        return fragment;
    }

    public IRISBNSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu
        if (getArguments() != null) {
            isbn = getArguments().getString(ISBN);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false); // 隱藏設定按鈕
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 20201223 Clean the session cookies
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                }
            });
        }
        else cookieManager.removeAllCookie();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_web, container,
                false);

        WebView webView = (WebView) rootView.findViewById(R.id.irWebView);
        //20201123 設置允許混合加載內容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setJavaScriptEnabled(true);

        try {
            input = getActivity().getAssets().open("server.cer");
            sslContext = PinnedSSLContextFactory.getSSLContext(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webViewClient = new WebViewChecker(webView, ISBN_SEARCH_URL_SSL, sslContext);
        webView.setWebViewClient(webViewClient);
        if (((WebViewChecker) webViewClient).deleteFragment == true){
            deleteFragment();
        }

        if(((WebViewChecker) webViewClient).isVerified) {
            webView.loadUrl(String.format(ISBN_SEARCH_URL_SSL, ((EnvChecker.isLunarSetting())?"cht":"eng"), isbn));
        }


        return rootView;
    }
    //20201123 To do delete title while clicking ok button in alert box
    /**********************************/
    /**
     * 註冊ITitleChangeListener介面的標題刪除方法
     *
     * @param activity
     **/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            titleChangeListener = (ITitleChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ITitleChangeListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!((WebViewChecker) webViewClient).isVerified){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteFragment();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        titleChangeListener = null;
    }
    private void deleteFragment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                titleChangeListener.deleteTitle();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /************************************************/
}
