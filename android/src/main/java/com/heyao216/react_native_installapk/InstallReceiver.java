package com.heyao216.react_native_installapk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("CALL RECEIVER!!");
        String activityName = intent.getStringExtra("ACTIVITY_NAME");
        try {
            Class<?> c = Class.forName(activityName);
            Intent startIntent = new Intent(context, c);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(startIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
