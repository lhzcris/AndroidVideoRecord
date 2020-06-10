package com.smart.android.vrecord.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smart.android.utils.ActivityStackManager

/**
 * @author liuhuazhong
 * @since 20200610
 */
open class CBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityStackManager.getInstance().addActivity(this)
    }
}