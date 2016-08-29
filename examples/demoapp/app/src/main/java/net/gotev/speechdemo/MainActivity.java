package net.gotev.speechdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import net.gotev.speech.DelayedOperation;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionException;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.toyproject.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    private Button button;
    private Button speak;
    private TextView text;
    private DelayedOperation delayedStopListening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(view -> onButtonClick());

        speak = (Button) findViewById(R.id.speak);
        speak.setOnClickListener(view -> onSpeakClick());

        text = (TextView) findViewById(R.id.text);

        delayedStopListening = new DelayedOperation(this, "delayStopListen", 1500);
    }

    private void onButtonClick() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            RxPermissions.getInstance(this)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .subscribe(granted -> {
                        if (granted) { // Always true pre-M
                            onRecordAudioPermissionGranted();
                        } else {
                            Toast.makeText(MainActivity.this, "You need to grant permission", Toast.LENGTH_LONG);
                        }
                    });
        }
    }

    private void onRecordAudioPermissionGranted() {
        try {
            Speech.getInstance().startListening(MainActivity.this);
            delayedStopListening.start(new DelayedOperation.Operation() {
                @Override
                public void onDelayedOperation() {
                    Speech.getInstance().stopListening();
                }

                @Override
                public boolean shouldExecuteDelayedOperation() {
                    return true;
                }
            });
        } catch (SpeechRecognitionNotAvailable exc) {
            Toast.makeText(this, "Speech recognition is not available on this device!", Toast.LENGTH_LONG).show();
        }
    }

    private void onSpeakClick() {
        Speech.getInstance().say(getString(R.string.demo_tts));
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechResult(String result) {
        delayedStopListening.cancel();
        text.setText("Result: " + result);
        Speech.getInstance().say(result);
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        text.setText("Partial: ");
        for (String partial : results) {
            text.append(partial + " ");
        }
        delayedStopListening.resetTimer();
    }

    @Override
    public void onStartOfSpeech() {
        Log.i(getClass().getSimpleName(), "Speech recognition started");
        button.setText(getString(R.string.stop_listening));
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(getClass().getSimpleName(), "Speech recognition ended");
        button.setText(getString(R.string.start_listening));
    }

    @Override
    public void onError(SpeechRecognitionException exception) {
        Log.e(getClass().getSimpleName(), "Speech recognition error", exception);
    }
}
