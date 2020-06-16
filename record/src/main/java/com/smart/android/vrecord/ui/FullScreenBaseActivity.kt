package com.smart.android.vrecord.ui

import android.os.Bundle
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar

/**
 * @author liuhuazhong
 * @since 20200616
 */
abstract class FullScreenBaseActivity : CBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ImmersionBar.with(this).fullScreen(true)
            .transparentNavigationBar()
            .fitsSystemWindows(false)
            .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            .init()
    }

}