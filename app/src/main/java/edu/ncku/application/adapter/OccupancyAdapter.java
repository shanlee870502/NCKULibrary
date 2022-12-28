package edu.ncku.application.adapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import edu.ncku.application.util.EnvChecker;

import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;

import edu.ncku.application.R;
import edu.ncku.application.fragments.LibMapFragment;
import edu.ncku.application.model.Occupancy;

public class OccupancyAdapter extends ArrayAdapter<Occupancy> implements IOConstatnt {

    public ArrayList<Occupancy> occupancyArrayList;
    public OccupancyAdapter(Context context, ArrayList<Occupancy> occupancy_lst) {
        super(context, R.layout.fragment_occupancy_card);
        this.occupancyArrayList = occupancy_lst;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OccupancyViewHolder holder;
        Log.d("occupancy_arra_adapter", occupancyArrayList.get(0).getTitle());
        if(position == 0){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_section_notify, parent, false);
            holder = new OccupancyViewHolder(convertView);

            convertView.setTag(holder);

        }else if(position == 3) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_section_24hr, parent, false);
            holder = new OccupancyViewHolder(convertView);

            convertView.setTag(holder);
        }else{
            Occupancy model = getItem(position);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.fragment_occupancy_card, parent, false);
            holder = new OccupancyViewHolder(convertView);

            convertView.setTag(holder);

            String cur_occupancy_txt = getContext().getResources().getString(R.string.current_occupancy) + model.getCurOccupancy();
            String total_occupancy_txt = getContext().getResources().getString(R.string.total_occupancy) + model.getTotalOccupancy();
            String manage_dept_txt = getContext().getResources().getString(R.string.manage_dept) + model.getManageDept();
            String contact_txt = getContext().getResources().getString(R.string.contact_extension) + model.getContact();

            holder.occupancy_chart.setPercent(model.getPercentage());
            holder.percentText.setText(String.valueOf(model.getPercentage()));
            holder.dormTitle.setText(model.getTitle());
            holder.dormSubtitle.setText(cur_occupancy_txt + total_occupancy_txt);
            holder.manageDept.setText(manage_dept_txt);
            holder.contact.setText(contact_txt);


            //mainlib setting
            if (model.getNameID().equals("main_lib")) {
                // 總圖沒有管理單位以及聯絡分機 文字置中
                holder.dormTitle.setPadding(8, 80, 0, 0);
                holder.manageDept.setText("");
                holder.contact.setText("");
            }

            // popUp Menu Setting
            final String rule_url = getRuleURL(model.getTitle());
            final String info_url = getInfoURL(model.getNameID());
            final double lat = model.getLatLng().first;
            final double lng = model.getLatLng().second;

            holder.three_dot_bottom.setOnClickListener(new View.OnClickListener() {
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
                R.array.dorm_name_list);
        for(int i=0; i<occupancy_list.length;i++){
            if(name.equals(occupancy_list[i])){
                switch(i) {
                    case 0: // mainlib
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
        // check facebook app is installed or not
        boolean fb_install = true;
        try {
            getContext().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
        } catch (PackageManager.NameNotFoundException e){
            fb_install = false;
        }

        HashMap<String, String> url_map = new HashMap<>();
        url_map.put("main_lib", null);
        url_map.put("kun_yen", Medlib_INFO_URL_SSL);
        url_map.put("knowLEDGE", null);

        if(!fb_install){ // if not install fb app, open webpage
            url_map.put("d24", D24_INFO_URL_SSL);
            url_map.put("future_venue", Xcollege_INFO_URL_SSL);
        }else{
            if(EnvChecker.isLunarSetting()){
                url_map.put("d24", D24_INFO_URL_SSL_FB);
                url_map.put("future_venue", Xcollege_INFO_URL_SSL_FB);
            }
        }
        return url_map.get(name);
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
    static class OccupancyViewHolder {
        ColorfulRingProgressView occupancy_chart;
        TextView percentText;
        TextView dormTitle;
        TextView dormSubtitle;
        TextView manageDept;
        TextView contact;
        ImageView three_dot_bottom;

        OccupancyViewHolder(View view) {
            occupancy_chart = (ColorfulRingProgressView) view.findViewById(R.id.occupancy_chart);
            percentText = (TextView) view.findViewById(R.id.occupancy_percent);
            dormTitle = (TextView) view.findViewById(R.id.dorm_title);
            dormSubtitle = (TextView) view.findViewById(R.id.dorm_subtitle);
            manageDept = (TextView) view.findViewById(R.id.manage_dept);
            contact = (TextView) view.findViewById(R.id.contact);
            three_dot_bottom = (ImageButton)view.findViewById(R.id.three_dot_button);
        }
    }
}
