package edu.ncku.application.io.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import edu.ncku.application.util.EnvChecker;

//import edu.ncku.application.service.RegistrationIntentService;

/**
 * 此Receiver類別由NetworkListenerService註冊，接收網路狀態改變的事件
 */
public class NetworkCheckReceiver extends BroadcastReceiver {

    private static final String DEBUG_FLAG = NetworkCheckReceiver.class
            .getName();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(6);

    public NetworkCheckReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        try {
            if (context == null) return;

            /* 當連上網路時，在背景執行資料更新的工作 */
            if (EnvChecker.isNetworkConnected(context)) {
                scheduledExecutorService.submit(new LibOpenTimeReceiveTask(context));
                scheduledExecutorService.submit(new UpcomingEventsReceiveTask(context));
                scheduledExecutorService.submit(new FloorInfoReceiveTask(context));
                scheduledExecutorService.submit(new ContactInfoReceiveTask(context));
                scheduledExecutorService.submit(new VisitorRecieveTask(context, true, false));
                scheduledExecutorService.submit(new CollapseLogSendTask(context));
                scheduledExecutorService.submit(new OccupancyReceiveTask(context, true, false));
                scheduledExecutorService.submit(new OccupancyLimitTask(context));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
