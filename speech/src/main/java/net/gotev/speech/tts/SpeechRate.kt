package net.gotev.speech.tts

sealed class SpeechRate(val value: Float) {
    object Slowest : SpeechRate(0.1f)
    object Half : SpeechRate(0.5f)
    object Slower : SpeechRate(0.75f)
    object Normal : SpeechRate(1.0f)
    object Faster : SpeechRate(1.5f)
    object Double : SpeechRate(2.0f)
    class Custom(value: Float) : SpeechRate(value)
}
