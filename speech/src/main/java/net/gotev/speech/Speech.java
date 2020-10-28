package net.gotev.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import net.gotev.speech.listener.BaseSpeechRecognitionListener;
import net.gotev.speech.listener.DummyOnInitListener;
import net.gotev.speech.listener.SpeechRecognitionListener;
import net.gotev.speech.listener.BaseTextToSpeechListener;
import net.gotev.speech.listener.TextToSpeechListener;
import net.gotev.speech.ui.SpeechProgressView;

import java.util.Locale;

/**
 * Helper class to easily work with Android speech recognition.
 *
 * @author Aleksandar Gotev
 */
public class Speech {

    private static Speech instance = null;

    private Context mContext;

    private TextToSpeechListener ttsListener;
    private SpeechRecognitionListener speechRecognitionListener;

    private Speech(final Context context) {
        this(context, null, new DummyOnInitListener(), new BaseSpeechRecognitionListener());
    }

    private Speech(final Context context, final String callingPackage) {
        this(context, callingPackage, new DummyOnInitListener(), new BaseSpeechRecognitionListener());
    }

    public Speech(final Context context, final String callingPackage, TextToSpeech.OnInitListener onInitListener) {
        this(context, callingPackage, onInitListener, new BaseSpeechRecognitionListener());
    }

    public Speech(final Context context, final String callingPackage, TextToSpeech.OnInitListener onInitListener, SpeechRecognitionListener speechRecognitionListener) {
        this(context, callingPackage, onInitListener, speechRecognitionListener, new BaseTextToSpeechListener());
    }

    public Speech(final Context context, final String callingPackage, TextToSpeech.OnInitListener onInitListener, SpeechRecognitionListener speechRecognitionListener, TextToSpeechListener textToSpeechListener) {
        mContext = context;

        this.speechRecognitionListener = speechRecognitionListener;
        this.speechRecognitionListener.setCallingPackage(callingPackage);
        this.speechRecognitionListener.initSpeechRecognizer(context);

        this.ttsListener = textToSpeechListener;
        this.ttsListener.setOnInitListener(onInitListener);
        this.ttsListener.initTextToSpeech(context);
    }

    /**
     * Initializes speech recognition.
     *
     * @param context application context
     * @return speech instance
     */
    public static Speech init(final Context context) {
        if (instance == null) {
            instance = new Speech(context);
        }

        return instance;
    }

    /**
     * Initializes speech recognition.
     *
     * @param context        application context
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
    public static Speech init(final Context context, final String callingPackage) {
        if (instance == null) {
            instance = new Speech(context, callingPackage);
        }

        return instance;
    }

    public static Speech init(final Context context, final String callingPackage, TextToSpeech.OnInitListener onInitListener) {
        if (instance == null) {
            instance = new Speech(context, callingPackage, onInitListener);
        }

        return instance;
    }

    /**
     * Must be called inside Activity's onDestroy.
     */
    public synchronized void shutdown() {
        speechRecognitionListener.shutdown();
        ttsListener.shutdown();

        instance = null;
    }

