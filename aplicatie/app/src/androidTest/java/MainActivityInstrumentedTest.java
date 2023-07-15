import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.Root;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pytorch.demo.objectdetection.MainActivity;
import org.pytorch.demo.objectdetection.OutputHolder;
import org.pytorch.demo.objectdetection.R;
import org.pytorch.demo.objectdetection.Result;
import org.pytorch.demo.objectdetection.ToastMessageHolder;

import android.view.WindowManager;
import android.os.IBinder;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testMainActivityViewsDisplayed() {
        onView(ViewMatchers.withId(R.id.detectButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRedButtonIsDisplayed() {
        onView(ViewMatchers.withId(R.id.redButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testClassifyButtonIsDisplayedAndClickable() {
        onView(ViewMatchers.withId(R.id.classifyButton))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));
    }

    @Test
    public void testSelectButtonAndLiveButtonAreDisplayedAndClickable() {
        onView(ViewMatchers.withId(R.id.selectButton))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        onView(ViewMatchers.withId(R.id.liveButton))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));
    }

    @Test
    public void testDetectButtonIsDisplayedAndClickable() {
        onView(ViewMatchers.withId(R.id.detectButton))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));
    }
    @Test
    public void testClassifyButtonShowsToast() {
        // Click the classify button
        Espresso.onView(ViewMatchers.withId(R.id.classifyButton)).perform(click());

        // Wait for the toast to appear
        try {
            Thread.sleep(5000); // waits 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String toastMessage = ToastMessageHolder.getInstance().getToastMessage();
        assertEquals("Class: very good (bortle 1-4)", toastMessage);
    }
    @Test
    public void testDetectButtonTestImage() {
        // Click the classify button
        Espresso.onView(ViewMatchers.withId(R.id.detectButton)).perform(click());

        // Wait for the toast to appear
        try {
            Thread.sleep(5000); // waits 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int results = OutputHolder.getInstance().getResults();
        assertEquals(79, results);
    }
}
