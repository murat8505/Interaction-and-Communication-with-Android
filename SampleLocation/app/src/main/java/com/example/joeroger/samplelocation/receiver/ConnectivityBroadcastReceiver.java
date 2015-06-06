package com.example.joeroger.samplelocation.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import com.example.joeroger.samplelocation.network.ConnectivityUtils;
import com.example.joeroger.samplelocation.service.NetworkIntentService;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    public ConnectivityBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (ConnectivityUtils.isConnected(context)) {
                NetworkIntentService.startService(context);

                // Disable self..., since we sent the intent. No need to resignal if user
                // moves from mobile to wifi or vice versa.
                enableReceiver(context, false);
            }
        }
    }

    public static void enableReceiver(Context context, boolean enable) {

        PackageManager packageManager = context.getPackageManager();
        ComponentName receiver = new ComponentName(context, ConnectivityBroadcastReceiver.class);
        packageManager.setComponentEnabledSetting(receiver,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
