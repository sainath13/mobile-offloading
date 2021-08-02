package com.amsy.mobileoffloading.helper;

import android.content.Context;

import com.amsy.mobileoffloading.entities.ClientPayLoad;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.io.IOException;

public class DataTransfer {
    public static void sendPayload(Context context, String endpointId, ClientPayLoad tPayload) {
        try {
            Payload payload = PayloadConverter.toPayload(tPayload);
            Nearby.getConnectionsClient(context).sendPayload(endpointId, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

