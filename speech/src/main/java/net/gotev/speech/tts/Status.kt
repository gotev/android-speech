package net.gotev.speech.tts

internal sealed class Status(val utteranceID: String) {
    class Started(utteranceID: String) : Status(utteranceID)
    class Done(utteranceID: String) : Status(utteranceID)
    class Error(utteranceID: String) : Status(utteranceID)

    val isDone: Boolean
        get() = this is Done
}
