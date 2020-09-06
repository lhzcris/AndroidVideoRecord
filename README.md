

#引用
```groovy
implementation "com.smart.android:videorecord:0.1.3"
```

##参数

1。默认采用1080x1920的分辨率，暂无法动态修改分辨率
2。支持最大录制时间配置，单位毫秒
2。可配置预览尺寸

##使用

```
    1。默认样式的视频拍摄ø

    VideoRecordPicker.getInstance().recordBuilder =
            VideoRecordPicker.RecordBuilder().setMaxDuration((30 * 1000).toLong())

     VideoRecordPicker.getInstance().setFinishListener { videoPath ->
        ........
      }

    2。使用add CameraPreviewFragment 自定义ui交互
        参考：TakePictureActivity,TakeVideoActivity

```

