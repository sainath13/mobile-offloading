package com.amsy.mobileoffloading.services;

import android.content.Context;

import com.amsy.mobileoffloading.entities.ClientPayLoad;
import com.amsy.mobileoffloading.helper.PayloadConverter;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.io.IOException;

public class Connector {
    public static void sendToDevice(Context context, String endpointId, ClientPayLoad tPayload) {
        try {
            Payload payload = PayloadConverter.toPayload(tPayload);
            Connector.sendToDevice(context, endpointId, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToDevice(Context context, String endpointId, byte[] data) {
        Payload payload = Payload.fromBytes(data);
        Connector.sendToDevice(context, endpointId, payload);
    }

    public static void sendToDevice(Context context, String endpointId, Payload payload) {
        Nearby.getConnectionsClient(context).sendPayload(endpointId, payload);
    }
}
