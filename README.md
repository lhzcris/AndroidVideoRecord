视频录制，


#引用
```groovy
implementation "com.smart.android:videorecord:0.0.2"
```
#参数
1。默认采用1080x1920的分辨率，暂无法动态修改分辨率
2。支持最大录制时间配置，单位毫秒

#使用
```
    VideoRecordPicker.getInstance().recordBuilder =
            VideoRecordPicker.RecordBuilder().setMaxDuration((30 * 1000).toLong())

     VideoRecordPicker.getInstance().setFinishListener { videoPath ->
        ........
      }

```

