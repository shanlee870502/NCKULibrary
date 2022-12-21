package edu.ncku.application.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import edu.ncku.application.R;

/**
 * 此頁面只用來顯示運轉中，用在ISBN頁面等待0.5秒啟動時
 */
public class ProgressFragment extends Fragment {

    // TODO: Rename and change types and number of parameters
    public static ProgressFragment newInstance() {
        ProgressFragment fragment = new ProgressFragment();
        return fragment;
    }

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.frameLayout);
        frameLayout.setAlpha(0.6f);
        return view;
    }

}
