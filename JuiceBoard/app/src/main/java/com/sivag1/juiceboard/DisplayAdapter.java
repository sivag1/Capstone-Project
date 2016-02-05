package com.sivag1.juiceboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sivag1.juiceboard.data.JuiceLevel;

import java.util.Date;
import java.util.List;

public class DisplayAdapter extends ArrayAdapter<JuiceLevel> {

    Context context;
    List<JuiceLevel> juiceLevelList;
    int layoutResourceId;
    ContactHolder holder = null;
    SharedPreferences sp1;
    JuiceLevel juiceLevel;

    public DisplayAdapter(Context context, int resource, List<JuiceLevel> objects) {
        super(context, resource, objects);

        this.context = context;
        this.juiceLevelList = objects;
        this.layoutResourceId = resource;
        sp1 = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContactHolder();
            holder.txtName = (TextView) row.findViewById(R.id.txtName);
            holder.txtModel = (TextView) row.findViewById(R.id.txtModel);
            holder.imageStatus = (ImageView) row.findViewById(R.id.imgIcon);
            holder.txtPercent = (TextView) row.findViewById(R.id.txtPercent);
            holder.txtDate = (TextView) row.findViewById(R.id.txtDate);
            row.setTag(holder);
        } else {
            holder = (ContactHolder) row.getTag();
        }

        juiceLevel = juiceLevelList.get(position);
        holder.txtName.setText(juiceLevel.getDeviceName());
        holder.txtModel.setText(juiceLevel.getDeviceModel());

        holder.txtPercent.setText(String.valueOf((int) juiceLevel.getLastKnownPercentage()) + "%");

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


//        if (juiceLevel.getChargingIndicator() == BatteryManager.BATTERY_STATUS_CHARGING) {
//            drawable = context.getResources().getDrawable(juiceLevel.getBatteryIcon());
//        } else {
//            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//            Intent batteryStatus = context.registerReceiver(null, ifilter);
//            try {
//                drawable = context.getResources().getDrawable(batteryStatus.getIntExtra
//                        (BatteryManager.EXTRA_ICON_SMALL, -1));
//            } catch (Exception e) {
//                drawable = null;
//            }
//
//        }


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

        return row;
    }

    static class ContactHolder {
        TextView txtName;
        TextView txtModel;
        TextView txtPercent;
        TextView txtDate;
        ImageView imageStatus;
        ImageView imagePlugged;
    }
}
