package edu.ncku.application.io.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;

import edu.ncku.application.fragments.NewsFragment;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.model.News;
import edu.ncku.application.adapter.ListNewsAdapter;
import edu.ncku.application.util.EnvChecker;

/**
 * 此AsyncTask類別將會在最新消息頁面開啟時被執行，進行頁面資料讀取的工作
 */
public class NewsReaderTask extends AsyncTask<Void, Void, ListNewsAdapter> implements IOConstatnt{

	private static final String DEBUG_FLAG = NewsReaderTask.class.getName();
	private static final String FILE_NAME = NEWS_FILE;
	
	private NewsFragment newsFragment;
	private Context context;
	private ListNewsAdapter listViewAdapter;

	private int show;

	public NewsReaderTask(NewsFragment newsFragment, int show) {
		// TODO Auto-generated constructor stub
		this.newsFragment = newsFragment;
		this.context = newsFragment.getActivity().getApplicationContext();
		this.show = show;
	}

	@Override
	protected ListNewsAdapter doInBackground(Void... params) {
		// TODO Auto-generated method stub
		LinkedList<News> readNews = null;
		ObjectInputStream ois = null;
		File inputFile = null;

		try {
			inputFile = new File(context
					.getFilesDir(), FILE_NAME + ((EnvChecker.isLunarSetting())?"_cht":"_eng"));

			if (!inputFile.exists()) {
				if(showLogMsg){
					Log.d(DEBUG_FLAG, "file is not exist.");
				}
			} else {
				ois = new ObjectInputStream(new FileInputStream(inputFile));
				readNews = (LinkedList<News>) ois.readObject();
				if(showLogMsg){
					Log.v(DEBUG_FLAG,
							"Read msgs from file : " + readNews.size());
				}
				if (ois != null)
					ois.close();
			}

			if (readNews == null || readNews.size() == 0) {
				return null;
			}

			// 將取得的最新消息資料包裝成Adapter物件
			listViewAdapter = new ListNewsAdapter(newsFragment.getActivity(), readNews, show);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listViewAdapter;
	}


}