    /**
     * Gets speech recognition instance.
     *
     * @return SpeechRecognition instance
     */
    public static Speech getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Speech recognition has not been initialized! call init method first!");
        }

        return instance;
    }

    /**
     * Starts voice recognition.
     *
     * @param delegate delegate which will receive speech recognition events and status
     * @throws SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws GoogleVoiceTypingDisabledException when google voice typing is disabled on the device
     */
    public void startListening(final SpeechDelegate delegate)
            throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {
        startListening(null, delegate);
    }

    /**
     * Starts voice recognition.
     *
     * @param progressView view in which to draw speech animation
     * @param delegate     delegate which will receive speech recognition events and status
     * @throws SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws GoogleVoiceTypingDisabledException when google voice typing is disabled on the device
     */
    public void startListening(final SpeechProgressView progressView, final SpeechDelegate delegate)
            throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {

        speechRecognitionListener.startListening(progressView, delegate);
    }

    /**
     * Stops voice recognition listening.
     * This method does nothing if voice listening is not active
     */
    public void stopListening() {
        speechRecognitionListener.stopListening();
        returnPartialResultsAndRecreateSpeechRecognizer();
    }

    private void returnPartialResultsAndRecreateSpeechRecognizer() {
        speechRecognitionListener.returnPartialResultsAndRecreateSpeechRecognizer();

        // recreate the speech recognizer
        speechRecognitionListener.initSpeechRecognizer(mContext);
    }

    /**
     * Check if voice recognition is currently active.
     *
     * @return true if the voice recognition is on, false otherwise
     */
    public boolean isListening() {
        return speechRecognitionListener.isListening();
    }

    /**
     * Check if text to speak is currently speaking.
     *
     * @return true if the text to speak is speaking, false otherwise
     */
    public boolean isSpeaking() {
        return ttsListener.isSpeaking();
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message message to play
     */
    public void say(final String message) {
        say(message, null);
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message  message to play
     * @param callback callback which will receive progress status of the operation
     */
    public void say(final String message, final TextToSpeechCallback callback) {
        ttsListener.say(message, callback);
    }

    /**
     * Stops text to speech.
     */
    public void stopTextToSpeech() {
        ttsListener.stop();
    }

    /**
     * Set whether to only use an offline speech recognition engine.
     * The default is false, meaning that either network or offline recognition engines may be used.
     *
     * @param preferOffline true to prefer offline engine, false to use either one of the two
     * @return speech instance
     */
    public Speech setPreferOffline(final boolean preferOffline) {
        speechRecognitionListener.setPreferOffline(preferOffline);
        return this;
    }

    /**
     * Set whether partial results should be returned by the recognizer as the user speaks
     * (default is true). The server may ignore a request for partial results in some or all cases.
     *
     * @param getPartialResults true to get also partial recognition results, false otherwise
     * @return speech instance
     */
    public Speech setGetPartialResults(final boolean getPartialResults) {
        speechRecognitionListener.setPartialResults(getPartialResults);
        return this;
    }

    /**
     * Sets text to speech and recognition language.
     * Defaults to device language setting.
     *
     * @param locale new locale
     * @return speech instance
     */
    public Speech setLocale(final Locale locale) {
        speechRecognitionListener.setLocale(locale);
        ttsListener.setLocale(locale);
        return this;
    }

    /**
     * Sets the speech rate. This has no effect on any pre-recorded speech.
     *
     * @param rate Speech rate. 1.0 is the normal speech rate, lower values slow down the speech
     *             (0.5 is half the normal speech rate), greater values accelerate it
     *             (2.0 is twice the normal speech rate).
     * @return speech instance
     */
    public Speech setTextToSpeechRate(final float rate) {
        ttsListener.setSpeechRate(rate);
        return this;
    }

    /**
     * Sets the voice for the TextToSpeech engine.
     * This has no effect on any pre-recorded speech.
     *
     * @param voice Speech voice.
     * @return speech instance
     */
    public Speech setVoice(final Voice voice) {
        ttsListener.setVoice(voice);
        return this;
    }

    /**
     * Sets the speech pitch for the TextToSpeech engine.
     * This has no effect on any pre-recorded speech.
     *
     * @param pitch Speech pitch. 1.0 is the normal pitch, lower values lower the tone of the
     *              synthesized voice, greater values increase it.
     * @return speech instance
     */
    public Speech setTextToSpeechPitch(final float pitch) {
        ttsListener.setPitch(pitch);
        return this;
    }

    /**
     * Sets the idle timeout after which the listening will be automatically stopped.
     *
     * @param milliseconds timeout in milliseconds
     * @return speech instance
     */
    public Speech setStopListeningAfterInactivity(final long milliseconds) {
        speechRecognitionListener.setStopListeningAfterInactivity(milliseconds);
        speechRecognitionListener.init(mContext);
        return this;
    }

    /**
     * Sets the minimum interval between start/stop events. This is useful to prevent
     * monkey input from users.
     *
     * @param milliseconds minimum interval betweeb state change in milliseconds
     * @return speech instance
     */
    public Speech setTransitionMinimumDelay(final long milliseconds) {
        speechRecognitionListener.setTransitionMinimumDelay(milliseconds);
        return this;
    }

    /**
     * Sets the text to speech queue mode.
     * By default is TextToSpeech.QUEUE_FLUSH, which is faster, because it clears all the
     * messages before speaking the new one. TextToSpeech.QUEUE_ADD adds the last message
     * to speak in the queue, without clearing the messages that have been added.
     *
     * @param mode It can be either TextToSpeech.QUEUE_ADD or TextToSpeech.QUEUE_FLUSH.
     * @return speech instance
     */
    public Speech setTextToSpeechQueueMode(final int mode) {
        ttsListener.setTextToSpeechQueueMode(mode);
        return this;
    }

    /**
     * Sets the audio stream type.
     * By default is TextToSpeech.Engine.DEFAULT_STREAM, which is equivalent to
     * AudioManager.STREAM_MUSIC.
     *
     * @param audioStream A constant from AudioManager.
     *                    e.g. {@link android.media.AudioManager#STREAM_VOICE_CALL}
     * @return speech instance
     */
    public Speech setAudioStream(final int audioStream) {
        ttsListener.setAudioStream(audioStream);
        return this;
    }

}
