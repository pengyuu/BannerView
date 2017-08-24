package com.pengyuu.banner;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Description:由于ViewPager默认的切换速度有点快,因此用一个Scroller来控制切换的速度
 * er实际上ViewPager切换本来就是用Scroller来做的,因此可以通过反射来获取到
 * ViewPager的mScroller属性,然后替换成自己的Scroller
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class ViewPagerScroller extends Scroller {
    private int duration = 800;//ViewPager默认的最大Duration为600,值越大越慢
    private boolean isUseDefaultDuration = false;

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, duration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, isUseDefaultDuration ? duration : this.duration);
    }

    public void setUseDefaultDuration(boolean useDefaultDuration) {
        isUseDefaultDuration = useDefaultDuration;
    }

    public boolean isUseDefaultDuration() {
        return isUseDefaultDuration;
    }

    public int getScrollDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
