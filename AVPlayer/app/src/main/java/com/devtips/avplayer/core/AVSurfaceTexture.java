package com.devtips.avplayer.core;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.devtips.avplayer.core.opengl.AVKitGLUtils;

/******************************************************************
 * AVPlayer 
 * com.devtips.avplayer.core
 *
 * @author sprint
 * @Date 2019-06-14 14:08
 ******************************************************************/
public class AVSurfaceTexture {

    /** SurfaceTexture */
    private SurfaceTexture mSurfaceTexture;
    /** Surface */
    private Surface mSurface;
    /** mSurfaceTextureID */
    private int mSurfaceTextureID = -1;


    public AVSurfaceTexture() {

        // 生成纹理id
        mSurfaceTextureID = AVKitGLUtils.createOESTexture();

        // 创建 SurfaceTexture
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
        // 创建 Surface
        mSurface = new Surface(mSurfaceTexture);
    }

    public SurfaceTexture getSurfaceTexture()  {
        return mSurfaceTexture;
    }

    public int getTextureID()
    {
        return mSurfaceTextureID;
    }

    public void updateTexImage()
    {
        mSurfaceTexture.updateTexImage();
    }

    public Surface getSurface()
    {
        return mSurface;
    }
}
