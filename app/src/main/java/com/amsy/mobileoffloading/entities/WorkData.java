package com.amsy.mobileoffloading.entities;

import java.io.Serializable;

public class WorkData implements Serializable {
    private int partitionIndex;
    private int[] rows;
    private int[] cols;

    private int amountWork;

    public int getPartitionIndex() {
        return partitionIndex;
    }

    public void setPartitionIndex(int partitionIndex) {
        this.partitionIndex = partitionIndex;
    }

    public int[] getRows() {
        return rows;
    }

    public void setRows(int[] rows) {
        this.rows = rows;
    }

    public int[] getCols() {
        return cols;
    }

    public void setCols(int[] cols) {
        this.cols = cols;
    }

    public int getAmountWork() {
        return amountWork;
    }

    public void setAmountWork(int amountWork) {
        this.amountWork = amountWork;
    }

}
