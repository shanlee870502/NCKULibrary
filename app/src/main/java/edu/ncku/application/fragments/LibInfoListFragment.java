package edu.ncku.application.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import edu.ncku.application.R;
import edu.ncku.application.adapter.ListViewInfoAdapter;
import edu.ncku.application.util.EnvChecker;

/**
 * 圖書館相關資訊列表頁面，讓使用者選擇開放時間、樓層簡介、聯絡資訊、地理位置等頁面
 */
public class LibInfoListFragment extends Fragment {

    private String DEBUG_FLAG = LibInfoListFragment.class.getName();

    private ListView listview;
    private Toast toast;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LibInfoListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibInfoListFragment newInstance() {
        return new LibInfoListFragment();
    }

    public LibInfoListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lib_info_list, container,
                false);
        listview = (ListView) rootView.findViewById(R.id.infoListView);

        String[] lib_info_list = getResources().getStringArray(
                R.array.lib_info_list);

        listview.setAdapter(new ListViewInfoAdapter(this
                .getActivity().getApplicationContext(), lib_info_list));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                listview.setEnabled(false);
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = LibInfoOpenTimeFragment.newInstance();
                        break;
                    case 1:
                        fragment = LibFloorFragment.newInstance();
                        break;
                    case 2:
                        fragment = LibContactFragment.newInstance();
                        break;
                    case 3:
                        if (checkNetwork(getString(R.string.map_network))) {
                            fragment = LibMapFragment.getInstance();
                            Bundle bundle = new Bundle();
                            double lat_lng[]= {22.999770, 120.219925};
                            bundle.putDoubleArray("lat_lng", lat_lng);
                            fragment.setArguments(bundle);
                        }
                        break;
                    default:
                        break;
                }

                if (fragment != null && !fragment.isAdded()) {
                    FragmentManager fragmentManager = getActivity()
                            .getFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                            android.R.animator.fade_in, android.R.animator.fade_out);
                    fragmentManager.beginTransaction().addToBackStack(null)
                            .add(R.id.content_frame, fragment).commit();
                }
                listview.setEnabled(true);
            }

        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false); // 隱藏設定按鈕
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 確認網路狀態
     *
     * @param text 當網路無法連結時，要顯示的Toast
     * @return
     */
    private boolean checkNetwork(String text) {
        Context context = this.getActivity();
        if (!EnvChecker.pingGoogleDNS(context)) {
            if (toast == null)
            {
                toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            }
            toast.show();
            return false;
        }

        return true;
    }
}
