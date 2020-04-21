package net.gotev.speechdemo

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import net.gotev.speech.GoogleVoiceTypingDisabledException
import net.gotev.speech.Speech
import net.gotev.speech.SpeechDelegate
import net.gotev.speech.SpeechRecognitionNotAvailable
import net.gotev.speech.SpeechUtil
import net.gotev.speech.tts.TextToSpeechEngine
import net.gotev.toyproject.R

class MainActivity : AppCompatActivity(), SpeechDelegate {
    private val PERMISSIONS_REQUEST = 1
    private val ttsEngine = TextToSpeechEngine(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Speech.init(this, packageName)
        text.setOnClickListener {
            lifecycleScope.launch {
                ttsEngine.stop()
                ttsEngine.speak("stopped")
            }
        }
        button.setOnClickListener { onButtonClick() }
        speak.setOnClickListener { onSpeakClick() }

        val colors = intArrayOf(
            ContextCompat.getColor(this, android.R.color.black),
            ContextCompat.getColor(this, android.R.color.darker_gray),
            ContextCompat.getColor(this, android.R.color.black),
            ContextCompat.getColor(this, android.R.color.holo_orange_dark),
            ContextCompat.getColor(this, android.R.color.holo_red_dark)
        )
        progress.setColors(colors)
    }

    override fun onDestroy() {
        super.onDestroy()
        Speech.getInstance().shutdown()
    }

    private fun onButtonClick() {
        if (Speech.getInstance().isListening) {
            Speech.getInstance().stopListening()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onRecordAudioPermissionGranted()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSIONS_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                onRecordAudioPermissionGranted()
            } else {
                // permission denied, boo!
                Toast.makeText(this@MainActivity, R.string.permission_required, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun onRecordAudioPermissionGranted() {
        button.visibility = View.GONE
        linearLayout.visibility = View.VISIBLE

        try {
            Speech.getInstance().stopTextToSpeech()
            Speech.getInstance().startListening(progress, this@MainActivity)
        } catch (exc: SpeechRecognitionNotAvailable) {
            showSpeechNotSupportedDialog()
        } catch (exc: GoogleVoiceTypingDisabledException) {
            showEnableGoogleVoiceTyping()
        }
    }

    private fun onSpeakClick() {
        if (textToSpeech.text.isNullOrBlank()) {
            lifecycleScope.launchWhenResumed {
                speak.isEnabled = false
                println(ttsEngine.speak(getString(R.string.input_something)))
                speak.isEnabled = true
            }
            Toast.makeText(this, R.string.input_something, Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launchWhenResumed {
            speak.isEnabled = false
            println(ttsEngine.speak(textToSpeech.text.toString()))
            speak.isEnabled = true
        }
    }

    override fun onStartOfSpeech() {}
    override fun onSpeechRmsChanged(value: Float) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    override fun onSpeechResult(result: String) {
        button.visibility = View.VISIBLE
        linearLayout.visibility = View.GONE
        text.text = result

        if (result.isEmpty()) {
            Speech.getInstance().say(getString(R.string.repeat))
        } else {
            Speech.getInstance().say(result)
        }
    }

    override fun onSpeechPartialResults(results: List<String>) {
        text.text = ""
        for (partial in results) {
            text.append("$partial ")
        }
    }

    private fun showSpeechNotSupportedDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> SpeechUtil.redirectUserToGoogleAppOnPlayStore(
                        this@MainActivity
                    )
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.speech_not_available)
            .setCancelable(false)
            .setPositiveButton(R.string.yes, dialogClickListener)
            .setNegativeButton(R.string.no, dialogClickListener)
            .show()
    }

    private fun showEnableGoogleVoiceTyping() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.enable_google_voice_typing)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialogInterface, i ->
                // do nothing
            }
            .show()
    }
}
