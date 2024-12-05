package dev.lhrb.peilomat

import junit.framework.TestCase.assertEquals
import org.junit.Test

class PeilomatBLTest {

    @Test
    fun `should convert lat, lon to UTM`()  {
        val result = convertLatLonToUTM(lat = 50.252751, lon = 7.372818)
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


}