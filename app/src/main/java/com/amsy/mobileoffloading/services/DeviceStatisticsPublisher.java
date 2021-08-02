package com.amsy.mobileoffloading.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.amsy.mobileoffloading.entities.ClientPayLoad;
import com.amsy.mobileoffloading.entities.DeviceStatistics;
import com.amsy.mobileoffloading.helper.Constants;
import com.amsy.mobileoffloading.helper.DataTransfer;

public class DeviceStatisticsPublisher {

    private Context context;
    private String endpointId;
    private Handler handler;
    private Runnable runnable;
    private int interval;

    public DeviceStatisticsPublisher(Context context, String endpointId, int updateInterval) {
        this.context = context;
        this.endpointId = endpointId;
        this.interval = updateInterval;
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            publish();
            handler.postDelayed(runnable, interval);
        };
    }

    public void start() {
        handler.postDelayed(runnable,  interval);
        LocationService.getInstance(context).start(interval);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        LocationService.getInstance(context).stop();
    }

    private void publish() {
        DeviceStatisticsPublisher.publish(this.context, this.endpointId);
    }

    public static void publish(Context context, String endpointId) {
        // Get Device Statistics
        DeviceStatistics deviceStats = new DeviceStatistics();
            deviceStats.setBatteryLevel(getBatteryLevel(context));
            deviceStats.setCharging(isPluggedIn(context));
            deviceStats.setLocation(getLocation(context));
        if(endpointId != null) {
            ClientPayLoad payload = new ClientPayLoad().setTag(Constants.PayloadTags.DEVICE_STATS).setData(deviceStats);
            DataTransfer.sendPayload(context, endpointId, payload);
        }
        Log.d("DEVICE_STATS", "DEVICE STATUS B: " + deviceStats.getBatteryLevel() + " P: " + deviceStats.isCharging() +  " L: " + deviceStats.getLatitude() + " " + deviceStats.getLongitude());
    }

    public static Location getLocation(Context context) {
        Location location = LocationService.getInstance(context).getLastAvailableLocation();
        return location;
    }

    public static int getBatteryLevel(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public static boolean isPluggedIn(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugStatus == BatteryManager.BATTERY_STATUS_CHARGING
                || plugStatus == BatteryManager.BATTERY_PLUGGED_AC
                || plugStatus == BatteryManager.BATTERY_PLUGGED_USB
                || plugStatus == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

}

