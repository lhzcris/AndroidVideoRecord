package com.smart.android.vrecord.ui

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import com.smart.android.vrecord.R
import kotlinx.android.synthetic.main.cx_activity_take_video.*

class TakeVideoActivity : CBaseActivity(), PreViewOnChanagerListener {


    override fun layout() = R.layout.cx_activity_take_video

    /**
     * 视频存放路径
     */
    private val mVideoPath: String? = null
    /**
     * 录制视频时候的计时动画
     */
    private val mAnimator: ObjectAnimator? = null
    /**
     * 是否正在录制视频
     */
    private var isRecording = false
    /**
     * 暂停中
     */
    private var isSuspend = false
    /**
     * 是否有暂停能力
     */
    private var canSuspend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    fun initUI() {

        /**预览fragment*/
        val f = supportFragmentManager.findFragmentByTag("take_vidoe")
            ?: CameraPreviewFragment.newInstance()
        (f as CameraPreviewFragment).setPreViewOnChanagerListener(this)
        supportFragmentManager.beginTransaction().add(R.id.fragment, f, "take_vidoe").commitNow()

        /**暂停录制只有在24以上才生效*/
        canSuspend = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    }

    override fun onPreviewResume() {
        mAnimator?.resume()
    }

    override fun onPreviewPause() {
        rest()
    }




    /*重置*/
    private fun rest() {
        isRecording = false
        isSuspend = false
        iv_record.setImageResource(R.drawable.vr_image_start)
        tv_title.text = ""
        tv_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        if (canSuspend) btn_suspend.visibility = View.GONE
        iv_facing.visibility = View.VISIBLE
        ll_cover.visibility = View.GONE
        mAnimator?.cancel()
    }
}
