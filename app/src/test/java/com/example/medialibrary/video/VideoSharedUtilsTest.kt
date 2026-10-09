package com.example.medialibrary.video

import models.shared.Tag
import models.video.VideoFilter
import models.video.VideoSetup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VideoSharedUtilsTest {

    @Test
    @DisplayName("VideoFilter initialization and default properties")
    fun testVideoFilterDefaults() {
        val filter = VideoFilter()
        assertNotNull(filter)
        assertEquals(null, filter.Search)
    }

    @Test
    @DisplayName("VideoSetup container holds tag setup list")
    fun testVideoSetupTags() {
        val setup = VideoSetup().apply {
            Tag = arrayListOf(Tag("1", "4K Ultra HD"))
        }

        assertEquals(1, setup.Tag.size)
        assertEquals("4K Ultra HD", setup.Tag[0].Name)
    }
}
