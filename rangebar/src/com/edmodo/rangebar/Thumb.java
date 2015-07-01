/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package com.edmodo.rangebar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Range;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Represents a thumb in the RangeBar slider. This is the handle for the slider
 * that is pressed and slid.
 */
class Thumb {

    // Private Constants ///////////////////////////////////////////////////////

    // The radius (in dp) of the touchable area around the thumb. We are basing
    // this value off of the recommended 48dp Rhythm. See:
    // http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm
    private static final float MINIMUM_TARGET_RADIUS_DP = 24;

    // Sets the default values for radius, normal, pressed if circle is to be
    // drawn but no value is given.
    private static final float DEFAULT_THUMB_RADIUS_DP = 9;

    // Corresponds to android.R.color.holo_blue_light.
    private static final int DEFAULT_THUMB_COLOR_NORMAL = 0xff33b5e5;
    private static final int DEFAULT_THUMB_COLOR_PRESSED = 0xff33b5e5;

    // Member Variables ////////////////////////////////////////////////////////

    // Radius (in pixels) of the touch area of the thumb.
    private final float mTargetRadiusPx;


    // Variables to store half the width/height for easier calculation.
    private final float mHalfWidthNormal;

    private final float mHalfWidthPressed;
    private final RangeBar bar;

    // Indicates whether this thumb is currently pressed and active.
    private boolean mIsPressed = false;

    // The y-position of the thumb in the parent view. This should not change.
    private final float mY;

    // The current x-position of the thumb in the parent view.
    private float mX;

    // mPaint to draw the thumbs if attributes are selected
    private Paint mPaintNormal;
    private Paint mPaintPressed;

    // Radius of the new thumb if selected
    private float mThumbRadiusPx;

    // Colors of the thumbs if they are to be drawn
    private int mThumbColorNormal;
    private int mThumbColorPressed;

    private ValueAnimator currentAnimator = null;
    private ValueAnimator.AnimatorUpdateListener animatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mThumbRadiusPx = (Float)valueAnimator.getAnimatedValue();
            bar.invalidate();
        }
    };

    // Constructors ////////////////////////////////////////////////////////////

    Thumb(RangeBar bar,
          float y,
          int thumbColorNormal,
          int thumbColorPressed,
          float thumbRadiusDP) {

        final Resources res = bar.getContext().getResources();

        this.bar = bar;

        // If one of the attributes are set, but the others aren't, set the
        // attributes to default
        if (thumbRadiusDP == -1)
            mThumbRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                       DEFAULT_THUMB_RADIUS_DP,
                                                       res.getDisplayMetrics());
        else
            mThumbRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                       thumbRadiusDP,
                                                       res.getDisplayMetrics());

        if (thumbColorNormal == -1)
            mThumbColorNormal = DEFAULT_THUMB_COLOR_NORMAL;
        else
            mThumbColorNormal = thumbColorNormal;

        if (thumbColorPressed == -1)
            mThumbColorPressed = DEFAULT_THUMB_COLOR_PRESSED;
        else
            mThumbColorPressed = thumbColorPressed;

        // Creates the paint and sets the Paint values
        mPaintNormal = new Paint();
        mPaintNormal.setColor(mThumbColorNormal);
        mPaintNormal.setAntiAlias(true);

        mPaintPressed = new Paint();
        mPaintPressed.setColor(mThumbColorPressed);
        mPaintPressed.setAntiAlias(true);

        mHalfWidthNormal = mThumbRadiusPx;

        mHalfWidthPressed = mThumbRadiusPx * 1.5f;

        // Sets the minimum touchable area, but allows it to expand based on
        // image size
        int targetRadius = (int) Math.max(MINIMUM_TARGET_RADIUS_DP, thumbRadiusDP);

        mTargetRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                    targetRadius,
                                                    res.getDisplayMetrics());

        mX = mHalfWidthNormal;
        mY = y;
    }

    // Package-Private Methods /////////////////////////////////////////////////

    float getHalfWidth() {
        return mHalfWidthNormal;
    }

    void setX(float x) {
        mX = x;
    }

    float getX() {
        return mX;
    }

    boolean isPressed() {
        return mIsPressed;
    }

    void press() {
        mIsPressed = true;
        if(currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = ValueAnimator.ofFloat(mThumbRadiusPx, mHalfWidthPressed).setDuration(150);
        currentAnimator.addUpdateListener(animatorListener);
        currentAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        currentAnimator.start();
    }

    void release() {
        mIsPressed = false;
        if(currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = ValueAnimator.ofFloat(mThumbRadiusPx, mHalfWidthNormal).setDuration(150);
        currentAnimator.addUpdateListener(animatorListener);
        currentAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        currentAnimator.start();
    }

    /**
     * Determines if the input coordinate is close enough to this thumb to
     * consider it a press.
     * 
     * @param x the x-coordinate of the user touch
     * @param y the y-coordinate of the user touch
     * @return true if the coordinates are within this thumb's target area;
     *         false otherwise
     */
    boolean isInTargetZone(float x, float y) {

        if (Math.abs(x - mX) <= mTargetRadiusPx && Math.abs(y - mY) <= mTargetRadiusPx) {
            return true;
        }
        return false;
    }

    /**
     * Draws this thumb on the provided canvas.
     * 
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *            View#onDraw()}
     */
    void draw(Canvas canvas) {
        // Otherwise use a circle to display.
        canvas.drawCircle(mX, mY, mThumbRadiusPx, mPaintPressed);
    }
}
