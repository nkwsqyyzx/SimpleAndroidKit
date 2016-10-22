package space.wsq.kit.devkit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

@SuppressWarnings({"unused"})
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    private static final Object lockForSyncImageDecoder = new Object();

    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (maxWidth <= 0 || maxHeight < 0 || bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        if (bitmap.getWidth() <= maxWidth && bitmap.getHeight() <= maxHeight) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float temp;
        if (((float) maxHeight / (float) height) > (((float) maxWidth) / (float) width)) {
            temp = (((float) maxWidth) / (float) width);
        } else {
            temp = ((float) maxHeight / (float) height);
        }

        synchronized (lockForSyncImageDecoder) {
            Matrix matrix = new Matrix();
            matrix.postScale(temp, temp);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (resizedBitmap != bitmap) {
                // according to createBitmap, maybe the result is the input one
                bitmap.recycle();
            }
            return resizedBitmap;
        }
    }

    public static Bitmap bytes2Bitmap(byte[] b) {
        Bitmap bm = null;
        if (b != null && b.length != 0) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            try {
                synchronized (lockForSyncImageDecoder) {
                    bm = BitmapFactory.decodeByteArray(b, 0, b.length, opt);
                }
            } catch (OutOfMemoryError e) {
                ILogger.defaultLogger.e(TAG, "bites2Bitmap got oom", e);
            } catch (Throwable e) {
                ILogger.defaultLogger.e(TAG, "bites2Bitmap got error", e);
            }
        }
        return bm;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = null;
        try {
            synchronized (lockForSyncImageDecoder) {
                output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(output);
                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                final RectF rectF = new RectF(rect);
                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);

                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);
            }

            if (output != bitmap) {
                bitmap.recycle();
            }
        } catch (Throwable e) {
            ILogger.defaultLogger.e(TAG, "getRoundedCornerBitmap", e);
        }

        return output;
    }

    // The following functions are from Volley

    public static Bitmap decodeFromByteArray(byte[] bytes, final int maxWidth, final int maxHeight, ImageView.ScaleType scaleType, final Bitmap.Config decodeConfig) {
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap;
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = decodeConfig;
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                    actualWidth, actualHeight, scaleType);
            int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                    actualHeight, actualWidth, scaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize =
                    findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap =
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap,
                        desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        return bitmap;
    }

    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary Maximum size of the primary dimension (i.e. width for
     *        max width), or zero to maintain aspect ratio with secondary
     *        dimension
     * @param maxSecondary Maximum size of the secondary dimension, or zero to
     *        maintain aspect ratio with primary dimension
     * @param actualPrimary Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     * @param scaleType The ScaleType used to calculate the needed image size.
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                           int actualSecondary, ImageView.ScaleType scaleType) {
        // If no dominant value at all, just return the actual.
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ImageView.ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    private static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }
}
