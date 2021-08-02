package com.amsy.mobileoffloading.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.Task;

public class MasterDiscoveryService {

    private Context context;
    private DiscoveryOptions discoveryOptions;

    public MasterDiscoveryService(Context context) {
        this.context = context;

        this.discoveryOptions =
                new DiscoveryOptions.Builder()
                        .setStrategy(Strategy.P2P_CLUSTER)
                        .build();
    }

    public Task<Void> start(EndpointDiscoveryCallback endpointDiscoveryCallback) {
      return Nearby.getConnectionsClient(context)
                .startDiscovery(context.getPackageName(), endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener((unused) -> {
                    Log.d("MASTER", "DISCOVERY IN PROGRESS");
                })
                .addOnFailureListener((Exception e) -> {
                    Log.d("MASTER", "DISCOVERING FAILED");
                    e.printStackTrace();
                });
    }

    public void stop() {
        Nearby.getConnectionsClient(context).stopDiscovery();
    }
}