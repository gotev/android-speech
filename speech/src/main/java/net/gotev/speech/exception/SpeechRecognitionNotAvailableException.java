package net.gotev.speech.exception;

/**
 * @author Aleksandar Gotev
 */
public class SpeechRecognitionNotAvailableException extends Exception {
    public SpeechRecognitionNotAvailableException() {
        super("Speech recognition not available");
    }
}
