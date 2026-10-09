package com.example.medialibrary.other

import com.example.medialibrary.other.ui.utils.SortFilterViewmodel
import models.shared.Filter
import models.shared.SortModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OtherSortFilterViewModelTest {

    private lateinit var viewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        viewModel = SortFilterViewmodel()
    }

    @Test
    @DisplayName("Initial filter state flows are null")
    fun testInitialState() {
        assertNull(viewModel.currentItemFilter.value)
        assertNull(viewModel.currentMainFilter.value)
    }

    @Test
    @DisplayName("getOrCreateItemFilter creates new OtherFilter when null and returns existing when populated")
    fun testGetOrCreateItemFilter() {
        val filter1 = viewModel.getOrCreateItemFilter()
        assertNotNull(filter1)

        filter1.Search = "Filter Test"
        viewModel.updateItemFilter(filter1)

        val filter2 = viewModel.getOrCreateItemFilter()
        assertEquals("Filter Test", filter2.Search)
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
    @DisplayName("updateSortModel updates sort model and syncs properties onto item filter")
    fun testUpdateSortModel() {
        val filter = viewModel.getOrCreateItemFilter()
        assertNotNull(filter)

        val sortModel = SortModel().apply {
            Alphabetical = true
            Priority = false
            ItemMediaType = true
            SortOrder = arrayListOf("1", "2")
        }

        viewModel.updateSortModel(sortModel)

        val currentFilter = viewModel.currentItemFilter.value
        assertNotNull(currentFilter)
        assertEquals(true, currentFilter?.SortAlphabetical)
        assertEquals(false, currentFilter?.SortPriority)
        assertEquals(true, currentFilter?.SortItemMediaType)
        assertEquals(listOf("1", "2"), currentFilter?.SortOrder)
    }
}
