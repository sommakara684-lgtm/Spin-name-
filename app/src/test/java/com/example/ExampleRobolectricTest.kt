package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.WheelPaletteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Spin Name", appName)
    }

    @Test
    fun `palette themes have valid colors`() {
        WheelPaletteTheme.entries.forEach { palette ->
            assertNotNull(palette.displayName)
            assert(palette.composeColors.isNotEmpty())
            val color = palette.getColorForIndex(0)
            assertNotNull(color)
        }
    }
}
