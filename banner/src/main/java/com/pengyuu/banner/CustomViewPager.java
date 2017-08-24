package com.pengyuu.banner;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class CustomViewPager extends ViewPager {
    private ArrayList<Integer> childCenterXAbs = new ArrayList<>();
    private SparseArray<Integer> childIndex = new SparseArray<>();

    public CustomViewPager(Context context) {
        super(context);
        init();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context);
        init();
    }

    private void init() {
        setClipToPadding(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * 第n个位置的child的绘制索引
     *
     * @param childCount
     * @param i
     * @return
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == 0 || childIndex.size() != childCount) {
            childCenterXAbs.clear();
            childIndex.clear();
            int viewCenterX = getViewCenterX(this);

            for (int j = 0; j < childCount; j++) {
                int indexAbs = Math.abs(viewCenterX - getViewCenterX(this));
                //两个距离相同,后来的那个做自增,从而保持abs不同
                if (childIndex.get(indexAbs) != null) {
                    ++indexAbs;
                }
                childCenterXAbs.add(indexAbs);
                childIndex.append(indexAbs, j);
            }
            Collections.sort(childCenterXAbs);
        }
        //哪个item距离中心点远一些,就先draw它.(最近的就是中间放大的item,最后draw)
        return childIndex.get(childCenterXAbs.get(childCount - 1 - i));
    }

    private int getViewCenterX(View view) {
        int[] array = new int[2];
        view.getLocationOnScreen(array);
        return array[0] + view.getWidth() / 2;
    }
}
