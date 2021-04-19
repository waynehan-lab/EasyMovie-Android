package com.example.videofilter;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRender implements GLSurfaceView.Renderer {

    //顶点着色器
    private static final String vertextShaderSource =
            "#version 300 es                                        \n"
                    + "layout (location = 0) in vec4 vPosition;     \n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "    gl_Position = vPosition;                 \n"
                    + "}                                            \n";

    //片段着色器
    private static final String fragmentShaderSource =
            "#version 300 es		 			          	        \n"
                    + "precision mediump float;					  	\n"
                    + "out vec4 fragColor;	 			 		  	\n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  fragColor = vec4 ( 0.0, 1.0, 1.0, 1.0 );	\n"
                    + "}                                            \n";


    private static final float[] vertices = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f};
    private final FloatBuffer verticesBuffer;

    private int shaderProgram;
    private int vPosition;

    public TriangleRender() {
        // android 平台上，app 运行在 jvm 中，内存由 jvm 管理，而 opengles 运行在 native 环境，
        // 所以为了使 opengles 能够访问图形顶点数据，需要把顶点拷贝到 native 内存中。
        verticesBuffer = ByteBuffer.allocateDirect(vertices.length * 4) //直接分配 native 内存，不会被gc
                .order(ByteOrder.nativeOrder()) //和本地平台保持一致的字节序（大/小头）
                .asFloatBuffer(); //将底层字节映射到FloatBuffer实例，方便使用
        verticesBuffer.put(vertices) //将顶点拷贝到 native 内存中
                .position(0); //每次 put position 都会 + 1，需要在绘制前重置为0
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.8f, 0.8f, 0.8f, 0f);

        //编译链接shader
        shaderProgram = Utils.buildProgram(vertextShaderSource, fragmentShaderSource);

        //获取 vPosition 属性的位置
        vPosition = GLES30.glGetAttribLocation(shaderProgram, "vPosition");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //使用shader
        GLES30.glUseProgram(shaderProgram);

        //加载顶点数据到 vPosition 属性位置
        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 0, verticesBuffer);
        GLES30.glEnableVertexAttribArray(vPosition);

        //绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        GLES30.glDisableVertexAttribArray(vPosition);

    }
}
