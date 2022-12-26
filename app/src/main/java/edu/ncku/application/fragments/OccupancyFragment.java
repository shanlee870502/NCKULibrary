package edu.ncku.application.fragments;

import static edu.ncku.application.util.EnvChecker.isNetworkConnected;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ncku.application.R;
import edu.ncku.application.adapter.OccupancyAdapter;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.Occupancy;

public class OccupancyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, IOConstatnt{

    private OccupancyAdapter adapter = null;
    private ListView occupancyViewer;
    private ArrayList<Occupancy> occupancy_lst = new ArrayList<>();
    private String [] dorm_id_lst = new String[]{"main_lib", "kun_yen", "knowLEDGE", "d24","future_venue"};
    private HashMap<String, String[]> all_info = new HashMap<String, String[]>();

    public OccupancyFragment(){

    }

    public static Fragment newInstance() {
        OccupancyFragment fragment = new OccupancyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceSate){
        super.onCreate(savedInstanceSate);

        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("android.intent.action.OCCUPANCY_RECEIVER");
        getActivity().getApplicationContext().registerReceiver(mOccupancyReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.OCCUPANCY_LIMIT_RECEIVER");
        getActivity().getApplicationContext().registerReceiver(mOccupancyLimitReceiver, filter2);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false); // 隱藏設定按鈕
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void load_occupancy_lst(){

        String [] dorm_name_lst = getResources().getStringArray(R.array.dorm_name_list);
        String [] manage_dept_lst = getResources().getStringArray(R.array.manage_dept_list);

        for(int i=0; i< dorm_name_lst.length; i++){
            all_info.put(dorm_id_lst[i], new String[]{dorm_name_lst[i], manage_dept_lst[i]});
        }

        occupancy_lst.add(new Occupancy("main_lib", all_info.get("main_lib"), 0, 1207, "", new Pair<Double, Double>(22.999770, 120.219925)));
        occupancy_lst.add(new Occupancy("kun_yen", all_info.get("kun_yen"), 0, 359, "#75120", new Pair<Double, Double>(23.0020569, 120.2204054)));
        occupancy_lst.add(new Occupancy("knowLEDGE", all_info.get("knowLEDGE"),0, 162,"#34906", new Pair<Double, Double>(23.002007, 120.222580)));
        occupancy_lst.add(new Occupancy("d24", all_info.get("d24"),0, 67, "#50360", new Pair<Double, Double>(22.994473, 120.219900)));
        occupancy_lst.add(new Occupancy("future_venue", all_info.get("future_venue"), 0, 50, "#80926", new Pair<Double, Double>(22.995375, 120.219537)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState){
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_occupancy, container,
                false);
        occupancyViewer = rootView.findViewById(R.id.occupancyListView);


        load_occupancy_lst();

        adapter = new OccupancyAdapter(getActivity());
        for(int i=0 ; i < occupancy_lst.size() ; i++){
            if (i == 0){
                adapter.add(null);//加上以實際現場為主section(fragment_occypancy_section_notify)
                adapter.add(occupancy_lst.get(i));
            }
            else{
                if(i==2){
                    adapter.add(null); //加上24小時開放場館的section
                }
                adapter.add(occupancy_lst.get(i));
            }
        }
        occupancyViewer.setAdapter(adapter);
        return rootView;
    }
    private BroadcastReceiver mOccupancyLimitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle !=null){
                ArrayList<String> occ_limit_arr= bundle.getStringArrayList("limit_arr");
                for(int i=0; i< occ_limit_arr.size();i++) {
                    occupancy_lst.get(i).setTotalOccupancy(Integer.parseInt(occ_limit_arr.get(i)));
                }
            }
            adapter.notifyDataSetChanged();

        }
    };
    private BroadcastReceiver mOccupancyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                for(int i=0; i< occupancy_lst.size(); i++){
                    String name_id = occupancy_lst.get(i).getNameID();
                    occupancy_lst.get(i).setCurOccupancy(Integer.parseInt(bundle.getString(name_id, null)));
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onRefresh() {

    }
    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!isNetworkConnected(getContext())){
                getFragmentManager().popBackStack();
            }
        }
        super.onResume();
    }
    @Override
    public void onDestroy() {
        occupancy_lst.clear();
        getActivity().getApplicationContext().unregisterReceiver(mOccupancyReceiver);
        getActivity().getApplicationContext().unregisterReceiver(mOccupancyLimitReceiver);
        super.onDestroy();
    }
}