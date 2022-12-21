package edu.ncku.application.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.FloorInfoReaderTask;

/**
 * 將樓層資訊利用網頁(Webview)的形式顯示
 */
public class LibFloorFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = LibFloorFragment.class.getName();

    private String html = "";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LibFloorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibFloorFragment newInstance() {
        LibFloorFragment fragment = new LibFloorFragment();
        return fragment;
    }

    public LibFloorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            /* 從檔案當中讀取樓層資訊 */
            FloorInfoReaderTask floorInfoReaderTask = new FloorInfoReaderTask(getActivity().getApplicationContext());
            floorInfoReaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Map<String, String> floorInfo = floorInfoReaderTask.get(1, TimeUnit.SECONDS);
            if(floorInfo == null){
                html = null;
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "floorInfoReaderTask return null");
                }
            }else{
                for(String key : floorInfo.keySet()){
                    html += "<span class=\"floortitle\">" + key + "</span><br></br>";
                    html += floorInfo.get(key) + "<br><br>";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lib_floor, container,
                false);

        WebView webView = (WebView) rootView.findViewById(R.id.lib_floor_webView);
        TextView networkHint = (TextView) rootView.findViewById(R.id.networkHint);

        if(html != null) {
            webView.setVisibility(View.VISIBLE);
            networkHint.setVisibility(View.INVISIBLE);

            webView.loadDataWithBaseURL("file:///android_asset/", "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://app.lib.ncku.edu.tw/css/mobile.css\" />" + html, "text/html",
                    "utf-8", null); // 網頁圖片資源取得(本地)
        }else{
            webView.setVisibility(View.INVISIBLE);
            networkHint.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

}
