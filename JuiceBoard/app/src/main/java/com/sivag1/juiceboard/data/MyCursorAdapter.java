package com.sivag1.juiceboard.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sivag1.juiceboard.R;

import java.util.Date;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MyCursorAdapter extends CursorAdapter {

    JuiceLevel juiceLevel;
    SharedPreferences sp1;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        TextView txtName;
        TextView txtModel;
        TextView txtPercent;
        TextView txtDate;
        ImageView imageStatus;

        public ViewHolder(View view) {
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtModel = (TextView) view.findViewById(R.id.txtModel);
            imageStatus = (ImageView) view.findViewById(R.id.imgIcon);
            txtPercent = (TextView) view.findViewById(R.id.txtPercent);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
        }
    }

    public MyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int layoutId = R.layout.listview_item_row;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        sp1 = PreferenceManager.getDefaultSharedPreferences(context);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();


        juiceLevel = cupboard().withCursor(cursor).get(JuiceLevel.class);

        //juiceLevel = Utility.mapCursorToJL(cursor);
        holder.txtName.setText(juiceLevel.getDeviceName());
        holder.txtName.setContentDescription(juiceLevel.getDeviceName());
        holder.txtModel.setText(juiceLevel.getDeviceModel());
        holder.txtModel.setContentDescription(juiceLevel.getDeviceModel());

        holder.txtPercent.setText(String.valueOf((int) juiceLevel.getLastKnownPercentage()) + "%");
        holder.txtPercent.setContentDescription(String.valueOf((int) juiceLevel.getLastKnownPercentage()) + "%");

        Drawable drawable;
        int id = -1;
        if (juiceLevel.getPluggedIndicator() == 0) {
            id = sp1.getInt("NOT_CHARGING", -1);
        } else {
            id = sp1.getInt("CHARGING", -1);
        }

        if (id == -1) {
            holder.imageStatus.setVisibility(View.INVISIBLE);
        } else {
            try {
                drawable = context.getResources().getDrawable(id);
                holder.imageStatus.setVisibility(View.VISIBLE);
                if (drawable != null && drawable instanceof LevelListDrawable) {
                    LevelListDrawable batteryLevel = (LevelListDrawable) drawable;
                    batteryLevel.setLevel((int) juiceLevel.getLastKnownPercentage());
                    holder.imageStatus.setBackgroundDrawable(batteryLevel);
                } else {
                    holder.imageStatus.setImageDrawable(drawable);
                }
            } catch (RuntimeException e) {
                holder.imageStatus.setVisibility(View.INVISIBLE);
            }
        }

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
        holder.txtDate.setText(text);
        holder.txtDate.setContentDescription(text);

    }

}