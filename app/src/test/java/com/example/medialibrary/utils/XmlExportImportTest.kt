package com.example.medialibrary.utils

import models.book.Book
import models.book.Publisher
import models.other.Other
import models.other.OtherItem
import models.shared.DataContainer
import models.shared.Tag
import org.junit.Assert
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

    @Test
    fun testExportAndImportOtherCollection() {
        val otherItem = OtherItem().apply {
            Id = "item1"
            Title = "Volume 1"
            Owned = true
        }
        val other = Other().apply {
            Id = "other1"
            Title = "Statue Collection"
            Items = arrayListOf(otherItem)
        }
        val container = DataContainer().apply {
            Others = arrayListOf(other)
        }

        val xmlString = XmlExportImport.exportToXml(container)

        assertNotNull(xmlString)
        assertTrue(xmlString.contains("Statue Collection"))
        assertTrue(xmlString.contains("Volume 1"))

        val importedContainer = XmlExportImport.importFromXml(xmlString)
        assertNotNull(importedContainer.Others)
        assertTrue(importedContainer.Others.size == 1)
        val importedOther = importedContainer.Others[0]
        Assert.assertEquals("Statue Collection", importedOther.Title)
        assertNotNull(importedOther.Items)
        assertTrue(importedOther.Items.size == 1)
        Assert.assertEquals("Volume 1", importedOther.Items[0].Title)
        assertTrue(importedOther.Items[0].Owned == true)
    }
}
