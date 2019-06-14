package com.devtips.avplayer.opengl;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.devtips.avplayer.R;
import com.devtips.avplayer.core.opengl.GPUProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * @PACKAGE_NAME: com.devtips.avplayer.opengl
 * @Package: com.devtips.avplayer.opengl
 * @ClassName: DemoGLTriangleActivity
 * @Author: ligh
 * @CreateDate: 2019/5/12 12:46 PM
 * @Version: 1.0
 * @Description: 绘制一个简单的三角形
 */
public class DemoGLTriangleActivity extends Activity {

    private static final String VERTEX_SHADER =
            "attribute vec3 aPosition;" +
            "void main(void) {" +
            "    gl_Position = vec4(aPosition,1.0);" +
            "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "void main(void) {" +
            "    gl_FragColor = vec4(1.0,0.5,0.2,1.0);" +
            "}";


    float vertices[] = {
            -1.f, -0.f,
            1.f, 0.f,
            0.f,  1.f,
    };

    private GLSurfaceView mGLSurfaceView;
    private GPUProgram mProgram;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_gl_triangle);

        mGLSurfaceView = findViewById(R.id.gl_surfaceView);
        // openGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mRender);
        // 设置渲染模式  GLSurfaceView.RENDERMODE_WHEN_DIRTY 只有使用  requestRender() 是才会触发渲染
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private GLSurfaceView.Renderer mRender = new GLSurfaceView.Renderer() {

        // 顶点坐标位置句柄
        private int aPosition;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 创建一个 Program
            mProgram =  new GPUProgram(VERTEX_SHADER,FRAGMENT_SHADER);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // 调整视口大小
            GLES20.glViewport(0 ,0,width,height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // 激活当前程序
            mProgram.useProgram();

            // 获取顶点坐标位置索引
            aPosition = mProgram.glGetAttribLocation("aPosition");

            // 为顶点坐标属性绑定数据
            GLES20.glVertexAttribPointer( aPosition, 2,
                    GLES20.GL_FLOAT, false, 0, GPUProgram.createFloatBuffer(vertices));
            GLES20.glEnableVertexAttribArray(aPosition);

            /** first:  vertices 顶点数组起始位置  count: 三角形顶点数量 */
            glDrawArrays(GL_TRIANGLES, 0, 3);

        }
    };
}
