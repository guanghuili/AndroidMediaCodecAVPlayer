package com.devtips.avplayer.core.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * @PACKAGE_NAME: com.devtips.avplayer.core.opengl
 * @Package: com.devtips.avplayer.core.opengl
 * @ClassName: GPUTextureProgram
 * @Author: ligh
 * @CreateDate: 2019/5/11 5:57 PM
 * @Version: 1.0
 * @Description: 可绘制 2D 纹理
 */
public class GPUTextureProgram extends GPUProgram {

    public enum ProgramType
    {
        /**
         * 显示普通的 2D 纹理
         */
        TEXTURE_2D,
        /**
         * 显示 SurfaceTexture 采集得到的 OES 纹理
         */
        TEXTURE_EXT
    }

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position =  aPosition;\n" +
                    "    vTextureCoord =  aTextureCoord.xy;\n" +
                    "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    private FloatBuffer textureCoordBuffer;

    private  FloatBuffer vertexBuffer;


    private GPUTextureProgram(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public GPUTextureProgram() {
        this(ProgramType.TEXTURE_2D);
    }

    public GPUTextureProgram(ProgramType programType){
        this(VERTEX_SHADER, programType == ProgramType.TEXTURE_2D ? FRAGMENT_SHADER_2D : FRAGMENT_SHADER_EXT);

        int aTextureCoordLocation = glGetAttribLocation("aTextureCoord");
        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);

        int aPositionLocation = glGetAttribLocation("aPosition");
        GLES20.glEnableVertexAttribArray(aPositionLocation);


        // 顶点坐标
        final float imageVertices[] = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f,  -1.0f,
                1.0f,  -1.0f
        };

         // 纹理坐标
        final float textureCoordinates[] = {
             0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f,1.0f,

        };


        vertexBuffer = createFloatBuffer(imageVertices);
        textureCoordBuffer = createFloatBuffer(textureCoordinates);

    }

    /**
     * 绘制图片
     *
     * @param textureId 纹理
     */
    public void draw(int textureId)
    {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 激活程序
        this.useProgram();

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(glGetUniformLocation("sTexture"), 0);

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer( glGetAttribLocation("aPosition"), 2,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(glGetAttribLocation("aTextureCoord"), 2,
                GLES20.GL_FLOAT, false, 0, textureCoordBuffer);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }
}
