package com.amsy.mobileoffloading.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.amsy.mobileoffloading.R;
import com.amsy.mobileoffloading.entities.ConnectedDevice;
import com.amsy.mobileoffloading.helper.Constants;

import java.util.List;

public class ConnectedDevicesAdapter extends RecyclerView.Adapter<ConnectedDevicesAdapter.ViewHolder>{

    private Context context;
    private List<ConnectedDevice> connectedDevices;

    public ConnectedDevicesAdapter(@NonNull Context context, List<ConnectedDevice> connectedDevices) {
        this.context = context;
        this.connectedDevices = connectedDevices;
    }

    @NonNull
    @Override
    public ConnectedDevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.connected_device_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedDevicesAdapter.ViewHolder holder, int position) {
        holder.setClientId(connectedDevices.get(position).getEndpointId(), connectedDevices.get(position).getEndpointName());
        holder.setBatteryLevel(connectedDevices.get(position).getDeviceStats().getBatteryLevel());
        holder.setRequestStatus(connectedDevices.get(position).getRequestStatus());
    }

    @Override
    public int getItemCount() {
        return connectedDevices.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private TextView ClientId;
        private TextView BatteryLevel;
        private ImageView RequestStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ClientId = itemView.findViewById(R.id.client_id);
            BatteryLevel = itemView.findViewById(R.id.battery_level);
            RequestStatus = itemView.findViewById(R.id.request_status_image);
        }

        public void setClientId(String endpointId, String endpointName) {
            this.ClientId.setText(endpointName.toUpperCase()  + "\n(" + endpointId.toUpperCase() + ")");
        }

        public void setBatteryLevel(int batteryLevel) {
            if (batteryLevel > 0 && batteryLevel <= 100) {
                this.BatteryLevel.setText(batteryLevel + "%");
            } else {
                this.BatteryLevel.setText("");
            }
        }

        public void setRequestStatus(String requestStatus) {
            if (requestStatus.equals(Constants.RequestStatus.ACCEPTED)) {
                this.RequestStatus.setBackgroundResource(R.drawable.ic_outline_check_circle_24);
            } else if (requestStatus.equals(Constants.RequestStatus.REJECTED)) {
                this.RequestStatus.setBackgroundResource(R.drawable.ic_outline_cancel_24);
            } else {
                this.RequestStatus.setBackgroundResource(R.drawable.ic_outline_pending_24);
            }
        }
    }
}
