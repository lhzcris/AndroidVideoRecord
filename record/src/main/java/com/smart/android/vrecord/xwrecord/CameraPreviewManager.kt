package com.smart.android.vrecord.xwrecord

import com.smart.android.vrecord.camera2.CameraVideo
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener
import com.smart.android.vrecord.ui.PreViewOnChanagerListener

/**
 * @author liuhuazhong
 * @since 20201209
 */
class CameraPreviewManager {

    var mLastClickTime = 0L
    val TIME_INTERVAL = 800L
    /**权限 */
    private var isPermission = false
    /**
     * act不在被覆盖时 是否执行 mCameraVideo.onPause()；
     */
    private var is2Pause = false
    private var mCameraVideo: CameraVideo? = null

    private var preViewOnChanagerListener: PreViewOnChanagerListener? = null

    private var resultListener: OnCameraResultListener? = null

    private var outPath: String? = null

    fun isHasPermission() = isPermission

    fun getOutPath() = outPath

    fun setIs2Pause(pause: Boolean) {
        is2Pause = pause
    }

    fun setPreViewOnChanagerListener(listener: PreViewOnChanagerListener?) {
        this.preViewOnChanagerListener = listener
    }


    fun setOnCameraResultListener(resultListener: OnCameraResultListener) {
        this.resultListener = resultListener
    }


}