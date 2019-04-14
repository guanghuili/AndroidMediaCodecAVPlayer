package com.devtips.avplayer;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @PACKAGE_NAME: com.devtips.avplayer
 * @Package: com.devtips.avplayer
 * @ClassName: DemoMediaExtractorActivity
 * @Author: ligh
 * @CreateDate: 2019/4/14 11:13 AM
 * @Version: 1.0
 * @Description:
 */
public class DemoMediaExtractorActivity extends Activity {

    private static final String TAG = "DemoMediaExtractor";

    private QMUITopBarLayout mTopBar;
    private TextView mInfoTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_media_extroctor);

        mInfoTextView = findViewById(R.id.info_textview);
        this.initTopBar();
    }

    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setBackgroundResource(com.qmuiteam.qmui.R.color.qmui_config_color_blue);
        TextView textView = mTopBar.setTitle("MediaExtractor 示例");
        textView.setTextColor(Color.WHITE);
    }


    /* 分离视频信息 */
    public void doExtract(View sender) {
        // step 1：创建一个媒体分离器
        MediaExtractor extractor = new MediaExtractor();
        // step 2：为媒体分离器装载媒体文件路径
        // 指定文件路径
        Uri videoPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.img_video);
        try {
            extractor.setDataSource(this, videoPathUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3：获取并选中指定类型的轨道
        // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
        int trackCount = extractor.getTrackCount();
        // mime type 指示需要分离的轨道类型
        String extractMimeType = "video/";
        MediaFormat trackFormat = null;
        // 记录轨道索引id，MediaExtractor 读取数据之前需要指定分离的轨道索引
        int trackID = -1;
        for (int i = 0; i < trackCount; i++) {
            trackFormat = extractor.getTrackFormat(i);
            if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith(extractMimeType)) {
                trackID = i;
                break;
            }
        }
        // 媒体文件中存在视频轨道
        if (trackID != -1)
            extractor.selectTrack(trackID);

        // step 4：分离指定轨道的数据
        // 获取最大缓冲区大小，
        int maxInputSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        // 开辟一个字节缓冲区，用于存放分离的媒体数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(maxInputSize);
        // 记录当前帧数据大小
        int sampleDataSize = 0;

        while ((sampleDataSize = extractor.readSampleData(byteBuffer, 0)) > 0) {

            extractor.readSampleData(byteBuffer,0);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.offset = 0;
            bufferInfo.presentationTimeUs = extractor.getSampleTime();
            bufferInfo.size = sampleDataSize;
            bufferInfo.flags = extractor.getSampleFlags();
            extractor.advance();

            mInfoTextView.setText(String.format("presentationTimeUs : %s", String.valueOf(extractor.getSampleTime())));

            Log.i(TAG,String.format("presentationTimeUs : %s", String.valueOf(extractor.getSampleTime())));
        }

        // 释放分离器，释放后 extractor 将不可用
        extractor.release();
    }

    /** 获取视频信息 */
    public void printInfo(View sender) {

        // step 1：创建一个媒体分离器
        MediaExtractor extractor = new MediaExtractor();
        // step 2：为媒体分离器装载媒体文件路径
        // 指定文件路径
        Uri videoPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.img_video);
        try {
            extractor.setDataSource(this, videoPathUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3：获取并选中指定类型的轨道
        // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
        int trackCount = extractor.getTrackCount();
        // mime type 指示需要分离的轨道类型
        String extractMimeType = "video/";
        MediaFormat trackFormat = null;
        // 记录轨道索引id，MediaExtractor 读取数据之前需要指定分离的轨道索引
        int trackID = -1;
        for (int i = 0; i < trackCount; i++) {
            trackFormat = extractor.getTrackFormat(i);
            if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith(extractMimeType)) {
                trackID = i;
                break;
            }
        }

        mInfoTextView.setText(trackFormat.toString());

        // 释放分离器，释放后 extractor 将不可用
        extractor.release();

    }

}
