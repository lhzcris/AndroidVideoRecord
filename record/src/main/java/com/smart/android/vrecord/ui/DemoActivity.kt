package com.smart.android.vrecord.ui

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.smart.android.vrecord.R


class DemoActivity : AppCompatActivity() {

    var textureView: TextureView? = null

    var mCameraDevice: CameraDevice? = null
    var cameraManager: CameraManager? = null

    var mCameraId = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        init()
    }


    @SuppressLint("MissingPermission")
    fun init() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

//        for (cameraId in cameraManager!!.cameraIdList){
//            print(cameraId)
//            Log.e("sss",cameraId)
//        }
        cameraManager?.openCamera("0", object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice=camera
            }

            override fun onDisconnected(camera: CameraDevice) {
                mCameraDevice?.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                mCameraDevice?.close()
            }

        }, null)

    }


}
