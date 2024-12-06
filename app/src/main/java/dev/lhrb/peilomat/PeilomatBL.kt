package dev.lhrb.peilomat

import androidx.compose.foundation.pager.PagerSnapDistance
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import java.util.Locale
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * longitude = easting (RW)
 * latitude = northing (HW)
 *
 * we are only interested in m precision
 */
data class UTM32(val easting: Int, val northing: Int)

sealed class CRS(val name: String, val epsg: String) {
    object EPSG31468 : CRS(name = "Gauß-Krüger", epsg = "epsg:31468")
    object EPSG32632 : CRS(name ="UTM", epsg = "epsg:32632")

    companion object {
        // Factory method to create a CRS safely
        fun fromName(name: String): CRS? = when (name) {
            EPSG31468.name -> EPSG31468
            EPSG32632.name -> EPSG32632
            else -> null
        }
    }
}

class PeilomatBL(private val locationProvider: LocationProvider) {

    suspend fun getCurrentPosition(targetCRS: CRS) : CurrentPositionData {
        val coordinates = locationProvider.getLocation()
        val converted = convertLatLonToUTM(coordinates.lat, coordinates.lon, targetCRS)
        return CurrentPositionData(
            lat = coordinates.lat,
            lon = coordinates.lon,
            easting = converted.easting,
            northing = converted.northing
        )
    }

    suspend fun calculatePoint(
        useAngle: Boolean,
        angle: String,
        distance: String,
        useGivenPoints: Boolean,
        easting: String,
        northing: String,
        targetCRS: CRS
    ) : Coordinates {
        val parsedAngle = angle.toDouble()
        val parsedDistance = distance.toDouble()
        val calculatedAngle = if (useAngle) parsedAngle else marschzahlToAngle(parsedAngle)

        val converted = if (useGivenPoints) {
            UTM32(easting.toInt(), northing.toInt())
        } else {
            val coordinates = locationProvider.getLocation()
            convertLatLonToUTM(coordinates.lat, coordinates.lon, targetCRS)
        }

        val newPoint = calculatePoint(
            converted.easting,
            converted.northing,
            calculatedAngle,
            parsedDistance
        )

        return convertUTMtoLatLon(newPoint.easting, newPoint.northing, targetCRS)
    }
}

fun marschzahlToAngle(mz: Double) : Double {
    return mz * 360.0 / 64.0;
}

fun convertLatLonToUTM(
    lat: Double,
    lon: Double,
    targetCRS: CRS = CRS.EPSG32632
) : UTM32 {
    val crsFactory = CRSFactory()
    val crsWGS84 = crsFactory.createFromName("epsg:4326")
    val crsUTM = crsFactory.createFromName(targetCRS.epsg)

    val ctFactory = CoordinateTransformFactory()
    val wgsToUtm = ctFactory.createTransform(crsWGS84, crsUTM)

    val result = ProjCoordinate()
    wgsToUtm.transform(ProjCoordinate(lon, lat), result)

    val easting = Math.round(result.x).toInt()
    val northing = Math.round(result.y).toInt()

    return UTM32(easting = easting, northing = northing)
}

fun convertUTMtoLatLon(
    easting: Int,
    northing: Int,
    targetCRS: CRS = CRS.EPSG32632
) : Coordinates {
    val crsFactory = CRSFactory()
    val crsWGS84 = crsFactory.createFromName("epsg:4326")
    val crsUTM = crsFactory.createFromName(targetCRS.epsg)

    val ctFactory = CoordinateTransformFactory()
    val wgsToUtm = ctFactory.createTransform(crsUTM, crsWGS84)

    val result = ProjCoordinate()
    wgsToUtm.transform(ProjCoordinate(easting.toDouble(), northing.toDouble()), result)

    val lat = formatToSixDigits(result.y)
    val lon = formatToSixDigits(result.x)
    return Coordinates(lat = lat, lon = lon)
}

/**
 * formats a double to a number with 6 digits after the decimal point
 */
fun formatToSixDigits(num: Double) : Double {
    // use us locale for since they use a dot instead of comma ;)
    return String.format(Locale.US, "%.6f", num).toDouble()
}

fun calculatePoint(
    x: Int,
    y: Int,
    bearing: Double,
    distance: Double
) : UTM32 {
    val bearingAsRad = Math.toRadians(bearing)
    val _x = x + distance * cos(bearingAsRad)
    val _y = y + distance * sin(bearingAsRad)
    val newX = Math.round(_x).toInt()
    val newY = Math.round(_y).toInt()

    return UTM32(newX,newY)
}