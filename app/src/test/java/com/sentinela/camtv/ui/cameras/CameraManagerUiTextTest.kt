package com.sentinela.camtv.ui.cameras

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraManagerUiTextTest {
    @Test
    fun connectedTabMentionsEditMosaic() {
        assertEquals(
            "Confira as câmeras conectadas. Para trocar posições ou excluir, use Editar mosaico no menu rápido do mosaico.",
            CONNECTED_TAB_DESCRIPTION,
        )
    }
}
