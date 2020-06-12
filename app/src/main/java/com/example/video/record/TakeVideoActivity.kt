package com.example.video.record

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.VideoRecordPicker
import com.smart.android.vrecord.camera2.listener.OnCameraResultAdapter
import com.smart.android.vrecord.ui.CBaseActivity
import com.smart.android.vrecord.ui.CameraPreviewFragment
import com.smart.android.vrecord.ui.PreViewOnChanagerListener
import com.smart.android.vrecord.ui.VideoPlayActivity
import kotlinx.android.synthetic.main.activity_take_video_1.*
import java.util.*

class TakeVideoActivity : CBaseActivity(), PreViewOnChanagerListener {


    override fun layout() = R.layout.activity_take_video_1

    /**
     * 录制视频时候的计时动画
     */
    private var mAnimator: ObjectAnimator? = null
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


    var fragment: CameraPreviewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    fun initUI() {

        /**暂停录制只有在24以上才生效*/
        canSuspend = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        /**预览fragment*/
        fragment = supportFragmentManager.findFragmentByTag("take_vidoe") as CameraPreviewFragment?
            ?: CameraPreviewFragment.newInstance()

        fragment?.setPreViewOnChanagerListener(this)
        fragment?.setOnCameraResultListener(object : OnCameraResultAdapter() {
            override fun onVideoRecorded(filePath: String?) {
                super.onVideoRecorded(filePath)
                stopRecord()
                ToastUtils.showShort("file==$filePath")
            }
        })
        supportFragmentManager.beginTransaction().add(R.id.fragment1, fragment!!, "take_vidoe")
            .commitNow()

        btn_suspend.setOnClickListener {
            if (!canSuspend || fragment?.isHasPermission() == false) return@setOnClickListener
            if (isSuspend) {
                fragment?.resumeRecordVideo()
                btn_suspend.text = "暂停"
            } else {
                fragment?.pauseRecordVideo()
                btn_suspend.text = "继续"
            }
            isSuspend = !isSuspend
        }
        iv_record.setOnClickListener {
            if (fragment?.isHasPermission() == false) return@setOnClickListener
            if (isRecording) {
                stopRecord()
            } else {
                startRecord()
            }
        }
        iv_facing.setOnClickListener {
            if (fragment?.isHasPermission() == false) return@setOnClickListener
            fragment?.switchCameraFacing()
        }
        tv_cancle.setOnClickListener { reStartPreview() }
        tv_pick.setOnClickListener {
            if (VideoRecordPicker.getInstance().finishListener != null) {
                VideoRecordPicker.getInstance()
                    .finishListener.onFinishListener(fragment?.getOutPath())
            }
            finish()
        }
        video_play.setOnClickListener {
            fragment?.setIs2Pause(true)
            VideoPlayActivity.start(this, fragment?.getOutPath())
        }


    }

    override fun onPreviewResume() {
        mAnimator?.resume()
    }

    override fun onPreviewPause() {
        rest()
    }

    fun startRecord() {

        isRecording = true
        iv_record.setImageResource(R.drawable.vr_image_pause)
        if (canSuspend) btn_suspend.visibility = View.VISIBLE
        iv_facing.visibility = View.GONE
        ll_cover.visibility = View.GONE
        tv_title.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.vr_drawable_timer_dot, 0, 0, 0
        )
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofFloat(tv_title, "alpha", 1f, 0.4f, 1f)
                .setDuration(1000)
            mAnimator!!.repeatCount = ObjectAnimator.INFINITE
            mAnimator!!.repeatMode = ObjectAnimator.REVERSE
        }
        mAnimator!!.start()

        fragment?.startRecord()
    }

    fun stopRecord() {
        rest()
        ll_cover.visibility = View.VISIBLE
        fragment?.stopRecord()
    }

    override fun progressChangeListener(duration: Int) {
//        mCurrentDuration = duration
        val minute = duration / 60
        val second = duration % 60
        tv_title.text = String.format(
            Locale.getDefault(), "%02d:%02d", minute, second
        )
    }

    fun reStartPreview() {
        rest()
        fragment?.startPreview()
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
