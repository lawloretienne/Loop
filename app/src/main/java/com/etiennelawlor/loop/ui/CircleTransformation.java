//package com.etiennelawlor.loop.ui;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapShader;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//
//import com.squareup.picasso.Transformation;
//
//import java.util.UUID;
//
//
//public class CircleTransformation implements // region Interfaces
//Transformation
//// endregion
//{
//
//    // region Member Variables
//    private int mBackgroundColor;
//    private int mBorderColor;
//    // endregion
//
//    // region Constructors
//    public CircleTransformation(int backgroundColor, int borderColor)
//    {
//        this.mBackgroundColor = backgroundColor;
//        this.mBorderColor = borderColor;
//    }
//
//    public CircleTransformation(int backgroundColor)
//    {
//        this.mBackgroundColor = backgroundColor;
//    }
//    // endregion
//
//    // region Transformation methods
//    @Override
//    public Bitmap transform(Bitmap source) {
//        int size = Math.min(source.getWidth(), source.getHeight());
//
//        Bitmap squaredBitmap = Bitmap.createBitmap(source, 0, 0, size, size);
//        if (squaredBitmap != source) {
//            source.recycle();
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(220, 220, source.getConfig());
//        float radius = (200)/2f;
//        Canvas canvas = new Canvas(bitmap);
//
//        if(mBorderColor != 0)
//        {
//            Paint borderPaint = new Paint();
//            borderPaint.setColor(mBorderColor);
//            borderPaint.setAntiAlias(true);
//            canvas.drawCircle(radius + 10, radius + 10, radius + 10, borderPaint);
//        }
//
//        Paint backgroundPaint = new Paint();
//        backgroundPaint.setColor(mBackgroundColor);
//        backgroundPaint.setAntiAlias(true);
//
//        canvas.drawCircle(radius + 10, radius + 10, radius, backgroundPaint );
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        canvas.drawCircle(radius + 10, radius + 10, radius, paint);
//        squaredBitmap.recycle();
//        return bitmap;
//    }
//
//    @Override
//    public String key() {
//        return UUID.randomUUID().toString();
//    }
//    // endregion
//}