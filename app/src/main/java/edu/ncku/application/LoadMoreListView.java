package edu.ncku.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 此UI類別是給NewsFragment與MessagerFragment使用，當使用者在這兩個頁面卷軸拉到最底下時，
 * 將會觸發OnScrollListener介面事件，並且此UI類別將會進行對應的動作
 */
public class LoadMoreListView extends ListView implements OnScrollListener{
	private View footer;  
	private TextView mReloadTextView, mIsLoadTextView;
	private ProgressBar mProgressBar;
    
    private int totalItem;  
    private int lastItem;  
      
    private boolean isLoading;  
      
    private OnLoadMore onLoadMore;  
      
    private LayoutInflater inflater;  
      
    public LoadMoreListView(Context context) {  
        super(context);  
        init(context);  
    }  
  
    public LoadMoreListView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init(context);  
    }  
  
    public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        init(context);  
    }  
    
    @SuppressLint("InflateParams")  
    private void init(Context context) {  
        inflater = LayoutInflater.from(context);  
        footer = inflater.inflate(R.layout.load_more_footer, null , false);
        /* footer元件將會接受使用者主動點擊的事件 */
        footer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isLoading){  
	                isLoading = true;
	                mReloadTextView.setVisibility(View.GONE);  
	            	mIsLoadTextView.setVisibility(View.VISIBLE);
	            	mProgressBar.setVisibility(View.VISIBLE);
	                onLoadMore.loadMore();  
	            }  
			}
        	
        });
        mReloadTextView = (TextView) footer.findViewById(R.id.reload_textView);
        mIsLoadTextView = (TextView) footer.findViewById(R.id.no_more_textView);
        mProgressBar = (ProgressBar) footer.findViewById(R.id.load_more_progressBar);
        mIsLoadTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        
        this.addFooterView(footer);  
        this.setOnScrollListener(this);  
    }  

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.lastItem = firstVisibleItem + visibleItemCount;  
        this.totalItem = totalItemCount;  
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if(this.totalItem == lastItem && scrollState == SCROLL_STATE_IDLE){
            if(!isLoading){  
                isLoading=true;
                mReloadTextView.setVisibility(View.GONE);  
            	mIsLoadTextView.setVisibility(View.VISIBLE);
            	mProgressBar.setVisibility(View.VISIBLE);
                onLoadMore.loadMore();  
            }  
        }  
		
	}
	
	public void setLoadMoreListen(OnLoadMore onLoadMore){  
        this.onLoadMore = onLoadMore;  
    }  

    /**
        * 加载完成调用此方法
       */
    public void onLoadComplete(){  
    	mReloadTextView.setVisibility(View.VISIBLE);  
    	mIsLoadTextView.setVisibility(View.GONE);
    	mProgressBar.setVisibility(View.GONE);
        isLoading = false;  
    }


    public interface OnLoadMore{  
        public void loadMore();  
    }  
}
