package edu.ncku.application.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.SubscribeTask;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.PreferenceKeys;

/**
 * 設定頁面，會根據登入狀態來決定要載入的XML資源檔
 */
public class PrefFragment extends PreferenceFragment implements IOConstatnt{

    private static final String DEBUG_FLAG = PrefFragment.class.getName();

    private ProgressDialog progressDialog;

    private Context context;

    //20200309 Add titlechangerListener
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.white));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = this.getActivity().getWindow().getContext();

        this.addPreferencesFromResource(R.xml.preferences_login);

        final SwitchPreference switchPref = (SwitchPreference) getPreferenceManager().findPreference(PreferenceKeys.SUBSCRIPTION);

        if (switchPref != null) // 註冊switchPref的狀態改變事件
            switchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(showLogMsg){
                        Log.d(DEBUG_FLAG, "" + switchPref.isChecked());
                    }

                    /* 判斷網路狀態 */
                    if (EnvChecker.isNetworkConnected(context)) {

                        // Start IntentService to register this application with GCM.
                        if (progressDialog != null) {
                            progressDialog.dismiss(); // 關閉顯示處理中的Dialog
                            progressDialog = null;
                        }
                        progressDialog = ProgressDialog.show(context, getResources().getString(R.string.please_wait), getResources().getString(R.string.handle_subscription), true); // 顯示處理中的Dialog
                        SubscribeTask subscribeTask = new SubscribeTask(context);
                        subscribeTask.execute(!switchPref.isChecked());
                        Boolean check;
                        try {
                            check = subscribeTask.get(3, TimeUnit.SECONDS); // 啟動背景訂閱程式，以3秒為上限
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            check = false;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            check = false;
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            check = false;
                        }

                        if (check == null) check = false;

                        final boolean transCheck = check;
                        /* 更新UI */
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null) {
                                    progressDialog.dismiss(); // 關閉顯示處理中的Dialog
                                    progressDialog = null;
                                }
                                Toast.makeText(context, (transCheck) ? R.string.sub_handled : R.string.sub_fail, Toast.LENGTH_LONG).show(); // 顯示Toast
                            }
                        }, 1000);

                        return transCheck;
                    } else {
                        Toast.makeText(context, R.string.network_disconnected, Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });


    }

}
