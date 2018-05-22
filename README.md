# Android Speech
<!-- [![Build Status](https://travis-ci.org/gotev/android-speech.svg?branch=master)](https://travis-ci.org/gotev/android-speech) [ ![Download](https://api.bintray.com/packages/gotev/maven/android-speech/images/download.svg) ](https://bintray.com/gotev/maven/android-speech/_latestVersion) <a href="http://www.methodscount.com/?lib=net.gotev%3Aspeech%3A1.1.9"><img src="https://img.shields.io/badge/Methods and size-316 | 37 KB-e91e63.svg"/></a>  -->

Android speech recognition and text to speech made easy

## Setup
### Gradle
Repo in progress...
```
compile 'io.aira:speech:1.3.2'
```

## Initialization
To start using the library, you have to initialize it in your Activity
```java
public class YourActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_layout);

        Speech.init(this, getPackageName());
    }

    @Override
    protected void onDestroy() {
        // prevent memory leaks when activity is destroyed
        Speech.getInstance().shutdown();
    }
}
```

## Logging
By default the library logging is disabled. You can enable debug log by invoking:
```java
Logger.setLogLevel(LogLevel.DEBUG);
```
wherever you want in your code. You can adjust the level of detail from DEBUG to OFF.

The library logger uses `android.util.Log` by default, so you will get the output in `LogCat`. If you want to redirect logs to different output or use a different logger, you can provide your own delegate implementation like this:
```java
Logger.setLoggerDelegate(new Logger.LoggerDelegate() {
    @Override
    public void error(String tag, String message) {
        //your own implementation here
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        //your own implementation here
    }

    @Override
    public void debug(String tag, String message) {
        //your own implementation here
    }

    @Override
    public void info(String tag, String message) {
        //your own implementation here
    }
});
```

## Example
You can find a fully working demo app which uses this library in the `examples` directory. Just checkout the project and give it a try.

## Usage
### Speech recognition
Inside an activity:
```java
try {
    // you must have android.permission.RECORD_AUDIO granted at this point
    Speech.getInstance().startListening(new SpeechDelegate() {
        @Override
        public void onStartOfSpeech() {
            Log.i("speech", "speech recognition is now active");
        }

        @Override
        public void onSpeechRmsChanged(float value) {
            Log.d("speech", "rms is now: " + value);
        }

        @Override
        public void onSpeechPartialResults(List<String> results) {
            StringBuilder str = new StringBuilder();
            for (String res : results) {
                str.append(res).append(" ");
            }

            Log.i("speech", "partial result: " + str.toString().trim());
        }

        @Override
        public void onSpeechResult(String result) {
            Log.i("speech", "result: " + result);
        }
    });
} catch (SpeechRecognitionNotAvailable exc) {
    Log.e("speech", "Speech recognition is not available on this device!");
    // You can prompt the user if he wants to install Google App to have
    // speech recognition, and then you can simply call:
    //
    // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
    //
    // to redirect the user to the Google App page on Play Store
} catch (GoogleVoiceTypingDisabledException exc) {
    Log.e("speech", "Google voice typing must be enabled!");
}
```

### Release resources
In your Activity's `onDestroy`, add:
```java
@Override
protected void onDestroy() {
    Speech.getInstance().shutdown();
}
```
To prevent memory leaks.

### Display progress animation
Add this to your layout:
```xml
<LinearLayout
    android:orientation="vertical"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/linearLayout">

    <net.gotev.speech.ui.SpeechProgressView
        android:id="@+id/progress"
        android:layout_width="120dp"
        android:layout_height="150dp"/>

</LinearLayout>
```
It's important that the `SpeechProgressView` is always inside a LinearLayout to function properly. You can adjust width and height accordingly to the bar height settings (see below).

then, when you start speech recognition, pass also the `SpeechProgressView`:

```java
Speech.getInstance().startListening(speechProgressView, speechDelegate);
```

#### Set custom bar colors
You can set all the 5 bar colors as you wish. This is just an example:
```java
int[] colors = {
        ContextCompat.getColor(this, android.R.color.black),
        ContextCompat.getColor(this, android.R.color.darker_gray),
        ContextCompat.getColor(this, android.R.color.black),
        ContextCompat.getColor(this, android.R.color.holo_orange_dark),
        ContextCompat.getColor(this, android.R.color.holo_red_dark)
};
speechProgressView.setColors(colors);
```

#### Set custom maximum bar height
```java
int[] heights = {60, 76, 58, 80, 55};
speechProgressView.setBarMaxHeightsInDp(heights);
```

### Text to speech
Inside an activity:
```java
Speech.getInstance().say("say something");
```

You can also provide a callback to receive status:
```java
Speech.getInstance().say("say something", new TextToSpeechCallback() {
    @Override
    public void onStart() {
        Log.i("speech", "speech started");
    }

    @Override
    public void onCompleted() {
        Log.i("speech", "speech completed");
    }

    @Override
    public void onError() {
        Log.i("speech", "speech error");
    }
});
```

## Configuration
You can configure various parameters by using the setter methods on the speech instance, which you can get like this anywhere in your code:
```java
Speech.getInstance()
```
Refer to JavaDocs for a complete reference.

## Credits
Thanks to @zagum for the original implementation of the [speech recognition view](https://github.com/zagum/SpeechRecognitionView).

## License

    Copyright (C) 2017 Aleksandar Gotev

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributors
Thanks to [Kristiyan Petrov](https://github.com/kristiyanP) for code review, bug fixes and library improvement ideas.
