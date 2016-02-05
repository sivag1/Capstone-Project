package com.sivag1.juiceboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        try {
            BackendDataFacade.update(context, false, intent);
        } catch (RuntimeException e) {
            Toast.makeText(context, context.getString(R.string.app_name) + ": Error updating " +
                    "status.", Toast.LENGTH_SHORT).show();
        }
    }
}
