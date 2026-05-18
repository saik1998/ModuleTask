package com.example.moduletask;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter that displays paired and discovered Bluetooth devices.
 * Each row shows the device name, MAC address, and bond state icon.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    private final List<BluetoothDevice> devices;
    private final OnDeviceClickListener listener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices, OnDeviceClickListener listener) {
        this.devices  = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDeviceName;
        private final TextView tvDeviceAddress;
        private final ImageView ivBondState;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName    = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress = itemView.findViewById(R.id.tvDeviceAddress);
            ivBondState     = itemView.findViewById(R.id.ivBondState);
        }

        @SuppressWarnings("MissingPermission")
        void bind(BluetoothDevice device, OnDeviceClickListener listener) {
            String name = device.getName();
            tvDeviceName.setText(name != null ? name : "Unknown Device");
            tvDeviceAddress.setText(device.getAddress());

            // Show paired icon if device is already bonded
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//                ivBondState.setImageResource(R.drawable.ic_bluetooth_paired);
                ivBondState.setVisibility(View.VISIBLE);
            } else {
                ivBondState.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }
    }
}
