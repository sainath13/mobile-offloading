package com.amsy.mobileoffloading;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amsy.mobileoffloading.callback.ClientConnectionListener;
import com.amsy.mobileoffloading.helper.Constants;
import com.amsy.mobileoffloading.services.Advertiser;
import com.amsy.mobileoffloading.services.DeviceStatisticsPublisher;
import com.amsy.mobileoffloading.services.NearbyConnectionsManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import eo.view.batterymeter.BatteryMeterView;
import pl.droidsonroids.gif.GifImageView;

public class WorkerAdvertisement extends AppCompatActivity {
    private Advertiser advertiser;
    private String workerId;
    private String masterId = "";
    private ClientConnectionListener connectionListener;
    private Dialog confirmationDialog;
    private DeviceStatisticsPublisher deviceStatsPublisher;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_advertisement);
        workerId = Build.MANUFACTURER + " " + Build.MODEL;
        initialiseDialog();
        //Start Advertisement
        advertiser = new Advertiser(this.getApplicationContext());
        deviceStatsPublisher = new DeviceStatisticsPublisher(getApplicationContext(), null, Constants.UPDATE_INTERVAL_UI);
        setDeviceId("Device ID: " + workerId);
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            refreshCardData();
            handler.postDelayed(runnable, Constants.UPDATE_INTERVAL_UI);
        };
        setConnectionCallback();
    }

    void setConnectionCallback() {
        connectionListener = new ClientConnectionListener() {
            @Override
            public void onConnectionInitiated(String id, ConnectionInfo connectionInfo) {
                Log.d("WORKER", "Connection Received: " + id + " Endpoint name: " + connectionInfo.getEndpointName());
                masterId = id;
                showDialog(connectionInfo.getEndpointName());
            }

            @Override
            public void onConnectionResult(String id, ConnectionResolution connectionResolution) {
                Log.d("WORKER", "Connection Accepted By: " + id + " " + connectionResolution.getStatus().toString());
            }

            @Override
            public void onDisconnected(String id) {
                Log.d("WORKER", "Connection Disconnected: " + id);
                finish();
            }
        };
    }

    void setStatusText(String text, boolean available) {
        TextView st = findViewById(R.id.statusText);
        st.setText(text);
        ImageView online = findViewById(R.id.online);
        online.setVisibility(available ? View.VISIBLE : View.INVISIBLE);
        GifImageView loading = findViewById(R.id.loading);
        loading.setVisibility(available ? View.INVISIBLE : View.VISIBLE);
    }

    void setDeviceId(String text) {
        TextView st = findViewById(R.id.deviceId);
        st.setText(text.toUpperCase());
    }

    void refreshCardData() {
        TextView st = findViewById(R.id.percentage);
        BatteryMeterView bv = findViewById(R.id.batteryMeter);
        bv.setChargeLevel(DeviceStatisticsPublisher.getBatteryLevel(this));
        bv.setCharging(DeviceStatisticsPublisher.isPluggedIn(this));
        st.setText("Percentage: " + DeviceStatisticsPublisher.getBatteryLevel(this) + "%");
        TextView st2 = findViewById(R.id.plugged);
        st2.setText(String.format("Charging Status: %s", DeviceStatisticsPublisher.isPluggedIn(this) ? "Plugged In" : "Not Charging"));
        if (DeviceStatisticsPublisher.getLocation(this) != null) {
            TextView la = findViewById(R.id.latitude);
            la.setText(String.format("Latitude: %s", DeviceStatisticsPublisher.getLocation(this).getLatitude()));
            TextView lo = findViewById(R.id.longitude);
            lo.setText("Longitude: " + DeviceStatisticsPublisher.getLocation(this).getLongitude());
        } else {
            TextView la = findViewById(R.id.latitude);
            la.setText("Latitude: Not Available");
            TextView lo = findViewById(R.id.longitude);
            lo.setText("Longitude: Not Available");
        }
    }


    void initialiseDialog() {
        confirmationDialog = new BottomSheetDialog(this);
        confirmationDialog.setContentView(R.layout.confirmation_dialog);
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.findViewById(R.id.accept).setOnClickListener(v -> acceptConnection());
        confirmationDialog.findViewById(R.id.reject).setOnClickListener(v -> rejectConnection());
    }

    void showDialog(String masterInfo) {
        TextView title = confirmationDialog.findViewById(R.id.dialogText);
        title.setText(String.format("%s is trying to connect. Do you accept the connection ?", masterInfo));
        confirmationDialog.show();
    }

    void acceptConnection() {
        NearbyConnectionsManager.getInstance(getApplicationContext()).acceptConnection(masterId);
        confirmationDialog.dismiss();
        startWorkerComputation();
    }

    void rejectConnection() {
        NearbyConnectionsManager.getInstance(getApplicationContext()).rejectConnection(masterId);
        confirmationDialog.dismiss();
    }

    @Override
    protected void onResume() {
        setStatusText("Initializing...", false);
        super.onResume();
        advertiser.start(workerId).addOnSuccessListener(command -> {
            Log.d("WORKER", "Discoverable by all devices");
            setStatusText("Discoverable by all devices", true);
        }).addOnFailureListener(c -> {
            if (((ApiException) c).getStatusCode() == 8001) {
                Log.d("WORKER", "Discoverable by all devices");
                setStatusText("Discoverable by all devices", true);
            } else {
                setStatusText("Failed to host device", false);
            }
        });
        NearbyConnectionsManager.getInstance(getApplicationContext()).registerClientConnectionListener(connectionListener);
        Log.d("WORKER", "Starting Device Stats");
        deviceStatsPublisher.start();
        handler.postDelayed(runnable, Constants.UPDATE_INTERVAL_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NearbyConnectionsManager.getInstance(getApplicationContext()).unregisterClientConnectionListener(connectionListener);
        Log.d("WORKER", "Stopping Device Stats");
        deviceStatsPublisher.stop();
        handler.removeCallbacks(runnable);
    }

    private void startWorkerComputation() {
        Intent intent = new Intent(getApplicationContext(), WorkerComputation.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.MASTER_ENDPOINT_ID, masterId);
        intent.putExtras(bundle);
        startActivity(intent);
        advertiser.stop();
        Log.d("WORKER", "Device is not discoverable");
        finish();
    }

    @Override
    public void onBackPressed() {
        advertiser.stop();
        Log.d("WORKER", "Device is not discoverable");
        if (!masterId.equals("")) {
            NearbyConnectionsManager.getInstance(getApplicationContext()).disconnectFromEndpoint(masterId);
            NearbyConnectionsManager.getInstance(getApplicationContext()).rejectConnection(masterId);
        }
        finish();
        super.onBackPressed();
    }
}