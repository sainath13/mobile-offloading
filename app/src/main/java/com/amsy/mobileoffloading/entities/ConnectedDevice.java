package com.amsy.mobileoffloading.entities;

import java.io.Serializable;

public class ConnectedDevice implements Serializable {
    private String endpointId;
    private String endpointName;
    private DeviceStatistics deviceStats;
    private String requestStatus;


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

    public DeviceStatistics getDeviceStats() {
        return deviceStats;
    }

    public void setDeviceStats(DeviceStatistics deviceStats) {
        this.deviceStats = deviceStats;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}
