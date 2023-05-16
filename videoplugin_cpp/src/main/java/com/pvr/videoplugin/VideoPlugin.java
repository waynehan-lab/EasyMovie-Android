package com.pvr.videoplugin;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

public class VideoPlugin implements OnFrameAvailableListener {

    static {
        System.loadLibrary("videoplugin");
    }

    private final Activity mActivity;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private MediaPlayer mMediaPlayer;
    private boolean mIsUpdateFrame;

    private native int createOESTextureID();

    private native void renderInit(int width, int height, int textureId);

    private native void renderDraw();

    private native void renderRelease();

    public VideoPlugin(Activity activity) {
        mActivity = activity;
    }

    public void start(int textureId, int width, int height) {
        int oesTextureId = createOESTextureID();
        log("start, width=" + width + ", height=" + height + ", textureId=" + textureId + ", oesTextureId=" + oesTextureId);
        mSurfaceTexture = new SurfaceTexture(oesTextureId);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);

        renderInit(width, height, textureId);

        initMediaPlayer();
    }

    public void release() {
        log("release");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        renderRelease();
        mIsUpdateFrame = false;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(mSurface);
        try {
            AssetFileDescriptor fd = mActivity.getAssets().openFd("test.mp4");
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                log("MediaPlayer onPrepared");
                mMediaPlayer.start();
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        log("onFrameAvailable");
        mIsUpdateFrame = true;
    }

    public void updateTexture() {
        log("updateTexture");
        mIsUpdateFrame = false;
        mSurfaceTexture.updateTexImage();
        renderDraw();
    }

    public boolean isUpdateFrame() {
        return mIsUpdateFrame;
    }

    private void log(String msg) {
        Log.i("PVRFBO", msg);
    }

}
