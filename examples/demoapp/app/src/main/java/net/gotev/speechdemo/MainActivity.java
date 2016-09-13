package net.gotev.speechdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.TextToSpeechCallback;
import net.gotev.speech.ui.SpeechProgressView;
import net.gotev.toyproject.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    private Button button;
    private Button speak;
    private TextView text;
    private SpeechProgressView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(view -> onButtonClick());

        speak = (Button) findViewById(R.id.speak);
        speak.setOnClickListener(view -> onSpeakClick());

        text = (TextView) findViewById(R.id.text);
        progress = (SpeechProgressView) findViewById(R.id.progress);
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
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, MainActivity.this);
        } catch (SpeechRecognitionNotAvailable exc) {
            Toast.makeText(this, "Speech recognition is not available on this device!", Toast.LENGTH_LONG).show();
        }
    }

    private void onSpeakClick() {
        Speech.getInstance().say(getString(R.string.demo_tts), new TextToSpeechCallback() {
            @Override
            public void onStart() {
                Toast.makeText(MainActivity.this, "onStart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "onCompleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechResult(String result) {
        button.setText(getString(R.string.start_listening));
        Log.i(getClass().getSimpleName(), "onSpeechResult");
        text.setText("Result: " + result);

        if (result.isEmpty()) {
            Speech.getInstance().say("Ripeti per favore");

        } else {
            Speech.getInstance().say(result);
        }
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        text.setText("Partial: ");
        for (String partial : results) {
            text.append(partial + " ");
        }
    }

    @Override
    public void onStartOfSpeech() {
        Log.i(getClass().getSimpleName(), "onStartOfSpeech");
        button.setText(getString(R.string.stop_listening));
    }

}
