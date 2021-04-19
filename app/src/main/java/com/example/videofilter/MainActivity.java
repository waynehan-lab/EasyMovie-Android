package com.example.videofilter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = findViewById(R.id.glSV);
        if (!checkGLVersion()) {
            finish();
        }
        //使用 OpenGLES 3.0
        mGLSurfaceView.setEGLContextClientVersion(3);
        //设置渲染器
        mGLSurfaceView.setRenderer(new TextureRender(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //执行渲染
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停渲染
        mGLSurfaceView.onPause();
    }

    /**
     * 检验是否支持 OpenGLES 3.0
     *
     * @return
     */
    private boolean checkGLVersion() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo ci = am.getDeviceConfigurationInfo();
        return ci.reqGlEsVersion >= 0x30000;
    }

}
