package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DailyDuaCatalog
import com.example.data.local.PrebundledQuranData
import com.example.data.local.SurahMetadata
import com.example.util.BengaliFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("মুকিয়াঘোনা যুব সংগঠন", appName)
    }

    @Test
    fun `verify quran metadata and dua catalog completeness`() {
        assertEquals(114, SurahMetadata.allSurahs.size)
        assertTrue(PrebundledQuranData.bundledSurahs.containsKey(1))
        assertTrue(PrebundledQuranData.bundledSurahs.containsKey(112))
        assertTrue(DailyDuaCatalog.allDuas.size >= 25)
        assertEquals("৳ ১,৫০০", BengaliFormatters.formatBdt(1500L))
    }
}
