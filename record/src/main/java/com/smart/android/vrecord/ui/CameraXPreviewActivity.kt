package com.smart.android.vrecord.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smart.android.vrecord.R

class CameraXPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x_preview)


        val f =
            supportFragmentManager.findFragmentByTag("cx") ?: CameraXPreviewFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragment, f, "cx").commitNow()

    }


}
