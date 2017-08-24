package com.pengyuu.banner.holder;

import android.content.Context;
import android.view.View;

/**
 * Description: BannerViewHolder
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public interface BannerViewHolder<T> {

    /**
     * 创建
     *
     * @param context
     * @return
     */
    View createView(Context context);

    /**
     * 绑定数据
     *
     * @param context
     * @param position
     * @param data
     */
    void onBind(Context context, int position, T data);
}
