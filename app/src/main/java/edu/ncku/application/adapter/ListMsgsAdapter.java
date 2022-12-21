package edu.ncku.application.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.MsgRemoveTask;
import edu.ncku.application.model.Message;

/**
 * 推播訊息Adapter
 * 被Deprecated掉的建構子或方法，是原本用來做部分顯示功能，後來被取消掉，請無視。
 */
public class ListMsgsAdapter extends BaseAdapter implements IOConstatnt{

    private static final String DEBUG_TAG = ListMsgsAdapter.class.getName();

    private Activity activity;

    private LinkedList<Message> allMsgs = new LinkedList<Message>(), showMsgs = new LinkedList<Message>();

    private int show;

    @Deprecated
    public ListMsgsAdapter(Activity activity, LinkedList<Message> readMessages, int localShow) {
        this.activity = activity;
        this.show = (localShow > readMessages.size()) ? readMessages.size() : localShow;

        allMsgs.addAll(readMessages);

        for (int i = 0; i < show; i++) {
            showMsgs.add(allMsgs.get(i));
        }
    }

    public ListMsgsAdapter(Activity activity, LinkedList<Message> readMessages) {
        this.activity = activity;

        allMsgs.addAll(readMessages);
        showMsgs.addAll(readMessages);
    }

    /**
     * Update news list which shows on the screen
     *
     * @param moreShow the number of the news which want to show more
     * @return the number of the news which show more
     */
    @Deprecated
    public int showMoreOldMessage(int moreShow) {
        try {
            int original = this.getCount();
            if (original == allMsgs.size())
                return 0;// 當沒有舊的訊息時不再更新

            if (original + moreShow >= allMsgs.size()) {
                if(showLogMsg){
                    Log.v(DEBUG_TAG, "全部資料顯示出來");
                }
                for (int i = original; i < allMsgs.size(); i++) {
                    showMsgs.addLast(allMsgs.get(i));
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
                showMsgs.addLast(allMsgs.get(i));
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

    /**
     * 刪除選擇到的推播訊息
     *
     * @param checkedItemPositions
     */
    public void deleteSelect(SparseBooleanArray checkedItemPositions) {
        try {
            LinkedList<Integer> removeList = new LinkedList<Integer>();

            for (int i = 0; i < checkedItemPositions.size(); i++) {
                if (checkedItemPositions.get(checkedItemPositions.keyAt(i))) {
                    int key = checkedItemPositions.keyAt(i);
                    if(showLogMsg){
                        Log.d(DEBUG_TAG, "remove " + key);
                    }
                    removeList.add(key);
                }
            }

            if(showLogMsg){
                Log.d(DEBUG_TAG, "start MsgRemoveTask");
            }

            MsgRemoveTask msgsRemoveTask = new MsgRemoveTask(activity);
            msgsRemoveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, removeList);
            msgsRemoveTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return showMsgs.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return showMsgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater) activity.getApplicationContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            convertView = mInflater
                    .inflate(R.layout.fragment_msgs_list_item, null);

            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView
                    .findViewById(R.id.txtTitle);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            holder.itemStateIcon = (ImageView) convertView.findViewById(R.id.itemStateIcon);

            convertView.setTag(holder);

            Message items = (Message) getItem(position);
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            int timeStamp = items.getPubTime();
            String title = items.getTitle(), date = sdFormat.format(new Date((long) timeStamp * 1000));

            /* 將推播訊息的標題、日期填入 */
            holder.txtTitle.setText((title != null) ? title : "");
            holder.txtDate.setText((date != null) ? date : "");
            if (!items.getIsRead()) {
                holder.txtTitle.setTypeface(null, Typeface.BOLD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {
        TextView txtTitle;
        TextView txtDate;
        ImageView itemStateIcon;
    }
}
