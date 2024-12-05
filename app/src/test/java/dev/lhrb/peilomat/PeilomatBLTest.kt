package dev.lhrb.peilomat

import junit.framework.TestCase.assertEquals
import org.junit.Test

class PeilomatBLTest {
    @Test
    fun conversionTest() {
        val result = convertLatLonToUTM(lat = 50.252751, lon = 7.372818)
        println(result)
        assertEquals(384000, result.easting)
        assertEquals(5568000, result.northing)
    }
}