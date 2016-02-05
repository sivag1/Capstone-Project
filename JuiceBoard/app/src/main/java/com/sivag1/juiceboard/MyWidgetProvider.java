package com.sivag1.juiceboard;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;



public class MyWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, MyWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.widget_main);

            rv.setRemoteAdapter(appWidgetIds[i], android.R.id.list, intent);

            rv.setEmptyView(android.R.id.list, android.R.id.empty);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
    }
}
