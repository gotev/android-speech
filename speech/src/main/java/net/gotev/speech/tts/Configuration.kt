package net.gotev.speech.tts

import android.speech.tts.Voice
import java.util.Locale

data class Configuration(
    val availableLanguages: Set<Locale>,
    val availableVoices: Set<Voice>,
    val currentVoice: Voice,
    val defaultVoice: Voice
)
