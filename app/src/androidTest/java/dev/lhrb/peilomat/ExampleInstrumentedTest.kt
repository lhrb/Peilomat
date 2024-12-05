package dev.lhrb.peilomat

import android.location.Location
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("dev.lhrb.peilomat", appContext.packageName)
    }

    @Test
    fun distanceBetweenPoints() {
        val l1 = Location("test").apply {
            latitude = 50.252753
            longitude = 7.38719
        }

        val l2 = Location("test").apply {
            latitude = 50.252947
            longitude = 7.38684
        }

        Log.d("DISTANCE", "distance: ${l1.distanceTo(l2)}")

    }
}