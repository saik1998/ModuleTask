package com.example.moduletask;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Manages Bluetooth printer connection and ESC/POS printing operations.
 * Handles socket lifecycle, image rasterization, and command sending.
 */
public class BluetoothPrinterManager {

    private static final String TAG = "BluetoothPrinterManager";

    // Standard SPP (Serial Port Profile) UUID used by most Bluetooth printers
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // ESC/POS command constants
    private static final byte[] ESC_INIT           = {0x1B, 0x40};             // Initialize printer
    private static final byte[] ALIGN_CENTER       = {0x1B, 0x61, 0x01};       // Center alignment
    private static final byte[] ALIGN_LEFT         = {0x1B, 0x61, 0x00};       // Left alignment
    private static final byte[] LINE_FEED          = {0x0A};                    // Line feed
    private static final byte[] PAPER_CUT          = {0x1D, 0x56, 0x41, 0x10}; // Partial cut

    // How many bytes to write per chunk — prevents buffer overrun on slow printers
    private static final int CHUNK_SIZE = 512;
    // Delay between chunks in ms — give the printer time to process
    private static final int CHUNK_DELAY_MS = 10;

    private final Context context;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public interface PrinterCallback {
        void onConnected(BluetoothDevice device);
        void onConnectionFailed(String error);
        void onPrintSuccess();
        void onPrintFailed(String error);
        void onDisconnected();
    }

    public BluetoothPrinterManager(Context context) {
        this.context = context;
    }

