package com.devtips.avplayer.core.media;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/******************************************************************
 * AVPlayer
 * com.devtips.avplayer.core.media
 *
 * @author sprint
 * @Date 2019-07-08 17:00
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public class AVMediaSyncClock {

    private static final long TIME_UNSET = Long.MIN_VALUE + 1;
    private static final long TIME_END_OF_SOURCE = Long.MIN_VALUE;

    /** 帧基准时间 */
    private long mBasePositionUs;

    /** 指示当前播放速度 */
    private float mSpeed = 1;
    /** 运行基准时间 */
    private long mBaseElapsedMs;
    /** 当前时钟是否已开始计时 */
    private boolean mStarted;

    /** 启动时钟 */
    public void start() {
        if (mStarted) return;
        this.reset();
        mStarted = true;
    }

    /** 停止时钟 */
    public void stop() {
        mBasePositionUs = 0;
        mStarted = false;
        mBaseElapsedMs = 0;
    }

    private void reset() {
        mBasePositionUs = 0;
        mBaseElapsedMs = SystemClock.elapsedRealtime();
    }

    /**
     * 锁定
     *
     * @param positionUs 必须保证真实显示时间 （连续递增）
     */
    public void lock(long positionUs,long diff) {

        if (!mStarted)  {
            return;
        }

        if (mBasePositionUs == 0)
            mBasePositionUs = positionUs;

        long speedPositionUs = (long)((positionUs - mBasePositionUs) * (1.f/mSpeed));

        long duraitonMs = usToMs(speedPositionUs) + diff;
        long endTimeMs =  mBaseElapsedMs + duraitonMs;
        long sleepTimeMs = endTimeMs - SystemClock.elapsedRealtime();

        if (sleepTimeMs > 0) {
            try {

                // 睡眠 锁定线程
                TimeUnit.MILLISECONDS.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 设置播放速度
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        /** 设置速率时必须重置相关基数 */
        reset();
        mSpeed = speed;
    }

    /**
     * 获取当前播放速度
     *
     * @return
     */
    public float getSpeed() {
        return  mSpeed;
    }


    public static long usToMs(long timeUs) {
        return (timeUs == TIME_UNSET || timeUs == TIME_END_OF_SOURCE) ? timeUs : (timeUs / 1000);
    }

    public static long msToUs(long timeMs) {
        // 防止越界
        return (timeMs == TIME_UNSET || timeMs == TIME_END_OF_SOURCE) ? timeMs : (timeMs * 1000);
    }

}
