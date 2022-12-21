package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.adapter.ListNewsAdapter;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.NewsReaderTask;
import edu.ncku.application.model.News;
import edu.ncku.application.service.DataReceiveService;
import edu.ncku.application.util.PreferenceKeys;

/**
 * 最新消息頁面，實作頁面刷新介面(OnRefreshListener)
 */
public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, IOConstatnt {

    public static final String FINISH_FLUSH_FLAG = "FinishFlushFlag";

    private static final String DEBUG_FLAG = NewsFragment.class.getName();
    private static final String POSITION = "POSITION";

    private static int PRELOAD_MSGS_NUM;

    private Handler mHandler = new Handler();

    private Activity activity;

    private ProgressBar progressBar;
    private TextView newsTip;
    private ListView listView;
    private SwipeRefreshLayout swipe;

    private NewsReceiver receiver;
    private ListNewsAdapter listViewAdapter;
    private SharedPreferences sp;
    private String noDataMsg = "None";

    private int numShowedMsgs = 0;

    /**
     * 當position大於等於0時，直接跳轉到該位置的最新消息頁面
     *
     * @param position 最新消息位置
     * @return
     */
    public static NewsFragment getInstance(int position) {
        NewsFragment instance = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        instance.setArguments(bundle);
        return instance;
    }

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu

        this.activity = getActivity();
        this.sp = PreferenceManager.getDefaultSharedPreferences(activity);
        /* 設置預載入的最新消息數量(已取消此功能但保留實作) */
        PRELOAD_MSGS_NUM = Integer.valueOf(sp.getString("PRELOAD_MSGS_MAX",
                "20"));

        if (PRELOAD_MSGS_NUM <= 0) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "PRELOAD_MSGS_NUM is smaller than zero");
            }
        }

        /* 當無任何最新消息時的顯示訊息 */
        noDataMsg = sp.getString(PreferenceKeys.NO_DATA_MSGS, getString(R.string.news_empty));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news,
                container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.newsProgressBar);
        newsTip = (TextView) rootView.findViewById(R.id.newsTip);
        listView = (ListView) rootView.findViewById(R.id.newsListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "position : " + position);
                }
                changeToNewsViewer(position);
            }
        });

        swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swip);
        swipe.setOnRefreshListener(this);
        swipe.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);

        /* 註冊NewsReceiver */
        receiver = new NewsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MY_RECEIVER");
        if (activity != null) activity.registerReceiver(receiver, filter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false); // 隱藏設定按鈕
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "Refresh");
        }
        onceActiveUpdateMessageData(); // 一開始進入最新消息頁面時，主動刷新一次
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (activity != null) activity.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        try {
            if (mHandler != null)
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onceActiveUpdateMessageData();
                    }
                }, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 當使用者點擊時，轉換到顯示最新消息內容的頁面
     *
     * @param position
     */
    private void changeToNewsViewer(int position) {
        if (listViewAdapter == null) return;

        News news = (News) listViewAdapter.getItem(position);

        /* 將News物件裡的資料用Bundle倒給顯示頁面 */
        Bundle bundle = new Bundle();
        bundle.putString("title", news.getTitle());
        bundle.putString("date", new SimpleDateFormat("yyyy/MM/dd").format((long) news.getPubTime() * 1000));
        bundle.putString("unit", news.getUnit());
        bundle.putString("contents", news.getContents().replace("\r\n", "<br>").trim());

        NewsViewerFragment msgViewerFragment = new NewsViewerFragment();
        msgViewerFragment.setArguments(bundle);

        FragmentManager fragmentManager = activity.getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.content_frame, msgViewerFragment).commit();
        }
    }

    /**
     * 當頁面資料更新時，要更新Adapter才會真正改變UI
     *
     * @param adapter
     */
    private void setListAdapter(final ListAdapter adapter) {
        if (mHandler != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (listView != null) listView.setAdapter(adapter);
                }
            });
    }

    /**
     * broadcast to update message data once
     */
    private void onceActiveUpdateMessageData() {
        DataReceiveService.startActionONCE(getActivity().getApplicationContext());

        if (mHandler != null)
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipe.setRefreshing(false);
                }
            }, 800);
    }

    /**
     * 刷新最新消息頁面
     *
     * @return
     * @throws Exception
     */
    private boolean updateList() throws Exception {
        /* Read data in background and reflesh the listview of this activity */
        if (numShowedMsgs < PRELOAD_MSGS_NUM) {
            numShowedMsgs = PRELOAD_MSGS_NUM;
        }

        /* 最新消息資料會先儲存進手機端，然後在這邊用NewsReaderTask讀出來 */
        NewsReaderTask newsReaderTask = new NewsReaderTask(this, numShowedMsgs);
        newsReaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        listViewAdapter = newsReaderTask.get(1, TimeUnit.SECONDS); // 回傳Adapter

        if (listViewAdapter != null) { // 拿讀到Adapter刷新頁面
            setListAdapter(listViewAdapter);
            numShowedMsgs = listViewAdapter.getCount();
            newsTip.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * broadcast receiver
     *
     * @author root
     */
    private class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(progressBar != null && newsTip != null)

                if (updateList()) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    newsTip.setVisibility(View.VISIBLE);
                    newsTip.setText(noDataMsg);
                }

                Bundle bundle = intent.getExtras();
                String flag = bundle.getString("flag");
                if (null != flag) {
                    if (!FINISH_FLUSH_FLAG.equals(flag)) {
                        Toast.makeText(context, flag, Toast.LENGTH_SHORT)
                                .show();
                    }
                    swipe.setRefreshing(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
