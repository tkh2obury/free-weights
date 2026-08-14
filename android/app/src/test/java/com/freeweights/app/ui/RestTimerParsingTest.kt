package com.freeweights.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RestTimerParsingTest {
    @Test
    fun `custom timer accepts seconds`() {
        assertEquals(75, parseRestTime("75"))
    }

    @Test
    fun `custom timer accepts minutes and seconds`() {
        assertEquals(150, parseRestTime("2:30"))
    }

    @Test
    fun `custom timer rejects invalid seconds field`() {
        assertNull(parseRestTime("1:75"))
    }
}
