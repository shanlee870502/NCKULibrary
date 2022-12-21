package edu.ncku.application.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import edu.ncku.application.LoginDialog;
import edu.ncku.application.MainActivity;
import edu.ncku.application.R;
import edu.ncku.application.fragments.HomePageFragment;
import edu.ncku.application.fragments.MessagerFragment;
import edu.ncku.application.adapter.DrawerListAdapter;

/**
 * Created by NCKU on 2016/1/12.
 * 為了減少DrawerList在狀態間的轉換所造成的hard code，
 * 故將DrawerList的狀態轉換透過此類別來進行。
 * 此外，透過抽象化類別DrawerListItem，同一點擊事件將
 * 具有同一實體，提高可維護性與效能。
 */
public class DrawerListSelector implements ListView.OnItemClickListener{

    private static final String DEBUG_FLAG = DrawerListSelector.class.getName();

    private final DrawerListAdapter logoutDrawerListAdapter;
    private final ArrayList<DrawerListItem> loginDrawerListItems;

    private MainActivity activity;
    private DrawerLayout mDrawerLayout;
    //20200116 added : To close the whole relative layout
    private RelativeLayout mDrawerRelative;
    private ListView mDrawerList;
    private FragmentManager fragmentManager;
    //public DrawerListSelector(final MainActivity activity, final DrawerLayout mDrawerLayout, final ListView mDrawerList) {
    //20200116 modified : To add relativelayout as input
    public DrawerListSelector(final MainActivity activity, final RelativeLayout mDrawerRelative, final DrawerLayout mDrawerLayout, final ListView mDrawerList) {
        this.activity = activity;
        this.mDrawerLayout = mDrawerLayout;
        this.mDrawerRelative = mDrawerRelative;
        this.mDrawerList = mDrawerList;
        this.fragmentManager = activity.getFragmentManager();
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerList.setOnItemClickListener(this);

        /* 實作點擊事件物件 */
        DrawerListItem homePageAdapterItem = new DrawerListItem(activity.getResources().getString(R.string.home_page)) {
            private final HomePageFragment mHomePageFragment = HomePageFragment.newInstance();

            @Override
            public void onDrawerItemClick() {
                activity.setTitle(R.string.home_page);
                clearBackStackFragment();
                replaceFragment(mHomePageFragment);
            }
        };
        // 設定home page 的 icon
        homePageAdapterItem.setItemImage(R.drawable.ic_home_black_24dp);

        DrawerListItem loginAdapterItem = new DrawerListItem(activity.getResources().getString(R.string.login)) {
            @Override
            public void onDrawerItemClick() {
                //20200429 Add third parameter "MainActivity". See LoginDialog.java
                new LoginDialog(DrawerListSelector.this, activity, activity).show(activity.getFragmentManager(), "Dialog");
                //20200116 modified : 原本是關閉ListView，但更改布局之後，要關閉整個RelativeLayout
                mDrawerLayout.closeDrawer(mDrawerRelative);
                /*Old one(20200116 deleted)*/
                //mDrawerLayout.closeDrawer(mDrawerList);
                UniversalAbility.HideKeyboard(activity);
            }
        };
        loginAdapterItem.setItemImage(R.drawable.ic_announcement_black_24dp);

        DrawerListItem logoutAdapterItem = new DrawerListItem("登出") {
            @Override
            public void onDrawerItemClick() {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                sharedPreferences.edit().remove(PreferenceKeys.ACCOUNT).apply();
                //20200116 modified : 原本是關閉ListView，但更改布局之後，要關閉整個RelativeLayout
                mDrawerLayout.closeDrawer(mDrawerRelative);
                /*Old one(20200116 deleted)*/
                //mDrawerLayout.closeDrawer(mDrawerList);
                logoutState();
                UniversalAbility.HideKeyboard(activity);
            }
        };
        DrawerListItem messengerAdapterItem = new DrawerListItem(activity.getResources().getString(R.string.messenger)) {
            @Override
            public void onDrawerItemClick() {
                clearBackStackFragment();
                activity.setTitle(R.string.messenger);
                replaceFragment(MessagerFragment.getInstance(-1));
                UniversalAbility.HideKeyboard(activity);
            }
        };
        // 設定messenger page 的 icon
        messengerAdapterItem.setItemImage(R.drawable.ic_announcement_black_24dp);
        /* 將點擊事件物件加進各狀態列表之中 */
        loginDrawerListItems = new ArrayList<DrawerListItem>(); // 登入狀態列表
        loginDrawerListItems.add(homePageAdapterItem);
        loginDrawerListItems.add(messengerAdapterItem);
        //20200903 Commented
        //loginDrawerListItems.add(logoutAdapterItem);   // 如果不要登出功能請刪除或註解這行

        ArrayList<DrawerListItem> logoutDrawerListItems = new ArrayList<DrawerListItem>(); // 登出狀態列表
        logoutDrawerListItems.add(homePageAdapterItem);
        logoutDrawerListItems.add(loginAdapterItem);

        /* 將狀態列表放進各Adapter之中，之後將透過Adapters來進行列表狀態轉換 */
        logoutDrawerListAdapter = new DrawerListAdapter(activity, logoutDrawerListItems, false);

        /* 初始化完成後，自動進入Homepage頁面 */
        homePageAdapterItem.onDrawerItemClick();
    }

    public void fragmentToMessager(final int position){
        new DrawerListItem(activity.getResources().getString(R.string.messenger)) {
            @Override
            public void onDrawerItemClick() {
                replaceFragment(MessagerFragment.getInstance(position));
            }
        }.onDrawerItemClick();
    }

    /**
     * 轉換成登入狀態列表
     */
    public void loginState(String name) {

        ArrayList<DrawerListItem> copy = (ArrayList<DrawerListItem>) loginDrawerListItems.clone();
        copy.add(0, new DrawerListItem(activity.getString(R.string.login_title) + "\n" + name){

            @Override
            public void onDrawerItemClick() {

            }
        });
        mDrawerList.setAdapter(new DrawerListAdapter(activity, copy, true));
    }

    /**
     * 轉換成登出狀態列表
     */
    public void logoutState() {
        mDrawerList.setAdapter(logoutDrawerListAdapter);
    }

    /**
     * 進行頁面取代轉換
     * @param fragment 要轉換的頁面
     */
    private void replaceFragment(Fragment fragment){
        if (fragment != null && !fragment.isAdded()) {
            clearBackStackFragment();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

        }
    }

    /**
     * 清空頁面堆疊與標題堆疊
     */
    private void clearBackStackFragment() {
        // clear the back stack for fragmentManager
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }
        activity.clearTitleStack();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /* 透過當前的Adapter取得對應的點擊事件物件來呼叫實作的點擊事件 */
        ((DrawerListItem)mDrawerList.getAdapter().getItem(position)).onDrawerItemClick();
        mDrawerList.setItemChecked(position, true); // 將點擊目標設為該項物件
        //20200116 modified : 原本是關閉ListView，但更改布局之後，要關閉整個RelativeLayout
        mDrawerLayout.closeDrawer(mDrawerRelative);
        /*Old one(20200116 deleted)*/
        //mDrawerLayout.closeDrawer(mDrawerList); // 關閉DrawerList
        ((DrawerListAdapter)mDrawerList.getAdapter()).setCurrentPosition(position); //變更現在的位置
        ((DrawerListAdapter)mDrawerList.getAdapter()).notifyDataSetChanged(); //讓list adapter 注意到點擊位置變更
    }
}
