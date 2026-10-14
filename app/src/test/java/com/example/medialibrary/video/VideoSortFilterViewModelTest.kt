package com.example.medialibrary.video

import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import models.shared.SortModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VideoSortFilterViewModelTest {

    private lateinit var viewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        viewModel = SortFilterViewmodel()
    }

    @Test
    @DisplayName("Initial filter state flows are null")
    fun testInitialState() {
        assertNull(viewModel.currentVideoFilter.value)
        assertNull(viewModel.currentMainFilter.value)
    }

    @Test
    @DisplayName("getOrCreateVideoFilter creates new VideoFilter when null and returns existing when populated")
    fun testGetOrCreateVideoFilter() {
        val filter1 = viewModel.getOrCreateVideoFilter()
        assertNotNull(filter1)

        filter1.Search = "Video Filter Test"
        viewModel.updateVideoFilter(filter1)

        val filter2 = viewModel.getOrCreateVideoFilter()
        assertEquals("Video Filter Test", filter2.Search)
    }

    @Test
    @DisplayName("getOrCreateMainFilter creates new Filter when null and returns existing when populated")
    fun testGetOrCreateMainFilter() {
        val filter1 = viewModel.getOrCreateMainFilter()
        assertNotNull(filter1)

        filter1.Search = "Main Search"
        viewModel.updateMainFilter(filter1)

        val filter2 = viewModel.getOrCreateMainFilter()
        assertEquals("Main Search", filter2.Search)
    }

    @Test
    @DisplayName("updateSortModel updates sort model and syncs properties onto video filter")
    fun testUpdateSortModel() {
        val filter = viewModel.getOrCreateVideoFilter()
        assertNotNull(filter)

        val sortModel = SortModel().apply {
            Alphabetical = true
            Priority = false
            ItemMediaType = true
            SortOrder = arrayListOf("1", "2")
        }

        viewModel.updateSortModel(sortModel)

        val currentFilter = viewModel.currentVideoFilter.value
        assertNotNull(currentFilter)
        assertEquals(true, currentFilter?.SortAlphabetical)
        assertEquals(false, currentFilter?.SortPriority)
        assertEquals(true, currentFilter?.SortItemMediaType)
        assertEquals(listOf("1", "2"), currentFilter?.SortOrder)
    }
}
