package com.sentinela.camtv.ui.mosaic

import org.junit.Assert.assertEquals
import org.junit.Test

class MosaicUiTextTest {
    @Test
    fun reorderHintExplainsSwapDeleteAndBack() {
        assertEquals(
            "Selecione duas câmeras para trocar. Pressione OK por alguns segundos para excluir uma câmera. Pressione Back para concluir.",
            MosaicUiText.REORDER_HINT,
        )
    }

    @Test
    fun deleteConfirmationMakesSingleCameraScopeClear() {
        assertEquals(
            "Excluir câmera? Apenas esta câmera será removida.",
            MosaicUiText.DELETE_CAMERA_CONFIRMATION,
        )
    }
}
