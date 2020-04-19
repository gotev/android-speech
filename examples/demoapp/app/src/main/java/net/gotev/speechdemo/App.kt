package net.gotev.speechdemo

import android.app.Application
import net.gotev.speech.Logger

/**
 * @author Aleksandar Gotev
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.setLogLevel(Logger.LogLevel.DEBUG)
    }
}
