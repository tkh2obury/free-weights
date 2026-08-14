package com.freeweights.app.model

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateTest {
    @Test
    fun `new app starts without plans or workout history`() {
        val state = AppState()

        assertTrue(state.plans.isEmpty())
        assertTrue(state.logs.isEmpty())
        assertEquals("#00FF66", state.themeTextColor)
    }
}
