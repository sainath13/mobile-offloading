package com.amsy.mobileoffloading.entities;

import java.io.Serializable;

public class WorkInfo implements Serializable {
    private int partitionIndex;
    private String status;
    private int result;

    public void setStatusInfo(String status) {
        this.status = status;
    }

    public void setPartitionIndexInfo(int partitionIndex) {
        this.partitionIndex = partitionIndex;
    }

    public void setResultInfo(int result) {
        this.result = result;
    }

    public int getPartitionIndexInfo() {
        return partitionIndex;
    }

    public int getResultInfo() {
        return result;
    }

    public String getStatusInfo() {
        return status;
    }


}
