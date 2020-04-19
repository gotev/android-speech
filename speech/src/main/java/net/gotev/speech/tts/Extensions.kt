package net.gotev.speech.tts

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.channels.Channel
import java.util.Locale

@JvmName("filterLocalesByLanguage")
fun Set<Locale>.filterByLanguage(locale: Locale = Locale.getDefault()) =
    filter { it.language == locale.language }

@JvmName("filterVoicesByLanguage")
fun Set<Voice>.filterByLanguage(locale: Locale = Locale.getDefault()) =
    filter { it.locale.language == locale.language }

internal val Int.isSuccessful: Boolean
    get() = this == TextToSpeech.SUCCESS

internal suspend fun Channel<Status>.waitFor(condition: (Status) -> Boolean): Status {
    var status: Status?

    do {
        status = receive().takeIf(condition)
    } while (status == null)

    return status
}
