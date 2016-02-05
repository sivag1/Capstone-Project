package com.sivag1.juiceboard.data;

import java.util.Date;

public class JuiceLevel {

    public JuiceLevel() {
    }

//   private long _id;

    private String objectId;

    private String deviceModel;

    private String deviceName;

    private String deviceId;

    private float lastKnownPercentage;

    private int chargingIndicator;

    private int pluggedIndicator;

    private Date lastUpdatedDate;

    private int batteryIcon;

//    public long get_id() {
//        return _id;
//    }
//
//    public void set_id(long _id) {
//        this._id = _id;
//    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getChargingIndicator() {
        return chargingIndicator;
    }

    public void setChargingIndicator(int chargingIndicator) {
        this.chargingIndicator = chargingIndicator;
    }

    public int getPluggedIndicator() {
        return pluggedIndicator;
    }

    public void setPluggedIndicator(int pluggedIndicator) {
        this.pluggedIndicator = pluggedIndicator;
    }


    public float getLastKnownPercentage() {
        return lastKnownPercentage;
    }

    public void setLastKnownPercentage(float lastKnownPercentage) {
        this.lastKnownPercentage = lastKnownPercentage;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public int getBatteryIcon() {
        return batteryIcon;
    }

    public void setBatteryIcon(int batteryIcon) {
        this.batteryIcon = batteryIcon;
    }
}
