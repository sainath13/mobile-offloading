package com.amsy.mobileoffloading.callback;

import com.amsy.mobileoffloading.entities.DeviceStatistics;
import com.amsy.mobileoffloading.entities.WorkInfo;
import com.amsy.mobileoffloading.helper.Constants;

public interface WorkerStatusListener {

    void onWorkStatusReceived(String endpointId, WorkInfo workInfo);

     void onDeviceStatsReceived(String endpointId, DeviceStatistics deviceStats);

}
