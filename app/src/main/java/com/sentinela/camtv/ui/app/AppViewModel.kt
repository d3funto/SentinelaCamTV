package com.sentinela.camtv.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.data.camera.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppDestination {
    Loading,
    Home,
    Mosaic,
    Cameras,
    Settings,
}

data class AppUiState(
    val destination: AppDestination = AppDestination.Loading,
    val hasCameras: Boolean = false,
)

class AppViewModel(
    private val cameraRepository: CameraRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCameras = cameraRepository.hasEnabledCameras()
            _state.value = AppUiState(
                destination = if (hasCameras) AppDestination.Mosaic else AppDestination.Home,
                hasCameras = hasCameras,
            )
        }
        viewModelScope.launch {
            cameraRepository.observeEnabledCameras().collect { cameras ->
                _state.value = _state.value.copy(hasCameras = cameras.isNotEmpty())
            }
        }
    }

    fun openHome() {
        _state.value = _state.value.copy(destination = AppDestination.Home)
    }

    fun openMosaic() {
        if (_state.value.hasCameras) {
            _state.value = _state.value.copy(destination = AppDestination.Mosaic)
        }
    }

    fun openCameras() {
        _state.value = _state.value.copy(destination = AppDestination.Cameras)
    }

    fun openSettings() {
        _state.value = _state.value.copy(destination = AppDestination.Settings)
    }
}
