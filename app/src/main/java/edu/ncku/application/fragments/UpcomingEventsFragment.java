package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.file.UpcomingEventsReaderTask;
import edu.ncku.application.io.network.UpcomingEventsReceiveTask;
import edu.ncku.application.model.ActivityInfo;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;

/**
 * 顯示最近活動頁面(背景為透明)
 */
public class UpcomingEventsFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = UpcomingEventsFragment.class
            .getName();

    private static List<ActivityInfo> imgSuperLinks;
    private static String[] imgURLs;

    private static Gallery gallery;
    private ProgressDialog dialog;

    //20200304 Add close imagebutton
    private ImageButton mCloseImageButton;
    //20200309 Add titlechangerListener
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    public static UpcomingEventsFragment newInstance() {
        return new UpcomingEventsFragment();
    }

    public UpcomingEventsFragment() {
        // Required empty public constructor
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu

//        final Context context = getActivity().getApplicationContext();
//        LoadingData(context);
    }

    private void LoadingData(Context context) {
        try {
            /* 有網路時，進行更新動作 */
            if (EnvChecker.isNetworkConnected(context)) {
                Thread refresh = new Thread(new UpcomingEventsReceiveTask(context));
                refresh.start();
                refresh.join(3000);
                //Log.d(DEBUG_FLAG, "近期活動網路更新");
                if(showLogMsg){
                    Log.d("internet", "近期活動網路更新");
                }
            }
            else{
                if(showLogMsg){
                    Log.d("internet", "沒網路");
                }
            }

            /* 從檔案當中讀取近期活動資料 */
            UpcomingEventsReaderTask urlReceiveTask = new UpcomingEventsReaderTask(context);
            urlReceiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            imgSuperLinks = urlReceiveTask.get(3, TimeUnit.SECONDS);
            Date now = new Date();
            for(Iterator<ActivityInfo> iterator = imgSuperLinks.iterator(); iterator.hasNext();){
                ActivityInfo activityInfo = iterator.next();
                if(now.after(activityInfo.getStartTime()) && activityInfo.getEndTime().after(now)){
                    continue;
                }else {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* 確保應用程式不會崩潰的保險 */
        if (imgSuperLinks == null || imgSuperLinks.isEmpty()) {
            imgSuperLinks = new ArrayList<>();
            imgSuperLinks.add(new ActivityInfo());
        }

        imgURLs = new String[imgSuperLinks.size()];
        for(int i = 0;i < imgSuperLinks.size();i++){
            if(showLogMsg){
                Log.e("imgURLs", imgSuperLinks.get(i).getImgUrl());
            }
            // 20210203
            if (imgSuperLinks.get(i).getImgUrl().contains("https"))// 取得各個最近活動的網址(https)
                imgURLs[i] = imgSuperLinks.get(i).getImgUrl();
            else{// 取得各個最近活動的網址(http)
                imgURLs[i] = imgSuperLinks.get(i).getImgUrl().replace("http","https");
            }

        }
//        imgSuperLinks.keySet().toArray(imgURLs); // 取得各個最近活動的網址
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent_activity,
                container, false);
        final Context context = getActivity().getApplicationContext();
        LoadingData(context);
        gallery = (Gallery) rootView.findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(getActivity()));


        /* 註冊點擊事件 */
        gallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(showLogMsg){
                    Log.d(DEBUG_FLAG, "position : " + position);
                }
                /* 顯示該網址的網頁 */
                String link = null;
                for(ActivityInfo activityInfo : imgSuperLinks){
                    if(activityInfo.getImgUrl() == imgURLs[position % imgURLs.length]){
                        link = activityInfo.getActivityUrl();
                    }
                }
//                String link = imgSuperLinks.get(imgURLs[position % imgURLs.length]).getActivityUrl();
                if(link != null && !link.isEmpty() && EnvChecker.pingGoogleDNS(context)) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(i);
                }
            }
        });
        gallery.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                gallery.setSelected(true);
                gallery.setSelection(imgURLs.length);
            }

        });
        /*20200304 Link close Imagebutton and set click event*/
        mCloseImageButton = (ImageButton) rootView.findViewById(R.id.closeImgBtn);
        mCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                deleteFragment();
            }
        });
        return rootView;
    }
    //20200309 To do delete title while clicking x button
    /**********************************/
    /**
     * 註冊ITitleChangeListener介面的標題刪除方法
     *
     * @param activity
     **/
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
    private void deleteFragment(){
        titleChangeListener.deleteTitle();
    }
    /************************************************/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private static class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private DisplayImageOptions options;

        private ImageLoader imageLoader;

        ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            /* ImageLoader選項 */
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading_pic)
                    .showImageForEmptyUri(R.drawable.empty_pic)
                    .showImageOnFail(R.drawable.error_pic).cacheInMemory(true)
                    .cacheOnDisk(true).considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(20)).build();

            if(imageLoader == null) {
                imageLoader = ImageLoader.getInstance(); // 取得ImageLoader實體
            }
            imageLoader.init(ImageLoaderConfiguration.createDefault(inflater
                    .getContext())); // ImageLoader初始化
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE; // 設置模擬無限左右滑動
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position % imgURLs.length; // 設置模擬無限左右滑動
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = (ImageView) convertView;
            if (imageView == null) {
                imageView = (ImageView) inflater.inflate(
                        R.layout.item_gallery_image, parent, false);
            }

            int width = gallery.getWidth() * 2 / 3, height = gallery.getHeight() * 2 / 3; // 長寬變成原本的 2/3

            imageView.setAdjustViewBounds(true);
            imageView.setLayoutParams(new Gallery.LayoutParams(width, height));

            // 使用ImageLoader套件顯示圖片(具有自動cache網路圖片功能，提高效能)
            String url = imgURLs[position % imgURLs.length];
            if(showLogMsg){
                Log.d("imageloader", "url = " + url);
            }
            imageLoader.displayImage(url, imageView, options);
            return imageView;
        }
    }

}
