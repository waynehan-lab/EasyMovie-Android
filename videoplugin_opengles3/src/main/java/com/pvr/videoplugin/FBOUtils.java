package com.pvr.videoplugin;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

public class FBOUtils {

    // 根据类型编译着色器
    public static int compileShader(int type, String shaderCode) {
        // 根据不同的类型创建着色器 ID
        final int shaderObjectId = GLES30.glCreateShader(type);
        if (shaderObjectId == 0) {
            return 0;
        }
        // 将着色器 ID 和着色器程序内容连接
        GLES30.glShaderSource(shaderObjectId, shaderCode);
        // 编译着色器
        GLES30.glCompileShader(shaderObjectId);
        // 为验证编译结果是否失败
        final int[] compileStatus = new int[1];
        // glGetShaderiv函数比较通用，在着色器阶段和 OpenGL 程序阶段都会通过它来验证结果。
        GLES30.glGetShaderiv(shaderObjectId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            log("compileShader error --> " + shaderCode);
            // 失败则删除
            GLES30.glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    // 创建 OpenGL 程序和着色器链接
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 创建 OpenGL 程序 ID
        final int programObjectId = GLES30.glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }
        // 链接上 顶点着色器
        GLES30.glAttachShader(programObjectId, vertexShaderId);
        // 链接上 片段着色器
        GLES30.glAttachShader(programObjectId, fragmentShaderId);
        // 链接着色器之后，链接 OpenGL 程序
        GLES30.glLinkProgram(programObjectId);
        // 验证链接结果是否失败
        final int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            log("linkProgram error");
            // 失败则删除 OpenGL 程序
            GLES30.glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    // 链接了 OpenGL 程序后，就是验证 OpenGL 是否可用。
    public static boolean validateProgram(int programObjectId) {
        GLES30.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, GLES30.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

    // 创建 OpenGL 程序过程
    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        // 编译顶点着色器
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource);
        // 编译片段着色器
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource);
        int program = linkProgram(vertexShader, fragmentShader);
        boolean valid = validateProgram(program);
        log("buildProgram valid = " + valid);
        return program;
    }

    public static int createFBO() {
        int[] fbo = new int[1];
        GLES30.glGenFramebuffers(fbo.length, fbo, 0);
        return fbo[0];
    }

    public static int createVAO() {
        int[] vao = new int[1];
        GLES30.glGenVertexArrays(vao.length, vao, 0);
        return vao[0];
    }

    public static int createVBO() {
        int[] vbo = new int[1];
        GLES30.glGenBuffers(2, vbo, 0);
        return vbo[0];
    }

    public static int createOESTextureID() {
        int[] texture = new int[1];
        GLES30.glGenTextures(texture.length, texture, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glGenerateMipmap(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        return texture[0];
    }

    public static int create2DTextureId(int width, int height) {
        int[] textures = new int[1];
        GLES30.glGenTextures(textures.length, textures, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        return textures[0];
    }

    public static void log(String msg) {
        Log.i("PVRFbo", msg);
    }

    public static void checkError(String msg) {
        log(msg + " -- error --> " + GLES30.glGetError());
    }

}
