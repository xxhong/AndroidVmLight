/*
 *    Copyright 2018 huangyz0918
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.watermark.androidwm;

import android.content.Context;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.watermark.androidwm.bean.WatermarkImage;
import com.watermark.androidwm.bean.WatermarkText;

import java.util.List;

/**
 * The main class for watermark processing library.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class Watermark {
    private WatermarkText watermarkText;
    private WatermarkImage watermarkImg;
    private Bitmap backgroundImg;
    private Context context;
    private List<WatermarkImage> wmBitmapList;
    private List<WatermarkText> wmTextList;
    private Bitmap outputImage;

    /**
     * Constructors for WatermarkImage
     */
    Watermark(@NonNull Context context,
              @NonNull Bitmap backgroundImg,
              @Nullable WatermarkImage watermarkImg,
              @Nullable List<WatermarkImage> wmBitmapList,
              @Nullable WatermarkText inputText,
              @Nullable List<WatermarkText> wmTextList) {

        this.context = context;
        this.watermarkImg = watermarkImg;
        this.wmBitmapList = wmBitmapList;
        this.backgroundImg = backgroundImg;
        this.wmTextList = wmTextList;
        this.watermarkText = inputText;

        createWatermarkImage();
        createWatermarkText();

    }

    /**
     * Getters for those attrs.
     */
    public Context getContext() {
        return context;
    }

    public WatermarkText getInputText() {
        return watermarkText;
    }

    public Bitmap getBackgroundImg() {
        return backgroundImg;
    }

    public WatermarkImage getWatermarkImg() {
        return watermarkImg;
    }

    public List<WatermarkImage> getWmBitmapList() {
        return wmBitmapList;
    }

    public List<WatermarkText> getWmTextList() {
        return wmTextList;
    }

    /**
     * Creating the composite image with {@link WatermarkImage}.
     * This method cannot be called outside.
     */
    private void createWatermarkImage() {
        Paint watermarkPaint = new Paint();
        if (watermarkImg != null) {
            watermarkPaint.setAlpha(watermarkImg.getAlpha());
            Bitmap newBitmap = Bitmap.createBitmap(backgroundImg.getWidth(),
                    backgroundImg.getHeight(), backgroundImg.getConfig());
            Canvas watermarkCanvas = new Canvas(newBitmap);
            watermarkCanvas.drawBitmap(backgroundImg, 0, 0, null);

            Bitmap scaledWMBitmap = resizeBitmap();
            scaledWMBitmap = adjustPhotoRotation(scaledWMBitmap,
                    (int) watermarkImg.getPosition().getRotation());
            watermarkCanvas.drawBitmap(scaledWMBitmap,
                    (float) watermarkImg.getPosition().getPositionX() * backgroundImg.getWidth(),
                    (float) watermarkImg.getPosition().getPositionY() * backgroundImg.getHeight(),
                    watermarkPaint);
            outputImage = newBitmap;
        }

    }

    /**
     * Creating the composite image with  {@link WatermarkText}.
     * This method cannot be called outside.
     */
    private void createWatermarkText() {
        Paint watermarkPaint = new Paint();
        if (watermarkText != null) {
            watermarkPaint.setColor(watermarkText.getColor());
            watermarkPaint.setStyle(watermarkText.getStyle());
            watermarkPaint.setAlpha(watermarkText.getAlpha());
            watermarkPaint.setTextSize((float) watermarkText.getSize());

            Bitmap newBitmap = Bitmap.createBitmap(backgroundImg.getWidth(),
                    backgroundImg.getHeight(), backgroundImg.getConfig());
            Canvas watermarkCanvas = new Canvas(newBitmap);

            watermarkCanvas.drawBitmap(backgroundImg, 0, 0, null);
            watermarkCanvas.drawPaint(watermarkPaint);

            watermarkCanvas.drawText(watermarkText.getText(),
                    (float) watermarkText.getPosition().getPositionX() * backgroundImg.getWidth(),
                    (float) watermarkText.getPosition().getPositionY() * backgroundImg.getHeight(),
                    watermarkPaint);
            outputImage = newBitmap;
        }
    }

    /**
     * interface for getting the watermark bitmap.
     *
     * @return {@link Bitmap} in watermark.
     */
    public Bitmap getWatermarkBitmap() {
        return watermarkImg.getImage();
    }

    /**
     * interface for getting the watermark text.
     *
     * @return {@link Bitmap} in watermark.
     */
    public String getWatermarkText() {
        return watermarkText.getText();
    }

    /**
     * You can use this function to set the composite
     * image into an ImageView.
     *
     * @param target the target {@link ImageView}.
     */
    public void setToImageView(ImageView target) {
        target.setImageBitmap(outputImage);
    }

    /**
     * Adjust the rotation of a bitmap.
     *
     * @param bitmap           input bitmap.
     * @param orientationAngle the orientation angle.
     * @return {@link Bitmap} the new bitmap.
     */
    private Bitmap adjustPhotoRotation(Bitmap bitmap, final int orientationAngle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(orientationAngle,
                (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        bitmap = Bitmap.createBitmap(bitmap,
                0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    /**
     * this method is for image resizing, we should get
     * the size from the input {@link WatermarkImage}
     * objects, and, set the size from 0 to 1 ,which means:
     * size = watermarkImageWidth / backgroundImageWidth
     *
     * @return {@link Bitmap} the new bitmap.
     */
    private Bitmap resizeBitmap() {
        Bitmap bitmap = watermarkImg.getImage();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleWidth = (float) (backgroundImg.getWidth() * watermarkImg.getSize()) / bitmapWidth;
        float scaleHeight = (float) (bitmap.getHeight() / bitmap.getWidth()) * scaleWidth;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmapWidth, bitmapHeight, matrix, true);
    }

    /**
     * resize image form local resources, we need to resize it first
     * to speed up computer processing time.
     *
     * @return {@link Bitmap} the bitmap return.
     */
//    private Bitmap resizeLocalPicture(String localPath) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(localPath, options);
//        double radio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);
//        options.inSampleSize = (int) Math.ceil(radio);
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(localPath, options);
//    }

}
