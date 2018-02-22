package com.ifmo.youshare;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioFrameGrabber {
    private Thread thread;
    private boolean cancel = false;
    private int frequency;
    private FrameCallback frameCallback;

    public void setFrameCallback(FrameCallback callback) {
        frameCallback = callback;
    }

    /**
     * Starts recording.
     *
     * @param frequency - Recording frequency.
     */
    public void start(int frequency) {
        Log.d(MainActivity.APP_NAME, "start");

        this.frequency = frequency;

        cancel = false;
        thread = new Thread(this::recordThread);
        thread.start();
    }

    /**
     * Records audio and pushes to buffer.
     */
    public void recordThread() {
        Log.d(MainActivity.APP_NAME, "recordThread");

        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        Log.i(MainActivity.APP_NAME, "AudioRecord buffer size: " + bufferSize);

        // 16 bit PCM stereo recording was chosen as example.
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, frequency, channelConfiguration,
                audioEncoding, bufferSize);
        recorder.startRecording();

        // Make bufferSize be in samples instead of bytes.
        bufferSize /= 2;
        short[] buffer = new short[bufferSize];
        while (!cancel) {
            int bufferReadResult = recorder.read(buffer, 0, bufferSize);
            // Utils.Debug("bufferReadResult: " + bufferReadResult);
            if (bufferReadResult > 0) {
                frameCallback.handleFrame(buffer, bufferReadResult);
            } else if (bufferReadResult < 0) {
                Log.w(MainActivity.APP_NAME, "Error calling recorder.read: " + bufferReadResult);
            }
        }
        recorder.stop();

        Log.d(MainActivity.APP_NAME, "exit recordThread");
    }

    /**
     * Stops recording.
     */
    public void stop() {
        Log.d(MainActivity.APP_NAME, "stop");

        cancel = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(MainActivity.APP_NAME, "", e);
        }
    }

    public interface FrameCallback {
        void handleFrame(short[] audio_data, int length);
    }
}
