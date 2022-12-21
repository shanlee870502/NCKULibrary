package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.ncku.application.MainActivity;
import edu.ncku.application.R;
import edu.ncku.application.adapter.ListMsgsAdapter;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.MsgEditTask;
import edu.ncku.application.io.file.MsgsReaderTask;
import edu.ncku.application.model.Message;
import edu.ncku.application.util.ITitleChangeListener;
import edu.ncku.application.util.Preference;
import edu.ncku.application.util.UniversalAbility;

/**
 * 顯示推播訊息的列表頁面，當參數大於等於0時，進入該位置的推播訊息
 */
public class MessagerFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = MessagerFragment.class.getName();
    private static final String POSITION = "POSITION";

    private MainActivity activity;

    private ProgressBar progressBar;
    private TextView textView;
    private ListView listView;
    /*20200820 新增到設定頁面的按鈕*/
    private Button btn_setting;
    /*20200820 新增context變數以取得目前訂閱狀態*/
    private Context context;
    /*20200820 新增設定頁面實體*/
    private Fragment mSettingFragment = new PrefFragment(); // 設定頁面實體
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    private Handler mHandler = new Handler();
    private ListMsgsAdapter listViewAdapter;
    private List<Integer> updateMsgList = new ArrayList<>();
    private List<Boolean> updateMsgIsReadList = new ArrayList<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MessagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagerFragment getInstance(int position) {
        MessagerFragment messagerFragment = new MessagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        messagerFragment.setArguments(bundle);
        return messagerFragment;
    }

    public MessagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (MainActivity) getActivity();
        context = this.getActivity().getWindow().getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_messager, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.msgProgressBar);
        textView = (TextView) rootView.findViewById(R.id.msgTip);
        listView = (ListView) rootView.findViewById(R.id.msgListView);
        /*20200820 新增到設定頁面的按鈕*/
        btn_setting = (Button) rootView.findViewById(R.id.btn_go2setting);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "position : " + position);
                }
                updateView(position);
                changeToMsgViewer(position);
            }
        }); // 註冊點擊事件

        /* 實現多選刪除功能 */
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                activity.getMenuInflater().inflate(R.menu.menu_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.selectMenuItem:
                            for (int position = 0; position < listViewAdapter.getCount(); position++) {
                                listView.setItemChecked(position, true);
                            }
                            break;
                        case R.id.deleteMenuItem:
                            (new AlertDialog.Builder(getActivity()))
                                    .setMessage(R.string.deleteHint)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User clicked OK button
                                            listViewAdapter.deleteSelect(listView.getCheckedItemPositions());
                                            onActivityCreated(null);
                                            mode.finish();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User cancelled the dialog
                                        }
                                    }).show();
                            break;
                        default:
                            Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show();
                            break;
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {

            }

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, final int position, long id, final boolean checked) {
                mode.setTitle(Integer.toString(selectItems()));
                mode.invalidate();
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, String.format("Position : %d %s", position, (checked) ? "checked" : "isn't checked"));
                }
            }

            private int selectItems() {
                int select = 0;
                SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    if (checkedItemPositions.get(checkedItemPositions.keyAt(i))) {
                        select++;
                    }
                }

                return select;
            }

        });

        /*20200820 新增到設定頁面按鈕的點擊事件*/
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFragment(mSettingFragment, getResources().getString(R.string.setting));
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(showLogMsg){
                        Log.d(DEBUG_FLAG, "ReaderTask start!");
                    }
                    // 20201112 Change logic for showing hint and messages
                    if(Preference.isSub(context,Preference.getUsername(context))){
                        if (updateList()){
                            progressBar.setVisibility(View.INVISIBLE);
                            listView.setVisibility(View.VISIBLE);
                        } else{
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.msg_empty);
                            btn_setting.setVisibility(View.INVISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }
                    } else{
                        progressBar.setVisibility(View.INVISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(R.string.setting_hint);
                        btn_setting.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                    }
                    /*if (updateList()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.VISIBLE);
                    } else {
                        //20200820 Check if the user subscribes
                        if (Preference.isSub(context,Preference.getUsername(context))) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.msg_empty);
                            btn_setting.setVisibility(View.INVISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }
                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(R.string.setting_hint);
                            btn_setting.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }

                    }*/

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem settingItem = menu.findItem(R.id.settingMenuItem);
        if (settingItem != null) {
            settingItem.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    private void setListAdapter(final ListAdapter adapter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                listView.setAdapter(adapter);
            }
        });
    }

    /**
     * 從檔案讀入推播訊息，並顯示出來
     *
     * @return 載入推播訊息是否成功
     * @throws Exception
     */
    private boolean updateList() throws Exception {
        MsgsReaderTask msgsReaderTask = new MsgsReaderTask(this);
        msgsReaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        listViewAdapter = msgsReaderTask.get();

        if (listViewAdapter != null) {
            setListAdapter(listViewAdapter);
            int position = getArguments().getInt(POSITION);

            /* 當有來自Notification的位置參數，將會自動轉入該推播訊息 */
            if(showLogMsg){
                Log.d(DEBUG_FLAG, "position : " + position);
            }
            if (position >= 0 && position < listViewAdapter.getCount()) {
                int realPosition = listViewAdapter.getCount() - (position + 1);
                changeToMsgViewer(realPosition);
                getArguments().putInt(POSITION, -1);
            }
            return true;
        } else {
            if(showLogMsg){
                Log.e(DEBUG_FLAG, "listViewAdapter is null!");
            }
            return false;
        }
    }

    /**
     * 進入該推播訊息內容頁面
     *
     * @param position 推播訊息位置
     */
    private void changeToMsgViewer(int position) {
        Message news = (Message) listViewAdapter.getItem(position);

        Bundle bundle = new Bundle();
        bundle.putString("title", news.getTitle());
        bundle.putString("date", new SimpleDateFormat("yyyy/MM/dd HH:mm").format((long) news.getPubTime() * 1000));
        bundle.putString("unit", ""); // 推播訊息沒有單位
        bundle.putString("contents", news.getContents().replace("\r\n", "<br>").trim());

        NewsViewerFragment msgViewerFragment =  new NewsViewerFragment();
        msgViewerFragment.setArguments(bundle);

        FragmentManager fragmentManager = activity.getFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                android.R.animator.fade_in, android.R.animator.fade_out);
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .add(R.id.content_frame, msgViewerFragment).commit();
    }

    private void updateView(int index){
        View v = listView.getChildAt(index - listView.getFirstVisiblePosition());

        if(v == null)
            return;

        TextView txtTitle = (TextView) v.findViewById(R.id.txtTitle);
        int txtStyle = textView.getTypeface().getStyle();
        if (txtStyle == Typeface.NORMAL) {
            updateMsgList.add(index);
            updateMsgIsReadList.add(true);
        }
        txtTitle.setTypeface(null, Typeface.NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MsgEditTask msgEditTask = new MsgEditTask(activity, updateMsgIsReadList);
        msgEditTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updateMsgList);
    }

    /**20200820
     * 註冊ITitleChangeListener介面的標題變更方法給下面addFragment使用
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            titleChangeListener = (ITitleChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ITitleChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        titleChangeListener = null;
    }

    /**20200820
     * 新增Fragment堆疊，驅動標題變更事件方法
     *
     * @param fragment
     * @param title 標題
     */
    private void addFragment(Fragment fragment, String title){
        if(fragment != null && !fragment.isAdded()) {
            FragmentTransaction fragmentTransaction = getActivity()
                    .getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            //20200820 要從.add改成.replace，從設定頁面回來的時候，才會再執行一次生命週期
            fragmentTransaction.addToBackStack(null)
                    .replace(R.id.content_frame, fragment)
                    .commit();
            if(title != null && !title.isEmpty()) titleChangeListener.onChangeTitle(title);
            UniversalAbility.HideKeyboard(getActivity());
        }
    }
}