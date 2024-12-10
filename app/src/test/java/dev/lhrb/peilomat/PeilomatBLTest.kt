package dev.lhrb.peilomat

import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class PeilomatBLTest {

    @Test
    fun `should convert lat, lon to UTM`()  {
        val result = convertLatLonToTargetCRS(lat = 50.252751, lon = 7.372818)
        assertEquals(384000, result.easting)
        assertEquals(5568000, result.northing)
    }

    @Test
    fun `should convert UTM to lat lon`() {
        val result = convertUTMtoLatLon(384000, 5568000)
        println(result)
        assertEquals(50.252751, result.lat, 0.000001)
        assertEquals(7.372818, result.lon, 0.000001)
    }

    @Test
    fun `should calculate point with angle and distance`() {
        val x = 1
        val y = 1
        val bearing = 45.0
        val distance = sqrt(2.0)

        val expected = UTM32(2,2)
        val result = calculatePoint(x,y,bearing,distance)

        assertEquals(expected, result)
    }

    @Test
    fun `should convert mz to angle`() {
        val mz = 32.0
        val result = marschzahlToAngle(mz)
        assertEquals(180.0, result, 0.1)
    }

    @Test
    fun `should convert gaus krueger`() {
        val result = convertLatLonToTargetCRS(lat = 50.487985, lon = 7.477516, CRS.EPSG31466)
        println(result)
    }
}