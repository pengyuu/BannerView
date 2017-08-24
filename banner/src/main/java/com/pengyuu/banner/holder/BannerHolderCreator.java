package com.pengyuu.banner.holder;

/**
 * Description:
 * Created by PengYu on 2017/8/24.
 * Version: v1.0
 */

public interface BannerHolderCreator<VH extends BannerViewHolder> {

    /**
     * 创建ViewHolder
     *
     * @return
     */
    VH createViewHolder();
}
