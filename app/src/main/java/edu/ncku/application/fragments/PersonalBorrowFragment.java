package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import edu.ncku.application.R;
import edu.ncku.application.io.network.PinnedSSLContextFactory;
import edu.ncku.application.util.CustomWebView;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.SSLChecker;
import kotlin.jvm.JvmOverloads;
/**
 * 顯示個人借閱網頁頁面
 */
public class PersonalBorrowFragment extends Fragment {
    private static final String DEBUG_FLAG = IRSearchFragment.class.getName();

    //private static final String URL = "http://140.116.207.50/patroninfo/login_my_account%s.php";
    private static final String URL = "http://m.lib.ncku.edu.tw/patroninfo/login_my_account%s.php";
    private static final String URL_SSL = "https://m.lib.ncku.edu.tw/patroninfo/login_my_account%s.php";
    private CustomWebView webView;
    //20201123 Add titlechangerListener
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    private Context context;

    private InputStream input;
    private SSLContext sslContext;
    private Boolean isVerified = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IRSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalBorrowFragment newInstance() {
        return new PersonalBorrowFragment();
    }

    public PersonalBorrowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu
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
        View rootView = inflater.inflate(R.layout.fragment_custom_web, container,
                false);

        webView = (CustomWebView) rootView.findViewById(R.id.web_view);
        InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(webView.getWindowToken(), 0);
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

        webView.setWebViewClient(new WebViewClient() {
            //20201123 實作ssl error handler，若發生錯誤則返回首頁
            SSLChecker ssl_checker = new SSLChecker(webView);


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
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
            //實現對網頁中超連結的攔截，true表示不會執行
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse result = ssl_checker.checkSsl(view, URL_SSL, sslContext);
                return result;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

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
        });
        // disable autofill
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        if(isVerified) {
            webView.loadUrl(String.format(URL_SSL, (EnvChecker.isLunarSetting()) ? "" : "_eng"));
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
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
        if(!isVerified){
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
        titleChangeListener.deleteTitle();
    }
    /************************************************/
}
