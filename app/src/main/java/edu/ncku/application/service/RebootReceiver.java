package edu.ncku.application.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * 此Receiver類別由，接收開機的事件
 */
public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // 重開機(手機)後，自動重啟網路監視Service (背景)
        context.startService(new Intent(context, NetworkListenerService.class));// 重開機(手機)後，自動重啟網路監視Service (前景)
    }
}
