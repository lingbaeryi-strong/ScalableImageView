package com.scalableimageview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;

public class Utils {

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static float dpTopixel(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static Bitmap getAvatar(Resources resources, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, R.mipmap.image, options);
        options.inJustDecodeBounds = false;
        options.inDensity = options.outWidth;
        options.inTargetDensity = width;
        return BitmapFactory.decodeResource(resources, R.mipmap.image, options);
    }

    //适配
    public static float getZFormCamera() {
        return -6 * Resources.getSystem().getDisplayMetrics().density;
    }
}
