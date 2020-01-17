package com.smart.android.vrecord;

import java.io.Serializable;

/**
 * @author liuhuazhong
 * @since 20200117
 */
public class VideoRecordPicker {

    private static VideoRecordPicker mInstance;

    private OnRecordFinishListener finishListener;

    private RecordBuilder recordBuilder;

    public static VideoRecordPicker getInstance() {
        if (mInstance == null) {
            synchronized (VideoRecordPicker.class) {
                if (mInstance == null) {
                    mInstance = new VideoRecordPicker();
                }
            }
        }
        return mInstance;
    }

    public void clearAll() {
        finishListener = null;
        recordBuilder = null;
    }

    public long getMaxDuration() {
        return recordBuilder == null ? 0 : recordBuilder.maxDuration;
    }

    public RecordBuilder getRecordBuilder() {
        return recordBuilder;
    }

    public void setRecordBuilder(RecordBuilder recordBuilder) {
        this.recordBuilder = recordBuilder;


    }

    public OnRecordFinishListener getFinishListener() {
        return finishListener;
    }

    public void setFinishListener(OnRecordFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    public static class RecordBuilder implements Serializable {

//        public RecordBuilder DEFEAT = new RecordBuilder().setMaxDuration(30 * 1000);

        private long maxDuration;//最大录制时长

        public long getMaxDuration() {
            return maxDuration;
        }

        public RecordBuilder setMaxDuration(long maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }
    }
}
