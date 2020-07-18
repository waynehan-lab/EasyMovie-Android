package com.example.videoplugin;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FilterFBOTexture {

    private final String vertexShaderCode =
            "attribute vec4 av_Position; \n" +
                    "attribute vec2 af_Position; \n" +
                    "varying vec2 v_texPo; \n" +
                    "void main() { \n" +
                    "   gl_Position = av_Position; \n" +
                    "   v_texPo = af_Position; \n" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require \n" +
                    "precision mediump float; \n" +
                    "varying vec2 v_texPo;\n" +
                    "uniform samplerExternalOES s_Texture;\n" +
                    "void main() { \n" +
                    "   gl_FragColor = texture2D(s_Texture, v_texPo);\n" +
                    "}";

    static float[] vertexData = {
            -1f, 1f, // 左上角
            1f, 1f,  // 右上角
            -1f, -1f,// 左下角
            1f, -1f, // 右下角
    };

    static float[] textureData = {
            0f, 0f, // 左上角
            1f, 0f, // 左下角
            0f, 1f, // 右上角
            1f, 1f,  // 右上角
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private int av_Position;
    private int af_Position;
    private int s_Texture;

    private int program;

    private int videoTextureId;
    private int unityTextureId;
    private int fboId;

    private Context context;
    private int width;
    private int height;

    public FilterFBOTexture(Context context, int videoTextureId) {
        this.context = context;
        this.videoTextureId = videoTextureId;
        fboId = FBOUtils.createFBO();

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);
        textureBuffer.position(0);

        program = FBOUtils.buildProgram(vertexShaderCode, fragmentShaderCode);
        av_Position = GLES20.glGetAttribLocation(program, "av_Position");
        af_Position = GLES20.glGetAttribLocation(program, "af_Position");
        s_Texture = GLES20.glGetUniformLocation(program, "s_Texture");
    }

    public void surfaceChanged(int width, int height, int unityTextureId){
        this.width = width;
        this.height = height;
        this.unityTextureId = unityTextureId;
    }

    public void draw(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, unityTextureId, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glViewport(0, 0, width, height);

        GLES20.glEnableVertexAttribArray(av_Position);
        GLES20.glVertexAttribPointer(av_Position, 2, GLES20.GL_FLOAT, false, 2*4, vertexBuffer);

        GLES20.glEnableVertexAttribArray(af_Position);
        GLES20.glVertexAttribPointer(af_Position, 2, GLES20.GL_FLOAT, false, 2*4, textureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTextureId);

        GLES20.glUniform1i(s_Texture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(av_Position);

        GLES20.glDisableVertexAttribArray(af_Position);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
