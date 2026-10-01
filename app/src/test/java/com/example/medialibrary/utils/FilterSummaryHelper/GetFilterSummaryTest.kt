package com.example.medialibrary.utils.FilterSummaryHelper

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import models.book.BookFilter
import com.example.medialibrary.utils.FilterSummaryHelper
import models.book.Enums as BookEnums
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GetFilterSummaryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `returns empty string when filter is null`() {
        val result = FilterSummaryHelper.getFilterSummaryText(null, null)

        assertEquals("", result)
    }

    @Test
    fun `includes search text in summary`() {
        val filter = BookFilter().apply {
            Search = "Harry Potter"
        }

        val result = FilterSummaryHelper.getFilterSummaryText(filter, null)

        assertEquals("""Search: "Harry Potter"""", result)
    }

    @Test
    fun `includes collected ongoing and owned values`() {
        val filter = BookFilter().apply {
            Collected = true
            Ongoing = false
            AnyOwned = true
        }

        val result = FilterSummaryHelper.getFilterSummaryText(filter, null)

        assertAll(
            { assertTrue(result.contains("Collected: Yes")) },
            { assertTrue(result.contains("Completed: No")) },
            { assertTrue(result.contains("Started: Yes")) }
        )
    }
    @Test
    fun `includes book type when selected`() {
        val filter = BookFilter().apply {
            Type = BookEnums.BookType.Manga
        }
        val result = FilterSummaryHelper.getFilterSummaryText(filter, null)
        assertTrue(result.contains("Type: Manga"))
    }
    @Test
    fun `omits type when none selected`() {
        val filter = BookFilter().apply {
            Type = BookEnums.BookType.NoneSelected
        }
        val result = FilterSummaryHelper.getFilterSummaryText(filter, null)
        assertFalse(result.contains("Type:"))
    }
}