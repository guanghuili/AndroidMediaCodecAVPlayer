package com.devtips.avplayer.core.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED;
import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;

/******************************************************************
 * AVPlayer 
 * com.devtips.avplayer.core.media
 *
 * @author sprint
 * @Date 2019-07-08 17:00
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public class AVAssetTrackDecoder {

    /** 解码的轨道类型 */
    private  String mDecodeMimeType;
    private  Context mContext;
    private  Uri mUri;

    private AVAssetTrackDecoderDelegate mDelegate;

    private boolean mRuning;

    public interface AVAssetTrackDecoderDelegate {
        void newFrameReady(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
        void outputFormatChaned(MediaFormat mediaFormat);
    }

    public AVAssetTrackDecoder(Context context,Uri uri,String mimeType) {
         this.mContext = context;
         this.mUri = uri;
         this.mDecodeMimeType = mimeType;
    }

    /**
     * 设置委托
     * @param delegate
     */
    public void setDelegate(AVAssetTrackDecoderDelegate delegate) {
        this.mDelegate = delegate;
    }

    /**
     * 喂入数据到解码器
     *
     * @return true 喂入成功
     * @since v3.0.1
     */
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
     * @since v3.0.1
     */
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
                if(mDelegate != null)
                    mDelegate.outputFormatChaned(outputFormat);

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

                    if (mDelegate != null && mDecodeMimeType.equalsIgnoreCase("audio/")) {

                        mDelegate.newFrameReady(outputBuffer,bufferInfo);
                        mediaCodec.releaseOutputBuffer(outIndex,true);

                    }else
                    {
                        mediaCodec.releaseOutputBuffer(outIndex,true);
                        mDelegate.newFrameReady(outputBuffer,bufferInfo);

                    }

                }

                return true;
            }
        }
    }

    /**
     * 启动解码器
     */
    public void doDecoder(Surface surface){

        // step 1：创建一个媒体分离器
        MediaExtractor extractor = new MediaExtractor();
        // step 2：为媒体分离器装载媒体文件路径
        // 指定文件路径
        try {
            extractor.setDataSource(mContext, mUri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // step 3：获取并选中指定类型的轨道
        // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
        int trackCount = extractor.getTrackCount();
        // mime type 指示需要分离的轨道类型
        String extractMimeType = mDecodeMimeType;
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
            mediaCodec.configure(trackFormat,surface,null,0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRuning = true;

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

    }

    public void stop(){
        mRuning = false;
    }

}
