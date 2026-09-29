package com.example.medialibrary.FilterSummaryHelper

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.book.BookFilter

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasActiveFiltersTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun hasActiveFilters_filterIsNull_returnsFalse() {
        assertFalse(com.example.medialibrary.utils.FilterSummaryHelper.hasActiveFilters(null))
    }

    @Test
    fun hasActiveFilters_filterExists_returnsTrue() {
        val filter = BookFilter().apply {
            Search = "Harry Potter"
        }

        assertTrue(com.example.medialibrary.utils.FilterSummaryHelper.hasActiveFilters(filter))
    }
}