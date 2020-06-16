package com.smart.android.vrecord;

import android.content.Context;
import android.content.Intent;

import com.smart.android.vrecord.ui.RecordVideoActivity;

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


    public static void startRecord(Context context) {
        context.startActivity(new Intent(context, RecordVideoActivity.class));
    }

    public void clearAll() {
        finishListener = null;
        recordBuilder = null;
    }

    public long getMaxDuration() {
        return recordBuilder == null ? 0 : recordBuilder.maxDuration;
    }

    public int getOptionSize() {
        return recordBuilder == null ? OptionSize.size16_9 : recordBuilder.optionSize;
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

        private int optionSize = OptionSize.size16_9;

        public long getMaxDuration() {
            return maxDuration;
        }

        public RecordBuilder setMaxDuration(/*ms 毫米啊*/long maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }

        public RecordBuilder setOptionSize(int optionSize) {
            this.optionSize = optionSize;
            return this;
        }
    }
}
