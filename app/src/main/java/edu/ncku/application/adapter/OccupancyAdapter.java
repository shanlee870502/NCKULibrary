package edu.ncku.application.adapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import edu.ncku.application.util.EnvChecker;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import edu.ncku.application.io.IOConstatnt;

import com.timqi.sectorprogressview.ColorfulRingProgressView;

import edu.ncku.application.R;
import edu.ncku.application.fragments.LibMapFragment;
import edu.ncku.application.model.Occupancy;

public class OccupancyAdapter extends ArrayAdapter<Occupancy> implements IOConstatnt {
    public OccupancyAdapter(Context context) {
        super(context, R.layout.fragment_occupancy_card);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(position == 0){

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_section_notify, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);

        }else if(position == 3) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_section_24hr, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        }else{
            Occupancy model = getItem(position);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_card, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);


            holder.occupancy_chart.setPercent(model.getPercentage());
            holder.percentTextView.setText(String.valueOf(model.getPercentage()));
            holder.dormTitle.setText(model.getTitle());
            holder.dormSubtitle.setText(model.getSubtitle());
            holder.manage_dept.setText(model.getManage_dept());
            holder.contact.setText(model.getContact());

            /* Special Setting for some row */
            String[] occupancy_list = getContext().getResources().getStringArray(
                    R.array.occupancy_list);
            //mainlib setting
            if (model.getTitle().equals(occupancy_list[0])) {
                // 總圖沒有管理單位以及聯絡分機 文字置中
                holder.dormTitle.setPadding(8, 80, 0, 0);
            }


            // popUp Menu Setting
            final String rule_url = getRuleURL(model.getTitle());
            final String info_url = getInfoURL(model.getTitle());
            final double lat = model.getLat();
            final double lng = model.getLng();

            holder.three_dot_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    showPopUpMenu(v, lat, lng, rule_url, info_url,position);
                    v.setEnabled(true);
                }
            });
        }

        return convertView;
    }
    private String getRuleURL(String name) {
        String URL = null;
        String[] occupancy_list = getContext().getResources().getStringArray(
                R.array.occupancy_list);
        for(int i=0; i<occupancy_list.length;i++){
            if(name.equals(occupancy_list[i])){
                switch(i) {
                    case 0: // mainlib
                        URL = LIB_RULE_URL_SSL; break;
                    case 1: //medlib
                        URL = LIB_RULE_URL_SSL; break;
                    case 2: //knowLEDGE
                        URL = KnowLEDGE_RULE_URL_SSL; break;
                    case 3: //D24
                        URL = D24_RULE_URL_SSL; break;
                    case 4: //Xcollege
                        URL = Xcollege_RULE_URL_SSL; break;
                }
            }
        }
        if (EnvChecker.isLunarSetting()) {  // if it's chinese
            URL = URL+"cht";
        }else{
            URL = URL+"en";
        }
        return URL;
    }
    private String getInfoURL(String name) {
        String URL = null;
        String[] occupancy_list = getContext().getResources().getStringArray(
                R.array.occupancy_list);

        // check facebook app is installed or not
        ApplicationInfo ai = null;
        boolean fb_install = true;
        try {
            ai = getContext().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
        } catch (PackageManager.NameNotFoundException e){
            fb_install = false;
        }

        for(int i=0; i<occupancy_list.length;i++){
            if(name.equals(occupancy_list[i])){
                switch(i) {
                    case 0: // mainlib
                        break;
                    case 1: //medlib
                        URL = Medlib_INFO_URL_SSL;
                        break;
                    case 2: //knowLEDGE
                        break;
                    case 3: //D24-> facebook fanpage
                        if(!fb_install){ // if not install fb app, open webpage
                            URL = "https://www.facebook.com/NCKUSDAD/";
                        }else{
                            URL = D24_INFO_URL_SSL;
                            if (EnvChecker.isLunarSetting()) {  // if it's chinese
                                URL = URL+"cht";
                            }else{
                                URL = URL+"en";
                            }
                        } break;
                    case 4: //Xcollege -> facebook fanpage
                        if(!fb_install){ // if not install fb app, open webpage
                            URL = "https://www.facebook.com/NCKU.Future.Venue/";
                        }else{
                            URL = Xcollege_INFO_URL_SSL;
                            if (EnvChecker.isLunarSetting()) {  // if it's chinese
                                URL = URL+"cht";
                            }else{
                                URL = URL+"en";
                            }
                        }
                        break;
                }
            }
        }
        return URL;
    }

    private void showPopUpMenu(final View v, final double lat, final double lng,
                               final String rule_url, final String info_url,int position) {
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.menu_occupancy, menu.getMenu());
        if (position == 1) { // main library
            menu.getMenu().findItem(R.id.show_map).setVisible(false);
            menu.getMenu().findItem(R.id.more_info).setVisible(false);
        }
        if (position == 4) {// knowLEDGE
            menu.getMenu().findItem(R.id.more_info).setVisible(false);
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Fragment fragment = null;
                switch(item.getItemId()){
                    case R.id.manage_rule:
                        Intent intent1 = new Intent();
                        intent1.setAction(Intent.ACTION_VIEW);
                        intent1.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent1.setData(Uri.parse(rule_url));
                        getContext().startActivity(intent1);
                        break;
                    case R.id.show_map:
                        fragment = LibMapFragment.getInstance();
                        Bundle bundle = new Bundle();
                        double lat_lng[]= {lat, lng};
                        bundle.putDoubleArray("lat_lng", lat_lng);
                        fragment.setArguments(bundle);
                        break;
                    case R.id.more_info:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(info_url));
                        getContext().startActivity(intent);
                        break;
                }

                if (fragment != null && !fragment.isAdded()) {
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentManager fragmentManager = activity
                            .getFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                            android.R.animator.fade_in, android.R.animator.fade_out);
                    fragmentManager.beginTransaction().addToBackStack(null)
                            .add(R.id.content_frame, fragment).commit();
                }

                return true;
            }
        });
        menu.show();

    }
    static class ViewHolder {
        ColorfulRingProgressView occupancy_chart;
        TextView percentTextView;
        TextView dormTitle;
        TextView dormSubtitle;
        TextView manage_dept;
        TextView contact;
        ImageView three_dot_button;

        ViewHolder(View view) {
            occupancy_chart = (ColorfulRingProgressView) view.findViewById(R.id.occupancy_chart);
            percentTextView = (TextView) view.findViewById(R.id.occupancy_percent);
            dormTitle = (TextView) view.findViewById(R.id.dorm_title);
            dormSubtitle = (TextView) view.findViewById(R.id.dorm_subtitle);
            manage_dept = (TextView) view.findViewById(R.id.manage_dept);
            contact = (TextView) view.findViewById(R.id.contact);
            three_dot_button = (ImageButton)view.findViewById(R.id.three_dot_button);
        }
    }
}
