package com.sivag1.juiceboard;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sivag1.juiceboard.data.JuiceLevel;
import com.sivag1.juiceboard.data.MyProvider;

import java.util.Date;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by sivag1 on 2/5/16.
 */
public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyRemoteViewServiceFactory(this.getApplicationContext(), intent);
    }
}


class MyRemoteViewServiceFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private Cursor cursor;
    private int appWidgetId;
    SharedPreferences sp1;

    public MyRemoteViewServiceFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        sp1 = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreate() {
        cursor = context.getContentResolver().query(MyProvider.JUICELEVELS_URI, null, null, null, "deviceName" + " ASC");
        cursor.moveToFirst();
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        int count = cursor.getCount();
        return count;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews row = new RemoteViews(context.getPackageName(),
                R.layout.widget_item_row);

        cursor.moveToPosition(i);
        JuiceLevel juiceLevel = cupboard().withCursor(cursor).get(JuiceLevel.class);

        row.setTextViewText(R.id.txtName, juiceLevel.getDeviceName());
        row.setTextViewText(R.id.txtModel, juiceLevel.getDeviceModel());
        row.setTextViewText(R.id.txtPercent, String.valueOf((int) juiceLevel.getLastKnownPercentage()) + "%");

        long diffInMillis = new Date().getTime() - juiceLevel.getLastUpdatedDate().getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        String text;
        if (diffInMinutes < 60)
            text = diffInMinutes + " minute(s) ago";
        else {
            long diffInHours = diffInMinutes / 60;
            text = diffInHours + " hour(s) ago";
        }
        row.setTextViewText(R.id.txtDate, text);

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
