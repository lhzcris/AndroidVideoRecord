package com.smart.android.vrecord.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.smart.android.vrecord.R
import com.smart.android.vrecord.camera2.CameraVideo
import com.smart.android.vrecord.camera2.CameraVideoManager
import kotlinx.android.synthetic.main.cx_fragment_camera_preview.*

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

    /**
     * 预览画面
     */
//    private val mTextureView: AutoFitTextureView? = null

    /**权限 */
    private var isPermission = false
    /**
     * 是否正在录制视频
     */
    private val isRecording = false
    /**
     * act不在被覆盖时 是否执行 mCameraVideo.onPause()；
     */
    private var is2Pause = false
    private var mCameraVideo: CameraVideo? = null

    private var preViewOnChanagerListener: PreViewOnChanagerListener? = null


    fun setPreViewOnChanagerListener(listener: PreViewOnChanagerListener?) {
        this.preViewOnChanagerListener = listener
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
            setOnRecordFinishListener {
                //            stopRecord()
            }
            setOnProgressChangeListener { duration: Int ->
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

    fun startPreview(){
        mCameraVideo?.startPreview()
    }

    fun startRecord(){

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
                Toast.makeText(context, "权限申请失败", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}