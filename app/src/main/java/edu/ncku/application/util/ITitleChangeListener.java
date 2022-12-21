package edu.ncku.application.util;

import android.view.Menu;

/**
 * Created by NCKU on 2015/11/6.
 * 此介面用來通知MainActivity改變標題
 */
public interface ITitleChangeListener {

    void onChangeTitle(String title);
    //20200309 新增一刪除標題的method，一樣在Mainactinity.java implement
    //供UpcomingEventsFragment.java來做使用
    void deleteTitle();
}
