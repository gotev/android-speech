package net.gotev.speechdemo;

import android.app.Application;

import net.gotev.speech.Speech;
import net.gotev.speech.log.Logger;

/**
 * @author Aleksandar Gotev
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Logger.setLogLevel(Logger.LogLevel.DEBUG);
    }
}
