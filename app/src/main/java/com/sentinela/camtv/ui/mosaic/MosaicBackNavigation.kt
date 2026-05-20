package com.sentinela.camtv.ui.mosaic

internal fun shouldReturnHomeOnMosaicBack(state: MosaicUiState): Boolean =
    !state.isLoading &&
        state.fullscreenCamera == null &&
        state.cameras.isEmpty()
