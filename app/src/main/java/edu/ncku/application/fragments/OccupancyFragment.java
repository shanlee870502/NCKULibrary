package edu.ncku.application.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.adapter.OccupancyAdapter;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.OccupancyLimitTask;
import edu.ncku.application.io.network.OccupancyReceiveTask;
import edu.ncku.application.model.Occupancy;

/**
 * 本館席位使用頁面，紀錄各館使用席次人次、比例
 */
public class OccupancyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, IOConstatnt {
    private ListView occupancyViewer;
    private String[] occupancy_list; //各館名稱
    private String[] manage_dept_list; //各館管理單位
    private String[] contact_list; //各館聯絡資訊
    //order: library, kun-yen, knowLEDGE, D24, Future Venue
    private String[] num_of_people = { "0", "0", "0", "0", "0"};; // 目前人數
    private String[] total_num_of_people = {"1207", "359", "162", "67", "50"}; //總人數(上線人數)
    private Double[] lat_list = {22.999770,23.0020569, 23.002007,22.994473,22.995375}; //緯度
    private Double[] lng_list = {120.219925,120.2204054,120.222580,120.219900,120.219537};//經度

    private OccupancyAdapter adapter = null;

    public OccupancyFragment(){

    }

    public static Fragment newInstance() {
        OccupancyFragment fragment = new OccupancyFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        refreshOccupancyLimit();
        refreshVisitor(true, true); // 第一次進入時主動更新在館人數(前景 = true, 單次 = true)
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("android.intent.action.OCCUPANCY_RECEIVER");
        getActivity().getApplicationContext().registerReceiver(mOccypancyReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.OCCUPANCY_LIMIT_RECEIVER");
        getActivity().getApplicationContext().registerReceiver(mOccupancyLimitReceiver, filter2);

    }
    private void refreshOccupancyLimit(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(new OccupancyLimitTask(getActivity().getApplicationContext()), 1, TimeUnit.SECONDS);
        executor.shutdown();
    }
    private void refreshVisitor(boolean isBackground, boolean isOnce){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(new OccupancyReceiveTask(getActivity().getApplicationContext(), isBackground, isOnce), 1, TimeUnit.SECONDS);
        executor.shutdown();
//        Log.i("Occupancy","OnCreate function"+num_of_people[0].toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false); // 隱藏設定按鈕
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_occupancy, container,
                false);
        occupancyViewer = rootView.findViewById(R.id.occupancyListView);

        adapter = new OccupancyAdapter(getActivity());

        occupancyViewer.setAdapter(adapter);

        //setting dorm info
        occupancy_list = getResources().getStringArray(
                R.array.occupancy_list);
        //setting manage_dept info
        manage_dept_list = getResources().getStringArray(
                R.array.manage_dept_list);

        contact_list = getResources().getStringArray(
                R.array.contact_list);

        for(int i=0 ; i < occupancy_list.length ; i++){
            if (i == 0){
                adapter.add(null);//加上以實際現場為主section(fragment_occypancy_section_notify)
                adapter.add(new Occupancy((double)Integer.parseInt(num_of_people[i])/Integer.parseInt(total_num_of_people[i]), occupancy_list[i],
                        getResources().getString(R.string.current_occupancy) +num_of_people[i] + getResources().getString(R.string.total_occupancy) + total_num_of_people[i],
                        "","",lat_list[i], lng_list[i]));
            }
            else{
                if(i==2){
                    adapter.add(null); //加上24小時開放場館的section
                }
                adapter.add(new Occupancy((double)Integer.parseInt(num_of_people[i])/Integer.parseInt(total_num_of_people[i]), occupancy_list[i],
                        getResources().getString(R.string.current_occupancy) +num_of_people[i] + getResources().getString(R.string.total_occupancy) + total_num_of_people[i],
                        getResources().getString(R.string.manage_dept) + manage_dept_list[i],getResources().getString(R.string.contact_extension)+contact_list[i],lat_list[i], lng_list[i]));
            }
        }

        return rootView;
    }

    private BroadcastReceiver mOccupancyLimitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle !=null){
                ArrayList<String> occ_limit_arr= bundle.getStringArrayList("limit_arr");
                total_num_of_people = occ_limit_arr.toArray(new String[occ_limit_arr.size()]);
            }
        }
    };

    private BroadcastReceiver mOccypancyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String mainlib = "";
            String knowledge = "";
            String medlib = "";
            String d24 = "";
            String xcollege = "";

            if(bundle != null){
                mainlib = bundle.getString("mainlib",null);
                knowledge = bundle.getString("knowledge",null);
                medlib = bundle.getString("medlib",null);
                d24 = bundle.getString("d24",null);
                xcollege = bundle.getString("xcollege",null);

                if(mainlib!=null && !mainlib.isEmpty()){
                    Array.set(num_of_people, 0, mainlib);
                }else{
                    Array.set(num_of_people,0,"0");
                }
                if(medlib!=null && !medlib.isEmpty()){
                    Array.set(num_of_people, 1, medlib);
                }else{
                    Array.set(num_of_people,1,"0");
                }
                if(knowledge!=null && !knowledge.isEmpty()){
                    Array.set(num_of_people, 2, knowledge);
                }else{
                    Array.set(num_of_people,2,"0");
                }
                if(d24!=null && !d24.isEmpty()){
                    Array.set(num_of_people, 3, d24);
                }else{
                    Array.set(num_of_people,3,"0");
                }
                if(xcollege!=null && !xcollege.isEmpty()){
                    Array.set(num_of_people, 4, xcollege);
                }else{
                    Array.set(num_of_people,4,"0");
                }


                adapter.clear();
                for(int i=0 ; i < occupancy_list.length ; i++){
//                    Log.d("percentage"+i, String.valueOf((double)Integer.parseInt(num_of_people[i])/Integer.parseInt(total_num_of_people[i])));
                    if (i == 0){
                        adapter.add(null); //加上以實際現場為主section(fragment_occypancy_section_notify)
                        adapter.add(new Occupancy((double)Integer.parseInt(num_of_people[i])/Integer.parseInt(total_num_of_people[i]), occupancy_list[i],
                                getResources().getString(R.string.current_occupancy) +num_of_people[i] + getResources().getString(R.string.total_occupancy) + total_num_of_people[i],
                                "","",lat_list[i], lng_list[i]));
                    }
                    else{
                        if(i==2){
                            adapter.add(null); //加上24小時開放場館的section (fragment_occupancy_section_24hr)
                        }
                        adapter.add(new Occupancy((double)Integer.parseInt(num_of_people[i])/Integer.parseInt(total_num_of_people[i]), occupancy_list[i],
                                getResources().getString(R.string.current_occupancy) +num_of_people[i] + getResources().getString(R.string.total_occupancy) + total_num_of_people[i],
                                getResources().getString(R.string.manage_dept) + manage_dept_list[i],getResources().getString(R.string.contact_extension)+contact_list[i],lat_list[i], lng_list[i]));
                    }
                }

            }
            else{ // network not connected
                Toast.makeText(context, R.string.network_disconnected, Toast.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            }
        }
    };

    @Override
    public void onRefresh() {

    }
    @Override
    public void onResume() {
        if(!isNetworkConnected()){
            getFragmentManager().popBackStack();
        }
        super.onResume();
    }
    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().unregisterReceiver(mOccypancyReceiver);
        super.onDestroy();
    }

    protected boolean isNetworkConnected() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return (mNetworkInfo == null) ? false : true;

        }catch (NullPointerException e){
            return false;

        }
    }
}
