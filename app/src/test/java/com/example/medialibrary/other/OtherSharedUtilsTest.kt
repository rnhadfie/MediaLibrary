package com.example.medialibrary.other

import models.other.OtherFilter
import models.shared.MainSetup
import models.shared.Tag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OtherSharedUtilsTest {

    @Test
    @DisplayName("OtherFilter initialization and default properties")
    fun testOtherFilterDefaults() {
        val filter = OtherFilter()
        assertNotNull(filter)
        assertEquals(null, filter.Search)
    }

    @Test
    @DisplayName("MainSetup container holds tag setup list")
    fun testMainSetupTags() {
        val setup = MainSetup().apply {
            Tag = arrayListOf(Tag("1", "Action Figures"))
        }

        assertEquals(1, setup.Tag.size)
        assertEquals("Action Figures", setup.Tag[0].Name)
    }
}
