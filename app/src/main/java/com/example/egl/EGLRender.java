package com.example.egl;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.example.videofilter.R;
import com.example.videofilter.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class EGLRender extends HandlerThread {

    private EGLConfig eglConfig;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private final Context context;

    public EGLRender(Context context) {
        super("ELGRender");
        this.context = context;
    }

    private void createEGL() {
        //获取显示设备
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("egl error:" + EGL14.eglGetError());
        }
        //初始化EGL
        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("egl error:" + EGL14.eglGetError());
        }
        //EGL选择配置
        int[] configAttributeList = {
                EGL14.EGL_BUFFER_SIZE, 32,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };
        int[] numConfig = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(eglDisplay,
                configAttributeList, 0,
                configs, 0, configs.length,
                numConfig, 0)) {
            throw new RuntimeException("egl error:" + EGL14.eglGetError());
        }
        eglConfig = configs[0];
        //创建ELG上下文
        int[] contextAttributeList = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttributeList, 0);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("egl error:" + EGL14.eglGetError());
        }
    }

    private void destroyEGL() {
        EGL14.eglDestroyContext(eglDisplay, eglContext);
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglDisplay = EGL14.EGL_NO_DISPLAY;
    }

    @Override
    public synchronized void start() {
        super.start();
        new Handler(getLooper()).post(new Runnable() {
            @Override
            public void run() {
                createEGL();
            }
        });
    }

    public void release() {
        new Handler(getLooper()).post(new Runnable() {
            @Override
            public void run() {
                destroyEGL();
                quit();
            }
        });
    }

    public void render(Surface surface, int width, int height) {
        //创建屏幕上渲染区域：EGL窗口
        int[] surfaceAttributeList = {EGL14.EGL_NONE};
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttributeList, 0);
        //指定当前上下文
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        //获取着色器
        int vertexShader = EGLUtils.loadShader(GLES30.GL_VERTEX_SHADER, EGLUtils.loadShaderSource(context, R.raw.triangle_vertex_shader));
        int fragmentShader = EGLUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, EGLUtils.loadShaderSource(context, R.raw.triangle_fragment_shader));
        //创建并连接程序
        int program = EGLUtils.createAndLinkProgram(vertexShader, fragmentShader);
        //设置清除渲染时的颜色
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        //设置视口
        GLES30.glViewport(0, 0, width, height);
        //获取顶点、颜色数据
        FloatBuffer vertexBuffer = EGLUtils.getVertextBuffer();
        FloatBuffer vertexColorBuffer = EGLUtils.getVertexColorBuffer();
        //擦除屏幕
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        //使用程序
        GLES30.glUseProgram(program);
        //绑定顶点、颜色数据到指定属性位置
        int aPosition = GLES30.glGetAttribLocation(program, "aPosition");
        GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(aPosition);
        int aColor = GLES30.glGetAttribLocation(program, "aColor");
        GLES30.glEnableVertexAttribArray(aColor);
        GLES30.glVertexAttribPointer(aColor, 4, GLES30.GL_FLOAT, false, 0, vertexColorBuffer);
        //绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
        //交换 surface 和显示器缓存
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        //释放
        EGL14.eglDestroySurface(eglDisplay, eglSurface);
    }

}
