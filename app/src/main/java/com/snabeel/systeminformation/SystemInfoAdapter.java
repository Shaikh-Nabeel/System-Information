package com.snabeel.systeminformation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SystemInfoAdapter extends  RecyclerView.Adapter<SystemInfoAdapter.ViewHolder> {

    private List<SystemData> list;
    public SystemInfoAdapter(List<SystemData> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.system_rv_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SystemData sd = list.get(position);
        holder.availableMemory.setText(sd.getMemoryEmpty()+" GB");
        holder.lastStatus.setText(sd.getLastBatteryStatus());
        holder.connection.setText("Internet Connection : "+sd.getInternetConnection());
        holder.location.setText(sd.getLocation());
        holder.chargedPercent.setText(sd.getCharged());
        holder.isCharging.setText(sd.getCharging());
        holder.date.setText(Util.getFormattedDate(sd.getDate()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateAdapter(SystemData s){
        list.add(s);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView date, isCharging, chargedPercent, location, connection, lastStatus, availableMemory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateOfInfo);
            isCharging = itemView.findViewById(R.id.batteryCharging2);
            chargedPercent = itemView.findViewById(R.id.batteryPercentage2);
            location = itemView.findViewById(R.id.location2);
            connection = itemView.findViewById(R.id.internetConnection2);
            lastStatus = itemView.findViewById(R.id.lastChangeInChargingStatus2);
            availableMemory = itemView.findViewById(R.id.memoryUtilization2);
        }
    }
}
