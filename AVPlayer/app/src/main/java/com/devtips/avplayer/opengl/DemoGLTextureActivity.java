package com.devtips.avplayer.opengl;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.devtips.avplayer.R;
import com.devtips.avplayer.core.opengl.GPUTextureProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @PACKAGE_NAME: com.devtips.avplayer.opengl
 * @Package: com.devtips.avplayer.opengl
 * @ClassName: DemoGLTextureActivity
 * @Author: ligh
 * @CreateDate: 2019/5/11 5:24 PM
 * @Version: 1.0
 * @Description:
 */
public class DemoGLTextureActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;

    private GPUTextureProgram m2DTextureProgram;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_texture);

        mGLSurfaceView = findViewById(R.id.gl_surfaceView);
        // openGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mRender);
        // 设置渲染模式  GLSurfaceView.RENDERMODE_WHEN_DIRTY 只有使用  requestRender() 是才会触发渲染
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    /**
     * Create a texture 2d
     */
    private int createTexture()
    {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int texId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return texId;
    }

    private GLSurfaceView.Renderer mRender = new GLSurfaceView.Renderer() {

        private int mImageTexure;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            m2DTextureProgram =  new GPUTextureProgram();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0 ,0,width,height);
            mImageTexure  = createTexture();

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mImageTexure);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            m2DTextureProgram.draw(mImageTexure);
        }
    };
}
