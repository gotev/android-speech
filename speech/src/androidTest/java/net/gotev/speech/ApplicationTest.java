package net.gotev.speech;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    @Test
    public void useAppContext() {
        Application appContext = ApplicationProvider.getApplicationContext();

        assertEquals("net.gotev.speech.test", appContext.getPackageName());
    }
}
