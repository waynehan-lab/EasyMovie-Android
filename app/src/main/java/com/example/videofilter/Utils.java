package com.example.videofilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

public class Utils {

    public static void log(String msg) {
        Log.i("TestOpenGLES", msg);
    }

    // 根据类型编译着色器
    public static int compileShader(int type, String shaderCode) {
        // 根据不同的类型创建着色器 ID
        final int shaderObjectId = GLES30.glCreateShader(type);
        if (shaderObjectId == 0) {
            // 创建失败
            return 0;
        }
        // 将着色器 ID 和着色器程序内容连接
        GLES30.glShaderSource(shaderObjectId, shaderCode);
        // 编译着色器
        GLES30.glCompileShader(shaderObjectId);
        // 为验证编译结果是否失败
        final int[] compileStatus = new int[1];
        // glGetShaderiv函数比较通用，在着色器阶段和 OpenGL 程序阶段都会通过它来验证结果
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

    // 创建 OpenGL 程序过程
    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        // 编译顶点着色器
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource);
        // 编译片段着色器
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource);
        // 链接着色器
        int program = linkProgram(vertexShader, fragmentShader);
        boolean valid = validateProgram(program);
        log("buildProgram valid = " + valid);
        return program;
    }

    // 链接了 OpenGL 程序后，就是验证 OpenGL 是否可用
    public static boolean validateProgram(int programObjectId) {
        GLES30.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, GLES30.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

    // 检查是否有错
    public static void checkError(String msg) {
        log(msg + " -- error --> " + GLES30.glGetError());
    }

    // 加载图片
    public static int loadTexture(Context context, int resId) {
        //创建纹理对象
        int[] textureIds = new int[1];
        //生成纹理：纹理数量、保存纹理的数组，数组偏移量
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            throw new RuntimeException("创建纹理对象失败");
        }
        //原尺寸加载位图资源（禁止缩放）
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        if (bitmap == null) {
            //删除纹理对象
            GLES30.glDeleteTextures(1, textureIds, 0);
            throw new RuntimeException("加载位图失败");
        }
        //绑定纹理到opengl
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);
        //设置放大、缩小时的纹理过滤方式，必须设定，否则纹理全黑
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        //将位图加载到opengl中，并复制到当前绑定的纹理对象上
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        //创建 mipmap 贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        //释放bitmap资源（上面已经把bitmap的数据复制到纹理上了）
        bitmap.recycle();
        //解绑当前纹理，防止其他地方以外改变该纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        //返回纹理对象
        return textureIds[0];
    }


}
