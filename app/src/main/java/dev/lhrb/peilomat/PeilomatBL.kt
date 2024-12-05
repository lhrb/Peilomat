package dev.lhrb.peilomat

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

class PeilomatBL(private val locationProvider: LocationProvider) {

    suspend fun getCurrentPosition() : CurrentPositionData {
        val coordinates = locationProvider.getLocation()
        val converted = convertLatLonToUTM(coordinates.lat, coordinates.lon)
        return CurrentPositionData(
            lat = coordinates.lat,
            lon = coordinates.lon,
            easting = converted.easting,
            northing = converted.northing
        )
    }

}


/**
 * longitude = easting (RW)
 * latitude = northing (HW)
 *
 * we are only interested in m precision
 */
data class UTM32(val easting: Int, val northing: Int)

fun convertLatLonToUTM(lat: Double, lon: Double) : UTM32 {
    val crsFactory = CRSFactory()
    val crsWGS84 = crsFactory.createFromName("epsg:4326")
    val crsUTM = crsFactory.createFromName("epsg:32632")

    val ctFactory = CoordinateTransformFactory()
    val wgsToUtm = ctFactory.createTransform(crsWGS84, crsUTM)

    val result = ProjCoordinate()
    wgsToUtm.transform(ProjCoordinate(lon, lat), result)

    val easting = Math.round(result.x).toInt()
    val northing = Math.round(result.y).toInt()

    return UTM32(easting = easting, northing = northing)
}

fun convertUTMtoLatLon(easting: Int, northing: Int) : Coordinates {
    val crsFactory = CRSFactory()
    val crsWGS84 = crsFactory.createFromName("epsg:4326")
    val crsUTM = crsFactory.createFromName("epsg:32632")

    val ctFactory = CoordinateTransformFactory()
    val wgsToUtm = ctFactory.createTransform(crsUTM, crsWGS84)

    val result = ProjCoordinate()
    wgsToUtm.transform(ProjCoordinate(easting.toDouble(), northing.toDouble()), result)

    val lat = String.format("%.6f", result.y).replace(",", ".").toDouble()
    val lon = String.format("%.6f", result.x).replace(",", ".").toDouble()
    return Coordinates(lat = lat, lon = lon)
}