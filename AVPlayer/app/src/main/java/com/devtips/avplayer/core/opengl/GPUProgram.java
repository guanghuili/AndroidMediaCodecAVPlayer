package com.devtips.avplayer.core.opengl;

import android.opengl.GLES20;

import com.devtips.avplayer.core.AVLog;

import junit.framework.Assert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * @PACKAGE_NAME: com.devtips.avplayer.core.opengl
 * @Package: com.devtips.avplayer.core.opengl
 * @ClassName: GPUProgram
 * @Author: ligh
 * @CreateDate: 2019/5/11 5:28 PM
 * @Version: 1.0
 * @Description: OpenGL 程序
 */
public class GPUProgram {

    // 当前 GL 程序句柄
    private final int mProgram;

    private HashMap<String,Integer> mAttributeLocationMap = new HashMap<>();

    public GPUProgram(String vertexShader, String fragmentShader) {
        Assert.assertEquals(true, vertexShader != null);
        Assert.assertEquals(true, fragmentShader != null);

        mProgram = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (mProgram == 0) {
            AVLog.e("Could not create program");
            return;
        }

        int vShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShader);
        int fShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShader);

        if (!linkProgram(mProgram,vShader,fShader)) {
            GLES20.glDeleteProgram(mProgram);
            GLES20.glDeleteShader(vShader);
            GLES20.glDeleteShader(fShader);
        }
    }

    /**
     * 根据 Attri 属性名称获取属性索引
     * @param name 属性名称
     * @return 属性索引
     */
    public int glGetAttribLocation(String name) {
        Assert.assertEquals(true, mProgram > 0);

        if (mAttributeLocationMap.containsKey(name))
            return mAttributeLocationMap.get(name);

        int attribLocation = GLES20.glGetAttribLocation(mProgram,name);
        Assert.assertEquals(true,attribLocation >= 0);
        mAttributeLocationMap.put(name,attribLocation);

        return attribLocation;
    }

    /**
     * 根据 Uniform 属性名称获取属性索引
     * @param name 属性名称
     * @return 属性索引
     */
    public int glGetUniformLocation(String name) {
        Assert.assertEquals(true, mProgram > 0);

        if (mAttributeLocationMap.containsKey(name))
            return mAttributeLocationMap.get(name);


        int uniformLocation = GLES20.glGetUniformLocation(mProgram,name);
        Assert.assertEquals(true,uniformLocation >= 0);
        mAttributeLocationMap.put(name,uniformLocation);


        return uniformLocation;
    }

    /**
     * 激活当前 Program
     * @return true/false
     */
    public boolean useProgram() {
        if (mProgram <= 0) return false;
        GLES20.glUseProgram(mProgram);
        return true;
    }


    /**
     * 链接程序
     *
     * @param program 程序句柄
     * @param vShader 顶点着色器句柄
     * @param fShader 片元着色器句柄
     * @return 是否链接成功
     */
    private static boolean linkProgram(int program,int vShader,int fShader){

        GLES20.glAttachShader(program,vShader);
        checkGlError("glAttachShader vShader");
        GLES20.glAttachShader(program,fShader);
        checkGlError("glAttachShader fShader");

        GLES20.glLinkProgram(program);
        checkGlError("glLinkProgram");

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            AVLog.e("Could not link program: ");
            AVLog.e(GLES20.glGetProgramInfoLog(program));
            return false;
        }
        return true;
    }

    /**
     * 加载并编译着色器
     *
     * @param shaderType 着色器类型 GLES20.GL_VERTEX_SHADER /  GLES20.GL_FRAGMENT_SHADER
     * @param source 着色器源码
     * @return 着色器句柄
     */
    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            AVLog.e("Could not compile shader " + shaderType + ":");
            AVLog.e(GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * 检查当前 GL 是否有错误
     * @param op 附加日志
     */
    public static void checkGlError(String op)
    {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            AVLog.e(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    public static FloatBuffer createFloatBuffer(float[] coords)
    {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }
}
