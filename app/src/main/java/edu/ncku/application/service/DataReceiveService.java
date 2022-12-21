package edu.ncku.application.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Executors;

import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.NewsReceiveTask;

/**
 * 此IntentService類別是用來在背景處理NewsReceiveTask的啟動
 * 表示手動更新最新消息的背景程式
 * 透過靜態方法startActionONCE就可以讓NewsFragment驅動
 */
public class DataReceiveService extends IntentService implements IOConstatnt {
    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String DEBUG_FLAG = DataReceiveService.class.getName();

    private static final String ACTION_ONCE = "edu.ncku.application.service.action.ONCE";

    public DataReceiveService() {
        super("DataReceiveService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DataReceiveService(String name) {
        super(name);
    }

    /**
     * Starts this service to perform action ONCE with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionONCE(Context context) {
        try {
            Intent intent = new Intent(context, DataReceiveService.class);
            intent.setAction(ACTION_ONCE);
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ONCE.equals(action)) {
                handleActionOnce();
            }
        }
    }

    /**
     * Handle action ONCE in the provided background thread with the provided
     * parameters.
     */
    private void handleActionOnce() {
        // TODO: Handle action
        try {
            Executors.newScheduledThreadPool(1).submit(new NewsReceiveTask(getApplicationContext() , true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        //handleActionUNREGISTER();
        if(showLogMsg){
            Log.d(DEBUG_FLAG, "DataReceiveService destroy...");
        }
        super.onDestroy();
    }


}
