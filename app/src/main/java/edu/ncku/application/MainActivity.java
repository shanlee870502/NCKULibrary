package edu.ncku.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.LinkedList;
import java.util.concurrent.Executors;

import edu.ncku.application.fragments.IRISBNSearchFragment;
import edu.ncku.application.fragments.MessagerFragment;
import edu.ncku.application.fragments.NewsFragment;
import edu.ncku.application.fragments.PrefFragment;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.service.NetworkListenerService;
import edu.ncku.application.util.CollapseHandler;
import edu.ncku.application.util.DrawerListSelector;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ISBNMacher;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.Preference;
import edu.ncku.application.util.PreferenceKeys;
import edu.ncku.application.util.UniversalAbility;

import edu.ncku.application.io.network.MsgReceiveTask;

public class MainActivity extends AppCompatActivity implements ITitleChangeListener, IOConstatnt {

    private static final String DEBUG_FLAG = MainActivity.class.getName();

    private LinkedList<String> titleStack = new LinkedList<String>(); // 標題堆疊

    private CharSequence mTitle; // 當前App的標題

    private Fragment mSettingFragment = new PrefFragment(); // 設定頁面實體
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 20210324 Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new CollapseHandler(this)); // 註冊程式崩潰事件監聽

        mTitle = getTitle();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);
        Preference.DetectLoggin(this);

        // initialize User Interface
        initUI();

        /* 檢查NetworkListenerService是否開啟，如果否，則啟動該Service */
        if (!checkServicesState(NetworkListenerService.class)) {
            if (startService(new Intent(this, NetworkListenerService.class)) != null) {
                if(showLogMsg) {
                    Log.d(DEBUG_FLAG, "NetworkListenerService start!");
                }
            } else {
                if(showLogMsg){
                    Log.e(DEBUG_FLAG, "NetworkListenerService start fail!");
                }

            }
        }
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "中文環境 : " + EnvChecker.isLunarSetting());
        }

    } //The end of onCreate

    /**
     * 覆寫標題變更事件，MainActivity實作ITitleChangeListener，並由其他Fragment驅動此方法
     *
     * @param title 要變更的標題
     */
    @Override
    public void setTitle(CharSequence title) {
        //當啟動 setTitle 時會放進TitleStack裡面
        titleStack.push(mTitle.toString());
        mTitle = title;
        if (title == null) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "title null");
            }
            return;
        }
        if (getSupportActionBar() == null) {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "getSupportActionBar null");
            }
            return;
        }
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "Add TitleStack : " + title);
        }
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        int countStackSize = getFragmentManager().getBackStackEntryCount(); // Fragment堆疊的數量，由於有些Fragment可能沒有變更標題故需比較
        int titleStackSize = titleStack.size(); // 標題堆疊的數量

        if(showLogMsg){
            Log.d(DEBUG_FLAG, countStackSize + " / " + titleStackSize);
        }

        /* 當標題堆疊裡沒有標題卻按下返回鍵將會詢問使用者是否結束此App */
        if (countStackSize == 0 && titleStackSize == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_close_confirm)
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // nothing will happen
                        }
                    })
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /* shut down this App */
                            android.os.Process.killProcess(android.os.Process.myPid());
                            //kill process
                            onDestroy();
                        }
                    }).create().show();
        } else if (countStackSize > titleStackSize) {  // 當Fragment堆疊數量大於標題堆疊，表示有Fragment沒有變更標題，App標題不予變更
            getFragmentManager().popBackStack();
        } else if (countStackSize == titleStackSize) { // 當兩者數量相同，表示需要pop標題堆疊的標題來變更App標題
            mTitle = titleStack.pop();
            //觸發setTitle function
            //Back 鍵的set
            getSupportActionBar().setTitle(mTitle);// the reason why the code is written like this is to avoid to push title into titleStack
            getFragmentManager().popBackStack();
            //20200820修改變更圖示的時機，只有在回到主頁面的時候會執行，避免返回推播訊息時因找步道toolbar而導致Attempt to invoke interface method on a null
            if(getSupportActionBar().getTitle().equals("")){
                //20200325在操作退回鍵時也將右上角設定的圖示做更改
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                MenuItem menuItem = toolbar.getMenu().findItem(R.id.settingMenuItem);
                menuItem.setIcon(R.drawable.ic_settings);
            }

        } else { // 不應該發生的異常狀況
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "TitleStack Error");
            }
        }
    }

    @Override
    public void onChangeTitle(String title) {
        setTitle(title);
    }
    //20200309 Implement deleteTitle() of interface ITitleChangeListener
    @Override
    public void deleteTitle() {
        mTitle = titleStack.pop();
        //觸發setTitle function
        //Back 鍵的set
        getSupportActionBar().setTitle(mTitle);// the reason why the code is written like this is to avoid to push title into titleStack
        getFragmentManager().popBackStack();
    }
    //可能沒有用到
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    //可能沒有用到
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * 此方法根據https://github.com/dm77/barcodescanner參考實作Google的QR Code專案ZXing，處理QR Code或Barcode的資訊
     * 將會過濾不符合ISBN10或ISBN13的格式
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    //用來確認ISBN格式的 onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // Parsing bar code reader result
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                    String scanContent = result.getContents();
                    if (ISBNMacher.validateIsbn10(scanContent) || ISBNMacher.validateIsbn13(scanContent)) {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().addToBackStack(null)
                                .add(R.id.content_frame, IRISBNSearchFragment.newInstance(scanContent)).commit();
                        onChangeTitle(getResources().getString(R.string.homepage_ic_search));
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.not_match_isbn, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    /**
     * 清空標題堆疊
     */
    //可能沒有用到
    public void clearTitleStack() {
        this.titleStack.clear();
    }

    /**
     * 初始化MainActivity的UI元件
     */

    private void initUI() {

        final String setting = getResources().getString(R.string.setting);

        /* ToolBar configuration */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.settingMenuItem:
                        if (!mSettingFragment.isAdded()) {
                            //20200325點擊右上角設定時，更改圖示為叉叉
                            menuItem.setIcon(R.drawable.ic_cross);
                            getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.content_frame, mSettingFragment).commit();
                            setTitle(setting);
                            UniversalAbility.HideKeyboard(MainActivity.this);
                        } else {
                            //20200325點擊右上角叉叉時，更改圖示為設定
                            menuItem.setIcon(R.drawable.ic_settings);
                            onBackPressed();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        };
        Drawable logo = ContextCompat.getDrawable(this, R.drawable.ic_notification);
        toolbar.setLogo(logo);
        /* 以下這段用來設定Logo在Toolbar裡的邊界大小 */
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i); // Toolbar的子元件
            if (child != null)
                if (child.getClass() == ImageView.class) { // 確定元件為ImageView
                    ImageView logoImageView = (ImageView) child; // 取得ImageView實體
                    if (logoImageView.getDrawable() == logo) { // 找到Logo實體
                        logoImageView.setAdjustViewBounds(true);
                        logoImageView.setPadding(0, 0, 30, 0); // 設定其邊界值
                    }
                }
        }
        setSupportActionBar(toolbar); // 將設定好的Toolbar實體實裝
        toolbar.setOnMenuItemClickListener(onMenuItemClick); // 設定MenuItemClickListener

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //20200116 added : 因為在activity_main.xml裡面更改了原有的布局(從僅有ListView改成將其包在RelativeLayout之內)，
        //所以在此處新增了RelativeLayout的物件，用於創建DrawerListSelector(Line 294)
        RelativeLayout mDrawerRelative = (RelativeLayout) findViewById(R.id.drawer_frame);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //? 推播相關控制
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);    // 如果要有推播功能請刪除這行
        getSupportActionBar().setHomeButtonEnabled(true);                              // 如果要有推播功能請修改成true
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);                         // 如果要有推播功能請修改成true
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                toolbar,        // make menu button work
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                UniversalAbility.HideKeyboard(MainActivity.this);
                invalidateOptionsMenu();
            }
        };

