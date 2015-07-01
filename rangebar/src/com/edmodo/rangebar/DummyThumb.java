package com.edmodo.rangebar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;

/**
 * Created by cleme_000 on 24.06.2015.
 */
public class DummyThumb extends Thumb {

    public DummyThumb(RangeBar ctx, float y, int thumbColorNormal, int thumbColorPressed, float thumbRadiusDP) {
        super(ctx, y, thumbColorNormal, thumbColorPressed, thumbRadiusDP);
    }

    @Override
    void setX(float x) {

    }

    @Override
    float getX() {
        return 0;
    }

    @Override
    boolean isPressed() {
        return false;
    }

    @Override
    void press() {

    }

    @Override
    void release() {

    }

    @Override
    boolean isInTargetZone(float x, float y) {
        return false;
    }

    @Override
    void draw(Canvas canvas) {
    }
}
