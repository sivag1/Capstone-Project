package com.sivag1.juiceboard;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import com.sivag1.juiceboard.data.JuiceLevel;

import java.util.Date;

public class Utility {
    public static JuiceLevel constructJuiceLevel(Context context, String objectId,
                                                 Intent batteryStatus) {
        JuiceLevel juiceLevel = new JuiceLevel();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        try {
            batteryStatus = context.registerReceiver(null, ifilter);
        } catch (RuntimeException e) {
            if (batteryStatus == null)
                throw e;
        }

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        //boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        //      status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        //boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        //boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int imageId = batteryStatus.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1);

        float batteryPct = level / (float) scale;
        int batteryPctInt = (int) (batteryPct * 100);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        if (chargePlug == 0) {
            if (sp.getInt("NOT_CHARGING", -1) == -1) {
                editor.putInt("NOT_CHARGING", imageId);
                editor.commit();
            }
        } else {
            if (sp.getInt("CHARGING", -1) == -1) {
                editor.putInt("CHARGING", imageId);
                editor.commit();
            }
        }


        juiceLevel.setChargingIndicator(status);
        juiceLevel.setDeviceId(getDeviceId(context));
        if (juiceLevel.getDeviceId() == null)
            juiceLevel.setDeviceId(sp.getString("deviceName", ""));
        juiceLevel.setDeviceModel(getDeviceName());
        juiceLevel.setDeviceName(sp.getString("deviceName", ""));
        juiceLevel.setPluggedIndicator(chargePlug);
        juiceLevel.setLastKnownPercentage(batteryPctInt);
        juiceLevel.setBatteryIcon(imageId);

        if (objectId != null && objectId.length() > 0)
            juiceLevel.setObjectId(objectId);
        return juiceLevel;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static void notify(Context mContext, String text) {
        // NotificationCompatBuilder is a very convenient way to build backward-compatible
        // notifications.  Just throw in some data.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(mContext.getString(R.string.notif_title))
                        .setContentText(text);

        // Make something interesting happen when the user clicks on the notification.
        // In this case, opening the app is sufficient.
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                .TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    public static JuiceLevel mapCursorToJL(Cursor cursor) {
        JuiceLevel jl = new JuiceLevel();
        jl.setObjectId(cursor.getString(0));
        jl.setDeviceModel(cursor.getString(1));
        jl.setDeviceName(cursor.getString(2));
        jl.setDeviceId(cursor.getString(3));
        jl.setLastKnownPercentage(cursor.getFloat(4));
        jl.setChargingIndicator(cursor.getInt(5));
        jl.setPluggedIndicator(cursor.getInt(6));
        jl.setLastUpdatedDate(new Date(cursor.getLong(7)));
        jl.setBatteryIcon(cursor.getInt(8));
        return jl;
    }
}
