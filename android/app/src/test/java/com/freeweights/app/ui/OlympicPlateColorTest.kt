package com.freeweights.app.ui

import com.freeweights.app.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class OlympicPlateColorTest {
    @Test
    fun poundBumperPlatesFollowOlympicColors() {
        assertEquals(OlympicPlateColor.RED, olympicPlateColor(55.0, WeightUnit.LB))
        assertEquals(OlympicPlateColor.BLUE, olympicPlateColor(45.0, WeightUnit.LB))
        assertEquals(OlympicPlateColor.YELLOW, olympicPlateColor(35.0, WeightUnit.LB))
        assertEquals(OlympicPlateColor.GREEN, olympicPlateColor(25.0, WeightUnit.LB))
        assertEquals(OlympicPlateColor.BLACK, olympicPlateColor(15.0, WeightUnit.LB))
        assertEquals(OlympicPlateColor.GRAY, olympicPlateColor(10.0, WeightUnit.LB))
    }

    @Test
    fun kilogramCompetitionPlatesFollowIwfColors() {
        assertEquals(OlympicPlateColor.RED, olympicPlateColor(25.0, WeightUnit.KG))
        assertEquals(OlympicPlateColor.BLUE, olympicPlateColor(20.0, WeightUnit.KG))
        assertEquals(OlympicPlateColor.YELLOW, olympicPlateColor(15.0, WeightUnit.KG))
        assertEquals(OlympicPlateColor.GREEN, olympicPlateColor(10.0, WeightUnit.KG))
        assertEquals(OlympicPlateColor.WHITE, olympicPlateColor(5.0, WeightUnit.KG))
    }
}
