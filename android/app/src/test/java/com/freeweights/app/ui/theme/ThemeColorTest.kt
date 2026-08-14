package com.freeweights.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeColorTest {
    @Test
    fun `normalizes valid six digit theme colors`() {
        assertEquals("#00FF66", normalizeThemeHex("00ff66"))
        assertEquals("#020704", normalizeThemeHex("#020704"))
    }

    @Test
    fun `rejects invalid theme colors`() {
        assertNull(normalizeThemeHex("#12345"))
        assertNull(normalizeThemeHex("matrix"))
    }
}