    /**
     * Connects to a Bluetooth printer device on a background thread.
     */
    public void connect(BluetoothDevice device, PrinterCallback callback) {
        new Thread(() -> {
            try {
                if (!hasBluetoothPermission()) {
                    callback.onConnectionFailed("Bluetooth permission not granted.");
                    return;
                }

                // Close any existing connection first
                disconnect();

                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                Log.d(TAG, "Connected to: " + device.getName());
                callback.onConnected(device);

            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                callback.onConnectionFailed("Failed to connect: " + e.getMessage());
                closeSocket();
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception: " + e.getMessage());
                callback.onConnectionFailed("Permission denied: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Sends a QR code bitmap to the connected printer.
     *
     * Strategy: try GS v 0 (raster) first — works on most 58mm/80mm printers.
     * If your printer still produces nothing, call printQRCodeBitmapEscStar() instead,
     * which uses the older ESC * column-format command.
     *
     * Data is written in small chunks with a short delay between them to prevent
     * the printer's internal buffer from overflowing on slow Bluetooth links.
     */
    public void printQRCodeBitmap(Bitmap bitmap, PrinterCallback callback) {
        if (outputStream == null) {
            callback.onPrintFailed("Printer not connected.");
            return;
        }

        new Thread(() -> {
            try {
                writeBytes(ESC_INIT);           // Reset printer state
                Thread.sleep(200);              // Give printer time to initialise
                writeBytes(ALIGN_CENTER);

                byte[] imageData = bitmapToGsV0(bitmap);
                writeChunked(imageData);        // Send image in safe-sized chunks

                writeBytes(LINE_FEED);
                writeBytes(LINE_FEED);
                writeBytes(LINE_FEED);
                writeBytes(PAPER_CUT);

                outputStream.flush();
                Thread.sleep(300);             // Wait for printer to drain its buffer

                Log.d(TAG, "Print job sent successfully.");
                callback.onPrintSuccess();

            } catch (IOException e) {
                Log.e(TAG, "Print failed: " + e.getMessage());
                callback.onPrintFailed("Print failed: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                callback.onPrintFailed("Print interrupted.");
            }
        }).start();
    }

    /**
     * Alternative print method using ESC * (bit-image column format).
     * Use this if printQRCodeBitmap() connects fine but nothing comes out of the printer.
     * Some cheaper/older thermal printers only support this command.
     */
    public void printQRCodeBitmapEscStar(Bitmap bitmap, PrinterCallback callback) {
        if (outputStream == null) {
            callback.onPrintFailed("Printer not connected.");
            return;
        }

        new Thread(() -> {
            try {
                writeBytes(ESC_INIT);
                Thread.sleep(200);
                writeBytes(ALIGN_CENTER);

                byte[] imageData = bitmapToEscStar(bitmap);
                writeChunked(imageData);

                writeBytes(LINE_FEED);
                writeBytes(LINE_FEED);
                writeBytes(LINE_FEED);
                writeBytes(PAPER_CUT);

                outputStream.flush();
                Thread.sleep(300);

                Log.d(TAG, "ESC* print job sent successfully.");
                callback.onPrintSuccess();

            } catch (IOException e) {
                Log.e(TAG, "ESC* print failed: " + e.getMessage());
                callback.onPrintFailed("Print failed: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                callback.onPrintFailed("Print interrupted.");
            }
        }).start();
    }

    // ── Write helpers ─────────────────────────────────────────────────────────

    /** Writes a small command byte array directly. */
    private void writeBytes(byte[] data) throws IOException {
        outputStream.write(data);
    }

    /**
     * Writes a large byte array in CHUNK_SIZE pieces with a short sleep between
     * each chunk. This prevents buffer overrun on printers with small receive buffers.
     */
    private void writeChunked(byte[] data) throws IOException, InterruptedException {
        int offset = 0;
        while (offset < data.length) {
            int end = Math.min(offset + CHUNK_SIZE, data.length);
            outputStream.write(data, offset, end - offset);
            outputStream.flush();
            Thread.sleep(CHUNK_DELAY_MS);
            offset = end;
        }
    }

    // ── Image encoding: GS v 0 (raster) ──────────────────────────────────────

    /**
     * Converts a Bitmap into ESC/POS GS v 0 raster image bytes.
     * Command format:  1D 76 30 m xL xH yL yH [pixel data]
     *   m  = 0x00  (normal / single density)
     *   xL/xH = width in bytes (little-endian)
     *   yL/yH = height in dots (little-endian)
     *
     * Each byte in pixel data encodes 8 horizontal pixels, MSB = leftmost.
     * A set bit (1) prints a black dot; a clear bit (0) prints white.
     */
    private byte[] bitmapToGsV0(Bitmap original) {
        Bitmap bitmap = scaleBitmapForPrinter(original);
        int width      = bitmap.getWidth();
        int height     = bitmap.getHeight();
        int widthBytes = (width + 7) / 8;

        byte[] header = {
                0x1D, 0x76, 0x30, 0x00,
                (byte)(widthBytes & 0xFF), (byte)((widthBytes >> 8) & 0xFF),
                (byte)(height     & 0xFF), (byte)((height     >> 8) & 0xFF)
        };

        byte[] pixels = rasterize(bitmap, width, height, widthBytes);

        byte[] result = new byte[header.length + pixels.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(pixels, 0, result, header.length, pixels.length);
        return result;
    }

    // ── Image encoding: ESC * (column/bit-image) ─────────────────────────────

    /**
     * Converts a Bitmap into ESC/POS ESC * column-format image bytes.
     * Sends one 24-dot-tall horizontal stripe at a time.
     * Command per stripe:  1B 2A 21 nL nH [column data × (nL + nH×256)]
     *
     * This is the fallback for printers that don't support GS v 0.
     */
    private byte[] bitmapToEscStar(Bitmap original) {
        Bitmap bitmap    = scaleBitmapForPrinter(original);
        int width        = bitmap.getWidth();
        int height       = bitmap.getHeight();
        int stripeHeight = 24;    // 24-dot mode (ESC * mode 33 = 0x21)

        // Set line spacing to 24 dots so stripes tile without gaps
        byte[] setLineSpacing24 = {0x1B, 0x33, 0x18};
        // Reset to default line spacing after image
        byte[] resetLineSpacing = {0x1B, 0x32};

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            out.write(setLineSpacing24);

            for (int y = 0; y < height; y += stripeHeight) {
                int dotsThisStripe = Math.min(stripeHeight, height - y);

                // ESC * header: mode=33 (24-dot double density), nL, nH
                out.write(new byte[]{0x1B, 0x2A, 0x21,
                        (byte)(width & 0xFF), (byte)((width >> 8) & 0xFF)});

                // Column data: 3 bytes per column (24 dots = 3 × 8 bits)
                for (int x = 0; x < width; x++) {
                    for (int b = 0; b < 3; b++) {
                        byte colByte = 0;
                        for (int bit = 0; bit < 8; bit++) {
                            int row = y + b * 8 + bit;
                            if (row < height) {
                                int pixel = bitmap.getPixel(x, row);
                                int lum   = luminance(pixel);
                                if (lum < 128) {
                                    colByte |= (byte)(1 << (7 - bit));
                                }
                            }
                        }
                        out.write(colByte);
                    }
                }
                out.write(LINE_FEED);
            }

            out.write(resetLineSpacing);
        } catch (IOException ignored) { /* ByteArrayOutputStream never throws */ }

        return out.toByteArray();
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /**
     * Scales a bitmap down to fit within the printer's printable width (384px for 58mm,
     * 576px for 80mm). Width is always made a multiple of 8 to avoid half-byte padding issues.
     */
    private Bitmap scaleBitmapForPrinter(Bitmap original) {
        int maxWidth = 384;  // change to 576 for 80mm printers
        int w = original.getWidth();
        int h = original.getHeight();

        if (w > maxWidth) {
            h = (int)((float) h * maxWidth / w);
            w = maxWidth;
        }
        // Round width down to nearest multiple of 8
        w = (w / 8) * 8;
        if (w == 0) w = 8;

        return Bitmap.createScaledBitmap(original, w, h, true);
    }

    /** Builds a packed 1-bit-per-pixel raster array from a bitmap. */
    private byte[] rasterize(Bitmap bitmap, int width, int height, int widthBytes) {
        byte[] data = new byte[widthBytes * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (luminance(bitmap.getPixel(x, y)) < 128) {
                    int byteIdx = y * widthBytes + (x / 8);
                    int bitIdx  = 7 - (x % 8);
                    data[byteIdx] |= (byte)(1 << bitIdx);
                }
            }
        }
        return data;
    }

    /** Returns the luminance (0–255) of an ARGB pixel. */
    private int luminance(int pixel) {
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8)  & 0xFF;
        int b =  pixel        & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }

    /**
     * Disconnects from the current Bluetooth printer.
     */
    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing output stream: " + e.getMessage());
        }
        closeSocket();
    }

    private void closeSocket() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED;
    }
}