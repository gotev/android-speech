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
import java.util.Date;
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
            mDelayedStopListening.cancel();

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
            mDelayedStopListening.resetTimer();

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
    private DelayedOperation mDelayedStopListening;
    private Context mContext;

    private TextToSpeech mTextToSpeech;
    private Locale mLocale = Locale.getDefault();
    private float mTtsRate = 1.0f;
    private float mTtsPitch = 1.0f;
    private long mForceStopDelayInMs = 2000;
    private long mStopListeningDelayInMs = 3000;
    private long mMinimumStartStopDelay = 1000;
    private long mStartTimestamp;

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

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizer.setRecognitionListener(mListener);
        } else {
            mSpeechRecognizer = null;
        }

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

        initDelayedForceStop(context);
        initDelayedStopListening(context);
    }

    private void initDelayedForceStop(Context context) {
        if (mDelayedForceStop != null) {
            mDelayedForceStop.cancel();
            mDelayedForceStop = null;
        }

        mDelayedForceStop = new DelayedOperation(context, "delayForceStop", mForceStopDelayInMs);
    }

    private void initDelayedStopListening(Context context) {
        if (mDelayedStopListening != null) {
            mDelayedStopListening.cancel();
            mDelayedStopListening = null;
        }

        mDelayedStopListening = new DelayedOperation(context, "delayStopListening", mStopListeningDelayInMs);
    }

    /**
     * Initializes speech recognition.
     *
     * @param context application context
     * @return speech instance
     */
    public static Speech init(Context context) {
        if (instance == null) {
            instance = new Speech(context);
        }

        return instance;
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
     * @return speech instance
     */
    public static Speech init(Context context, String callingPackage) {
        if (instance == null) {
            instance = new Speech(context, callingPackage);
        }

        return instance;
    }

    /**
     * Gets speech recognition instance.
     * @return SpeechRecognition instance
     */
    public static Speech getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Speech recognition has not been initialized! call init method first!");
        }

        return instance;
    }

    /**
     * Starts voice recognition with the device default language.
     *
     * @param delegate delegate which will receive speech recognition events and status
     * @throws SpeechRecognitionNotAvailable when speech recognition is not available on the device
     */
    public void startListening(SpeechDelegate delegate)
        throws SpeechRecognitionNotAvailable {
        startListening(delegate, null);
    }

    /**
     * Starts voice recognition with a custom recognition language.
     * @param delegate delegate which will receive speech recognition events and status
     * @param overrideLanguage custom recognition language in the form of "en-US". Null to use
     *                         the device default language
     * @throws SpeechRecognitionNotAvailable when speech recognition is not available on the device
     */
    public void startListening(SpeechDelegate delegate, String overrideLanguage)
            throws SpeechRecognitionNotAvailable{
        if (mIsListening) return;

        if (mSpeechRecognizer == null)
            throw new SpeechRecognitionNotAvailable();

        if (delegate == null)
            throw new IllegalArgumentException("delegate must be defined!");

        mDelegate = delegate;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, mGetPartialResults)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLocale.getLanguage())
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
        mStartTimestamp = new Date().getTime();
        mDelegate.onStartOfSpeech();

        mDelayedStopListening.start(new DelayedOperation.Operation() {
            @Override
            public void onDelayedOperation() {
                stopListening();
            }

            @Override
            public boolean shouldExecuteDelayedOperation() {
                return true;
            }
        });

    }

    /**
     * Stops voice recognition listening.
     * This method does nothing if voice listening is not active
     */
    public void stopListening() {
        if (!mIsListening) return;

        if (new Date().getTime() <= (mStartTimestamp + mMinimumStartStopDelay)) {
            Log.d(getClass().getSimpleName(), "Hey man calm down! Throttling stop to prevent disaster!");
            return;
        }

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

    /**
     * Check if voice recognition is currently active.
     * @return true if the voice recognition is on, false otherwise
     */
    public boolean isListening() {
        return mIsListening;
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     * @param message message to play
     */
    public void say(String message) {
        mTextToSpeech.setLanguage(mLocale);
        mTextToSpeech.setPitch(mTtsPitch);
        mTextToSpeech.setSpeechRate(mTtsRate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTextToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /**
     * Stops text to speech.
     */
    public void stopTextToSpeech() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }

    /**
     * Set whether to only use an offline speech recognition engine.
     * The default is false, meaning that either network or offline recognition engines may be used.
     * @param preferOffline true to prefer offline engine, false to use either one of the two
     * @return speech instance
     */
    public Speech setPreferOffline(boolean preferOffline) {
        mPreferOffline = preferOffline;
        return this;
    }

    /**
     * Set whether partial results should be returned by the recognizer as the user speaks
     * (default is true). The server may ignore a request for partial results in some or all cases.
     * @param getPartialResults true to get also partial recognition results, false otherwise
     * @return speech instance
     */
    public Speech setGetPartialResults(boolean getPartialResults) {
        mGetPartialResults = getPartialResults;
        return this;
    }

    public Speech setLocale(Locale locale) {
        mLocale = locale;
        return this;
    }

    public Speech setTextToSpeechRate(float rate) {
        mTtsRate = rate;
        return this;
    }

    public Speech setTextToSpeechPitch(float pitch) {
        mTtsPitch = pitch;
        return this;
    }

    public Speech setForceStopDelay(long milliseconds) {
        mForceStopDelayInMs = milliseconds;
        initDelayedForceStop(mContext);
        return this;
    }

    public Speech setStopListeningAfterInactivity(long milliseconds) {
        mStopListeningDelayInMs = milliseconds;
        initDelayedStopListening(mContext);
        return this;
    }

    public Speech setMinimumStartStopDelay(long milliseconds) {
        mMinimumStartStopDelay = milliseconds;
        return this;
    }

}
