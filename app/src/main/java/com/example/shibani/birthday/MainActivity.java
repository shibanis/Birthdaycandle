package com.example.shibani.birthday;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int sampleRate = 8000;
    private AudioRecord audio;
    private int bufferSize;
    private double soundLevel = 0;
    private Thread thread;
    private static final int SAMPLE_DELAY = 75;
    private ImageView cakeImage;
    private boolean blown=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cakeImage = (ImageView)findViewById(R.id.birthdaycake);
        cakeImage.setKeepScreenOn(true);

        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }
    }

    protected void onResume() {
        super.onResume();
        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audio.startRecording();
        thread = new Thread(new Runnable() {
            public void run() {
                while(thread != null && !thread.isInterrupted()){
                    //Let's make the thread sleep for a the approximate sampling time
                    try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                    readAudioBuffer();//After this call we can get the last value assigned to the soundLevel variable

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if(soundLevel > 0 && soundLevel <= 500 && blown==false){
                                cakeImage.setImageResource(R.drawable.bdaywish);
                            }else
                            if(soundLevel > 0 && soundLevel <= 500 && blown==true){
                                cakeImage.setImageResource(R.drawable.bdaywishblown);
                                blown=true;

                            }else
                            if(soundLevel > 500 ){
                                cakeImage.setImageResource(R.drawable.bdaywishblown);
                                blown=true;
                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }

    /**
     * Functionality that gets the sound level out of the sample
     */
    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult = 1;

            if (audio != null) {

                // Sense the voice...
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                double calTot = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    calTot += buffer[i];
                }
                soundLevel = Math.abs((calTot / bufferReadResult));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        thread.interrupt();
        thread = null;
        try {
            if (audio != null) {
                audio.stop();
                audio.release();
                audio = null;
            }
        } catch (Exception e) {e.printStackTrace();}
    }
}
