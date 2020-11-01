package net.gotev.speechdemo;

import android.app.Application;

import net.gotev.speech.Logger;

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
