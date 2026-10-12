package com.example.medialibrary.utils

import models.book.Book
import models.book.Publisher
import models.shared.DataContainer
import models.shared.Tag
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class XmlExportImportTest {

    @Test
    fun testExportToXml() {
        val container = DataContainer().apply {
            Tags = arrayListOf(Tag("1", "Sci-Fi"), Tag("2", "Fantasy"))
            Publishers = arrayListOf(Publisher("10", "Penguin Random House"))
            Books = arrayListOf(Book().apply { Id = "100"; Title = "Dune"; Author = "Frank Herbert" })
        }

        val xmlString = XmlExportImport.exportToXml(container)

        assertNotNull(xmlString)
        assertTrue(xmlString.contains("<MediaLibrary>"))
        assertTrue(xmlString.contains("Sci-Fi"))
        assertTrue(xmlString.contains("Penguin Random House"))
        assertTrue(xmlString.contains("Dune"))
    }

    @Test
    fun testExportEmptyContainer() {
        val container = DataContainer().apply {
            Tags = arrayListOf()
            Publishers = arrayListOf()
            Books = arrayListOf()
        }

        val xmlString = XmlExportImport.exportToXml(container)

        assertNotNull(xmlString)
        assertTrue(xmlString.contains("<MediaLibrary>"))
    }
}
