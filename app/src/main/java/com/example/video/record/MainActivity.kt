package com.example.video.record

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import com.smart.android.vrecord.OnRecordFinishListener
import com.smart.android.vrecord.VideoRecordPicker
import com.smart.android.vrecord.ui.RecordVideoActivity
import com.smart.android.vrecord.ui.TestActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        VideoRecordPicker.getInstance().recordBuilder =
            VideoRecordPicker.RecordBuilder().setMaxDuration((30 * 1000).toLong())

        VideoRecordPicker.getInstance().setFinishListener { videoPath ->

            text.text = videoPath

            video_view?.let {
                video_view.stopPlayback()
                video_view.setVideoPath(videoPath)
                video_view.start()
            }

        }

        video_view.setMediaController(MediaController(this))

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


    override fun onDestroy() {
        super.onDestroy()
        VideoRecordPicker.getInstance().clearAll()
    }

}
