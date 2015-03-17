/*
 * Copyright (C) 2015 G-Wearable Inc.
 * All rights reserved.
 */

package com.children.littlewalter.soundrecorder;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by peter on 3/10/15.
 */
public class RecordService extends Service {

    private final String LOG_TAG = "RecordService";

    private MediaRecorder mRecorder;

    private String mFilePath;

    private boolean mIsRecording = false;

    private final IBinder mServiceBinder = new RecordServiceBinder();

    public class RecordServiceBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public String getAudioFilePath() {
        return mFilePath;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startRecord() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File dirpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/audio/");
        Log.d(LOG_TAG, dirpath.getAbsolutePath());
        if (!dirpath.exists()) {
            dirpath.mkdir();
        }
        String prefix = DateFormat.format("yyyy-MM-dd_hh_mm_ss", System.currentTimeMillis())
        .toString();
        File audiofile;
        try {
            audiofile = File.createTempFile(prefix, ".3gp", dirpath);
            mFilePath = audiofile.getAbsolutePath();
            mRecorder.setOutputFile(mFilePath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
            mRecorder.start();
            mIsRecording = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        if (mIsRecording) {
            mRecorder.stop();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DATA, mFilePath);
            getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
        if (mRecorder != null) {
            mRecorder.release();
        }
        mIsRecording = false;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public int getMaxAmplitude() {
        if (mRecorder != null) {
            int maxvalue = mRecorder.getMaxAmplitude();
            Log.d(LOG_TAG, "max amplitude:" + maxvalue);
            return maxvalue;
        }
        return 0;
    }

}
