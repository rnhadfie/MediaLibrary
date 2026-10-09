package com.example.medialibrary.home

import com.example.medialibrary.home.ui.utils.SharedUtils
import models.shared.DisplayMediaItem
import models.shared.Enums
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SharedUtilsTest {

    @Test
    @DisplayName("clipboardHelper correctly formats and sorts media items by category")
    fun testClipboardHelperWithData() {
        val book = DisplayMediaItem().apply {
            Title = "Dune"
            MediaType = Enums.MediaType.Book
        }
        val video = DisplayMediaItem().apply {
            Title = "Matrix"
            MediaType = Enums.MediaType.Video
        }
        val music = DisplayMediaItem().apply {
            Title = "Abbey Road"
            MediaType = Enums.MediaType.Music
        }
        val other = DisplayMediaItem().apply {
            Title = "Board Game"
            MediaType = Enums.MediaType.Other
        }

        val result = SharedUtils.clipboardHelper(listOf(book, video, music, other), "Test List")

        assertNotNull(result)
        assertTrue(result.contains("Books:\nDune"))
        assertTrue(result.contains("Videos:\nMatrix"))
        assertTrue(result.contains("Music Collection:\nAbbey Road"))
        assertTrue(result.contains("Other Collection:\nBoard Game"))
    }

    @Test
    @DisplayName("clipboardHelper produces empty categories when no items are present")
    fun testClipboardHelperWithNoData() {
        val result = SharedUtils.clipboardHelper(emptyList(), "Empty List")

        assertNotNull(result)
        assertTrue(result.contains("Books:"))
        assertTrue(result.contains("Videos:"))
        assertTrue(result.contains("Music Collection:"))
        assertTrue(result.contains("Other Collection:"))
    }
}
