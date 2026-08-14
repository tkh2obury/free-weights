package com.freeweights.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class PlateInventoryTest {
    @Test
    fun `plate inventory parses sorts and removes duplicates`() {
        assertEquals(listOf(45.0, 25.0, 10.0, 2.5), parsePlateInventory("10, 45, 2.5, 25, 10"))
    }
}