//        mDrawerToggle.setDrawerIndicatorEnabled(false);     // 如果要有推播功能請刪除這行
//        mDrawerToggle.syncState();                          // 如果要有推播功能請刪除這行

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        /* 此類別用於降低drawerItem的耦合性 */
        //20200116 modified (see Line 258) 和DrawerListSelector.java檔案有關
        DrawerListSelector selector = new DrawerListSelector(this, mDrawerRelative, mDrawerLayout, mDrawerList);
        /*Old one(20200116 deleted)*/
        //DrawerListSelector selector = new DrawerListSelector(this, mDrawerLayout, mDrawerList);
        //check state is loggin
        if (Preference.isLoggin(getApplicationContext())) {
            selector.loginState(Preference.getName(getApplicationContext()));
        } else {
            selector.logoutState();
            //TODO : 退出時推播資料應該刷新 回到登入畫面
        }

        /* Handle notification messages in a backgrounded app */
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            if(bundle.containsKey("user") && bundle.containsKey("msgNo") && bundle.containsKey("time")){
                String username = bundle.get("user").toString();
                String msgNo = bundle.get("msgNo").toString();
                String time =bundle.get("time").toString();

                Executors.newSingleThreadExecutor().submit(new MsgReceiveTask(this.getApplicationContext(), username, msgNo, Integer.valueOf(time)));

                if (bundle.containsKey("click_action")){
                    String click_action = bundle.get("click_action").toString();
                    if(click_action.equals("personal")) {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().addToBackStack(null)
                                .add(R.id.content_frame, MessagerFragment.getInstance(-1)).commit();
                        onChangeTitle(getResources().getString(R.string.messenger));
                    }
                }
            }
        }


        /* 透過Notification取得是否要進入特定頁面(推播或最新消息) */
        //TODO: 從這邊進入 要把推播訊息改成(已閱讀)
        int msgExtra = getIntent().getIntExtra(PreferenceKeys.MSGS_EXTRA, -1); // 是否是特定推播訊息
        boolean globalExtra = getIntent().getBooleanExtra(PreferenceKeys.GLOBAL_NEWS, false); // 是否有緊急通知
        if (msgExtra != -1) {
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "特定推播訊息 : " + msgExtra);
            }
            selector.fragmentToMessager(msgExtra);
        } else if (globalExtra) {
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "緊急通知");
            }
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().addToBackStack(null)
                    .add(R.id.content_frame, NewsFragment.getInstance(-1)).commit();
            onChangeTitle(getResources().getString(R.string.homepage_ic_news));
        }

    }// The end of initUI

    /**
     * 確認Service是否存活
     *
     * @param serviceClass
     * @return the service is alive or not
     */
    private boolean checkServicesState(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
