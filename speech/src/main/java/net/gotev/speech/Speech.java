package net.gotev.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to easily work with Android speech recognition.
 *
 * @author Aleksandar Gotev
 */
public class Speech {

    private static Speech instance = null;

    private RecognitionListener mListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
            mPartialData.clear();
            mUnstableData = null;
        }

        @Override
        public void onRmsChanged(float v) {
            mDelegate.onSpeechRmsChanged(v);
        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            mDelegate.onEndOfSpeech();
        }

        @Override
        public void onError(int code) {
            mDelegate.onError(new SpeechRecognitionException(code));
        }

        @Override
        public void onResults(Bundle bundle) {
            Log.i(getClass().getSimpleName(), "stopping delayed force stop");
            mDelayedForceStop.cancel();

            List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (results == null || results.isEmpty()) {
                Log.e(Speech.class.getSimpleName(), "No speech results");
                return;
            }

            String result = results.get(0);

            if (result == null || result.isEmpty()) {
                Log.e(Speech.class.getSimpleName(), "Empty speech result");
                return;
            }

            mIsListening = false;
            mDelegate.onSpeechResult(result);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            List<String> partialResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            List<String> unstableData = bundle.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");

            if (partialResults != null && !partialResults.isEmpty()) {
                mPartialData.clear();
                mPartialData.addAll(partialResults);
                mUnstableData = unstableData != null && !unstableData.isEmpty()
                              ? unstableData.get(0) : null;
                mDelegate.onSpeechPartialResults(partialResults);
            }
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    private SpeechRecognizer mSpeechRecognizer;
    private String mCallingPackage;
    private boolean mPreferOffline = false;
    private boolean mGetPartialResults = true;
    private SpeechDelegate mDelegate;
    private boolean mIsListening = false;

    private List<String> mPartialData = new ArrayList<>();
    private String mUnstableData;

    private DelayedOperation mDelayedForceStop;
    private Context mContext;

    private TextToSpeech mTextToSpeech;
    private Locale mTtsLocale = Locale.getDefault();
    private float mTtsRate = 1.0f;
    private float mTtsPitch = 1.3f;
    private long mForceStopDelayInMs = 2000;

    private Speech(Context context) {
        commonInitializer(context);
    }

    private Speech(Context context, String callingPackage) {
        commonInitializer(context);
        mCallingPackage = callingPackage;
    }

    private void commonInitializer(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context must be defined!");

        mContext = context;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(mListener);

        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                switch (status) {
                    case TextToSpeech.SUCCESS:
                        Log.i(Speech.class.getSimpleName(), "TextToSpeech engine successfully started");
                        break;

                    case TextToSpeech.ERROR:
                        Log.e(Speech.class.getSimpleName(), "Error while initializing TextToSpeech engine!");
                        break;

                    default:
                        Log.e(Speech.class.getSimpleName(), "Unknown TextToSpeech status: " + status);
                        break;
                }
            }
        });

        if (mDelayedForceStop == null)
            mDelayedForceStop = new DelayedOperation(context, "delayForceStop", mForceStopDelayInMs);
    }

    /**
     * Initializes speech recognition.
     *
     * @param context application context
     */
    public static void init(Context context) {
        if (instance == null) {
            instance = new Speech(context);
        }
    }

    /**
     * Initializes speech recognition.
     *
     * @param context application context
     * @param callingPackage The extra key used in an intent to the speech recognizer for
     *                       voice search. Not generally to be used by developers.
     *                       The system search dialog uses this, for example, to set a calling
     *                       package for identification by a voice search API.
     *                       If this extra is set by anyone but the system process,
     *                       it should be overridden by the voice search implementation.
     *                       By passing null or empty string (which is the default) you are
     *                       not overriding the calling package
     */
    public static void init(Context context, String callingPackage) {
        if (instance == null) {
            instance = new Speech(context, callingPackage);
        }
    }

    /**
     * Gets speech recognition instance.
     * @return @link{SpeechRecognition} instance
     */
    public static Speech getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Speech recognition has not been initialized! call init method first!");
        }

        return instance;
    }

    /**
     * Set whether to only use an offline speech recognition engine.
     * The default is false, meaning that either network or offline recognition engines may be used.
     * @param preferOffline true to prefer offline engine, false to use either one of the two
     */
    public void setPreferOffline(boolean preferOffline) {
        mPreferOffline = preferOffline;
    }

    /**
     * Set whether partial results should be returned by the recognizer as the user speaks
     * (default is true). The server may ignore a request for partial results in some or all cases.
     * @param getPartialResults true to get also partial recognition results, false otherwise
     */
    public void setGetPartialResults(boolean getPartialResults) {
        mGetPartialResults = getPartialResults;
    }

    /**
     * Starts voice recognition with the device default language.
     */
    public void startListening(SpeechDelegate delegate) {
        startListening(delegate, null);
    }

    /**
     * Starts voice recognition with a custom recognition language.
     * @param overrideLanguage custom recognition language in the form of "en-US". Null to use
     *                         the device default language
     */
    public void startListening(SpeechDelegate delegate, String overrideLanguage) {
        if (delegate == null)
            throw new IllegalArgumentException("delegate must be defined!");

        mDelegate = delegate;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, mGetPartialResults)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (overrideLanguage != null && !overrideLanguage.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, overrideLanguage);
        }

        if (mCallingPackage != null && !mCallingPackage.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mCallingPackage);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, mPreferOffline);
        }

        mSpeechRecognizer.startListening(intent);
        mIsListening = true;
        mDelegate.onStartOfSpeech();

    }

    public void stopListening() {
        mIsListening = false;
        mSpeechRecognizer.stopListening();

        mDelayedForceStop.start(new DelayedOperation.Operation() {
            @Override
            public void onDelayedOperation() {
                Log.i(Speech.class.getSimpleName(), "forcefully stop speech recognizer");
                mSpeechRecognizer.cancel();
                mSpeechRecognizer.destroy();
                mSpeechRecognizer = null;

                StringBuilder out = new StringBuilder();

                for (String partial : mPartialData) {
                    out.append(partial).append(" ");
                }

                if (mUnstableData != null && !mUnstableData.isEmpty())
                    out.append(mUnstableData);

                mDelegate.onEndOfSpeech();
                mDelegate.onSpeechResult(out.toString());

                // recreate the speech recognizer
                commonInitializer(mContext);
            }

            @Override
            public boolean shouldExecuteDelayedOperation() {
                return true;
            }
        });
    }

    public boolean isListening() {
        return mIsListening;
    }

    public void setTextToSpeechLocale(Locale locale) {
        mTtsLocale = locale;
    }

    public void setTextToSpeechRate(float rate) {
        mTtsRate = rate;
    }

    public void setTextToSpeechPitch(float pitch) {
        mTtsPitch = pitch;
    }

    public void setForceStopDelay(long milliseconds) {
        mForceStopDelayInMs = milliseconds;
    }

    public void say(String message) {
        mTextToSpeech.setLanguage(mTtsLocale);
        mTextToSpeech.setPitch(mTtsPitch);
        mTextToSpeech.setSpeechRate(mTtsRate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTextToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
