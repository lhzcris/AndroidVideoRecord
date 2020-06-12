package com.example.video.record

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import com.smart.android.utils.DisplayUtil
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.camera2.listener.OnCameraResultAdapter
import com.smart.android.vrecord.ui.CBaseActivity
import com.smart.android.vrecord.ui.CameraPreviewFragment
import com.smart.android.vrecord.ui.PreViewOnChanagerListener
import kotlinx.android.synthetic.main.activity_take_picture.*

class TakePictureActivity : CBaseActivity(), PreViewOnChanagerListener {

    override fun layout() = R.layout.activity_take_picture

    var fragment: CameraPreviewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragment = supportFragmentManager.findFragmentByTag("cx") as CameraPreviewFragment?
            ?: CameraPreviewFragment.newInstance()

        fragment?.setPreViewOnChanagerListener(this)
        fragment?.setOnCameraResultListener(object : OnCameraResultAdapter() {
            override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                super.onPhotoTaken(bytes, filePath)
                ToastUtils.showShort("path=$filePath")
                stopScan()
                //todo 自己实现获取数据方式
            }
        })

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment, fragment!!, "cx").commitNow()


        tv_take.setOnClickListener {
            fragment?.takePicture()
        }
    }


    fun playScan() {
        val height = DisplayUtil.getScreenHeight(this)

        val animation: TranslateAnimation = TranslateAnimation(0F, 0F, height / 4F, height * 2 / 3F)
        animation?.apply {
            duration = 1500
            repeatCount = -1
            repeatMode = Animation.REVERSE
            interpolator = LinearInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    tv_scan.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {
                    tv_scan.visibility = View.VISIBLE
                }

            })
        }

        tv_scan.animation = animation
        animation.start()
    }


    fun stopScan() {
        tv_scan.animation?.apply {
            cancel()
            null
        }
    }

    override fun onPreviewResume() {
        playScan()
    }

    override fun onPreviewPause() {
        stopScan()
    }

    override fun progressChangeListener(duration: Int) {
        //do nothing
    }

}
