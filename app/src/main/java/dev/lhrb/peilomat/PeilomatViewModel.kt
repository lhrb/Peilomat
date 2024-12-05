package dev.lhrb.peilomat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrentPositionData(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val easting: Int = 0,
    val northing: Int = 0
)

data class Coordinates(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class PeilomatUiState(
    val currentPositionData: CurrentPositionData = CurrentPositionData(),
    val convertRwHwToLatLonData: Coordinates = Coordinates(),
    val convertAngleAndDistanceToLatLonData: Coordinates = Coordinates()
)

class PeilomatViewModel(private val peilomatBL: PeilomatBL) : ViewModel() {
    private val _uiState = MutableStateFlow(PeilomatUiState())
    val uiState: StateFlow<PeilomatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentPosition = peilomatBL.getCurrentPosition()
            _uiState.update {
                it.copy(currentPositionData = currentPosition)
            }
        }
    }

    fun refresh() {
        Log.d("CLICK", "refresh clicked")
        viewModelScope.launch {
            val currentPosition = peilomatBL.getCurrentPosition()
            _uiState.update {
                it.copy(currentPositionData = currentPosition)
            }
        }
    }

    fun transformHwRw(easting: String, northing: String) {
        Log.d("CLICK", "convert clicked rw: $easting hw: $northing")

        // refactor parsing into testable context
        val coordinates = convertUTMtoLatLon(easting.toInt(), northing.toInt())
        _uiState.update {
            it.copy(convertRwHwToLatLonData = coordinates)
        }
    }

    fun transformAngleDistance(useAngle: Boolean, angle: String, distance: String) {
        Log.d("CLICK", "convert clicked useAngle: $useAngle angle: $angle distance: $distance")
        viewModelScope.launch {
            val newPoint = peilomatBL.calculatePoint(useAngle, angle, distance)
            _uiState.update {
                it.copy(convertAngleAndDistanceToLatLonData = newPoint)
            }
        }
    }
}

class PeilomatViewModelFactory(private val peilomatBL: PeilomatBL): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeilomatViewModel::class.java)) {
            return PeilomatViewModel(peilomatBL) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}