
package android.pattern.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class BitmapUtil {

    /**
     * Load bitmap from file, and covert the size to fit the dest rectangle
     * 
     * @param path The filename to open
     * @param maxWidth The destination rectangle width
     * @param maxHeight The destination rectangle height
     * @return The new bitmap generated, null if meet some error
     */
    static public Bitmap loadBitmapToFitRect(String path, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if (bounds.outWidth == -1) {
            return null;
        }

        int width = bounds.outWidth;
        int height = bounds.outHeight;
        boolean withinBounds = width <= maxWidth && height <= maxHeight;
        if (!withinBounds) {
            float sampleSizeF = Math.max((float)width / (float)maxWidth, (float)height / (float)maxHeight);

            int sampleSize = Math.round(sampleSizeF);
            BitmapFactory.Options resample = new BitmapFactory.Options();
            resample.inSampleSize = sampleSize;
            resample.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeFile(path, resample);
        } else {
            bitmap = BitmapFactory.decodeFile(path, null);
        }
        return bitmap;
    }

    /**
     * rotate the bitmap to some angle
     * 
     * @param oldBitmap The old Bitmap
     * @param angle The angle wanted to rotate
     * @return The new Bitmap
     */
    static public Bitmap rotateBitmap(Bitmap oldBitmap, int angle) {
        Matrix vMatrix = new Matrix();
        vMatrix.setRotate(angle);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), vMatrix,
                        true);
        return newBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, final float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    /**
     * Resize a bitmap as the caller expected
     * 
     * @param bitmap: the bitmap that will be resized
     * @param sWidth: the expect resize width
     * @param sHeight: the expect resize height
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap bitmap, int sWidth, int sHeight) {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();

        // 缩放图片的尺寸
        float scaleWidth = (float)sWidth / bmpWidth; // 按固定大小缩放 sWidth 写多大就多大
        float scaleHeight = (float)sHeight / bmpHeight; //

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 产生缩放后的Bitmap对象
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);

        return resizeBitmap;
    }

    /**
     * Save bitmap into sdcard
     * 
     * @param filename :file name of saved bitmap
     * @param bitmap :the bitmap to save
     * @param format :bitmap format
     */
    public static String saveBitmapToSdcard(String filename, Bitmap bitmap, CompressFormat format) {
        try {
            File sdCard = new File(filename);
            if (sdCard.exists()) {
                sdCard.delete();
            }
            FileOutputStream outStreamz = new FileOutputStream(sdCard);
            bitmap.compress(format, 100, outStreamz);
            outStreamz.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return filename;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        // canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
    //
    //        Bitmap bitmap = Bitmap.createBitmap(width, height,
    //                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
    //        Canvas canvas = new Canvas(bitmap);
    //        // canvas.setBitmap(bitmap);
    //        drawable.setBounds(0, 0, width, height);
    //        drawable.draw(canvas);
    //        return bitmap;
    //    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static Bitmap getBitmapFromResources(Resources res, int resID) {
        return BitmapFactory.decodeResource(res, resID);
    }

    public static Bitmap getBitmapFromSDcard(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
}
