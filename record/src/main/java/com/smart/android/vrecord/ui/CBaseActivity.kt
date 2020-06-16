package com.smart.android.vrecord.ui

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.smart.android.utils.ActivityStackManager

/**
 * @author liuhuazhong
 * @since 20200610
 */
abstract class CBaseActivity : AppCompatActivity() {

    protected abstract fun layout(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE) // 去掉标题栏
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) // 设置全屏
        ActivityStackManager.getInstance().addActivity(this)
        if (layout() > 0) {
            setContentView(layout())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityStackManager.getInstance().remove(this)
    }


}