package com.example.egl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.videofilter.R;

public class EGLActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private EGLRender mEGLRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl);

        mSurfaceView = findViewById(R.id.sv);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mEGLRender.render(holder.getSurface(), width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        mEGLRender = new EGLRender(this);
        mEGLRender.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEGLRender.release();
    }
}