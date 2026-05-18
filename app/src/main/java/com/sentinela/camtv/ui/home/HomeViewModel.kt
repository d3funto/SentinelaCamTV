package com.sentinela.camtv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinela.camtv.data.camera.CameraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val canOpenMosaic: Boolean = false,
)

class HomeViewModel(
    cameraRepository: CameraRepository,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = cameraRepository.observeEnabledCameras()
        .map { cameras -> HomeUiState(canOpenMosaic = cameras.isNotEmpty()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
