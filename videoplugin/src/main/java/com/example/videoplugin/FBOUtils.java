package com.example.videoplugin;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

public class FBOUtils {

    public static void log(String msg) {
        Log.i("VideoFilter", msg);
    }

    // 根据类型编译着色器
    public static int compileShader(int type, String shaderCode) {
        // 根据不同的类型创建着色器 ID
        final int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            return 0;
        }
        // 将着色器 ID 和着色器程序内容连接
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        // 编译着色器
        GLES20.glCompileShader(shaderObjectId);
        // 为验证编译结果是否失败
        final int[] compileStatus = new int[1];
        // glGetShaderiv函数比较通用，在着色器阶段和 OpenGL 程序阶段都会通过它来验证结果。
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            // 失败则删除
            GLES20.glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    // 创建 OpenGL 程序和着色器链接
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 创建 OpenGL 程序 ID
        final int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }
        // 链接上 顶点着色器
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        // 链接上 片段着色器
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        // 链接着色器之后，链接 OpenGL 程序
        GLES20.glLinkProgram(programObjectId);
        // 验证链接结果是否失败
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            // 失败则删除 OpenGL 程序
            GLES20.glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    // 链接了 OpenGL 程序后，就是验证 OpenGL 是否可用。
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

    // 创建 OpenGL 程序过程
    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        // 编译顶点着色器
        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        // 编译片段着色器
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        int program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);
        return program;
    }

    public static int createFBO() {
        log("createFBO");
        int[] fbo = new int[1];
        GLES20.glGenFramebuffers(fbo.length, fbo, 0);
        return fbo[0];
    }

    public static int createVideoTextureID() {
        log("createVideoTextureID");
        int[] texture = new int[1];
        GLES20.glGenTextures(texture.length, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public static int create2DTextureId(int width, int height) {
        log("create2DTextureId");
        int[] textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0,
                GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textures[0];
    }

}
