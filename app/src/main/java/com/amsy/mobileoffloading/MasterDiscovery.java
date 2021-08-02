package com.amsy.mobileoffloading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.amsy.mobileoffloading.adapters.ConnectedDevicesAdapter;
import com.amsy.mobileoffloading.callback.ClientConnectionListener;
import com.amsy.mobileoffloading.callback.PayloadListener;
import com.amsy.mobileoffloading.entities.ClientPayLoad;
import com.amsy.mobileoffloading.entities.ConnectedDevice;
import com.amsy.mobileoffloading.entities.DeviceStatistics;
import com.amsy.mobileoffloading.helper.Constants;
//import com.amsy.mobileoffloading.helper.FlushToFile;
//import com.amsy.mobileoffloading.helper.MatrixDS;
import com.amsy.mobileoffloading.helper.PayloadConverter;
import com.amsy.mobileoffloading.services.Connector;
import com.amsy.mobileoffloading.services.MasterDiscoveryService;
import com.amsy.mobileoffloading.services.NearbyConnectionsManager;
import com.amsy.mobileoffloading.services.WorkAllocator;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MasterDiscovery extends AppCompatActivity {

    private RecyclerView rvConnectedDevices;
    private ConnectedDevicesAdapter connectedDevicesAdapter;
    private List<ConnectedDevice> connectedDevices = new ArrayList<>();
    private MasterDiscoveryService masterDiscoveryService;
    private ClientConnectionListener clientConnectionListener;
    private PayloadListener payloadListener;

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("Stopped", false);
        masterDiscoveryService.stop();
        NearbyConnectionsManager.getInstance(getApplicationContext()).unregisterPayloadListener(payloadListener);
        NearbyConnectionsManager.getInstance(getApplicationContext()).unregisterClientConnectionListener(clientConnectionListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMasterDiscovery();
        NearbyConnectionsManager.getInstance(getApplicationContext()).registerPayloadListener(payloadListener);
        NearbyConnectionsManager.getInstance(getApplicationContext()).registerClientConnectionListener(clientConnectionListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_discovery);

        rvConnectedDevices = findViewById(R.id.rv_connected_devices);
        connectedDevicesAdapter = new ConnectedDevicesAdapter(this, connectedDevices);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvConnectedDevices.setLayoutManager(linearLayoutManager);

        rvConnectedDevices.setAdapter(connectedDevicesAdapter);
        connectedDevicesAdapter.notifyDataSetChanged();

        payloadListener = new PayloadListener() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                Log.d("MASTER_DISCOVERY", "PayloadListener -  onPayloadReceived");
                try {
                    ClientPayLoad tPayload = PayloadConverter.fromPayload(payload);
                    if (tPayload.getTag().equals(Constants.PayloadTags.DEVICE_STATS)) {
                        updateDeviceStats(endpointId, (DeviceStatistics) tPayload.getData());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
                Log.d("MASTER_DISCOVERY", "PayloadListener -  onPayloadTransferUpdate");
            }
        };


        clientConnectionListener = new ClientConnectionListener() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onConnectionInitiated");
                NearbyConnectionsManager.getInstance(getApplicationContext()).acceptConnection(endpointId);
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution connectionResolution) {

                Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onConnectionResult" + endpointId);

                int statusCode = connectionResolution.getStatus().getStatusCode();
                if (statusCode == ConnectionsStatusCodes.STATUS_OK) {
                    Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onConnectionResult - ACCEPTED");
                    updateConnectedDeviceRequestStatus(endpointId, Constants.RequestStatus.ACCEPTED);
                } else if (statusCode == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
                    Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onConnectionResult - REJECTED");
                    updateConnectedDeviceRequestStatus(endpointId, Constants.RequestStatus.REJECTED);
                } else if (statusCode == ConnectionsStatusCodes.STATUS_ERROR) {
                    Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onConnectionResult - ERROR");
                    removeConnectedDevice(endpointId, true);
                }
            }

            @Override
            public void onDisconnected(String endpointId) {
                Log.d("MASTER_DISCOVERY", "clientConnectionListener -  onDisconnected " + endpointId);
                removeConnectedDevice(endpointId, true);
            }
        };


    }


    public void assignTasks(View view) {
        ArrayList<ConnectedDevice> readyDevices = getDevicesInReadyState();
        if (readyDevices.size() == 0) {
            Toast.makeText(getApplicationContext(), "No worker Available at the moment", Toast.LENGTH_LONG).show();
            onBackPressed();
        } else {
            masterDiscoveryService.stop();
            startMasterActivity(readyDevices);
            finish();
        }
    }

    private ArrayList<ConnectedDevice> getDevicesInReadyState() {
        ArrayList<ConnectedDevice> res = new ArrayList<>();
        for (int i = 0; i < connectedDevices.size(); i++) {
            if (connectedDevices.get(i).getRequestStatus().equals(Constants.RequestStatus.ACCEPTED)) {
                if (connectedDevices.get(i).getDeviceStats().getBatteryLevel() > WorkAllocator.ThresholdsHolder.MINIMUM_BATTERY_LEVEL) {
                    res.add(connectedDevices.get(i));
                } else {
                    ClientPayLoad tPayload = new ClientPayLoad();
                    tPayload.setTag(Constants.PayloadTags.DISCONNECTED);

                    Connector.sendToDevice(getApplicationContext(), connectedDevices.get(i).getEndpointId(), tPayload);
                }
            } else {
                Log.d("MASTER_DISCOVERY", "LOOPING");
                ClientPayLoad tPayload = new ClientPayLoad();
                tPayload.setTag(Constants.PayloadTags.DISCONNECTED);

                Connector.sendToDevice(getApplicationContext(), connectedDevices.get(i).getEndpointId(), tPayload);
            }

        }
        return res;
    }

    private void updateConnectedDeviceRequestStatus(String endpointId, String status) {
        for (int i = 0; i < connectedDevices.size(); i++) {
            if (connectedDevices.get(i).getEndpointId().equals(endpointId)) {
                connectedDevices.get(i).setRequestStatus(status);
                Log.d("MASTER_DISCOVERY", "Status of end point set to "+status);
                connectedDevicesAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void startMasterDiscovery() {
                Log.d("MASTER_DISCOVERY", "Starting Master Discovery");
        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                Log.d("MASTER_DISCOVERY", "ENDPOINT FOUND " +endpointId);
                Log.d("MASTER_DISCOVERY", endpointId);
                Log.d("MASTER_DISCOVERY", discoveredEndpointInfo.getServiceId() + " " + discoveredEndpointInfo.getEndpointName());

                ConnectedDevice connectedDevice = new ConnectedDevice();
                connectedDevice.setEndpointId(endpointId);
                connectedDevice.setEndpointName(discoveredEndpointInfo.getEndpointName());
                connectedDevice.setRequestStatus(Constants.RequestStatus.PENDING);
                connectedDevice.setDeviceStats(new DeviceStatistics());

                connectedDevices.add(connectedDevice);
                connectedDevicesAdapter.notifyItemChanged(connectedDevices.size() - 1);

                Log.d("MASTER_DISCOVERY", "Added end point to connected devices : " +endpointId);

                NearbyConnectionsManager.getInstance(getApplicationContext()).requestConnection(endpointId, "MASTER");
                Log.d("MASTER_DISCOVERY", "Requested connection for : " +endpointId);

            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                Log.d("MASTER_DISCOVERY", "ENDPOINT LOST");
                Log.d("MASTER_DISCOVERY", endpointId);
                removeConnectedDevice(endpointId, false);
            }
        };

        masterDiscoveryService = new MasterDiscoveryService(this);
        masterDiscoveryService.start(endpointDiscoveryCallback)
                .addOnSuccessListener((unused) -> {
                    setStatus("Searching...", true);
                })
                .addOnFailureListener(command -> {
                    if (((ApiException) command).getStatusCode() == 8002) {
                        setStatus("Still Searching...", true);
                    } else {
                        setStatus("Discovering Failed", false);
                        finish();
                    }
                    command.printStackTrace();
                });
        ;
    }

    private void removeConnectedDevice(String endpointId, boolean forceRemove) {

        for (int i = 0; i < connectedDevices.size(); i++) {
            boolean checkStatus = forceRemove ? true :  !connectedDevices.get(i).getRequestStatus().equals(Constants.RequestStatus.ACCEPTED);
            if (connectedDevices.get(i).getEndpointId().equals(endpointId) && checkStatus) {
                Log.d("MASTER_DISCOVERY", "Removed end point from connected devices " + endpointId );
                connectedDevices.remove(i);
                connectedDevicesAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void updateDeviceStats(String endpointId, DeviceStatistics deviceStats) {
        canAssign(deviceStats);
        for (int i = 0; i < connectedDevices.size(); i++) {
            if (connectedDevices.get(i).getEndpointId().equals(endpointId)) {
                connectedDevices.get(i).setDeviceStats(deviceStats);

//                Toast.makeText(getApplicationContext(), "Success: updated battery level: can proceed", Toast.LENGTH_SHORT).show();
                connectedDevices.get(i).setRequestStatus(Constants.RequestStatus.ACCEPTED);
                connectedDevicesAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    void canAssign(DeviceStatistics deviceStats) {
        Button assignButton = findViewById(R.id.assignTask);
        assignButton.setVisibility(deviceStats.getBatteryLevel() > WorkAllocator.ThresholdsHolder.MINIMUM_BATTERY_LEVEL ? View.VISIBLE : View.INVISIBLE);
    }

    void setStatus(String text, boolean search) {
        TextView disc = findViewById(R.id.discovery);
        disc.setText(text);
        ProgressBar pb = findViewById(R.id.progressBar);
        pb.setIndeterminate(search);
    }

    @Override
    public void finish() {
        super.finish();
        masterDiscoveryService.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startMasterActivity(ArrayList<ConnectedDevice> connectedDevices) {
        Intent intent = new Intent(getApplicationContext(), MasterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.CONNECTED_DEVICES, connectedDevices);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

}