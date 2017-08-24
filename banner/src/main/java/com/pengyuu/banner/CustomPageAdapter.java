package com.pengyuu.banner;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.pengyuu.banner.holder.BannerHolderCreator;
import com.pengyuu.banner.holder.BannerViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class CustomPageAdapter<T> extends PagerAdapter {
    private List<T> datas;
    private BannerHolderCreator holderCreator;
    private ViewPager viewPager;
    private boolean canLoop;
    private BannerPageClickListener pageClickListener;
    private final int looperCountFactor = 500;

    public CustomPageAdapter(List<T> datas, BannerHolderCreator holderCreator, boolean canLoop) {
        this.datas = new ArrayList<>();
        if (datas != null) {
            this.datas.addAll(datas);
        }
        this.holderCreator = holderCreator;
        this.canLoop = canLoop;
    }

    public void setPageClickListener(BannerPageClickListener pageClickListener) {
        this.pageClickListener = pageClickListener;
    }

    public void setUpViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        this.viewPager.setAdapter(this);
        this.viewPager.getAdapter().notifyDataSetChanged();
        int currentItem = canLoop ? getStartSelectItem() : 0;
        //摄者当前选中的Item
        this.viewPager.setCurrentItem(currentItem);
    }

    public void setDatas(List<T> datas) {
        if (datas != null) {
            this.datas.clear();
            this.datas.addAll(datas);
        }
    }

    private int getStartSelectItem() {
        //设置当前选中的位置为Integer.MAX_VALUE / 2,这样开始就能往左滑动
        //但是要保证这个值与getRealPosition的余数为0,因为要从第一页开始显示
        int currentItem = getRealCount() * looperCountFactor / 2;
        if (currentItem % getRealCount() == 0) {
            return currentItem;
        }
        while (currentItem % getRealCount() != 0) {
            currentItem++;
        }
        return currentItem;
    }

    @Override
    public int getCount() {
        //如果getCount的返回值为Integer.MAX_VALUE的话,那么在setCurrentItem的时候会ANR(除了在onCreate调用之外)
        return canLoop ? getRealCount() * looperCountFactor : getRealCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(position, container);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (canLoop) {
            int position = viewPager.getCurrentItem();
            if (position == getCount() - 1) {
                position = 0;
                setCurrentItem(position);
            }
        }
    }

    private void setCurrentItem(int position) {
        try {
            viewPager.setCurrentItem(position, false);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取真实的Count
     *
     * @return
     */
    private int getRealCount() {
        return datas == null ? 0 : datas.size();
    }

    private View getView(int position, ViewGroup container) {
        final int realPosition = position % getRealCount();
        BannerViewHolder holder = null;
        holder = holderCreator.createViewHolder();
        if (holder == null) {
            throw new RuntimeException("can not return a null holder");
        }
        View view = holder.createView(container.getContext());

        if (datas != null && datas.size() > 0) {
            holder.onBind(container.getContext(), realPosition, datas.get(realPosition));
        }

        //添加点击事件
        view.setOnClickListener(v -> {
            if (pageClickListener != null) {
                pageClickListener.onPageClick(v, realPosition);
            }
        });
        return view;
    }
}
