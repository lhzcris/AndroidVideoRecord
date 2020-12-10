package com.example.video.record

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ImmersionBar
import com.smart.android.utils.ToastUtils
import com.smart.android.vrecord.OptionSize
import com.smart.android.vrecord.VideoRecordPicker
import com.smart.android.vrecord.listener.CaptureListener
import com.smart.android.vrecord.listener.ClickListener
import com.smart.android.vrecord.listener.JCameraListener
import com.smart.android.vrecord.ui.CBaseActivity
import com.smart.android.vrecord.ui.FullScreenBaseActivity
import kotlinx.android.synthetic.main.activity_take_wx_record.*

class TakeWxRecordActivity : FullScreenBaseActivity() {


    override fun layout() = R.layout.activity_take_wx_record

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VideoRecordPicker.getInstance().recordBuilder?.apply {
            setOptionSize(OptionSize.sizeFull)
        }

        jcameraview.apply {
            setMBarHeight(ImmersionBar.getStatusBarHeight(this@TakeWxRecordActivity))
            setLeftClickListener(ClickListener { finish() })
            setJCameraLisenter(object : JCameraListener {
                override fun recordSuccess(url: String?, firstFrame: Bitmap?) {
                    if (VideoRecordPicker.getInstance().finishListener != null) {
                        VideoRecordPicker.getInstance()
                            .finishListener.onFinishListener(url)
                    }
                    finish()
                }

                override fun captureSuccess(url: String?) {
                    if (VideoRecordPicker.getInstance().finishListener != null) {
                        VideoRecordPicker.getInstance()
                            .finishListener.onFinishListener(url)
                    }
                    finish()
                }
            })

        }

        askPermissions()
    }


    override fun onResume() {
        super.onResume()
        jcameraview.onResume()
    }

    override fun onPause() {
        super.onPause()
        jcameraview.onPause()
    }

    fun initCamera() {
        jcameraview.initCamera()
    }


    var isPermission = false
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
            this, permission
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
                    this,
                    resources.getString(com.smart.android.vrecord.R.string.permission_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}