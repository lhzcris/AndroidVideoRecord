package com.example.video.record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.marginTop
import com.smart.android.vrecord.camera2.RecordVideoActivity
import com.smart.android.vrecord.camera2.TestActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addView("录像", RecordVideoActivity::class.java)

        addView("拍照", TestActivity::class.java)
    }

    fun addView(name: String, clazz: Class<*>) {
        var button = Button(this)
        button.text = name
        button.setOnClickListener({ startActivity(Intent(this, clazz)) })
        button.setPadding(0, 40, 0, 0)
        var lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)
        button.layoutParams = lp

        lladd.addView(button)

    }

}
