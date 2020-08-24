package com.smart.android.vrecord.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.smart.android.utils.Logger
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.R
import com.smart.android.vrecord.camera2.CameraVideo
import com.smart.android.vrecord.camera2.CameraVideoManager
import com.smart.android.vrecord.camera2.listener.OnCameraResultListener
import kotlinx.android.synthetic.main.cx_fragment_camera_preview.*
import java.io.File


/**
 * @author liuhuazhong
 * @since 20200611
 *
 * 纯预览界面
 */
class CameraPreviewFragment : Fragment() {


    companion object {
        fun newInstance() = CameraPreviewFragment()
    }

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cx_fragment_camera_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askPermissions()
//        VideoRecordPicker.getInstance().finishListener?.onFinishListener(outPath)
    }


    override fun onResume() {
        super.onResume()
        if (isPermission) {
            if (!is2Pause) {
                mCameraVideo!!.onResume()
//                if (mAnimator != null) {
//                    mAnimator.resume()
//                }
                preViewOnChanagerListener?.onPreviewResume()
            }
            is2Pause = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPermission) {
            if (!is2Pause) {
                mCameraVideo!!.onPause()
//                rest()
                preViewOnChanagerListener?.onPreviewPause()
            }
        }
    }

    fun initCamera() {

        mCameraVideo = CameraVideoManager(context)
        mCameraVideo?.apply {
            setAutoFitTextureView(textureView)
            if (resultListener != null)
                setOnCameraResultListener(resultListener)
            setOnProgressChangeListener { duration: Int ->
                preViewOnChanagerListener?.progressChangeListener(duration)
                //            mCurrentDuration = duration
//            val minute = duration / 60
//            val second = duration % 60
//            mTitleView.setText(
//                String.format(
//                    Locale.getDefault(),
//                    "%02d:%02d", minute, second
//                )
//            )
            }
        }

    }

    fun startPreview() {
        mCameraVideo?.startPreview()
    }

    //切换摄像头
    fun switchCameraFacing() {
        mCameraVideo?.switchCameraFacing()
    }

    /**拍摄视频*/
    fun startRecord() {
        outPath = getVideoFilePath(activity!!)
        mCameraVideo?.startRecordingVideo(outPath)
    }

    //继续录制视频
    fun resumeRecordVideo() {
        mCameraVideo?.resumeRecordVideo()
    }

    //暂停录制视频
    fun pauseRecordVideo() {
        mCameraVideo?.pauseRecordVideo()
    }

    fun stopRecord() {
        mCameraVideo?.stopRecordingVideo()
    }


    /**拍照 拒绝快速重复点击*/
    fun takePicture() {
        val nowtime = System.currentTimeMillis()
        if (nowtime - mLastClickTime > TIME_INTERVAL) {
            mLastClickTime = nowtime
            outPath = getPicFilePath(activity!!)
            mCameraVideo?.takePicture(outPath)
        }
    }


    private fun getPicFilePath(context: Context): String? {
        val dir = File(context.getExternalFilesDir(null), "Photo")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/" + System.currentTimeMillis() + ".jpeg"
    }

    private fun getVideoFilePath(context: Context): String? {
        val dir = File(context.getExternalFilesDir(null), "Videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath + "/" + System.currentTimeMillis() + ".mp4"
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.RECORD_AUDIO)) {
                isPermission = true
                initCamera()
            } else {
                this.requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ),
                    1
                )
                isPermission = false
            }
        } else {
            isPermission = true
            initCamera()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) { //执行代码,这里是已经申请权限成功了,可以不用做处理
                isPermission = true
                initCamera()
            } else {
                isPermission = false
                Toast.makeText(
                    context,
                    resources.getString(R.string.permission_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}