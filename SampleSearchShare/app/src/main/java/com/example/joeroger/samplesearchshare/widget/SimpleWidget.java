package com.example.joeroger.samplesearchshare.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.joeroger.samplesearchshare.R;
import com.example.joeroger.samplesearchshare.activity.TopLevelActivity;


/**
 * Implementation of App Widget functionality.
 */
public class SimpleWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
        views.setTextViewText(R.id.title, "My Homework Widget");
        views.setTextViewText(R.id.subTitle, context.getString(R.string.notification_item_big_content, 1));
        views.setTextViewText(R.id.button, "Open App");
        views.setOnClickPendingIntent(R.id.button, TopLevelActivity.buildWidgetPendingIntent(context));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


