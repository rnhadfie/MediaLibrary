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
            // Using null for namespaces prevents empty xmlns="" declaration bugs
            serializer.startTag(null, "MediaLibrary")

            // --- Tags ---
            serializer.startTag(null, "Tags")
            container.Tags?.forEach { tag ->
                serializer.startTag(null, "Tag")
                serializer.attribute(null, "id", tag.Id.toString())
                serializer.attribute(null, "name", tag.Name ?: "")
                serializer.endTag(null, "Tag")
            }
            serializer.endTag(null, "Tags")

            // --- Publishers ---
            serializer.startTag(null, "Publishers")
            container.Publishers?.forEach { pub ->
                serializer.startTag(null, "Publisher")
                serializer.attribute(null, "id", pub.Id.toString())
                serializer.attribute(null, "name", pub.Name ?: "")
                serializer.endTag(null, "Publisher")
            }
            serializer.endTag(null, "Publishers")

            // --- Books ---
            serializer.startTag(null, "Books")
            container.Books?.forEach { book ->
                serializer.startTag(null, "Book")
                writeMediaItemFields(serializer, book)

                serializer.startTag(null, "Author")
                serializer.text(book.Author ?: "")
                serializer.endTag(null, "Author")

                serializer.startTag(null, "Artist")
                serializer.text(book.Artist ?: "")
                serializer.endTag(null, "Artist")

                serializer.startTag(null, "Type")
                serializer.text(book.Type?.name ?: "")
                serializer.endTag(null, "Type")

                serializer.startTag(null, "PublisherId")
                serializer.text(book?.Publisher ?: "")
                serializer.endTag(null, "PublisherId")

                serializer.startTag(null, "Items")
                book.Items?.forEach { item ->
                    serializer.startTag(null, "BookItem")

                    serializer.startTag(null, "VolumeNumber")
                    serializer.text(item.VolumeNumber ?: "")
                    serializer.endTag(null, "VolumeNumber")

                    serializer.startTag(null, "VolumeTitle")
                    serializer.text(item.VolumeTitle ?: "")
                    serializer.endTag(null, "VolumeTitle")

                    serializer.startTag(null, "Read")
                    serializer.text(item.Read.toString())
                    serializer.endTag(null, "Read")

                    serializer.startTag(null, "Owned")
                    serializer.text(item.Owned.toString())
                    serializer.endTag(null, "Owned")

                    serializer.startTag(null, "Format")
                    serializer.text(item.Format?.name ?: "")
                    serializer.endTag(null, "Format")

                    item.ItemCover?.let {
                        serializer.startTag(null, "ItemCover")
                        serializer.text(Base64.encodeToString(it, Base64.NO_WRAP))
                        serializer.endTag(null, "ItemCover")
                    }
                    serializer.endTag(null, "BookItem")
                }
                serializer.endTag(null, "Items")
                serializer.endTag(null, "Book")
            }
            serializer.endTag(null, "Books")

            // --- Videos ---
            serializer.startTag(null, "Videos")
            container.Videos?.forEach { video ->
                serializer.startTag(null, "Video")
                writeMediaItemFields(serializer, video)

                serializer.startTag(null, "Type")
                serializer.text(video.Type?.name ?: "")
                serializer.endTag(null, "Type")

                serializer.startTag(null, "VideoTag")
                serializer.text(video.VideoTag?.name ?: "")
                serializer.endTag(null, "VideoTag")

                serializer.startTag(null, "Items")
                video.Items?.forEach { item ->
                    serializer.startTag(null, "VideoItem")

                    serializer.startTag(null, "DiscNumber")
                    serializer.text(item.Season.toString())
                    serializer.endTag(null, "DiscNumber")

                    serializer.startTag(null, "DiscTitle")
                    serializer.text(item.DiscTitle ?: "")
                    serializer.endTag(null, "DiscTitle")

                    serializer.startTag(null, "Watched")
                    serializer.text(item.Watched.toString())
                    serializer.endTag(null, "Watched")

                    serializer.startTag(null, "Owned")
                    serializer.text(item.Owned.toString())
                    serializer.endTag(null, "Owned")

                    serializer.startTag(null, "Format")
                    serializer.text(item.Format?.name ?: "")
                    serializer.endTag(null, "Format")

                    item.ItemCover?.let {
                        serializer.startTag(null, "ItemCover")
                        serializer.text(Base64.encodeToString(it, Base64.NO_WRAP))
                        serializer.endTag(null, "ItemCover")
                    }
                    serializer.endTag(null, "VideoItem")
                }
                serializer.endTag(null, "Items")
                serializer.endTag(null, "Video")
            }
            serializer.endTag(null, "Videos")

            // --- Music ---
            serializer.startTag(null, "MusicList")
            container.Music?.forEach { music ->
                serializer.startTag(null, "Music")
                writeMediaItemFields(serializer, music)

                serializer.startTag(null, "Artist")
                serializer.text(music.Artist ?: "")
                serializer.endTag(null, "Artist")

                serializer.startTag(null, "Year")
                serializer.text(music.Year.toString())
                serializer.endTag(null, "Year")

                serializer.endTag(null, "Music")
            }
            serializer.endTag(null, "MusicList")

            // --- Others ---
            serializer.startTag(null, "Others")
            container.Others?.forEach { other ->
                serializer.startTag(null, "Other")
                writeMediaItemFields(serializer, other)
                serializer.endTag(null, "Other")
            }
            serializer.endTag(null, "Others")

            serializer.endTag(null, "MediaLibrary")
            serializer.endDocument()


            return writer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun writeMediaItemFields(serializer: XmlSerializer, item: MediaItem) {
        serializer.startTag(null, "Id")
        serializer.text(item.Id ?: "")
        serializer.endTag(null, "Id")

        serializer.startTag(null, "Title")
        serializer.text(item.Title ?: "")
        serializer.endTag(null, "Title")

        serializer.startTag(null, "collecting")
        serializer.text(item.Collecting.toString())
        serializer.endTag(null, "collecting")

        serializer.startTag(null, "Ongoing")
        serializer.text(item.Ongoing.toString())
        serializer.endTag(null, "Ongoing")

        serializer.startTag(null, "Collected")
        serializer.text(item.HasCollectedAllItems.toString())
        serializer.endTag(null, "Collected")

        serializer.startTag(null, "TagId")
        serializer.text(item.Tag ?: "")
        serializer.endTag(null, "TagId")

        serializer.startTag(null, "CollectingPriority")
        serializer.text(item.CollectingPriority?.name ?: "")
        serializer.endTag(null, "CollectingPriority")

        item.Genre?.let {
            serializer.startTag(null, "Genres")
            serializer.text(it.joinToString(","))
            serializer.endTag(null, "Genres")
        }
        item.Cover?.let {
            serializer.startTag(null, "Cover")
            serializer.text(Base64.encodeToString(it, Base64.NO_WRAP))
            serializer.endTag(null, "Cover")
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
