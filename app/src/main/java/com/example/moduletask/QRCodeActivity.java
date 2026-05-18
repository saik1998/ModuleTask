package com.example.moduletask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MainActivity — entry point that wires together the full print flow:
 *
 *   [Print Button] → Request Permissions → Show Device List
 *       → Connect to Printer → Generate QR → Print
 *
 * Replace SAMPLE_QR_CONTENT with real data (order ID, URL, etc.)
 * and swap in your custom QR layout view if needed.
 */
public class QRCodeActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // ── Replace with real data ────────────────────────────────────────────────
    private static final String SAMPLE_QR_CONTENT = "https://example.com/order/12345";
    private static final int    QR_SIZE_PX         = 300;   // pixels
    // ─────────────────────────────────────────────────────────────────────────

    private BluetoothPrinterManager printerManager;
    private BluetoothAdapter        bluetoothAdapter;

    // UI
    private Button   btnPrint;
    private TextView tvStatus;
    private ImageView ivQrPreview;

    // Permissions required on Android 12+ (API 31+)
    private static final String[] PERMISSIONS_API31 = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
    };

    // Permissions required below Android 12
    private static final String[] PERMISSIONS_LEGACY = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,   // required to discover devices
    };

    // ── Activity Result Launchers ─────────────────────────────────────────────

    /** Requests multiple permissions at once. */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionsResult);

    /** Asks the user to enable Bluetooth if it is off. */
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showDeviceDialog();
                } else {
                    setStatus("Bluetooth must be enabled to print.");
                }
            });

    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        printerManager   = new BluetoothPrinterManager(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnPrint    = findViewById(R.id.btnPrint);
        tvStatus    = findViewById(R.id.tvStatus);
        ivQrPreview = findViewById(R.id.ivQrPreview);

        // Show a preview of the QR code that will be printed
        Bitmap preview = QRCodeHelper.generate(SAMPLE_QR_CONTENT, QR_SIZE_PX);
        if (preview != null) ivQrPreview.setImageBitmap(preview);

        btnPrint.setOnClickListener(v -> onPrintClicked());
    }

    // ── Step 1 — Print button clicked ─────────────────────────────────────────

    private void onPrintClicked() {
        if (bluetoothAdapter == null) {
            setStatus("This device does not support Bluetooth.");
            return;
        }
        requestBluetoothPermissions();
    }

    // ── Step 2 — Request permissions ─────────────────────────────────────────

    private void requestBluetoothPermissions() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PERMISSIONS_API31
                : PERMISSIONS_LEGACY;

        List<String> missing = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                missing.add(perm);
            }
        }

        if (missing.isEmpty()) {
            // All permissions already granted
            onPermissionsGranted();
        } else {
            setStatus("Requesting Bluetooth permissions…");
            permissionLauncher.launch(missing.toArray(new String[0]));
        }
    }

    private void onPermissionsResult(@NonNull Map<String, Boolean> results) {
        boolean allGranted = true;
        for (Boolean granted : results.values()) {
            if (!granted) { allGranted = false; break; }
        }
        if (allGranted) {
            onPermissionsGranted();
        } else {
            setStatus("Bluetooth permissions denied. Cannot print.");
            Toast.makeText(this,
                    "Please grant Bluetooth permissions in Settings.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onPermissionsGranted() {
        if (!bluetoothAdapter.isEnabled()) {
            // Ask user to turn on Bluetooth
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableIntent);
        } else {
            showDeviceDialog();
        }
    }

    // ── Step 3 — Show device list dialog ─────────────────────────────────────

    private void showDeviceDialog() {
        setStatus("Select a Bluetooth printer…");
        BluetoothDeviceDialog dialog = BluetoothDeviceDialog.newInstance();
        dialog.setDeviceSelectionListener(this::onDeviceSelected);
        dialog.show(getSupportFragmentManager(), "BT_DEVICE_DIALOG");
    }

    // ── Step 4 — Connect to selected device ───────────────────────────────────

    private void onDeviceSelected(BluetoothDevice device) {
        String name = getDeviceDisplayName(device);
        setStatus("Connecting to " + name + "…");
        btnPrint.setEnabled(false);

        printerManager.connect(device, new BluetoothPrinterManager.PrinterCallback() {

            @Override
            public void onConnected(BluetoothDevice connectedDevice) {
                runOnUiThread(() -> {
                    setStatus("Connected to " + name + ". Printing…");
                    printQRCode();
                });
            }

            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(() -> {
                    setStatus("Connection failed: " + error);
                    btnPrint.setEnabled(true);
                });
            }

            @Override public void onPrintSuccess() {
                runOnUiThread(() -> {
                    setStatus("✓ QR code printed successfully.");
                    btnPrint.setEnabled(true);
                    printerManager.disconnect();
                });
            }

            @Override public void onPrintFailed(String error) {
                runOnUiThread(() -> {
                    setStatus("Print failed: " + error);
                    btnPrint.setEnabled(true);
                });
            }

            @Override public void onDisconnected() {
                runOnUiThread(() -> setStatus("Disconnected."));
            }
        });
    }

    // ── Step 5 — Generate and print the QR code ───────────────────────────────

    /**
     * Toggle this flag if nothing prints:
     *   false = GS v 0  (raster, works on most modern 58mm/80mm printers)
     *   true  = ESC *   (column format, for older or budget thermal printers)
     */
    private static final boolean USE_ESC_STAR_FALLBACK = false;

    private void printQRCode() {
        // ── Option A: Plain QR bitmap ──────────────────────────────────────
        Bitmap qrBitmap = QRCodeHelper.generate(SAMPLE_QR_CONTENT, QR_SIZE_PX);

        // ── Option B: QR + labels composite ───────────────────────────────
        // Bitmap qrBitmap = QRCodeHelper.generateWithLabels(
        //         SAMPLE_QR_CONTENT, QR_SIZE_PX, "My Company", "Order #12345");

        // ── Option C: Your custom layout rendered to Bitmap ────────────────
        // View customView = getLayoutInflater().inflate(R.layout.your_qr_layout, null);
        // // ... populate customView fields ...
        // Bitmap qrBitmap = ViewToBitmapUtil.render(customView, 384, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (qrBitmap == null) {
            setStatus("Failed to generate QR code.");
            btnPrint.setEnabled(true);
            return;
        }

        BluetoothPrinterManager.PrinterCallback printCallback =
                new BluetoothPrinterManager.PrinterCallback() {
                    @Override public void onConnected(BluetoothDevice d) {

                    }
                    @Override public void onConnectionFailed(String e)   {}
                    @Override public void onDisconnected()               {}

                    @Override
                    public void onPrintSuccess() {
                        runOnUiThread(() -> {
                            setStatus("✓ QR code printed successfully.");
                            btnPrint.setEnabled(true);
                            printerManager.disconnect();
                        });
                    }

                    @Override
                    public void onPrintFailed(String error) {
                        runOnUiThread(() -> {
                            setStatus("Print failed: " + error);
                            btnPrint.setEnabled(true);
                        });
                    }
                };

        // Dispatch to the correct ESC/POS image command based on the flag above
        if (USE_ESC_STAR_FALLBACK) {
            printerManager.printQRCodeBitmapEscStar(qrBitmap, printCallback);
        } else {
            printerManager.printQRCodeBitmap(qrBitmap, printCallback);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("MissingPermission")
    private String getDeviceDisplayName(BluetoothDevice device) {
        try {
            String name = device.getName();
            return (name != null && !name.isEmpty()) ? name : device.getAddress();
        } catch (SecurityException e) {
            return device.getAddress();
        }
    }

    private void setStatus(String message) {
        Log.d(TAG, message);
        tvStatus.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (printerManager != null) printerManager.disconnect();
    }
}