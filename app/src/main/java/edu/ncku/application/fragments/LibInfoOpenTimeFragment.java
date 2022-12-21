package edu.ncku.application.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.adapter.OpenTimeExpListAdapter;
import edu.ncku.application.io.file.LibOpenTimeReaderTask;

/**
 * 圖書館開放時間頁面，使用ExpandableListView顯示
 */
public class LibInfoOpenTimeFragment extends Fragment {

    private String DEBUG_FLAG = LibInfoOpenTimeFragment.class.getName();

    ExpandableListView mOpenTimeExpListView;
    OpenTimeExpListAdapter mOpenTimeExpListAdapter;
    Map<String, List<String>> mListData; // 內容
    TextView mNetworkHint;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LibInfoOpenTimeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibInfoOpenTimeFragment newInstance() {
        return new LibInfoOpenTimeFragment();
    }

    public LibInfoOpenTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 準備列表資料
        prepareListData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lib_info_open_time,
                container, false);

        mNetworkHint = (TextView) rootView
                .findViewById(R.id.networkHint);

        mOpenTimeExpListView = (ExpandableListView) rootView
                .findViewById(R.id.openTimeExpListView);

        if(mListData != null) {

            mNetworkHint.setVisibility(View.INVISIBLE);
            mOpenTimeExpListView.setVisibility(View.VISIBLE);

            List<String> titleList = new ArrayList<String>();
            for(String title : mListData.keySet()){
                titleList.add(title);
            }

		    /* listIv-圖示, listDataHeader-標題, listDataChild-內容 */
            mOpenTimeExpListAdapter = new OpenTimeExpListAdapter(this.getActivity()
                    .getApplicationContext(), titleList, mListData);

		    /* 取得螢幕寬度 */
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mOpenTimeExpListView.setIndicatorBounds(width - 100, width);
            } else {
                mOpenTimeExpListView.setIndicatorBoundsRelative(width - 100, width);
            }

            // 將列表資料加入至展開列表單
            mOpenTimeExpListView.setAdapter(mOpenTimeExpListAdapter);
            mOpenTimeExpListView
                    .setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                        @Override
                        public void onGroupCollapse(final int groupPosition) {
                            (new Handler()).post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView title = mOpenTimeExpListAdapter.getGroupHeaderView(groupPosition);
                                    title.setTextColor(android.graphics.Color.rgb(0, 0, 0));
                                }
                            });

                        }
                    });
            mOpenTimeExpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, final View v, final int groupPosition, long id) {
                    (new Handler()).post(new Runnable() {
                        @Override
                        public void run() {
                            TextView title = (TextView) v.findViewById(R.id.txtTitle);
                            title.setTextColor(android.graphics.Color.rgb(247, 80, 0));
                        }
                    });
                    return false;
                }
            });
        }else{
            /* 當沒有讀取到資料時，顯示請保持網路暢通的訊息 */
            mNetworkHint.setVisibility(View.VISIBLE);
            mOpenTimeExpListView.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    /**
     * 準備列表資料，從檔案中讀取
     */
    private void prepareListData() {
        // TODO Auto-generated method stub
        try {
            LibOpenTimeReaderTask openTimeReaderTask = new LibOpenTimeReaderTask(getActivity().getApplicationContext());
            openTimeReaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mListData = openTimeReaderTask.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
