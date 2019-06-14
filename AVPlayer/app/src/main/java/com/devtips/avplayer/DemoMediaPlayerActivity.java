package com.devtips.avplayer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.devtips.avplayer.core.AVSurfaceTexture;
import com.devtips.avplayer.core.opengl.GPUTextureProgram;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED;
import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;
import static com.devtips.avplayer.R.id.surfaceView;

/******************************************************************
 * AVPlayer 
 * com.devtips.avplayer
 *
 * @author sprint
 * @Date 2019-06-14 11:59
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public class DemoMediaPlayerActivity extends Activity {

    private static final String TAG = "DemoMediaPlayer";
    private QMUITopBarLayout mTopBar;

    // GLSurfaceView
    private GLSurfaceView mSurfaceView;


    private AVSurfaceTexture mSurfaceTexture;

    private GPUTextureProgram mProgram;


    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_player);
        this.initTopBar();

        step1();
        step2();
        step3();
        step4();
        step5();

    }

    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setBackgroundResource(com.qmuiteam.qmui.R.color.qmui_config_color_blue);
        TextView textView = mTopBar.setTitle("Player 示例");
        textView.setTextColor(Color.WHITE);
    }

    /**
     * 初始化 GLSurfaceView
     */
    private void step1() {
        mSurfaceView = findViewById(surfaceView);
        // openGL ES 2.0
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);
        // 设置渲染模式  GLSurfaceView.RENDERMODE_WHEN_DIRTY 只有使用  requestRender() 是才会触发渲染
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    /**
     * 初始化 SurfaceTexture
     */
    private void step2() {

        mSurfaceTexture = new AVSurfaceTexture();
        mSurfaceTexture.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mSurfaceView.requestRender();
            }
        });
    }

    /**
     * 初始化分离器
     */
    private void step3(){

        // step 1：创建一个媒体分离器
        mMediaExtractor = new MediaExtractor();
        // step 2：为媒体分离器装载媒体文件路径
        // 指定文件路径
        Uri videoPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo_video);
        try {
            mMediaExtractor.setDataSource(this, videoPathUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3：获取并选中指定类型的轨道
        // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
        int trackCount = mMediaExtractor.getTrackCount();
        // mime type 指示需要分离的轨道类型
        String extractMimeType = "video/";
        MediaFormat trackFormat = null;
        // 记录轨道索引id，MediaExtractor 读取数据之前需要指定分离的轨道索引
        int trackID = -1;
        for (int i = 0; i < trackCount; i++) {
            trackFormat = mMediaExtractor.getTrackFormat(i);
            if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith(extractMimeType)) {
                trackID = i;
                break;
            }
        }
        // 媒体文件中存在视频轨道
        // step 4：选中指定类型的轨道
        if (trackID != -1)
            mMediaExtractor.selectTrack(trackID);

    }

    /**
     * 初始化解码器
     */
    private void step4() {


        MediaFormat trackFormat = mMediaExtractor.getTrackFormat(mMediaExtractor.getSampleTrackIndex());

        try {
            mMediaCodec = MediaCodec.createDecoderByType(trackFormat.getString(MediaFormat.KEY_MIME));

            /** configure 中指定 Surface */
            mMediaCodec.configure(trackFormat,mSurfaceTexture.getSurface(),null,0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 启动解码器
     */
    private void step5() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doDecoder();
            }
        }).start();
    }


    private GLSurfaceView.Renderer mRenderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mProgram = new GPUTextureProgram(GPUTextureProgram.ProgramType.TEXTURE_EXT);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0,0 ,width,height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mSurfaceTexture.updateTexImage();
            mProgram.draw(mSurfaceTexture.getTextureID());
        }
    };

    /**
     * 喂入数据到解码器
     *
     * @return true 喂入成功
     * @since v3.0.1
     */
    public boolean feedInputBuffer(MediaExtractor source, MediaCodec codec) {

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
     * @since v3.0.1
     */
    public boolean drainOutputBuffer(MediaCodec mediaCodec) {

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
            case INFO_OUTPUT_FORMAT_CHANGED:return true;
            default:
            {
                if (outIndex >= 0 && info.size > 0)
                {
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    bufferInfo.presentationTimeUs = info.presentationTimeUs;
                    bufferInfo.size = info.size;
                    bufferInfo.flags = info.flags;
                    bufferInfo.offset = info.offset;

                    // render 必须 true 时，才会将数据更新到 Surface
                    mediaCodec.releaseOutputBuffer(outIndex, true);

                }

                return true;
            }
        }
    }

    /**
     * 启动解码器
     */
    private void doDecoder(){

        while (true) {
            // step 6: 向解码器喂入数据
            boolean ret = feedInputBuffer(mMediaExtractor,mMediaCodec);
            // step 7: 从解码器吐出数据
            boolean decRet = drainOutputBuffer(mMediaCodec);
            if (!ret && !decRet)break;;
        }

        // step 8: 释放资源

        // 释放分离器，释放后 extractor 将不可用
        mMediaExtractor.release();
        // 释放解码器
        mMediaExtractor.release();

    }

}
