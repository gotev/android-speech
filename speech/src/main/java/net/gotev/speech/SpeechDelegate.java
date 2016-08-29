package net.gotev.speech;

import java.util.List;

/**
 * Speech delegate interface. It contains the methods to receive speech events.
 *
 * @author Aleksandar Gotev
 */
public interface SpeechDelegate {

    /**
     * The sound level in the audio stream has changed.
     * There is no guarantee that this method will be called.
     * @param value the new RMS dB value
     */
    void onSpeechRmsChanged(float value);

    /**
     * Invoked when there is a speech result
     * @param result string resulting from speech recognition.
     *               This is ensured to be non null and non empty.
     */
    void onSpeechResult(String result);

    /**
     * Invoked when there are partial speech results.
     * @param results list of strings. This is ensured to be non null and non empty.
     */
    void onSpeechPartialResults(List<String> results);

    /**
     * Invoked when the speech recognition is started.
     */
    void onStartOfSpeech();

    /**
     * Invoked after the user stops speaking.
     */
    void onEndOfSpeech();

    /**
     * Invoked when an error occurs during speech recognition.
     * @param exception exception which caused the error
     */
    void onError(SpeechRecognitionException exception);
}
