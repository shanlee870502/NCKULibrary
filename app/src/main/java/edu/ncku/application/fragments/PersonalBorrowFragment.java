package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;

import edu.ncku.application.R;
import edu.ncku.application.io.network.PinnedSSLContextFactory;
import edu.ncku.application.util.CustomWebView;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.WebViewChecker;

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
    private WebViewClient webViewClient;
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
        webViewClient = new WebViewChecker(webView, URL_SSL, sslContext);
        webView.setWebViewClient(webViewClient);
        if (((WebViewChecker) webViewClient).deleteFragment == true){
            deleteFragment();
        }

        // disable autofill
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        if(((WebViewChecker) webViewClient).isVerified) {
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
        if(!((WebViewChecker) webViewClient).isVerified){
            deleteFragment();
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
