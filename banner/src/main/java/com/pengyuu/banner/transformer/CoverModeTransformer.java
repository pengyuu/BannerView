package com.pengyuu.banner.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class CoverModeTransformer implements ViewPager.PageTransformer {
    private float reduceX = 0.0f;
    private float itemWidth = 0;
    private float offsetPosition = 0f;
    private int coverWidth;
    private float scaleMax = 1.0f;
    private float scaleMin = 0.9f;
    private ViewPager viewPager;

    public CoverModeTransformer(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    @Override
    public void transformPage(View page, float position) {
        if (offsetPosition == 0f) {
            float paddingLeft = viewPager.getPaddingLeft();
            float paddingRight = viewPager.getPaddingRight();
            float width = viewPager.getMeasuredWidth();
            offsetPosition = paddingLeft / (width - paddingLeft - paddingRight);
        }

        float currentPosition = position - offsetPosition;
        if (itemWidth == 0) {
            itemWidth = page.getWidth();
            //由于左右边的缩小而减小的x的一般大小
            reduceX = (2.0f - scaleMax - scaleMin) * itemWidth / 2.0f;
        }
        if (currentPosition <= -1.0f) {
            page.setTranslationX(reduceX + coverWidth);
            page.setScaleX(scaleMin);
            page.setScaleY(scaleMin);
        } else if (currentPosition <= 1.0) {
            float scale = (scaleMax - scaleMin) * Math.abs(1.0f - Math.abs(currentPosition));
            float translationX = currentPosition * -reduceX;
            if (currentPosition <= -0.5) {//两个view中间的临界,这时两个view在同一层左侧view需要往X轴正方向移动覆盖的值
                page.setTranslationX(translationX + coverWidth * Math.abs(Math.abs(currentPosition) - 0.5f) / 0.5f);
            } else if (currentPosition <= 0.0f) {
                page.setTranslationX(translationX);
            } else if (currentPosition >= 0.5) {//两个view中间的临界,这时两个view在同一层
                page.setTranslationX(translationX - coverWidth * Math.abs(Math.abs(currentPosition) - 0.5f) / 0.5f);
            } else {
                page.setTranslationX(translationX);
            }
            page.setScaleX(scale + scaleMin);
            page.setScaleY(scale + scaleMin);
        } else {
            page.setScaleX(scaleMin);
            page.setScaleY(scaleMin);
            page.setTranslationX(-reduceX - coverWidth);
        }
    }
}
