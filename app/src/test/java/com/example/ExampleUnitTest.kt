package com.example

import com.example.util.AppLanguage
import com.example.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLocalizationDictionary() {
        assertEquals("Smart Study Manager", Strings.get("app_name", AppLanguage.ENGLISH))
        assertEquals("سمارت ستادي", Strings.get("app_name", AppLanguage.ARABIC))
        assertEquals("Dashboard", Strings.get("nav_dashboard", AppLanguage.ENGLISH))
        assertEquals("لوحة التحكم", Strings.get("nav_dashboard", AppLanguage.ARABIC))
    }

    @Test
    fun testScheduleTimeValidation() {
        val validStart = "10:00"
        val validEnd = "12:00"
        assertTrue(validStart < validEnd)

        val invalidStart = "14:00"
        val invalidEnd = "11:00"
        assertFalse(invalidStart < invalidEnd)
    }

    @Test
    fun testCompletionRateCalculation() {
        val total = 10
        val completed = 7
        val rate = (completed.toFloat() / total.toFloat()) * 100f
        assertEquals(70f, rate, 0.01f)
    }
}
