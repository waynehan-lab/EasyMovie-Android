package com.example.videoplugin;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.IOException;

public class VideoPlugin implements OnFrameAvailableListener {

    private SurfaceTexture mSurfaceTexture;
    private FilterFBOTexture mFilterFBOTexture;
    private MediaPlayer mMediaPlayer;
    private boolean mIsUpdateFrame;

    public void start(int unityTextureId, int width, int height) {
        Log.i("VideoFilter", "start--->" + Thread.currentThread().toString());

        int videoTextureId = FBOUtils.createVideoTextureID();

        mSurfaceTexture = new SurfaceTexture(videoTextureId);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mFilterFBOTexture = new FilterFBOTexture(UnityPlayer.currentActivity, videoTextureId);
        mFilterFBOTexture.surfaceChanged(width, height, unityTextureId);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        try {
            final File file = new File("/sdcard/test.mp4");
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setDataSource(Uri.fromFile(file).toString());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("VideoFilter", "MediaPlayer onPrepared " + Thread.currentThread().toString());
                mMediaPlayer.start();
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.i("VideoFilter", "onFrameAvailable--->" + Thread.currentThread().toString());
        mIsUpdateFrame = true;
    }

    public void updateTexture() {
        Log.i("VideoFilter", "updateTexture--->" + Thread.currentThread().toString());
        mIsUpdateFrame = false;
        mSurfaceTexture.updateTexImage();
        mFilterFBOTexture.draw();
    }

    public boolean isUpdateFrame() {
        return mIsUpdateFrame;
    }

}
