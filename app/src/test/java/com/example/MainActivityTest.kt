package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test
    fun testActivityLaunches() {
        try {
            val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
