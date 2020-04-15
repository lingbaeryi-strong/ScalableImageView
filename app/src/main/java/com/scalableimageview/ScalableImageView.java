package com.scalableimageview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScalableImageView extends View {

    private static final int IMAGE_WIDTH = (int) Utils.dpTopixel(500);
    private static final float OVER_SCALE_FACTOR = 1.5f;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap bitmap;
    float originaOffsetx;
    float originaOffsety;
    float offsetx;
    float offsety;
    float smallScale;
    float bigScale;
    float currentScale;
    ObjectAnimator objectAnimator;

    boolean big;
    //移动
    GestureDetectorCompat detector;
    //俩手指缩放放大处理
    ScaleGestureDetector scaleGestureDetector;
    HenGestureListener gestureListener = new HenGestureListener();
    HenScaleListener henScaleListener = new HenScaleListener();
    HenFlingRunner henFlingRunner = new HenFlingRunner();
    OverScroller overScroller;

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmap = Utils.getAvatar(getResources(), IMAGE_WIDTH);
        detector = new GestureDetectorCompat(context, gestureListener);
        scaleGestureDetector = new ScaleGestureDetector(context, henScaleListener);
        overScroller = new OverScroller(context);
    }

    public ScalableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        originaOffsetx = ((float) getWidth() - bitmap.getWidth()) / 2f;
        originaOffsety = ((float) getHeight() - bitmap.getHeight()) / 2f;
        if ((float) bitmap.getWidth() / bitmap.getHeight() > (float) getWidth() / getHeight()) {

            smallScale = (float) getWidth() / bitmap.getWidth();
            bigScale = (float) getHeight() / bitmap.getHeight() * OVER_SCALE_FACTOR;

        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * OVER_SCALE_FACTOR;
        }
        currentScale=smallScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scaleFaction = (currentScale - smallScale) / (bigScale - smallScale);
        canvas.translate(offsetx * scaleFaction, offsety * scaleFaction);//*scaleFaction 解决缩小回到正中间
        canvas.scale(currentScale, currentScale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(bitmap, originaOffsetx, originaOffsety, paint);
    }

    public ObjectAnimator getObjectAnimation() {
        objectAnimator = ObjectAnimator.ofFloat(this, "currentScale", 0);
        objectAnimator.setFloatValues(smallScale, bigScale);
        return objectAnimator;
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            result = detector.onTouchEvent(event);
        }
        return result;
    }

    class HenGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (big) {
                offsetx -= v;
                offsety -= v1;
                fixOffsets();
                invalidate();
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        // 惯性滑动
        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

            if (big) {
                overScroller.fling((int) offsetx, (int) offsety, (int) v, (int) v1,
                        -(int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        -(int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        (int) (bitmap.getHeight() * bigScale - getHeight()) / 2);
                //100,100 回弹效果
                postOnAnimation(henFlingRunner);
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            big = !big;
            if (big) {
                offsetx = (motionEvent.getX() - getWidth() / 2f) - (motionEvent.getX() - getWidth() / 2) * bigScale / smallScale;
                offsety = (motionEvent.getY() - getHeight() / 2f) - (motionEvent.getY() - getHeight() / 2) * bigScale / smallScale;
                getObjectAnimation().start();
            } else {
                getObjectAnimation().reverse();
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    private void fixOffsets() {
        offsetx = Math.min(offsetx, (bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetx = Math.max(offsetx, -(bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsety = Math.min(offsety, (bitmap.getHeight() * bigScale - getHeight()) / 2);
        offsety = Math.max(offsety, -(bitmap.getHeight() * bigScale - getHeight()) / 2);
    }


    class HenFlingRunner implements Runnable {
        @Override
        public void run() {
            if (overScroller.computeScrollOffset()) {
                offsetx = overScroller.getCurrX();
                offsety = overScroller.getCurrY();
                invalidate();
                postOnAnimation(this);
            }
        }
    }


    class HenScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        float initialScale;

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            currentScale = initialScale * scaleGestureDetector.getScaleFactor();
            invalidate();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            initialScale = currentScale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    }

}
