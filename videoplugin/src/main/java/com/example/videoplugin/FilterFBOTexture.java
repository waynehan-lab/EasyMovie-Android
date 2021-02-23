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
            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f,
    };

    static float[] textureData = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;

    private final int av_Position;
    private final int af_Position;
    private final int s_Texture;

    private final int program;

    private final int fboId;
    private final int width;
    private final int height;
    private final int videoTextureId;
    private final int unityTextureId;

    public FilterFBOTexture(int width, int height, int unityTextureId, int videoTextureId) {
        this.width = width;
        this.height = height;
        this.unityTextureId = unityTextureId;
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

    public void draw() {
        //视口
        GLES20.glViewport(0, 0, width, height);

        //清除颜色缓冲
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //激活帧缓冲
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //把一个纹理附加到帧缓冲
        //之后所有的渲染操作将会渲染到当前绑定帧缓冲的附件中
        //即所有渲染操作的结果将会被储存在unityTextureId对应的纹理图像中
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, unityTextureId, 0);
        //检查帧缓冲是否完整
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            FBOUtils.log("FrameBuffer error");
            return;
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        //激活着色器程序
        GLES20.glUseProgram(program);

        //告诉OpenGL该如何解析顶点数据(顶点坐标)，并启用顶点属性
        GLES20.glEnableVertexAttribArray(av_Position);
        GLES20.glVertexAttribPointer(av_Position, 2, GLES20.GL_FLOAT, false, 2 * 4, vertexBuffer);

        //告诉OpenGL该如何解析顶点数据(纹理坐标)，并启用顶点属性
        GLES20.glEnableVertexAttribArray(af_Position);
        GLES20.glVertexAttribPointer(af_Position, 2, GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);

        //激活纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE7);
        //绑定指定纹理到当前激活的纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTextureId);
        //为着色器中定义的采样器指定属于哪个纹理单元；不要忘记在设置uniform变量之前激活着色器程序
        GLES20.glUniform1i(s_Texture, 7);

        //绘制三角带
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(av_Position);
        GLES20.glDisableVertexAttribArray(af_Position);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        //激活默认帧缓冲
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
