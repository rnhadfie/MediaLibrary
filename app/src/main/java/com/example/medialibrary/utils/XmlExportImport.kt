package com.example.medialibrary.utils

import android.util.Base64
import android.util.Xml
import models.book.Book
import models.book.BookItem
import models.book.Publisher
import models.music.Music
import models.other.Other
import models.shared.DataContainer
import models.shared.MediaItem
import models.shared.Tag
import models.video.Video
import models.video.VideoItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.StringReader
import java.io.StringWriter
import models.book.Enums as BookEnums
import models.shared.Enums as SharedEmum
import models.video.Enums as VideoEnums

object XmlExportImport {

    fun exportToXml(container: DataContainer): String {
        try {
            val serializer: XmlSerializer = Xml.newSerializer()
            val writer = StringWriter()
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.startTag("", "MediaLibrary")

            // Tags
            serializer.startTag("", "Tags")
            container.Tags?.forEach { tag ->
                serializer.startTag("", "Tag")
                serializer.attribute("", "id", tag.Id.toString())
                serializer.attribute("", "name", tag.Name ?: "")
                serializer.endTag("", "Tag")
            }
            serializer.endTag("", "Tags")

            // Publishers
            serializer.startTag("", "Publishers")
            container.Publishers?.forEach { pub ->
                serializer.startTag("", "Publisher")
                serializer.attribute("", "id", pub.Id.toString())
                serializer.attribute("", "name", pub.Name ?: "")
                serializer.endTag("", "Publisher")
            }
            serializer.endTag("", "Publishers")

            // Books
            serializer.startTag("", "Books")
            container.Books?.forEach { book ->
                serializer.startTag("", "Book")
                writeMediaItemFields(serializer, book)
                serializer.startTag("", "Author").text(book.Author ?: "").endTag("", "Author")
                serializer.startTag("", "Artist").text(book.Artist ?: "").endTag("", "Artist")
                serializer.startTag("", "Type").text(book.Type?.name ?: "").endTag("", "Type")
                serializer.startTag("", "PublisherId").text(book?.Publisher ?: "")
                    .endTag("", "PublisherId")

                serializer.startTag("", "Items")
                book.Items?.forEach { item ->
                    serializer.startTag("", "BookItem")
                    serializer.startTag("", "VolumeNumber").text(item.VolumeNumber ?: "")
                        .endTag("", "VolumeNumber")
                    serializer.startTag("", "VolumeTitle").text(item.VolumeTitle ?: "")
                        .endTag("", "VolumeTitle")
                    serializer.startTag("", "Read").text(item.Read.toString()).endTag("", "Read")
                    serializer.startTag("", "Owned").text(item.Owned.toString()).endTag("", "Owned")
                    serializer.startTag("", "Format").text(item.Format?.name ?: "")
                        .endTag("", "Format")
                    item.ItemCover?.let {
                        serializer.startTag("", "ItemCover")
                            .text(Base64.encodeToString(it, Base64.DEFAULT)).endTag("", "ItemCover")
                    }
                    serializer.endTag("", "BookItem")
                }
                serializer.endTag("", "Items")
                serializer.endTag("", "Book")
            }
            serializer.endTag("", "Books")

            // Videos
            serializer.startTag("", "Videos")
            container.Videos?.forEach { video ->
                serializer.startTag("", "Video")
                writeMediaItemFields(serializer, video)
                serializer.startTag("", "Type").text(video.Type?.name ?: "").endTag("", "Type")
                serializer.startTag("", "VideoTag").text(video.VideoTag?.name ?: "")
                    .endTag("", "VideoTag")

                serializer.startTag("", "Items")
                video.Items?.forEach { item ->
                    serializer.startTag("", "VideoItem")
                    serializer.startTag("", "DiscNumber").text(item.Season.toString())
                        .endTag("", "DiscNumber")
                    serializer.startTag("", "DiscTitle").text(item.DiscTitle ?: "")
                        .endTag("", "DiscTitle")
                    serializer.startTag("", "Watched").text(item.Watched.toString())
                        .endTag("", "Watched")
                    serializer.startTag("", "Owned").text(item.Owned.toString()).endTag("", "Owned")
                    serializer.startTag("", "Format").text(item.Format?.name ?: "")
                        .endTag("", "Format")
                    item.ItemCover?.let {
                        serializer.startTag("", "ItemCover")
                            .text(Base64.encodeToString(it, Base64.DEFAULT)).endTag("", "ItemCover")
                    }
                    serializer.endTag("", "VideoItem")
                }
                serializer.endTag("", "Items")
                serializer.endTag("", "Video")
            }
            serializer.endTag("", "Videos")

            // Music
            serializer.startTag("", "MusicList")
            container.Music?.forEach { music ->
                serializer.startTag("", "Music")
                writeMediaItemFields(serializer, music)
                serializer.startTag("", "Artist").text(music.Artist ?: "").endTag("", "Artist")
                serializer.startTag("", "Year").text(music.Year.toString()).endTag("", "Year")
                serializer.endTag("", "Music")
            }
            serializer.endTag("", "MusicList")

            // Others
            serializer.startTag("", "Others")
            container.Others?.forEach { other ->
                serializer.startTag("", "Other")
                writeMediaItemFields(serializer, other)
                serializer.endTag("", "Other")
            }
            serializer.endTag("", "Others")

            serializer.endTag("", "MediaLibrary")
            serializer.endDocument()
            return writer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun writeMediaItemFields(serializer: XmlSerializer, item: MediaItem) {
        serializer.startTag("", "Id").text(item.Id ?: "").endTag("", "Id")
        serializer.startTag("", "Title").text(item.Title ?: "").endTag("", "Title")
        serializer.startTag("", "collecting").text(item.Collecting.toString()).endTag("", "collecting")
        serializer.startTag("", "Ongoing").text(item.Ongoing.toString()).endTag("", "Ongoing")
        serializer.startTag("", "Collected").text(item.HasCollectedAllItems.toString()).endTag("", "Collected")
        serializer.startTag("", "TagId").text(item.Tag.toString()).endTag("", "TagId")
        serializer.startTag("", "CollectingPriority").text(item.CollectingPriority.name).endTag("", "CollectingPriority")
        item.Genre?.let {
            serializer.startTag("", "Genres").text(it.joinToString(",")).endTag("", "Genres")
        }
        item.Cover?.let {
            serializer.startTag("", "Cover").text(Base64.encodeToString(it, Base64.DEFAULT)).endTag("", "Cover")
        }
    }

    fun importFromXml(xml: String): DataContainer {
        val container = DataContainer()
        container.Tags = mutableListOf()
        container.Publishers = mutableListOf()
        container.Books = mutableListOf()
        container.Videos = mutableListOf()
        container.Music = mutableListOf()
        container.Others = mutableListOf()

        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))
        var eventType = parser.eventType

        var currentBook: Book? = null
        var currentVideo: Video? = null
        var currentMusic: Music? = null
        var currentOther: Other? = null
        var currentBookItem: BookItem? = null
        var currentVideoItem: VideoItem? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "Tag" -> {
                            val id = parser.getAttributeValue("", "id") ?: ""
                            val name = parser.getAttributeValue("", "name") ?: ""
                            container.Tags.add(Tag(id, name))
                        }
                        "Publisher" -> {
                            val id = parser.getAttributeValue("", "id") ?: ""
                            val name = parser.getAttributeValue("", "name") ?: ""
                            container.Publishers.add(Publisher(id, name))
                        }
                        "Book" -> currentBook = Book().apply { Items = mutableListOf() }
                        "Video" -> currentVideo = Video().apply { Items = mutableListOf() }
                        "Music" -> currentMusic = Music()
                        "Other" -> currentOther = Other()
                        "BookItem" -> currentBookItem = BookItem()
                        "VideoItem" -> currentVideoItem = VideoItem()

                        // Fields
                        "Id" -> {
                            val text = parser.nextText()
                            currentBook?.Id = text
                            currentVideo?.Id = text
                            currentMusic?.Id = text
                            currentOther?.Id = text
                        }
                        "Title" -> {
                            val text = parser.nextText()
                            currentBook?.Title = text
                            currentVideo?.Title = text
                            currentMusic?.Title = text
                            currentOther?.Title = text
                        }
                        "collecting" -> {
                            val text = parser.nextText().toBoolean()
                            currentBook?.Collecting = text
                            currentVideo?.Collecting = text
                            currentMusic?.Collecting = text
                            currentOther?.Collecting = text
                        }
                        "HasEnded",
                        "Ongoing" -> {
                            val text = parser.nextText().toBoolean()
                            currentBook?.Ongoing = text
                            currentVideo?.Ongoing = text
                            currentMusic?.Ongoing = text
                            currentOther?.Ongoing = text
                        }
                        "CompletedCollecting",
                        "Collected" -> {
                            val text = parser.nextText().toBoolean()
                            currentBook?.HasCollectedAllItems = text
                            currentVideo?.HasCollectedAllItems = text
                            currentMusic?.HasCollectedAllItems = text
                            currentOther?.HasCollectedAllItems = text
                        }
                        "TagId" -> {
                            val text = parser.nextText() ?: ""
                            currentBook?.Tag = text
                            currentVideo?.Tag = text
                            currentMusic?.Tag = text
                            currentOther?.Tag = text
                        }
                        "Genres" -> {
                            val text = parser.nextText()
                            val genres = text.split(",").mapNotNull { it.trim().toIntOrNull() }
                            currentBook?.Genre = genres
                            currentVideo?.Genre = genres
                            currentMusic?.Genre = genres
                            currentOther?.Genre = genres
                        }
                        "Cover" -> {
                            val text = parser.nextText()
                            val bytes = Base64.decode(text, Base64.DEFAULT)
                            currentBook?.Cover = bytes
                            currentVideo?.Cover = bytes
                            currentMusic?.Cover = bytes
                            currentOther?.Cover = bytes
                        }
                        "Author" -> currentBook?.Author = parser.nextText()
                        "Artist" -> {
                            val text = parser.nextText()
                            currentBook?.Artist = text
                            currentMusic?.Artist = text
                        }
                        "Type" -> {
                            val text = parser.nextText()
                            currentBook?.let { b ->
                                b.Type = BookEnums.BookType.entries.find { it.name == text } ?: BookEnums.BookType.NoneSelected
                            }
                            currentVideo?.let { v ->
                                v.Type = VideoEnums.VideoType.entries.find { it.name == text }
                            }
                        }
                        "CollectingPriority" -> {
                            val text = parser.nextText()
                            currentBook?.let { b ->
                                b.CollectingPriority = SharedEmum.CollectingPriority.entries.find { it.name == text } ?: SharedEmum.CollectingPriority.None
                            }
                            currentVideo?.let { v ->
                                v.CollectingPriority = SharedEmum.CollectingPriority.entries.find { it.name == text } ?: SharedEmum.CollectingPriority.None
                            }
                            currentOther?.let { v ->
                                v.CollectingPriority = SharedEmum.CollectingPriority.entries.find { it.name == text } ?: SharedEmum.CollectingPriority.None
                            }
                            currentMusic?.let { v ->
                                v.CollectingPriority = SharedEmum.CollectingPriority.entries.find { it.name == text } ?: SharedEmum.CollectingPriority.None
                            }
                        }
                        "PublisherId" -> currentBook?.Publisher = parser.nextText() ?: ""
                        "VolumeNumber" -> currentBookItem?.VolumeNumber = parser.nextText()
                        "VolumeTitle" -> currentBookItem?.VolumeTitle = parser.nextText()
                        "Read" -> currentBookItem?.Read = parser.nextText().toBoolean()
                        "Owned" -> {
                            val text = parser.nextText().toBoolean()
                            currentBookItem?.Owned = text
                            currentVideoItem?.Owned = text
                        }
                        "Format" -> {
                            val text = parser.nextText()
                            currentBookItem?.let { i ->
                                i.Format = BookEnums.BookFormat.entries.find { it.name == text } ?: BookEnums.BookFormat.NoneSelected
                            }
                            currentVideoItem?.let { i ->
                                i.Format = VideoEnums.VideoFormat.entries.find { it.name == text }
                            }
                        }
                        "ItemCover" -> {
                            val text = parser.nextText()
                            val bytes = Base64.decode(text, Base64.DEFAULT)
                            currentBookItem?.ItemCover = bytes
                            currentVideoItem?.ItemCover = bytes
                        }
                        "VideoTag" -> {
                            val text = parser.nextText()
                            currentVideo?.VideoTag = VideoEnums.VideoTag.entries.find { it.name == text }
                        }
                        "DiscNumber" -> currentVideoItem?.Season = parser.nextText().toIntOrNull() ?: 0
                        "DiscTitle" -> currentVideoItem?.DiscTitle = parser.nextText()
                        "Watched" -> currentVideoItem?.Watched = parser.nextText().toBoolean()
                        "Year" -> currentMusic?.Year = parser.nextText().toIntOrNull() ?: 0
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "Book" -> currentBook?.let { container.Books.add(it); currentBook = null }
                        "Video" -> currentVideo?.let { container.Videos.add(it); currentVideo = null }
                        "Music" -> currentMusic?.let { container.Music.add(it); currentMusic = null }
                        "Other" -> currentOther?.let { container.Others.add(it); currentOther = null }
                        "BookItem" -> currentBookItem?.let { currentBook?.Items?.add(it); currentBookItem = null }
                        "VideoItem" -> currentVideoItem?.let { currentVideo?.Items?.add(it); currentVideoItem = null }
                    }
                }
            }
            eventType = parser.next()
        }

        return container
    }
}
