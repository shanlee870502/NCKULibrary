package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.VisitorRecieveTask;
import edu.ncku.application.util.AutoResizeEditText;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.Preference;
import edu.ncku.application.util.UniversalAbility;

/**
 * 主頁面，提供使用者選擇六個主要功能(最新消息、館藏查詢、個人借閱、本館資訊、最近活動、書籍掃描)
 * 並在最下方顯示在館人數
 */
public class HomePageFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = HomePageFragment.class.getName();

    private Fragment mLibInfoListFragment;
    private Fragment mIRSearchFragment;
    private Fragment mUpcomingEventsFragment;
    private Fragment mNewsFragment;
    private Fragment mPersonalBorrowFragment;
    private Fragment mOccupancyFragment;

    private ImageView mLibInfoImageView;
    private ImageView mNewsImageView;
    private ImageView mIRSearchImageView;
    private ImageView mPersonalBorrowImageView;
    private ImageView mUpcomingEventsView;
    private ImageView mScannerImageView;
    // 20200422 2 ImageView added
    private ImageView mSpacemgrImageView;
    private ImageView mFbFanPageImageView;
    // 20210324 Add "current occupancy" ImageView
    private ImageView mCuroccupancyImageView;
    /*20200806 Auto resized EditText*/
    //private EditText searchBarEditText;
    private AutoResizeEditText searchBarEditText;
    //20200316 改變更新人數的觸及區域
    //private LinearLayout footer;
    private RelativeLayout population_footer;
    //20200406 Replace TextView by TickerView
    //private TextView visitorText_main;
    //private TextView visitorText_k;
    //private TextView visitorText_k2;
    private TickerView visitorText_main;
    private TickerView visitorText_k;
    private TickerView visitorText_k2;
    private Context context;
    private Activity activity;
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    private Toast toast;
    //20200407 Add Progress bar and other interface item
    private ProgressBar progressBar;
    private LinearLayout population_k;
    private LinearLayout population_main;
    private View population_dividedLine;
    // TODO: Rename and change types and number of parameters
    public static HomePageFragment newInstance() {
        return new HomePageFragment();
    }

    public HomePageFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 打開動態工具按鈕，使onCreateOptionsMenu生效
        /* 取的分頁物件實體 */
        mNewsFragment = NewsFragment.getInstance(-1);
        mIRSearchFragment = IRSearchFragment.newInstance();
        mLibInfoListFragment = LibInfoListFragment.newInstance();
        mUpcomingEventsFragment = UpcomingEventsFragment.newInstance();
        mPersonalBorrowFragment = PersonalBorrowFragment.newInstance();
        mOccupancyFragment = OccupancyFragment.newInstance();

        context = this.getActivity().getApplicationContext();
        activity = this.getActivity();

        /* 註冊在館人數Receiver */
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.VISITORS_RECEIVER");
        activity.registerReceiver(mVisitorReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home_page,
                container, false);

        // make a link to library's homepage with the logo
        ImageView mItemStateIcon = (ImageView) rootView.findViewById(R.id.itemStateIcon);
        mItemStateIcon.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                new AlertDialog.Builder(getActivity())
                        .setMessage(getResources().getString(R.string.go_to_lib_web_page))
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getActivity(), getResources().getString(R.string.back_to_app),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                intent.setData(Uri.parse("https://www.lib.ncku.edu.tw/"));
                                startActivity(intent);
                            }
                        })
                        .show();
            }

        });
        mLibInfoImageView = (ImageView) rootView.findViewById(R.id.libInfoImgBtn);
        mLibInfoImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addFragment(mLibInfoListFragment, getResources().getString(R.string.homepage_ic_info));
            }

        });
        mIRSearchImageView = (ImageView) rootView.findViewById(R.id.IRSearchImgBtn);
        mIRSearchImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!checkNetworkToast()) return;

                addFragment(mIRSearchFragment, getResources().getString(R.string.homepage_ic_search));
            }

        });
        mUpcomingEventsView = (ImageView) rootView.findViewById(R.id.upcomingEventImgBtn);
        mUpcomingEventsView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addFragment(mUpcomingEventsFragment, getResources().getString(R.string.homepage_ic_activity));
            }

        });
        mNewsImageView = (ImageView) rootView.findViewById(R.id.newsImgBtn);
        mNewsImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addFragment(mNewsFragment, getResources().getString(R.string.homepage_ic_news));
            }

        });
        mPersonalBorrowImageView = (ImageView) rootView.findViewById(R.id.borrowImgBtn);
        mPersonalBorrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkNetworkToast()) return;

                addFragment(mPersonalBorrowFragment, getResources().getString(R.string.homepage_ic_barrow));
            }
        });
        mScannerImageView = (ImageView) rootView.findViewById(R.id.isbnImgBtn);
        mScannerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkNetworkToast()) return;

                final ProgressFragment progressFragment = ProgressFragment.newInstance();

                final FragmentManager fragmentManager = getActivity()
                        .getFragmentManager();
                fragmentManager.beginTransaction().addToBackStack(null)
                        .add(R.id.content_frame, progressFragment).commit();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fragmentManager.popBackStack();
                        IntentIntegrator integrator = new IntentIntegrator(activity);
                        integrator.setBarcodeImageEnabled(true);
                        integrator.setPrompt(getString(R.string.scan_isbn));
                        integrator.initiateScan();
                    }
                }, 500); // 0.5秒後開啟掃描器
            }
        });
        /*20200806 Auto resized EditText*/
        //searchBarEditText = (EditText) rootView.findViewById(R.id.searchBarEditText);
        searchBarEditText = (AutoResizeEditText) rootView.findViewById(R.id.searchBarEditText);
        searchBarEditText.setEnabled(true);
        searchBarEditText.setFocusableInTouchMode(true);
        searchBarEditText.setFocusable(true);
        searchBarEditText.setEnableSizeCache(false);
        searchBarEditText.setMovementMethod(null);
        searchBarEditText.setMaxHeight(330);

        searchBarEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) { // 監聽EditText的鍵盤事件
                    case EditorInfo.IME_NULL:
                    case EditorInfo.IME_ACTION_SEND:
                    case EditorInfo.IME_ACTION_DONE: // 鍵盤按下送出
                    case EditorInfo.IME_ACTION_SEARCH:
                        String searchingText = v.getText().toString().trim();

                        if(!searchingText.equals("")){
                            if (!checkNetworkToast()) {
                                // 20210128 出現網路問題，將原本的搜尋字串清空
                                v.getHandler().removeCallbacksAndMessages(null);
                                return true;      // 造成裝置反應遲鈍
                            }
                            if(showLogMsg){
                                Log.d("De", "go searching" + searchingText);
                            }
                            addFragment(IRSearchFragment.newInstance(searchingText), getResources().getString(R.string.homepage_ic_search)); // 跳轉到館藏搜尋頁面
                            v.setText(""); // 搜尋列清空
                        }
                        View view = activity.getCurrentFocus();
                        if (view != null) {
                            if(showLogMsg){
                                Log.d("De", "go hide");
                            }
                            UniversalAbility.HideKeyboard(getActivity());
                        }
                        break;
                }
                return true;
            }
        });
        // 20200422 added
        // make a link to library's space management system with the logo
        mSpacemgrImageView = (ImageView) rootView.findViewById(R.id.spacemgrImgBtn);
        mSpacemgrImageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                new AlertDialog.Builder(getActivity())
                        .setMessage(getResources().getString(R.string.go_to_spacemgr_web_page))
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getActivity(), getResources().getString(R.string.back_to_app),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                /*20200804 Change space web*/
                                //intent.setData(Uri.parse("https://spacemgr.lib.ncku.edu.tw/spacemgr/index.php"));
                                if(EnvChecker.isLunarSetting())
                                    intent.setData(Uri.parse("https://app.lib.ncku.edu.tw/redirect.php?dest=space&lan=cht"));
                                else
                                    intent.setData(Uri.parse("https://app.lib.ncku.edu.tw/redirect.php?dest=space&lan=eng"));

                                startActivity(intent);
                            }
                        })
                        .show();
            }

        });
        // 20200422 added
        // make a link to library's FB fanpage with the logo
        mFbFanPageImageView = (ImageView) rootView.findViewById(R.id.fbFanPageImgBtn);
        mFbFanPageImageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                new AlertDialog.Builder(getActivity())
                        .setMessage(getResources().getString(R.string.go_to_fbfanpage))
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getActivity(), getResources().getString(R.string.back_to_app),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                String facebookUrl = getFacebookPageURL(context);
                                if(showLogMsg){
                                    Log.d("fb_url", facebookUrl);
                                }
                                //String facebookUrl = "https://www.facebook.com/NCKULibrary";
                                intent.setData(Uri.parse(facebookUrl));

                                startActivity(intent);
                            }
                        })
                        .show();
            }

        });
        // 20210324 added
        // 20210414 Update current occupancy icon
        // make a link to current occupancy's website with the logo
        mCuroccupancyImageView = (ImageView) rootView.findViewById(R.id.occupancyImgBtn);
        mCuroccupancyImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!checkNetworkToast()) return;
                addFragment(mOccupancyFragment, getResources().getString(R.string.homepage_ic_occupancy));
            }

        });
        // 原本席位使用頁面為webview顯示 (現在已改為app內建使用人數席位)
