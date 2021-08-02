package com.amsy.mobileoffloading;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;

public class Discovery {

    private Context context;
    private DiscoveryOptions discoveryOptions;

    public Discovery(Context context) {
        this.context = context;
        this.discoveryOptions =  new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
    }

    public void start(EndpointDiscoveryCallback endpointDiscoveryCallback) {
        Nearby.getConnectionsClient(context)
                .startDiscovery(context.getPackageName(), endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener((unused) -> {
                    Log.d("MASTER", "Finding Devices");
                    Log.d("MASTER", unused + "");
                })
                .addOnFailureListener((Exception e) -> {
                    Log.d("MASTER", "Unable to find devices");
                    e.printStackTrace();
                });
    }

    public void stop() {
        Nearby.getConnectionsClient(context).stopDiscovery();
    }
}
