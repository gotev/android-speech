package net.gotev.speech.tts

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import net.gotev.speech.utils.selfReference
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TextToSpeechEngine(
    private val context: Context,
    private val initializer: (suspend (engine: TextToSpeechEngine) -> Unit)? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val progressChannel = Channel<Status>()
    private val progressListener = UtteranceChannelProgressListener(scope, progressChannel)

    private var textToSpeechInstance: TextToSpeech? = null

    private suspend fun textToSpeech(): TextToSpeech {
        textToSpeechInstance?.also { return it }
        val tts = suspendCoroutine<TextToSpeech> { continuation ->
            selfReference<TextToSpeech> {
                TextToSpeech(context) { status ->
                    if (status.isSuccessful) {
                        self.setOnUtteranceProgressListener(progressListener)
                        continuation.resume(self)
                    } else {
                        continuation.resumeWithException(
                            TextToSpeechError(
                                status
                            )
                        )
                    }
                }
            }
        }
        textToSpeechInstance = tts
        initializer?.invoke(this)
        return tts
    }

    // TODO: find examples with TTS Span
    // TODO: check max length
    suspend fun speak(
        message: CharSequence,
        utteranceID: String = UUID.randomUUID().toString(),
        flushQueue: Boolean = false,
        stream: Int = TextToSpeech.Engine.DEFAULT_STREAM
    ): Boolean {
        val queueMode = if (flushQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, stream)
        }

        if (!textToSpeech().speak(message, queueMode, params, utteranceID).isSuccessful)
            return false

        return progressChannel.waitFor { it.utteranceID == utteranceID && it !is Status.Started }.isDone
    }

    fun isSpeaking() = textToSpeechInstance?.isSpeaking ?: false

    fun stop() {
        textToSpeechInstance?.stop()
    }

    suspend fun configuration() = textToSpeech().let {
        Configuration(
            availableLanguages = it.availableLanguages,
            availableVoices = it.voices,
            currentVoice = it.voice,
            defaultVoice = it.defaultVoice
        )
    }

    suspend fun setPitch(pitch: Float) = textToSpeech().setPitch(pitch).isSuccessful

    suspend fun setSpeechRate(speechRate: SpeechRate) =
        textToSpeech().setSpeechRate(speechRate.value).isSuccessful

    suspend fun setAudioAttributes(audioAttributes: AudioAttributes) =
        textToSpeech().setAudioAttributes(audioAttributes).isSuccessful

    suspend fun setVoice(voice: Voice) = textToSpeech().setVoice(voice).isSuccessful

    fun shutdown() {
        textToSpeechInstance?.shutdown()
        textToSpeechInstance = null
    }
}
