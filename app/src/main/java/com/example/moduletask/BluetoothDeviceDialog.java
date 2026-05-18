package com.example.moduletask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * BottomSheetDialogFragment that shows paired devices and scans for new ones.
 * Fires onDeviceSelected() when the user taps a device row.
 */
public class BluetoothDeviceDialog extends BottomSheetDialogFragment {

    public interface DeviceSelectionListener {
        void onDeviceSelected(BluetoothDevice device);
    }

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceAdapter deviceAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceSelectionListener selectionListener;

    private ProgressBar progressBar;
    private TextView tvScanStatus;
    private Button btnScan;

    // BroadcastReceiver to catch newly discovered Bluetooth devices
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    deviceAdapter.notifyItemInserted(deviceList.size() - 1);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                onScanFinished();
            }
        }
    };

    public static BluetoothDeviceDialog newInstance() {
        return new BluetoothDeviceDialog();
    }

    public void setDeviceSelectionListener(DeviceSelectionListener listener) {
        this.selectionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bluetooth_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar  = view.findViewById(R.id.progressBar);
        tvScanStatus = view.findViewById(R.id.tvScanStatus);
        btnScan      = view.findViewById(R.id.btnScan);
        RecyclerView recyclerView = view.findViewById(R.id.rvDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceAdapter = new BluetoothDeviceAdapter(deviceList, device -> {
            stopDiscovery();
            if (selectionListener != null) {
                selectionListener.onDeviceSelected(device);
            }
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(deviceAdapter);

        btnScan.setOnClickListener(v -> startDiscovery());

        // Register receiver for discovery events
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireContext().registerReceiver(discoveryReceiver, filter);

        // Pre-populate with already-paired devices
        loadPairedDevices();

        // Auto-start scanning
        startDiscovery();
    }

    @SuppressWarnings("MissingPermission")
    private void loadPairedDevices() {
        if (bluetoothAdapter == null) return;
        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired != null && !paired.isEmpty()) {
            deviceList.addAll(paired);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startDiscovery() {
        if (bluetoothAdapter == null) return;
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        progressBar.setVisibility(View.VISIBLE);
        tvScanStatus.setText(R.string.scanning_for_devices);
        btnScan.setEnabled(false);
        bluetoothAdapter.startDiscovery();
    }

    @SuppressWarnings("MissingPermission")
    private void stopDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void onScanFinished() {
        progressBar.setVisibility(View.GONE);
        btnScan.setEnabled(true);
        tvScanStatus.setText(deviceList.isEmpty()
                ? getString(R.string.no_devices_found)
                : getString(R.string.devices_found, deviceList.size()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopDiscovery();
        try {
            requireContext().unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver was not registered
        }
    }
}
