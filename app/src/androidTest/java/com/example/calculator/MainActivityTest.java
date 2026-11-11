package com.example.calculator;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests using Android Espresso.
 * Verifies on-screen button grid clicks, display updates, division-by-zero error handling,
 * and rapid tap resilience on an emulator or physical device.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNumberInputAndDisplay() {
        onView(withId(R.id.btn7)).perform(click());
        onView(withId(R.id.btn8)).perform(click());

        onView(withId(R.id.tvExpression)).check(matches(withText("78")));
        onView(withId(R.id.tvResult)).check(matches(withText("78")));
    }

    @Test
    public void testSimpleAddition() {
        onView(withId(R.id.btn5)).perform(click());
        onView(withId(R.id.btnAdd)).perform(click());
        onView(withId(R.id.btn3)).perform(click());
        onView(withId(R.id.btnEquals)).perform(click());

        onView(withId(R.id.tvResult)).check(matches(withText("8")));
    }

    @Test
    public void testDivisionByZeroShowsError() {
        onView(withId(R.id.btn9)).perform(click());
        onView(withId(R.id.btnDivide)).perform(click());
        onView(withId(R.id.btn0)).perform(click());
        onView(withId(R.id.btnEquals)).perform(click());

        onView(withId(R.id.tvResult)).check(matches(withText("Error")));
    }

    @Test
    public void testClearButtonResetsDisplay() {
        onView(withId(R.id.btn1)).perform(click());
        onView(withId(R.id.btn2)).perform(click());
        onView(withId(R.id.btnClear)).perform(click());

        onView(withId(R.id.tvExpression)).check(matches(withText("")));
        onView(withId(R.id.tvResult)).check(matches(withText("0")));
    }

    @Test
    public void testBackspaceDeletesLastDigit() {
        onView(withId(R.id.btn4)).perform(click());
        onView(withId(R.id.btn5)).perform(click());
        onView(withId(R.id.btnBackspace)).perform(click());

        onView(withId(R.id.tvExpression)).check(matches(withText("4")));
        onView(withId(R.id.tvResult)).check(matches(withText("4")));
    }

    @Test
    public void testRapidOperatorTapsReplacesGracefully() {
        // Tapping 5 then + then × should replace + with × instead of crashing
        onView(withId(R.id.btn5)).perform(click());
        onView(withId(R.id.btnAdd)).perform(click());
        onView(withId(R.id.btnMultiply)).perform(click());
        onView(withId(R.id.btn4)).perform(click());
        onView(withId(R.id.btnEquals)).perform(click());

        onView(withId(R.id.tvResult)).check(matches(withText("20")));
    }
}
