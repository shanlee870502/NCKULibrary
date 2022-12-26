package edu.ncku.application.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.News;

/**
 * 最新消息Adapter
 * 被Deprecated掉的建構子或方法，是原本用來做部分顯示功能，後來被取消掉，請無視。
 */
public class ListNewsAdapter extends BaseAdapter implements IOConstatnt{

    private static final String DEBUG_TAG = ListNewsAdapter.class.getName();

    private Activity activity;
    private Context context;
    private LinkedList<News> AllNews = new LinkedList<News>(), showNews = new LinkedList<News>();

    private int show;

    public ListNewsAdapter(Activity activity, LinkedList<News> newsSet,
                           int localShow) {
        super();
        this.activity = activity;
        this.context = activity.getApplicationContext();
        //20200603 改成顯示所有的新聞
        //this.show = (localShow > newsSet.size()) ? newsSet.size(): localShow;
        this.show = newsSet.size();

        AllNews.addAll(newsSet);

        for (int i = 0; i < show; i++) {
            showNews.add(AllNews.get(i));
        }
    }

    private class ViewHolder {
        ImageView imgGroupIcon;
        TextView txtTitle;
        TextView txtDate;
    }

    /**
     * Update news list which shows on the screen
     *
     * @param moreShow the number of the news which want to show more
     * @return the number of the news which show more
     * <p/>
     * 已捨棄
     */
    @Deprecated
    public int showMoreOldMessaage(int moreShow) {
        try {
            int original = this.getCount();
            if (original == AllNews.size())
                return 0;// 當沒有舊的訊息時不再更新

            if (original + moreShow >= AllNews.size()) {
                if(showLogMsg){
                    Log.v(DEBUG_TAG, "全部資料顯示出來");
                }
                for (int i = original; i < AllNews.size(); i++) {
                    showNews.addLast(AllNews.get(i));
                }
                this.notifyDataSetChanged();    // 通知更新UI
                if(showLogMsg){
                    Log.v(DEBUG_TAG, "return "
                            + (this.getCount() - original));
                }
                return this.getCount() - original;
            }

            if(showLogMsg){
                Log.v(DEBUG_TAG, "未滿");
            }
            for (int i = original; i < original + moreShow; i++) {
                showNews.addLast(AllNews.get(i));
            }

            this.notifyDataSetChanged();
            if(showLogMsg){
                Log.v(DEBUG_TAG, "return " + moreShow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moreShow;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return showNews.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return showNews.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            convertView = mInflater
                    .inflate(R.layout.fragment_news_list_item, null);

            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView
                    .findViewById(R.id.txtTitle);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            holder.imgGroupIcon = (ImageView) convertView
                    .findViewById(R.id.imgIcon);

            convertView.setTag(holder);

            /* 取得各個最新消息資料 */
            News items = (News) getItem(position);
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
            int timeStamp = items.getPubTime(); // 取得最新消息時間戳記
            /* 日期格式化，取得標題跟單位 */
            String title = items.getTitle(), date = sdFormat.format(new Date((long) timeStamp * 1000)), unit = items.getUnit();


            holder.txtTitle.setText((title != null) ? title : "");
            holder.txtDate.setText((date != null) ? date : "");

            /* 取得單位字串陣列資源檔 */
            String[] unitStrings = convertView.getResources().getStringArray(R.array.unit_array);
            /*20200617 因應圖書館組織改造，新增組別並保留舊制*/


            final int SYSTEM_GROUP = 0,
                    COLLECTION_GROUP = 1,
                    CHIEF_ROOM = 2,
                    EDITORIAL_GROUP = 3,
                    INFORMATION_GROUP = 4,
                    MEDICAL_BRANCH = 5,
                    MULTIMEDIA = 6,
                    PEDROIDICAL_GROUP = 7,
                    READING_ROUP = 8,
                    ADMINISTRATION_DIVISION = 9,
                    COLLECTION_CIRCULATION_GROUP = 10,
                    INFORMATION_TECH_GROUP = 11,
                    KNOWLEDGE_SERVICES_GROUP = 12,
                    LIBRARY_EXTENSION_SERVICES_GROUP = 13;
            int id = R.drawable.ic_administration;

            /* 根據單位設置不同的圖片資源，預設為ic_chief_room */
            if (unit.equals(unitStrings[SYSTEM_GROUP]) || unit.equals(unitStrings[INFORMATION_TECH_GROUP])) {
                id = R.drawable.ic_system;
            } else if (unit.equals(unitStrings[COLLECTION_GROUP])) {
                id = R.drawable.ic_collection;
            } else if (unit.equals(unitStrings[CHIEF_ROOM]) || unit.equals(unitStrings[ADMINISTRATION_DIVISION])) {
                id = R.drawable.ic_administration;
            } else if (unit.equals(unitStrings[EDITORIAL_GROUP])) {
                id = R.drawable.ic_qcquisition;
            } else if (unit.equals(unitStrings[INFORMATION_GROUP])) {
                id = R.drawable.ic_information;
            } else if (unit.equals(unitStrings[MEDICAL_BRANCH])) {
                id = R.drawable.ic_medical;
            } else if (unit.equals(unitStrings[MULTIMEDIA])) {
                id = R.drawable.ic_media;
            } else if (unit.equals(unitStrings[PEDROIDICAL_GROUP])) {
                id = R.drawable.ic_periodicals;
            } else if (unit.equals(unitStrings[READING_ROUP]) || unit.equals((unitStrings[COLLECTION_CIRCULATION_GROUP]))) {
                id = R.drawable.ic_circulation;
            } else if (unit.equals(unitStrings[LIBRARY_EXTENSION_SERVICES_GROUP])){
                id = R.drawable.ic_extension;
            } else if (unit.equals(unitStrings[KNOWLEDGE_SERVICES_GROUP])){
                id = R.drawable.ic_knowledge;
            }

            holder.imgGroupIcon.setImageResource(id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

}
