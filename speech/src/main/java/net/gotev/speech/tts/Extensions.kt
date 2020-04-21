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

internal fun String.normalizedEarcon(): String {
    val hasStartingBrace = startsWith("[")
    val hasEndingBrace = endsWith("]")
    if (hasStartingBrace && hasEndingBrace) return this

    val prefix = if (hasStartingBrace) "" else "["
    val suffix = if (hasEndingBrace) "" else "]"

    return "$prefix$this$suffix"
}

internal suspend fun Channel<Status>.waitFor(condition: (Status) -> Boolean): Status {
    var status: Status?

    do {
        status = receive().takeIf(condition)
    } while (status == null)

    return status
}

internal suspend inline fun Channel<Status>.waitForUtterance(utteranceID: String): Boolean {
    return waitFor { it.utteranceID == utteranceID && it !is Status.Started }.isDone
}
