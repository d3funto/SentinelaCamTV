package com.sentinela.camtv.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun versionWithPatchGreaterThanCurrentIsNewer() {
        assertTrue(AppVersion.isNewer(candidate = "v1.0.1", current = "1.0.0"))
    }

    @Test
    fun sameVersionWithPrefixIsNotNewer() {
        assertFalse(AppVersion.isNewer(candidate = "v1.0.0", current = "1.0.0"))
    }

    @Test
    fun versionWithoutPatchIsEquivalentToZeroPatch() {
        assertFalse(AppVersion.isNewer(candidate = "1.0", current = "1.0.0"))
        assertFalse(AppVersion.isNewer(candidate = "1.0.0", current = "1.0"))
    }
}
