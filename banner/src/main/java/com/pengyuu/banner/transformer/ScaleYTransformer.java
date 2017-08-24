package com.pengyuu.banner.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class ScaleYTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.9f;

    @Override
    public void transformPage(View page, float position) {
        if (position < -1) {
            page.setScaleY(MIN_SCALE);
        } else if (position <= 1) {
            float scale = Math.max(MIN_SCALE, 1 - Math.abs(position));
            page.setScaleY(scale);
        } else {
            page.setScaleY(MIN_SCALE);
        }
    }
}
