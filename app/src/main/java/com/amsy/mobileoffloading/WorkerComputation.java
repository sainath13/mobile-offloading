package com.amsy.mobileoffloading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amsy.mobileoffloading.callback.ClientConnectionListener;
import com.amsy.mobileoffloading.callback.PayloadListener;
import com.amsy.mobileoffloading.entities.ClientPayLoad;
import com.amsy.mobileoffloading.entities.WorkData;
import com.amsy.mobileoffloading.entities.WorkInfo;
import com.amsy.mobileoffloading.helper.Constants;
import com.amsy.mobileoffloading.helper.DataTransfer;
import com.amsy.mobileoffloading.helper.MatrixDS;
import com.amsy.mobileoffloading.helper.PayloadConverter;
import com.amsy.mobileoffloading.services.Advertiser;
import com.amsy.mobileoffloading.services.DeviceStatisticsPublisher;
import com.amsy.mobileoffloading.services.NearbyConnectionsManager;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.IOException;
import java.util.HashSet;

import pl.droidsonroids.gif.GifImageView;

public class WorkerComputation extends AppCompatActivity {
    private String masterId;
    private DeviceStatisticsPublisher deviceStatsPublisher;
    private ClientConnectionListener connectionListener;
    private PayloadListener payloadCallback;
    private int currentPartitionIndex;
    private HashSet<Integer> finishedWork = new HashSet<>();
    BatteryManager mBatteryManager = null;
    Long initialEnergyWorker,finalEnergyWorker,energyConsumedWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_computation);
        extractBundle();
        startDeviceStatsPublisher();
        setConnectionCallback();
        connectToMaster();
        //start measuring the pwer consumption at Worker
        mBatteryManager = (BatteryManager)getSystemService(Context.BATTERY_SERVICE);
        initialEnergyWorker =
                mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
        Log.d("WORKER_COMPUTATION", "Capturing power consumption");
    }

    public void setStatusText(String text, boolean isWorking) {
        //UI Textview
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText(text);
        GifImageView waiting = findViewById(R.id.waiting);
        waiting.setVisibility(isWorking ? View.INVISIBLE : View.VISIBLE);
        GifImageView working = findViewById(R.id.working);
        working.setVisibility(isWorking ? View.VISIBLE : View.INVISIBLE);
    }

    public void onWorkFinished(String text) {
        //UI Textview
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText(text);
        GifImageView waiting = findViewById(R.id.waiting);
        waiting.setVisibility(View.INVISIBLE);
        GifImageView working = findViewById(R.id.working);
        working.setVisibility(View.INVISIBLE);
        GifImageView done = findViewById(R.id.done);
        done.setVisibility(View.VISIBLE);
        TextView powerConsumed = findViewById(R.id.powerValue);
        powerConsumed.setText("Power Consumption (Slave) : "  + Long.toString(energyConsumedWorker)+ " nWh");
    }


    public void setPartitionText(int count) {
//        TextView dispCount = findViewById(R.id.count);
//        //TODO : ANVESH
//        dispCount.setText(count + "");
    }

    private void extractBundle() {
        Bundle bundle = getIntent().getExtras();
        this.masterId = bundle.getString(Constants.MASTER_ENDPOINT_ID);
    }

    private void startDeviceStatsPublisher() {
        deviceStatsPublisher = new DeviceStatisticsPublisher(getApplicationContext(), masterId, Constants.UPDATE_INTERVAL_UI);
    }

    private void connectToMaster() {
        payloadCallback = new PayloadListener() {
            @Override
            public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                startWorking(payload);
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

            }
        };
        NearbyConnectionsManager.getInstance(getApplicationContext()).acceptConnection(masterId);
    }

    private void setConnectionCallback() {
        connectionListener = new ClientConnectionListener() {
            @Override
            public void onConnectionInitiated(String id, ConnectionInfo connectionInfo) {
            }

            @Override
            public void onConnectionResult(String id, ConnectionResolution connectionResolution) {
            }

            @Override
            public void onDisconnected(String id) {
                navBack();
            }
        };
    }

    private void navBack() {
        finishedWork = new HashSet<>();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NearbyConnectionsManager.getInstance(getApplicationContext()).registerPayloadListener(payloadCallback);
        NearbyConnectionsManager.getInstance(getApplicationContext()).registerClientConnectionListener(connectionListener);
        deviceStatsPublisher.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NearbyConnectionsManager.getInstance(getApplicationContext()).unregisterPayloadListener(payloadCallback);
        NearbyConnectionsManager.getInstance(getApplicationContext()).unregisterClientConnectionListener(connectionListener);
        deviceStatsPublisher.stop();
    }

    @Override
    public void finish() {
        super.finish();
        NearbyConnectionsManager.getInstance(getApplicationContext()).disconnectFromEndpoint(masterId);
        currentPartitionIndex = 0;
    }


    public void onDisconnect(View view) {
        WorkInfo workStatus = new WorkInfo();
        workStatus.setPartitionIndexInfo(currentPartitionIndex);
        workStatus.setStatusInfo(Constants.WorkStatus.DISCONNECTED);

        ClientPayLoad tPayload1 = new ClientPayLoad();
        tPayload1.setTag(Constants.PayloadTags.WORK_STATUS);
        tPayload1.setData(workStatus);

        DataTransfer.sendPayload(getApplicationContext(), masterId, tPayload1);
        navBack();
    }

    public void startWorking(Payload payload) {
        WorkInfo workStatus = new WorkInfo();
        ClientPayLoad sendPayload = new ClientPayLoad();
        sendPayload.setTag(Constants.PayloadTags.WORK_STATUS);

        try {
            ClientPayLoad receivedPayload = PayloadConverter.fromPayload(payload);
            if (receivedPayload.getTag().equals(Constants.PayloadTags.WORK_DATA)) {
                setStatusText("Working now", true);

                WorkData workData = (WorkData) receivedPayload.getData();
                int dotProduct = MatrixDS.getDotProduct(workData.getRows(), workData.getCols());

                Log.d("WORKER_COMPUTATION", "Partition Index: " + workData.getPartitionIndex());
                if (!finishedWork.contains(workData.getPartitionIndex())) {
                    finishedWork.add(workData.getPartitionIndex());
                }
                currentPartitionIndex = workData.getPartitionIndex();

                setPartitionText(finishedWork.size());
                workStatus.setPartitionIndexInfo(workData.getPartitionIndex());
                workStatus.setResultInfo(dotProduct);

                workStatus.setStatusInfo(Constants.WorkStatus.WORKING);
                sendPayload.setData(workStatus);
                DataTransfer.sendPayload(getApplicationContext(), masterId, sendPayload);

            } else if (receivedPayload.getTag().equals(Constants.PayloadTags.FAREWELL)) {
                // end measuring energy level
                finalEnergyWorker =
                        mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
                energyConsumedWorker = Math.abs(initialEnergyWorker-finalEnergyWorker);
                onWorkFinished("Work Done !!");
                Log.d("WORKER_COMPUTATION", "Work Done");
                workStatus.setStatusInfo(Constants.WorkStatus.FINISHED);
                sendPayload.setData(workStatus);
                DataTransfer.sendPayload(getApplicationContext(), masterId, sendPayload);
                deviceStatsPublisher.stop();

            } else if (receivedPayload.getTag().equals(Constants.PayloadTags.DISCONNECTED)) {
                navBack();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}

