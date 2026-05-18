package com.example.moduletask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

/**
 * Utility to render any Android View (including inflated custom layouts)
 * into a Bitmap that can be sent to the ESC/POS printer.
 *
 * Usage example (in MainActivity):
 *
 *   View qrLayout = getLayoutInflater().inflate(R.layout.your_qr_layout, null);
 *   // ... populate TextViews, ImageViews, etc. ...
 *   Bitmap bmp = ViewToBitmapUtil.render(qrLayout, 384, ViewGroup.LayoutParams.WRAP_CONTENT);
 *   printerManager.printQRCodeBitmap(bmp, callback);
 */
public class ViewToBitmapUtil {

    private ViewToBitmapUtil() {}

    /**
     * Measures, lays out, and draws a View onto a new Bitmap.
     *
     * @param view      The view to render (may be an inflated layout).
     * @param widthPx   Desired width in pixels (use 384 for 58mm thermal printers).
     * @param heightPx  Desired height in pixels, or ViewGroup.LayoutParams.WRAP_CONTENT (-2).
     * @return          A Bitmap containing the rendered view, or null on failure.
     */
    public static Bitmap render(View view, int widthPx, int heightPx) {
        if (view == null) return null;

        // Measure
        int widthSpec  = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY);
        int heightSpec = (heightPx == ViewGroup.LayoutParams.WRAP_CONTENT)
                ? View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                : View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY);

        view.measure(widthSpec, heightSpec);

        int measuredWidth  = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        if (measuredWidth <= 0 || measuredHeight <= 0) return null;

        // Layout
        view.layout(0, 0, measuredWidth, measuredHeight);

        // Draw
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(android.graphics.Color.WHITE); // white background for printer
        view.draw(canvas);

        return bitmap;
    }
}
