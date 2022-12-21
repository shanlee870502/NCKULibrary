package edu.ncku.application.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import edu.ncku.application.R;
import edu.ncku.application.adapter.ListViewInfoAdapter;

/**
 * 本館資訊頁面
 */
public class InfoListFragment extends Fragment {

    private ListView allInfoListview;

    public InfoListFragment() {
        // Required empty public constructor
    }

    public static InfoListFragment newInstance() {
        InfoListFragment fragment = new InfoListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_info_list, container,
                false);
        allInfoListview = (ListView) rootView.findViewById(R.id.allInfoListView);

        /* 從資源檔讀取本館資訊字串陣列 */
        String[] all_lib_info_list = getResources().getStringArray(
                R.array.all_lib_info_list);

        allInfoListview.setAdapter(new ListViewInfoAdapter(this
                .getActivity().getApplicationContext(), all_lib_info_list));
        allInfoListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                allInfoListview.setEnabled(false);
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = ElectronicResourceQueryFragment.newInstance();
                        break;
                    case 1:
                        fragment = LibInfoListFragment.newInstance();
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
                allInfoListview.setEnabled(true);
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
}
