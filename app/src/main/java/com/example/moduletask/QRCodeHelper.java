package com.example.moduletask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for generating QR code Bitmaps via ZXing.
 * Produces a clean black-and-white bitmap ready for ESC/POS rasterization.
 */
public class QRCodeHelper {

    private QRCodeHelper() {
        // Utility class — no instances
    }

    /**
     * Generates a QR code Bitmap for the given content string.
     *
     * @param content  The text/URL/data to encode.
     * @param sizePx   Width and height of the output bitmap in pixels.
     * @param margin   Quiet-zone margin in modules (ZXing default is 4).
     * @return         A square Bitmap, or null if encoding fails.
     */
    public static Bitmap generate(String content, int sizePx, int margin) {
        if (content == null || content.isEmpty()) return null;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, margin);

        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

            int width  = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Overload with default margin (1 module — tight, good for printing).
     */
    public static Bitmap generate(String content, int sizePx) {
        return generate(content, sizePx, 1);
    }

    /**
     * Renders a QR code onto a larger white Bitmap that includes a top label
     * and a bottom label, matching a typical receipt layout.
     *
     * @param qrContent    Data to encode in the QR.
     * @param qrSizePx     Size of the QR code portion.
     * @param topLabel     Text drawn above the QR (e.g. company name).
     * @param bottomLabel  Text drawn below the QR (e.g. order ID).
     * @return             Composite Bitmap ready to print.
     */
    public static Bitmap generateWithLabels(String qrContent, int qrSizePx,
                                            String topLabel, String bottomLabel) {
        Bitmap qr = generate(qrContent, qrSizePx);
        if (qr == null) return null;

        int padding   = 20;
        int labelHeight = 50;
        int totalHeight = padding + labelHeight + qrSizePx + labelHeight + padding;

        Bitmap result = Bitmap.createBitmap(qrSizePx + padding * 2, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);

        // Top label
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(28f);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        canvas.drawText(topLabel != null ? topLabel : "",
                result.getWidth() / 2f, padding + labelHeight - 10, paint);

        // QR code
        canvas.drawBitmap(qr, padding, padding + labelHeight, null);

        // Bottom label
        paint.setTextSize(24f);
        canvas.drawText(bottomLabel != null ? bottomLabel : "",
                result.getWidth() / 2f, padding + labelHeight + qrSizePx + labelHeight - 10, paint);

        return result;
    }
}
