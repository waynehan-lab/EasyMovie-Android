package com.example.videofilter;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureRender implements GLSurfaceView.Renderer {

    private final Context context;

    //顶点着色器
    private static final String vertexShaderSource =
            "#version 300 es                                        \n"
                    + "layout (location = 0) in vec4 a_Position;    \n"
                    + "layout (location = 1) in vec2 a_TexCoord;    \n"
                    + "out vec2 v_TexCoord;                         \n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "    gl_Position = a_Position;                \n"
                    + "    v_TexCoord = a_TexCoord;                 \n"
                    + "}                                            \n";

    //片段着色器
    private static final String fragmentShaderSource =
            "#version 300 es		 			          	        \n"
                    + "precision mediump float;					  	\n"
                    + "in vec2 v_TexCoord;                          \n"
                    + "uniform sampler2D s_Texture;                 \n"
                    + "out vec4 fragColor;	 			 		  	\n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  fragColor = texture(s_Texture, v_TexCoord);\n"
                    + "}                                            \n";

    //顶点坐标
    private static final float[] vertexData = {
            -0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, 0.5f,
            0.5f, -0.5f,
    };
    private final FloatBuffer vertexBuffer;

    //纹理坐标
    private static final float[] textureData = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f,
    };
    private final FloatBuffer textureBuffer;

    private int shaderProgram;
    private int a_Position;
    private int a_TexCoord;
    private int s_Texture;

    public TextureRender(Context context) {
        this.context = context;
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexData)
                .position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(textureData)
                .position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //编译链接shader
        shaderProgram = Utils.buildProgram(vertexShaderSource, fragmentShaderSource);

        //获取 a_Position 属性的位置
        a_Position = GLES30.glGetAttribLocation(shaderProgram, "a_Position");

        //获取 a_TexCoord 属性的位置
        a_TexCoord = GLES30.glGetAttribLocation(shaderProgram, "a_TexCoord");

        //获取 s_Texture 属性的位置
        s_Texture = GLES30.glGetUniformLocation(shaderProgram, "s_Texture");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //设置清空屏幕所用的颜色
        GLES30.glClearColor(0.8f, 0.8f, 0.8f, 0f);
        //清除颜色缓冲
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //启用面剔除
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        //只剔除背向面
        GLES30.glCullFace(GLES30.GL_BACK);

        //使用shader
        GLES30.glUseProgram(shaderProgram);

        //加载顶点数据到 a_Position 属性位置
        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(a_Position);

        //加载纹理数据到 a_TexCoord 属性位置
        GLES30.glVertexAttribPointer(a_TexCoord, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(a_TexCoord);

        //激活并绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, Utils.loadTexture(context, R.drawable.icon));
        //设置到0号纹理单元上
        GLES30.glUniform1i(s_Texture, 0);

        //绘制三角带
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glDisableVertexAttribArray(a_Position);
        GLES30.glDisableVertexAttribArray(a_TexCoord);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }
}
