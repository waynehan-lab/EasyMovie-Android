package com.example.egl;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class EGLUtils {

    public static String loadShaderSource(Context context, int resId) {
        StringBuilder res = new StringBuilder();
        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String nextLine;
        try {
            while ((nextLine = br.readLine()) != null) {
                res.append(nextLine);
                res.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    /**
     * 加载着色器源，并编译
     *
     * @param type         顶点着色器（GL_VERTEX_SHADER）/片段着色器（GL_FRAGMENT_SHADER）
     * @param shaderSource 着色器源
     * @return 着色器
     */
    public static int loadShader(int type, String shaderSource) {
        //创建着色器对象
        int shader = GLES30.glCreateShader(type);
        if (shader == 0) return 0;//创建失败
        //加载着色器源
        GLES30.glShaderSource(shader, shaderSource);
        //编译着色器
        GLES30.glCompileShader(shader);
        //检查编译状态
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("TestEGL", GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;//编译失败
        }

        return shader;
    }

    public static int createAndLinkProgram(int vertextShader, int fragmentShader) {
        //创建程序
        int program = GLES30.glCreateProgram();
        if (program == 0) {
            //创建失败
            throw new RuntimeException("opengl error: 程序创建失败");
        }
        //绑定着色器到程序
        GLES30.glAttachShader(program, vertextShader);
        GLES30.glAttachShader(program, fragmentShader);
        //连接程序
        GLES30.glLinkProgram(program);
        //检查连接状态
        int[] linked = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            GLES30.glDeleteProgram(program);
            throw new RuntimeException("opengl error: 程序连接失败");
        }
        return program;
    }

    //各数值类型字节数
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;
    //顶点，按逆时针顺序排列
    public static final float[] VERTEX = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f};
    //顶点颜色
    public static final float[] VERTEX_COLORS = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };
    //纹理坐标，（s,t），t坐标方向和顶点y坐标反着
    public static final float[] TEXTURE_COORD = {
            0.5f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    public static FloatBuffer getVertextBuffer() {
        return getFloatBuffer(VERTEX);
    }

    public static FloatBuffer getVertexColorBuffer() {
        return getFloatBuffer(VERTEX_COLORS);
    }

    public static FloatBuffer getTextureCoordBuffer() {
        return getFloatBuffer(TEXTURE_COORD);
    }

    public static FloatBuffer getFloatBuffer(float[] array) {
        //将数据拷贝映射到 native 内存中，以便opengl能够访问
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(array.length * BYTES_PER_FLOAT)//直接分配 native 内存，不会被gc
                .order(ByteOrder.nativeOrder())//和本地平台保持一致的字节序（大/小头）
                .asFloatBuffer();//将底层字节映射到FloatBuffer实例，方便使用
        buffer.put(array)//将顶点拷贝到 native 内存中
                .position(0);//每次 put position 都会 + 1，需要在绘制前重置为0

        return buffer;
    }

    public static ShortBuffer getShortBuffer(short[] array) {
        //将数据拷贝映射到 native 内存中，以便opengl能够访问
        ShortBuffer buffer = ByteBuffer
                .allocateDirect(array.length * BYTES_PER_SHORT)//直接分配 native 内存，不会被gc
                .order(ByteOrder.nativeOrder())//和本地平台保持一致的字节序（大/小头）
                .asShortBuffer();//将底层字节映射到Buffer实例，方便使用
        buffer.put(array)//将顶点拷贝到 native 内存中
                .position(0);//每次 put position 都会增加，需要在绘制前重置为0

        return buffer;
    }

}
