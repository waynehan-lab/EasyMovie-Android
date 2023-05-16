package com.pvr.videoplugin;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FilterFBOTexture {

    // 顶点着色器
    private static final String vertexShaderCode =
            "#version 300 es                       \n" +
                    "in vec4 a_Position;           \n" +
                    "in vec2 a_TexCoord;           \n" +
                    "out vec2 v_TexCoord;          \n" +
                    "void main() {                 \n" +
                    "   gl_Position = a_Position;  \n" +
                    "   v_TexCoord = a_TexCoord;   \n" +
                    "}                             \n";

    // 片段着色器
    private static final String fragmentShaderCode =
            "#version 300 es                                                 \n" +
                    "#extension GL_OES_EGL_image_external_essl3 : require    \n" +
                    "precision mediump float;                                \n" +
                    "in vec2 v_TexCoord;                                     \n" +
                    "out vec4 fragColor;                                     \n" +
                    "uniform samplerExternalOES s_Texture;                   \n" +
                    "void main() {                                           \n" +
                    "   fragColor = texture(s_Texture, v_TexCoord);          \n" +
                    "}                                                       \n";

    // 顶点坐标
    private final float[] vertexData = {
            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f,
    };
    private FloatBuffer vertexBuffer;
    private final int vertexVBO;

    // 纹理坐标
    private final float[] textureData = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };
    private FloatBuffer textureBuffer;
    private final int textureVBO;

    private final int shaderProgram;
    private final int a_Position;
    private final int a_TexCoord;
    private final int s_Texture;

    private final int width;
    private final int height;
    private final int unityTextureId;
    private final int oesTextureId;
    private final int FBO;

    FilterFBOTexture(int width, int height, int unityTextureId, int oesTextureId) {
        this.width = width;
        this.height = height;
        this.unityTextureId = unityTextureId;
        this.oesTextureId = oesTextureId;
        FBO = FBOUtils.createFBO();

        int[] vbo = new int[2];
        GLES30.glGenBuffers(2, vbo, 0);

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);
        vertexVBO = vbo[0];

        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);
        textureBuffer.position(0);
        textureVBO = vbo[1];

        shaderProgram = FBOUtils.buildProgram(vertexShaderCode, fragmentShaderCode);
        GLES30.glUseProgram(shaderProgram);
        a_Position = GLES30.glGetAttribLocation(shaderProgram, "a_Position");
        a_TexCoord = GLES30.glGetAttribLocation(shaderProgram, "a_TexCoord");
        s_Texture = GLES30.glGetUniformLocation(shaderProgram, "s_Texture");
    }

    void release() {
        int[] textures = new int[2];
        textures[0] = oesTextureId;
        textures[1] = unityTextureId;
        GLES20.glDeleteTextures(1, textures, 0);

        int[] vbo = new int[2];
        vbo[0] = vertexVBO;
        vbo[1] = textureVBO;
        GLES30.glDeleteBuffers(vbo.length, vbo, 0);
        vertexBuffer.clear();
        textureBuffer.clear();

        int[] fbo = new int[1];
        fbo[0] = FBO;
        GLES30.glDeleteFramebuffers(fbo.length, fbo, 0);

        GLES30.glDeleteProgram(shaderProgram);
    }

    void draw() {
        GLES30.glViewport(0, 0, width, height);

//        FBOUtils.checkError("0");

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

//        FBOUtils.checkError("1");

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, FBO);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, unityTextureId, 0);

//        FBOUtils.checkError("2");

        GLES30.glUseProgram(shaderProgram);

//        FBOUtils.checkError("3");

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData.length * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glEnableVertexAttribArray(a_Position);
        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);

//        FBOUtils.checkError("4");

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, textureData.length * 4, textureBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glEnableVertexAttribArray(a_TexCoord);
        GLES30.glVertexAttribPointer(a_TexCoord, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);

//        FBOUtils.checkError("5");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);
        GLES30.glUniform1i(s_Texture, 0);

//        FBOUtils.checkError("6");

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

//        FBOUtils.checkError("7");

        GLES30.glDisableVertexAttribArray(a_Position);
        GLES30.glDisableVertexAttribArray(a_TexCoord);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }


}
