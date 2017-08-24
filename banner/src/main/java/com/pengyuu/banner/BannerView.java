package com.pengyuu.banner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pengyuu.banner.holder.BannerHolderCreator;
import com.pengyuu.banner.transformer.CoverModeTransformer;
import com.pengyuu.banner.transformer.ScaleYTransformer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//import android.support.v4.view.PagerAdapter;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public class BannerView<T> extends RelativeLayout {
    private CustomViewPager viewPager;
    private CustomPageAdapter adapter;
    private List<T> datas;
    private boolean isAutoPlay = true;//是否自动播放
    private int currentItem = 0;//当前位置
    private Handler handler = new Handler();
    private int delayedTime = 3000;//Banner切换时间间隔
    private ViewPagerScroller viewPagerScroller;//控制ViewPager滑动速度的Scroller
    private boolean isOpenEffect = true;//是否开启动画效果
    private boolean isCanLoop = true;//是否轮播图片
    private LinearLayout indicatorContainer;//indicator容器
    private ArrayList<ImageView> indicators = new ArrayList<>();
    //indicatorRes[0] 未选中  indicatorRes[1]选中
    private int[] indicatorRes = new int[]{R.drawable.indicator_normal, R.drawable.indicator_selected};
    private int indicatorPaddingLeft = 0;
    private int indicatorPaddingRight = 0;
    private int modePadding = 0;//在开启特效模式下,由于前后显示了上下一个页面的部分，因此需要计算这部分padding
    private int indicatorAlign = 1;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private BannerPageClickListener bannerPageClickListener;

    public enum IndicatorAlign {
        LEFT,//左对齐
        CENTER,//居中
        RIGHT//右对齐
    }

    //中间page是否覆盖两边,默认覆盖
    private boolean isMiddlePageCover = true;

    public BannerView(@NonNull Context context) {
        super(context);
        init();
    }

    public BannerView(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
        readAttrs(context, attrs);
        init();
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(context, attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        readAttrs(context, attrs);
        init();
    }

    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        isOpenEffect = typedArray.getBoolean(R.styleable.BannerView_open_mode, true);
        isMiddlePageCover = typedArray.getBoolean(R.styleable.BannerView_middle_page_cover, true);
        isCanLoop = typedArray.getBoolean(R.styleable.BannerView_canLoop, true);
        indicatorAlign = typedArray.getInt(R.styleable.BannerView_indicatorAlign, 1);
        indicatorPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingLeft, 0);
        indicatorPaddingRight = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingRight, 0);
    }

    private void init() {
        View view = null;
        if (isOpenEffect) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_banner_effect, this, true);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_banner_normal, this, true);
        }
        indicatorContainer = view.findViewById(R.id.banner_indicator_container);
        viewPager = view.findViewById(R.id.vp_banner);
        viewPager.setOffscreenPageLimit(4);

        modePadding = dpToPx(30);
        // 初始化Scroller
        initViewPagerScroll();

        if (indicatorAlign == 0) {
            setIndicatorAlign(IndicatorAlign.LEFT);
        } else if (indicatorAlign == 1) {
            setIndicatorAlign(IndicatorAlign.CENTER);
        } else {
            setIndicatorAlign(IndicatorAlign.RIGHT);
        }
    }

    /**
     * 设置动画效果
     */
    private void setOpenEffect() {
        if (isOpenEffect) {
            if (isMiddlePageCover) {
                //中间页面覆盖两边
                viewPager.setPageTransformer(true, new CoverModeTransformer(viewPager));
            } else {
                //中间页面不覆盖,页面并排,只是Y轴缩小
                viewPager.setPageTransformer(false, new ScaleYTransformer());
            }
        }
    }

    /**
     * 设置ViewPager的滑动速度
     */
    private void initViewPagerScroll() {
        try {
            Field scroller = null;
            scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            viewPagerScroller = new ViewPagerScroller(viewPager.getContext());
            scroller.set(viewPager, viewPagerScroller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private final Runnable loopRunable = new Runnable() {
        @Override
        public void run() {
            if (isAutoPlay) {
                currentItem = viewPager.getCurrentItem();
                currentItem++;
                if (currentItem == adapter.getCount() - 1) {
                    currentItem = 0;
                    viewPager.setCurrentItem(currentItem, false);
                    handler.postDelayed(this, delayedTime);
                } else {
                    viewPager.setCurrentItem(currentItem);
                    handler.postDelayed(this, delayedTime);
                }
            } else {
                handler.postDelayed(this, delayedTime);
            }
        }
    };

    /**
     * 初始化指示器
     */
    private void initIndicator() {
        indicatorContainer.removeAllViews();
        indicators.clear();
        for (int i = 0; i < datas.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            if (indicatorAlign == IndicatorAlign.LEFT.ordinal()) {
                if (i == 0) {
                    int paddingLeft = isOpenEffect ? indicatorPaddingLeft + modePadding : indicatorPaddingLeft;
                    imageView.setPadding(paddingLeft + 6, 0, 6, 0);
                } else {
                    imageView.setPadding(6, 0, 6, 0);
                }
            } else if (indicatorAlign == IndicatorAlign.RIGHT.ordinal()) {
                if (i == datas.size() - 1) {
                    int paddingRight = isOpenEffect ? modePadding + indicatorPaddingRight : indicatorPaddingRight;
                    imageView.setPadding(6, 0, 6 + paddingRight, 0);
                } else {
                    imageView.setPadding(6, 0, 6, 0);
                }
            } else {
                imageView.setPadding(6, 0, 6, 0);
            }

            if (i == (currentItem % datas.size())) {
                imageView.setImageResource(indicatorRes[1]);
            } else {
                imageView.setImageResource(indicatorRes[0]);
            }

            indicators.add(imageView);
            indicatorContainer.addView(imageView);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isCanLoop) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            //按住Banner的时候停止自动轮播
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_DOWN:
                int paddingLeft = viewPager.getLeft();
                float touchX = ev.getRawX();
                //开启动画去除两边的区域
                if (touchX >= paddingLeft && touchX < getScreenWidth(getContext()) - paddingLeft) {
                    isAutoPlay = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                isAutoPlay = true;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        return width;
    }

    /******************************************************************************************************/
    /**                             对外API                                                               **/
    /******************************************************************************************************/
    /**
     * 开始轮播
     * <p>应该确保在调用用了{@link BannerView {@link #setPages(List, BannerHolderCreator)}} 之后调用这个方法开始轮播</p>
     */
    public void start() {
        // 如果Adapter为null, 说明还没有设置数据，这个时候不应该轮播Banner
        if (adapter == null) {
            return;
        }
        if (isCanLoop) {
            isAutoPlay = true;
            handler.postDelayed(loopRunable, delayedTime);
        }
    }

    /**
     * 停止轮播
     */
    public void pause() {
        isAutoPlay = false;
        handler.removeCallbacks(loopRunable);
    }

    /**
     * 设置BannerView的切换时间间隔
     *
     * @param delayedTime
     */
    public void setDelayedTime(int delayedTime) {
        this.delayedTime = delayedTime;
    }

    public void addPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setBannerPageClickListener(BannerPageClickListener bannerPageClickListener) {
        this.bannerPageClickListener = bannerPageClickListener;
    }

    /**
     * 是否显示小圆点指示器
     *
     * @param visible
     */
    public void setIndicatorVisible(boolean visible) {
        if (visible) {
            indicatorContainer.setVisibility(VISIBLE);
        } else {
            indicatorContainer.setVisibility(GONE);
        }
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    /**
     * 设置indicator 图片资源
     *
     * @param unSelectRes 未选中状态资源图片
     * @param selectRes   选中状态资源图片
     */
    public void setIndicatorRes(@DrawableRes int unSelectRes, @DrawableRes int selectRes) {
        indicatorRes[0] = unSelectRes;
        indicatorRes[1] = selectRes;
    }

    public void setPages(List<T> datas, BannerHolderCreator holderCreator) {
        if (datas == null || holderCreator == null) {
            return;
        }
        this.datas = datas;
        //如果在播放,先停止播放
        pause();

        //增加一个逻辑：由于特效模式会在一个页面展示前后页面的部分，因此，数据集合的长度至少为3,否则，自动为普通Banner模式
        //不管配置的:open_mode 属性的值是否为true
        if (datas.size() < 3) {
            isOpenEffect = false;
            MarginLayoutParams layoutParams = (MarginLayoutParams) viewPager.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            viewPager.setLayoutParams(layoutParams);
            setClipChildren(true);
            viewPager.setClipChildren(true);
        }
        setOpenEffect();
        // 将Indicator初始化放在Adapter的初始化之前，解决更新数据变化更新时crush.
        //初始化Indicator
        initIndicator();
        adapter = new CustomPageAdapter<>(datas, holderCreator, isCanLoop);
        adapter.setUpViewPager(viewPager);
        adapter.setPageClickListener(bannerPageClickListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int realPosition = position % indicators.size();
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                currentItem = position;

                //切换indicator
                int realSelectPosition = currentItem % indicators.size();
                for (int i = 0; i < datas.size(); i++) {
                    if (i == realSelectPosition) {
                        indicators.get(i).setImageResource(indicatorRes[1]);
                    } else {
                        indicators.get(i).setImageResource(indicatorRes[0]);
                    }
                }

                // 不能直接将mOnPageChangeListener 设置给ViewPager ,否则拿到的position 否则拿到的position
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(realSelectPosition);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        isAutoPlay = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        isAutoPlay = true;
                        break;
                }
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });
    }

    /**
     * 设置Indicator 的对齐方式
     *
     * @param indicatorAlign {@link IndicatorAlign#CENTER }{@link IndicatorAlign#LEFT }{@link IndicatorAlign#RIGHT }
     */
    public void setIndicatorAlign(IndicatorAlign indicatorAlign) {
        this.indicatorAlign = indicatorAlign.ordinal();
        LayoutParams layoutParams = (LayoutParams) indicatorContainer.getLayoutParams();
        if (indicatorAlign == IndicatorAlign.LEFT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (indicatorAlign == IndicatorAlign.RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        indicatorContainer.setLayoutParams(layoutParams);
    }

    /**
     * 设置ViewPager切换的速度
     *
     * @param duration 切换动画时间
     */
    public void setDuration(int duration) {
        viewPagerScroller.setDuration(duration);
    }

    /**
     * 设置是否使用ViewPager默认是的切换速度
     *
     * @param useDefaultDuration 切换动画时间
     */
    public void setUseDefaultDuration(boolean useDefaultDuration) {
        viewPagerScroller.setUseDefaultDuration(useDefaultDuration);
    }

    /**
     * 获取Banner页面切换动画时间
     *
     * @return
     */
    public int getDuration() {
        return viewPagerScroller.getScrollDuration();
    }


    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
