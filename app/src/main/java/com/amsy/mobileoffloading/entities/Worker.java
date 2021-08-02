package com.amsy.mobileoffloading.entities;

import java.io.Serializable;

public class Worker implements Serializable {

    private String endpointId, endpointName;
    private DeviceStatistics deviceStatistics;
    private WorkInfo workInfo;

    private int workQuantity;
    private float distanceFromMaster;

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public WorkInfo getWorkStatus() {
        return workInfo;
    }

    public void setWorkStatus(WorkInfo workStatus) {
        this.workInfo = workStatus;
    }

    public DeviceStatistics getDeviceStats() {
        return deviceStatistics;
    }

    public void setDeviceStats(DeviceStatistics deviceStats) {
        this.deviceStatistics = deviceStats;
    }

    public int getWorkAmount() {
        return workQuantity;
    }

    public void setWorkAmount(int workAmount) {
        this.workQuantity = workAmount;
    }

    public float getDistanceFromMaster() {
        return distanceFromMaster;
    }

    public void setDistanceFromMaster(float distanceFromMaster) {
        this.distanceFromMaster = distanceFromMaster;
    }

}
