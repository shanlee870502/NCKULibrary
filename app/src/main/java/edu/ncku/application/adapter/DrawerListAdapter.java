package edu.ncku.application.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.ncku.application.R;
import edu.ncku.application.util.DrawerListItem;
import edu.ncku.application.util.Preference;

/**
 * Drawer的Adapter
 * 決定Drawer要如何顯示資料項目
 */
public class DrawerListAdapter extends BaseAdapter {

    private static final String DEBUG_FLAG = DrawerListAdapter.class.getName();

    final private Activity activity;
    final private ArrayList<DrawerListItem> drawerListItems;
    final private boolean containName; // 是否包含使用者姓名
    final private int WELCOME_TEXT_SIZE = 24;
    private int currentPosition = 1;

    public DrawerListAdapter(Activity activity, ArrayList<DrawerListItem> drawerListItems, boolean containName) {
        this.activity = activity;
        this.drawerListItems = drawerListItems;
        this.containName = containName;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    @Override
    public int getCount() {
        return drawerListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return drawerListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater) activity.getApplicationContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        try {
            convertView = mInflater
                    .inflate(R.layout.fragment_drawer_list_item, null);

            holder = new ViewHolder();
            holder.drawerString = (TextView) convertView
                    .findViewById(R.id.txtDrawer);
            holder.drawerImg = (ImageView) convertView
                    .findViewById(R.id.imgDrawer);
            /* 當有使用者姓名時，對第一個DrawerList進行變動 */
            if (containName && position == 0) {
                convertView.setBackgroundColor(activity.getResources().getColor(R.color.drawerBackground));
                holder.drawerImg.setVisibility(View.GONE);
                holder.drawerString.setPadding(10, 80, 0, 10);
                holder.drawerString.setTextSize(WELCOME_TEXT_SIZE); // 姓名相對大一點只除以2
                holder.drawerString.setTextColor(Color.WHITE);
                /* 設定長按事件，用來顯示GCM Device ID */
                holder.drawerString.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        /* 跳出一個顯示GCM Device ID的Dialog，並附贈一個copy的按鈕可以複製到剪貼簿 */
                        new AlertDialog.Builder(activity)
                                .setMessage(Preference.getDeviceID(activity))
                                .setTitle(Preference.getUsername(activity) + "'s DeviceID")
                                .setPositiveButton("Copy",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                /* 將Device ID複製到剪貼簿 */
                                                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Copied did", Preference.getDeviceID(activity)));
                                                Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        })
                                .create().show();
                        return true;
                    }
                });
            } else {
                if(currentPosition == position) {
                    convertView.setBackgroundColor(activity.getResources().getColor(R.color.drawerSelectedBackground));
                    holder.drawerString.setTextColor(activity.getResources().getColor(R.color.drawerSelected));
                    holder.drawerImg.setImageResource(((DrawerListItem) getItem(position)).getItemImageID());
                    holder.drawerImg.setColorFilter(activity.getResources().getColor(R.color.drawerSelected));
                } else {
                    convertView.setBackgroundColor(Color.WHITE);
                    holder.drawerString.setTextColor(Color.GRAY);
                    holder.drawerImg.setImageResource(((DrawerListItem) getItem(position)).getItemImageID());
                    holder.drawerImg.setColorFilter(Color.GRAY);
                }
            }
            holder.drawerString.setText(((DrawerListItem) getItem(position)).getItemString()); // 將字給填入

            convertView.setTag(holder);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {
        TextView drawerString;
        ImageView drawerImg;
    }


}
