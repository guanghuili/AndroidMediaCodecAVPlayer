package com.devtips.avplayer;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED;
import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;

/******************************************************************
 * AVPlayer 
 * com.devtips.avplayer
 *
 * @author sprint
 * @Date 2019-06-27 17:16
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public class DemoAudioTrackPlayerActivity extends Activity {
    private static final String TAG = "DemoAudioTrackPlayer";

    private QMUITopBarLayout mTopBar;
    private TextView mInfoTextView;
    private Button mPlayButton;
    private Button mStopButton;


    private Handler mHandler;
    private boolean mRuning;

    private AudioTrack mAudioTrack;
    int mBufferSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_media_audio_track_player);
        mHandler = new Handler();

        mInfoTextView = findViewById(R.id.info_textview);
        mPlayButton = findViewById(R.id.start_decode_btn);
        mStopButton = findViewById(R.id.stop_decode_btn);
        mStopButton.setEnabled(false);
        this.initTopBar();
    }

    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setBackgroundResource(com.qmuiteam.qmui.R.color.qmui_config_color_blue);
        TextView textView = mTopBar.setTitle("AudioTrack 示例");
        textView.setTextColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlay(null);
    }

    /**
     * 喂入数据到解码器
     *
     * @return true 喂入成功
     * @since v3.0.1
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean feedInputBuffer(MediaExtractor source, MediaCodec codec) {

        if (source == null || codec == null) return false;

        int inIndex = codec.dequeueInputBuffer(0);
        if (inIndex < 0)  return false;

        ByteBuffer codecInputBuffer = codec.getInputBuffers()[inIndex];
        codecInputBuffer.position(0);
        int sampleDataSize = source.readSampleData(codecInputBuffer,0);

        if (sampleDataSize <=0 ) {

            // 通知解码器结束
            if (inIndex >= 0)
                codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return false;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.offset = 0;
        bufferInfo.presentationTimeUs = source.getSampleTime();
        bufferInfo.size = sampleDataSize;
        bufferInfo.flags = source.getSampleFlags();

        switch (inIndex)
        {
            case INFO_TRY_AGAIN_LATER: return true;
            default:
            {

                codec.queueInputBuffer(inIndex,
                        bufferInfo.offset,
                        bufferInfo.size,
                        bufferInfo.presentationTimeUs,
                        bufferInfo.flags
                );

                source.advance();

                return true;
            }
        }

    }

    /**
     * 吐出解码后的数据
     *
     * @return true 有可用数据吐出
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean drainOutputBuffer(MediaCodec mediaCodec) {

        if (mediaCodec == null) return false;

        final
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outIndex =  mediaCodec.dequeueOutputBuffer(info, 0);

        if ((info.flags & BUFFER_FLAG_END_OF_STREAM) != 0) {
            mediaCodec.releaseOutputBuffer(outIndex, false);
            return false;
        }

        switch (outIndex)
        {
            case INFO_OUTPUT_BUFFERS_CHANGED: return true;
            case INFO_TRY_AGAIN_LATER: return true;
            case INFO_OUTPUT_FORMAT_CHANGED: {

                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                int sampleRate = 44100;
                if (outputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE))
                    sampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);

                int channelConfig = AudioFormat.CHANNEL_OUT_MONO;

                if (outputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
                    channelConfig = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;


                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

                if (outputFormat.containsKey("bit-width"))
                    audioFormat = outputFormat.getInteger("bit-width") == 8 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;

                mBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2;
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,mBufferSize,AudioTrack.MODE_STREAM);
                mAudioTrack.play();

                return true;
            }
            default:
            {
                if (outIndex >= 0 && info.size > 0)
                {
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    bufferInfo.presentationTimeUs = info.presentationTimeUs;
                    bufferInfo.size = info.size;
                    bufferInfo.flags = info.flags;
                    bufferInfo.offset = info.offset;

                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffers()[outIndex];
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    byte[] audioData = new byte[bufferInfo.size];
                    outputBuffer.get(audioData);

                    mAudioTrack.write(audioData,bufferInfo.offset,Math.min(bufferInfo.size, mBufferSize));


                    // 释放
                    mediaCodec.releaseOutputBuffer(outIndex, false);


                    Log.i(TAG,String.format("pts:%s",info.presentationTimeUs));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mInfoTextView.setText(String.format("正在解码中..\npts:%s",info.presentationTimeUs));
                        }
                    });

                }

                return true;
            }
        }
    }

    /**
     * 启动解码器
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void doDecoder(){

        // step 1：创建一个媒体分离器
        MediaExtractor extractor = new MediaExtractor();
        // step 2：为媒体分离器装载媒体文件路径
        // 指定文件路径
        Uri videoPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo_video);
        try {
            extractor.setDataSource(this, videoPathUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3：获取并选中指定类型的轨道
        // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
        int trackCount = extractor.getTrackCount();
        // mime type 指示需要分离的轨道类型
        String extractMimeType = "audio/";
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
        // step 4：选中指定类型的轨道
        if (trackID != -1)
            extractor.selectTrack(trackID);

        // step 5：根据 MediaFormat 创建解码器
        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodec.createDecoderByType(trackFormat.getString(MediaFormat.KEY_MIME));
            mediaCodec.configure(trackFormat,null,null,0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (mRuning) {
            // step 6: 向解码器喂入数据
            boolean ret = feedInputBuffer(extractor,mediaCodec);
            // step 7: 从解码器吐出数据
            boolean decRet = drainOutputBuffer(mediaCodec);
            if (!ret && !decRet)break;;
        }

        // step 8: 释放资源

        // 释放分离器，释放后 extractor 将不可用
        extractor.release();
        // 释放解码器
        mediaCodec.release();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mPlayButton.setEnabled(true);
                mInfoTextView.setText("解码完成");
            }
        });

    }

    /** 启动视频解码 */
    public void startPlay(View sender) {
        mRuning = true;
        mPlayButton.setEnabled(false);
        mStopButton.setEnabled(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doDecoder();
            }
        }).start();
    }

    /** 停止播放 */
    public void stopPlay(View sender) {
        if (mAudioTrack == null) return;
        mRuning = false;
        mPlayButton.setEnabled(true);
        mStopButton.setEnabled(false);


        mAudioTrack.stop();
        mAudioTrack = null;
    }


}
