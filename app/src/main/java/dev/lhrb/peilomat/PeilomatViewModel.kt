package dev.lhrb.peilomat

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CurrentPositionData(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val rw: String = "",
    val hw: String = ""
)

data class ConvertRwHwToLatLonData(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class ConvertAngleAndDistanceToLatLonData(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class PeilomatUiState(
    val currentPositionData: CurrentPositionData = CurrentPositionData(),
    val convertRwHwToLatLonData: ConvertRwHwToLatLonData = ConvertRwHwToLatLonData(),
    val convertAngleAndDistanceToLatLonData: ConvertAngleAndDistanceToLatLonData = ConvertAngleAndDistanceToLatLonData()
)

class PeilomatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PeilomatUiState())
    val uiState: StateFlow<PeilomatUiState> = _uiState.asStateFlow()


    fun refresh() {
        Log.d("CLICK", "refresh clicked")
    }

    fun transformHwRw(rw: String, hw: String) {
        Log.d("CLICK", "convert clicked rw: $rw hw: $hw")
    }

    fun transformAngleDistance(useAngle: Boolean, angle: String, distance: String) {
        Log.d("CLICK", "convert clicked useAngle: $useAngle angle: $angle distance: $distance")
    }
}