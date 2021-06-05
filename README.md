# Android Speech ![Maven Central](https://img.shields.io/maven-central/v/net.gotev/speech)
#### [Latest version Release Notes and Demo App](https://github.com/gotev/android-speech/releases/latest) | [Demo App Sources](https://github.com/gotev/android-speech/tree/master/examples/demoapp/app/src/main/java/net/gotev/speechdemo)
Android speech recognition and text to speech made easy.

## Setup
### Gradle
```groovy
implementation 'net.gotev:speech:x.y.z'
```
Replace `x.y.z` with ![Maven Central](https://img.shields.io/maven-central/v/net.gotev/speech)

## Initialization
To start using the library, you have to initialize it in your Activity
```java
public class YourActivity extends Activity {

    Override
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

## Get current locale and voice (since 1.5.0)
Use `Speech.getInstance().getSpeechToTextLanguage()` and `Speech.getinstance().getTextToSpeechVoice()`. Check the demo app for a complete example.

## Get supported Speech To Text languages and Text To Speech voices (since 1.5.0)
Use `Speech.getInstance().getSupportedSpeechToTextLanguages(listener)` and `Speech.getInstance().getSupportedTextToSpeechVoices()`. Check the demo app for a complete example.

## Set Speech To Text Language and Text To Speech voice
Use `Speech.getInstance().setLocale(locale)` and `Speech.getInstance().setVoice(voice)`. Check the demo app for a complete example.

> When you set the locale, the voice is automatically changed to the default voice of that language. If you want to set a particular voice, remember to re-set it every time you change the locale, too.

## Credits
Thanks to @zagum for the original implementation of the [speech recognition view](https://github.com/zagum/SpeechRecognitionView).

## License

    Copyright (C) 2019 Aleksandar Gotev

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