//        mCuroccupancyImageView.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v){
//                new AlertDialog.Builder(getActivity())
//                        .setMessage(getResources().getString(R.string.go_to_curoccupancy_web_page))
//                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //Toast.makeText(getActivity(), getResources().getString(R.string.back_to_app),Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent();
//                                intent.setAction(Intent.ACTION_VIEW);
//                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
//
//                                if(EnvChecker.isLunarSetting())
//                                    intent.setData(Uri.parse("https://app.lib.ncku.edu.tw/redirect.php?dest=occupancy&lan=cht"));
//                                else
//                                    intent.setData(Uri.parse("https://app.lib.ncku.edu.tw/redirect.php?dest=occupancy&lan=eng"));
//
//                                startActivity(intent);
//                            }
//                        })
//                        .show();
//            }
//
//        });
        /*20200406 Replace old version of population(TextView) by TickerView
        visitorText_main = (TextView) rootView.findViewById(R.id.mainVisitorsText);
        visitorText_main.setText(Preference.getMainVisitor(context));
        visitorText_k = (TextView) rootView.findViewById(R.id.kVisitorsText);
        visitorText_k.setText(Preference.getKVisitor(context));
        visitorText_k2 = (TextView) rootView.findViewById(R.id.k2VisitorsText);
        visitorText_k2.setText(Preference.getKVisitor(context));*/
        visitorText_main = (TickerView) rootView.findViewById(R.id.mainVisitorsTicker);
        visitorText_main.setCharacterLists(TickerUtils.provideNumberList());
        visitorText_main.setText("0");
        visitorText_k = (TickerView) rootView.findViewById(R.id.kVisitorsTicker);
        visitorText_k.setCharacterLists(TickerUtils.provideNumberList());
        visitorText_k.setText("0");
        //                20210903 合併k館兩層樓人數(改為統一為visitors_k)
