package com.smart.android.vrecord.ui

import android.os.Bundle
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.R

class TakePictureActivity : CBaseActivity() {

    override fun layout() = R.layout.cx_activity_take_picture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val f =
            supportFragmentManager.findFragmentByTag("cx") ?: CameraPreviewFragment.newInstance()

        (f as CameraPreviewFragment).setPreViewOnChanagerListener(object :
            PreViewOnChanagerListener {
            override fun onPreviewResume() {
                ToastUtils.showShort("onResume")
            }

            override fun onPreviewPause() {
                ToastUtils.showShort("onPause")
            }

        })

        supportFragmentManager.beginTransaction().add(R.id.fragment, f, "cx").commitNow()

    }


}
