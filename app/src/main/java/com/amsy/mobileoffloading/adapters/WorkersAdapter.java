package com.amsy.mobileoffloading.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amsy.mobileoffloading.R;
import com.amsy.mobileoffloading.entities.Worker;
import com.amsy.mobileoffloading.helper.Constants;

import java.util.List;

import eo.view.batterymeter.BatteryMeterView;

public class WorkersAdapter extends RecyclerView.Adapter<WorkersAdapter.ViewHolder>{
    private Context context;
    private List<Worker> workers;

    public WorkersAdapter(@NonNull Context context, List<Worker> workers) {
        this.context = context;
        this.workers = workers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.item_worker, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setClientId(workers.get(position).getEndpointId(), workers.get(position).getEndpointName());
        holder.setWorkStatus(workers.get(position).getWorkStatus().getStatusInfo());
        holder.setBatteryLevel(workers.get(position).getDeviceStats().getBatteryLevel(), workers.get(position).getDeviceStats().isCharging());
        holder.setWorkFinished(workers.get(position).getWorkAmount());
        holder.setLocation(workers.get(position).getDistanceFromMaster());
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvClientId;
        private TextView tvWorkStatus;
        private TextView tvBatteryLevel;
        private BatteryMeterView tvChargingStatus;
        private TextView tvWorkFinished;
        private TextView tvLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClientId = itemView.findViewById(R.id.tv_client_id);
            tvWorkStatus = itemView.findViewById(R.id.tv_work_status);
            tvBatteryLevel = itemView.findViewById(R.id.tv_battery_level);
            tvChargingStatus = itemView.findViewById(R.id.workerBattery);
            tvWorkFinished = itemView.findViewById(R.id.tv_work_finished);
            tvLocation = itemView.findViewById(R.id.tv_location);

        }

        public void setClientId(String endpointId, String endpointName) {
            this.tvClientId.setText(endpointName.toUpperCase() + " (" + endpointId.toUpperCase() + ")");
        }

        public void setWorkStatus(String workStatus) {
            if (workStatus.equals(Constants.WorkStatus.WORKING)) {
                this.tvWorkStatus.setText("WORKING...");
            } else if (workStatus.equals(Constants.WorkStatus.FINISHED)) {
                this.tvWorkStatus.setText("FINISHED");
            } else if (workStatus.equals(Constants.WorkStatus.FAILED)) {
                this.tvWorkStatus.setText("FAILED");
            } else {
                this.tvWorkStatus.setText("DISCONNECTED");
            }
        }

        public void setBatteryLevel(int batteryLevel, boolean charging) {
            this.tvBatteryLevel.setText(batteryLevel + "%");
            this.tvChargingStatus.setCharging(charging);
            this.tvChargingStatus.setChargeLevel(batteryLevel);
        }

        public void setWorkFinished(int amountWork) {
            tvWorkFinished.setText( amountWork +"");
        }

        public void setLocation(float distance) {
            tvLocation.setText(String.format("%.2f", distance) + " meters");
        }
    }
}
