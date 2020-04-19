package net.gotev.speech.tts

import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class UtteranceChannelProgressListener(
    private val scope: CoroutineScope,
    private val channel: Channel<Status>
) : UtteranceProgressListener() {
    override fun onStart(utteranceID: String) = sendEvent(Status.Started(utteranceID))
    override fun onDone(utteranceID: String) = sendEvent(Status.Done(utteranceID))
    override fun onError(utteranceID: String) = sendEvent(Status.Error(utteranceID))

    private fun sendEvent(status: Status) {
        scope.launch { channel.send(status) }
    }
}