//        visitorText_k2 = (TickerView) rootView.findViewById(R.id.k2VisitorsTicker);
//        visitorText_k2.setCharacterLists(TickerUtils.provideNumberList());
//        visitorText_k2.setText("0");

        //20200325 更新點擊觸發人數更新的layout
        /*footer = (LinearLayout) rootView.findViewById(R.id.footer);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Handler()).post(new Runnable() {
                    @Override
                    public void run() {
                        visitorText_main.setText(R.string.wait_update); // 監聽總館人數點擊事件
                        visitorText_k.setText(R.string.wait_update); // 監聽總館人數點擊事件
                        visitorText_k2.setText(R.string.wait_update);
                        refreshVisitor(false, true);
                    }
                });
            }
        });*/
        /*20200406 新增捲動動畫更新在館人數功能，將點擊更新移除
        population_footer = (RelativeLayout) rootView.findViewById(R.id.population);
        population_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Handler()).post(new Runnable() {
                    @Override
                    public void run() {
                        visitorText_main.setText(R.string.wait_update); // 監聽總館人數點擊事件
                        visitorText_k.setText(R.string.wait_update); // 監聽總館人數點擊事件
                        visitorText_k2.setText(R.string.wait_update);
                        refreshVisitor(false, true);
                    }
                });
            }
        });*/
        //20200407 Bind ProgressBar & other interface item
        progressBar = (ProgressBar) rootView.findViewById(R.id.visitorProgressBar);
        population_dividedLine = (View) rootView.findViewById(R.id.population_dividedline);
        population_main = (LinearLayout) rootView.findViewById(R.id.population_subLayout2);
        population_k = (LinearLayout) rootView.findViewById(R.id.population_subLayout3);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        refreshVisitor(true, true); // 第一次進入主頁面時主動更新在館人數(前景 = true, 單次 = true)
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        /*  */
        /* 只有在Home頁面且登入時時，才顯示設定按鈕 */
        MenuItem settingItem = menu.findItem(R.id.settingMenuItem);

        /* 只有在Home頁面且登入時時，才顯示設定按鈕 */
        if (settingItem != null) {
            settingItem.setVisible(Preference.isLoggin(context));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        /* App關閉時，註銷掉在館人數Receiver */
        Preference.setMainVisitor(context, "");
        //20210903 合併k館兩層樓人數
        Preference.setKVisitor(context, "");
//        Preference.setK2Visitor(context, "");
        activity.unregisterReceiver(mVisitorReceiver);
        super.onDestroy();
    }

    /**
     * 註冊ITitleChangeListener介面的標題變更方法
     *
     * @param activity
     */
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
    public void onDetach() {
        super.onDetach();
        titleChangeListener = null;
    }

    /**
     * 新增Fragment堆疊，驅動標題變更事件方法
     *
     * @param fragment
     * @param title 標題
     */
    private void addFragment(Fragment fragment, String title){
        if(fragment != null && !fragment.isAdded()) {
            FragmentTransaction fragmentTransaction = getActivity()
                    .getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);

            fragmentTransaction.addToBackStack(null)
                    .add(R.id.content_frame, fragment)
                    .commit();
            if(title != null && !title.isEmpty()) titleChangeListener.onChangeTitle(title);
            UniversalAbility.HideKeyboard(getActivity());
        }
    }

    /**
        * 確認當前網路狀態
        *
        *@return conntected or not
        */
    private boolean checkNetworkToast() {
        if (!EnvChecker.pingGoogleDNS(context)) {
            if (toast == null)
            {
                toast = Toast.makeText(context, R.string.network_disconnected, Toast.LENGTH_LONG);
            }
            toast.show();
            return false;
        }
        return true;
    }

    /**
     * 啟動背景在館人數更新工作
     *
     * @param isBackground 是否為背景
     * @param isOnce 是否為單次執行
     */
    private void refreshVisitor(boolean isBackground, boolean isOnce){
        //20200407 載入人數前的空檔用動畫
        loadVisitor(false);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(new VisitorRecieveTask(context, isBackground, isOnce), 1, TimeUnit.SECONDS);
        executor.shutdown();
    }

    private BroadcastReceiver mVisitorReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context mContext, Intent intent) {

            //20200407 When #visitors is received, set progressbar invisible
            loadVisitor(true);

            Bundle bundle = intent.getExtras();
            String visitors_main = "";
            String visitors_k = "";
//            String visitors_k2 = "";
            if(bundle != null){
                visitors_main = bundle.getString("visitors_main", "");
//                20210903 合併k館兩層樓人數(改為統一為visitors_k)
                visitors_k = bundle.getString("visitors_k", "");
//                visitors_k2 = bundle.getString("visitors_k2", "");

            }
            // 更新 K 館的人數
            if(visitors_k != null && !visitors_k.isEmpty()){
                visitorText_k.setText(visitors_k);
            }else{

                visitorText_k.setText("--");
            }
//                20210903 合併k館兩層樓人數(改為統一為visitors_k)
//            if(visitors_k2 != null && !visitors_k2.isEmpty()){
//                visitorText_k2.setText(visitors_k2);
//            }else{
//                visitorText_k2.setText("--");
//            }

            // 更新總館的人數
            if(visitors_main != null && !visitors_main.isEmpty()){
                visitorText_main.setText(visitors_main);
            }else{
                visitorText_main.setText("--");
                if (toast == null)
                {
                    toast = Toast.makeText(context, R.string.network_disconnected, Toast.LENGTH_LONG);
                }
                toast.show();
            }
        }
    };

    //20200407 Add loading #visitors state
    private void loadVisitor(boolean finished){
        if(progressBar != null && population_dividedLine != null && population_main != null && population_k != null)
        if(finished){
            progressBar.setVisibility(View.INVISIBLE);
            population_dividedLine.setVisibility(View.VISIBLE);
            population_main.setVisibility(View.VISIBLE);
            population_k.setVisibility(View.VISIBLE);
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            population_dividedLine.setVisibility(View.INVISIBLE);
            population_main.setVisibility(View.INVISIBLE);
            population_k.setVisibility(View.INVISIBLE);
        }
    }

    //20200422 Added to get fb url function among different version of FB & whether FB is installed
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();

        try {
            //20200521 check whether fb app is enabled or not
            ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo("com.facebook.katana",0);

            if (ai.enabled){
                return "fb://page/432596400160881";
            }
            else{
                return "https://www.facebook.com/NCKULibrary"; //normal web url
            }

        } catch (PackageManager.NameNotFoundException e) {
            return "https://www.facebook.com/NCKULibrary"; //normal web url
        }
    }
}
