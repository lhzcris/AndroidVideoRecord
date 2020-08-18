package com.smart.android.vrecord.utils

import android.view.View

/**
 * @author liuhuazhong
 * @since 20200818
 */


const val MIN_DELAY_TIME: Long = 1_000  // 两次点击间隔不能少于1000ms
var lastClickTime = 0L

fun View.isFastDoubleClick(): Boolean {
    val currentClickTime = System.currentTimeMillis()
    val isFastDoubleClick = ((currentClickTime - lastClickTime) < MIN_DELAY_TIME)
    lastClickTime = currentClickTime
    return isFastDoubleClick
}

/**
 * View单击事件(拦截了快速多次点击)
 */
inline fun View.setOnSingleClickListener(
    crossinline onClick: (View) -> Unit
) {
    this.setOnClickListener {
        this.isClickable = false
        onClick(it)
        this.postDelayed({ this.isClickable = true }, MIN_DELAY_TIME)
    }
}