package com.pvr.videoplugin;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

public class VideoPlugin implements OnFrameAvailableListener {

    private final Activity mActivity;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private FilterFBOTexture mFilterFBOTexture;
    private MediaPlayer mMediaPlayer;
    private boolean mIsUpdateFrame;

    public VideoPlugin(Activity activity) {
        mActivity = activity;
    }

    public void start(int unityTextureId, int width, int height) {
        FBOUtils.log("start, unityTextureId=" + unityTextureId);

        int videoTextureId = FBOUtils.createOESTextureID();

        mSurfaceTexture = new SurfaceTexture(videoTextureId);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mSurface = new Surface(mSurfaceTexture);

        mFilterFBOTexture = new FilterFBOTexture(width, height, unityTextureId, videoTextureId);

        initMediaPlayer();
    }

    public void release() {
        FBOUtils.log("release");
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
        if (mFilterFBOTexture != null) {
            mFilterFBOTexture.release();
            mFilterFBOTexture = null;
        }
        mIsUpdateFrame = false;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(mSurface);
        try {
            AssetFileDescriptor fd = mActivity.getAssets().openFd("test.mp4");
//            final File file = new File("/sdcard/test.mp4");
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
//            mMediaPlayer.setDataSource(Uri.fromFile(file).toString());
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                FBOUtils.log("MediaPlayer onPrepared");
                mMediaPlayer.start();
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        FBOUtils.log("onFrameAvailable");
        mIsUpdateFrame = true;
    }

    public void updateTexture() {
//        FBOUtils.log("updateTexture");
        mIsUpdateFrame = false;
        mSurfaceTexture.updateTexImage();
        mFilterFBOTexture.draw();
    }

    public boolean isUpdateFrame() {
        return mIsUpdateFrame;
    }

}
