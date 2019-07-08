package com.devtips.avplayer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.TextView;

import com.devtips.avplayer.core.AVSurfaceTexture;
import com.devtips.avplayer.core.media.AVAssetTrackDecoder;
import com.devtips.avplayer.core.media.AVMediaSyncClock;
import com.devtips.avplayer.core.opengl.GPUTextureProgram;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.media.AudioTrack.WRITE_BLOCKING;
import static com.devtips.avplayer.R.id.surfaceView;

/******************************************************************
 * AVPlayer 
 * com.devtips.avplayer
 *
 * @author sprint
 * @Date 2019-06-14 11:59
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public class DemoAVPlayer01Activity extends Activity {

    private static final String TAG = "DemoMediaPlayer";
    private QMUITopBarLayout mTopBar;

    /** 视频解码及播放 */
    private GLSurfaceView mSurfaceView;
    private AVSurfaceTexture mSurfaceTexture;
    private GPUTextureProgram mProgram;
    private AVAssetTrackDecoder mVideoDecoder;

    // 音频解码及播放
    private AudioTrack mAudioTrack;
    int mBufferSize;
    private AVAssetTrackDecoder mAudioDecoder;

    /** 同步时钟 用作音视频同步 */
    private AVMediaSyncClock mMediaSyncClock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_player);
        this.initTopBar();

        step1();
        step2();
        step3();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioDecoder.stop();
        mVideoDecoder.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioDecoder.stop();
        mVideoDecoder.stop();
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
     * 初始化解码器
     */
    private void step3() {

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
     * 启动解码器
     */
    private void doDecoder() {

        final Uri videoPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo_video);

        mMediaSyncClock = new AVMediaSyncClock();
        mMediaSyncClock.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mVideoDecoder = new AVAssetTrackDecoder(DemoAVPlayer01Activity.this, videoPathUri, "video/");
                mVideoDecoder.setDelegate(mVideoDecoderDelegate);
                mVideoDecoder.doDecoder(mSurfaceTexture.getSurface());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioDecoder = new AVAssetTrackDecoder(DemoAVPlayer01Activity.this, videoPathUri, "audio/");
                mAudioDecoder.setDelegate(mAudioDecoderDelegate);
                mAudioDecoder.doDecoder(null);

            }
        }).start();
    }

    /** 视频解码器回调 */
    private AVAssetTrackDecoder.AVAssetTrackDecoderDelegate mVideoDecoderDelegate  = new AVAssetTrackDecoder.AVAssetTrackDecoderDelegate() {
        @Override
        public void newFrameReady(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {

            // 锁定时钟
            mMediaSyncClock.lock(bufferInfo.presentationTimeUs,0);
        }

        @Override
        public void outputFormatChaned(MediaFormat mediaFormat) {

        }
    };

    /** 音频解码器回调 */
    private AVAssetTrackDecoder.AVAssetTrackDecoderDelegate mAudioDecoderDelegate  = new AVAssetTrackDecoder.AVAssetTrackDecoderDelegate() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void newFrameReady(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {

            // 锁定时钟
            mMediaSyncClock.lock(bufferInfo.presentationTimeUs,0);
            mAudioTrack.write(byteBuffer,bufferInfo.size,WRITE_BLOCKING,bufferInfo.presentationTimeUs);

        }

        @Override
        public void outputFormatChaned(MediaFormat outputFormat) {

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
        }
    };

}
