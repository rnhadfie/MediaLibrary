package com.example.medialibrary.backend.utils;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.medialibrary.backend.models.book.*;
import com.example.medialibrary.backend.models.book.BookItem;
import com.example.medialibrary.backend.models.book.Enums;
import com.example.medialibrary.backend.models.music.Music;
import com.example.medialibrary.backend.models.shared.Enums.CollectingPriority;
import com.example.medialibrary.backend.models.music.Enums.MusicGenre;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherItem;
import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.models.video.Video;
import com.example.medialibrary.backend.models.video.VideoItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseMappings {

    public static String MapMediaItem(Cursor cursor, MediaItem item) {
        item.Id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        item.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
        item.Ongoing = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ONGOING)) == 1;
        item.HasCollectedAllItems = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTED)) == 1;
        item.Tag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG));
        item.CollectingPriority = CollectingPriority.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING_PRIORITY))];
        item.Cover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_COVER)));

        String genreString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE));
        if (genreString != null && !genreString.isEmpty()) {
            List<Integer> genres = new ArrayList<>();
            for (String s : genreString.split(",")) {
                try {
                    genres.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {}
            }
            item.Genre = genres;
        }
        return item.Id;
    }

    //region Books

    public static ContentValues MapBookItemContentValues(String bookId, BookItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_ID, UUID.randomUUID().toString());
        itemValues.put(COLUMN_SERIES, bookId);
        itemValues.put(COLUMN_VOLUME_NUMBER, item.VolumeNumber);
        itemValues.put(COLUMN_VOLUME_TITLE, item.VolumeTitle);
        itemValues.put(COLUMN_READ, item.Read ? 1 : 0);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }

    public static ContentValues MapBookContentValues(String publisherId, String tagId, Book book) {

        ContentValues bookValues = new ContentValues();
        bookValues.put(COLUMN_ID, book.Id);
        bookValues.put(COLUMN_TITLE, book.Title);
        bookValues.put(COLUMN_COLLECTING, (book.Collecting != null && book.Collecting) ? 1 : 0);
        bookValues.put(COLUMN_ONGOING, (book.Ongoing != null && book.Ongoing) ? 1 : 0);
        bookValues.put(COLUMN_COLLECTED, (book.HasCollectedAllItems != null && book.HasCollectedAllItems) ? 1 : 0);
        bookValues.put(COLUMN_TAG, tagId);
        bookValues.put(COLUMN_COVER, compressBitmap(book.Cover));
        bookValues.put(COLUMN_GENRE, serializeGenre(book.Genre));
        bookValues.put(COLUMN_AUTHOR, book.Author);
        bookValues.put(COLUMN_ARTIST, book.Artist);
        bookValues.put(COLUMN_TYPE, book.Type != null ? book.Type.ordinal() : 0);
        bookValues.put(COLUMN_PUBLISHER, publisherId);
        bookValues.put(COLUMN_COLLECTING_PRIORITY, book.CollectingPriority != null ? book.CollectingPriority.ordinal() : 0);
        return bookValues;
    }

    public static void MapBookItem(Cursor cursor, BookItem item) {
        item.Id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.VolumeNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_NUMBER));
        item.VolumeTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_TITLE));
        item.Series = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Read = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READ)) == 1;
        item.Format = Enums.BookFormat.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FORMAT))];
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        var itemCover = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER));
        item.ItemCover = decompressBitmap(itemCover);
    }

    public static void MapBook(Cursor bookCursor, Book book) {
        MapMediaItem(bookCursor, book);
        book.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Book;
        book.Author = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
        book.Artist = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_ARTIST));
        book.Publisher = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_PUBLISHER));

        int typeValue = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_TYPE));
        if (typeValue >= 0 && typeValue < Enums.BookType.values().length) {
            book.Type = Enums.BookType.values()[typeValue];
        }

        int collectingPriority = bookCursor.getInt(bookCursor.getColumnIndexOrThrow(COLUMN_COLLECTING_PRIORITY));
        if (collectingPriority >= 0 && collectingPriority < com.example.medialibrary.backend.models.shared.Enums.CollectingPriority.values().length) {
            book.CollectingPriority = com.example.medialibrary.backend.models.shared.Enums.CollectingPriority.values()[collectingPriority];
        }
    }

    //endregion

    //region Video

    public static void MapVideoItem(Cursor cursor, VideoItem item) {
        item.Id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Season = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_NUMBER));
        item.DiscTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_TITLE));
        item.Series = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Watched = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WATCHED)) == 1;
        item.Format = com.example.medialibrary.backend.models.video.Enums.VideoFormat.values()[cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FORMAT))];
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        item.ItemCover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER)));
    }

    public static String MapVideo(Cursor videoCursor, Video video) {
       String id = MapMediaItem(videoCursor, video);
        video.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Video;
        video.Tag = videoCursor.getString(videoCursor.getColumnIndexOrThrow(COLUMN_TAG));
        video.VideoTag = com.example.medialibrary.backend.models.video.Enums.VideoTag.values()[videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_VIDEO_TAG))];

        int typeValue = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_TYPE));
        if (typeValue >= 0 && typeValue < com.example.medialibrary.backend.models.video.Enums.VideoType.values().length) {
            video.Type = com.example.medialibrary.backend.models.video.Enums.VideoType.values()[typeValue];
        }

        int collectingPriority = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(COLUMN_COLLECTING_PRIORITY));
        if (collectingPriority >= 0 && collectingPriority < com.example.medialibrary.backend.models.shared.Enums.CollectingPriority.values().length) {
            video.CollectingPriority = com.example.medialibrary.backend.models.shared.Enums.CollectingPriority.values()[collectingPriority];
        }
        return id;
    }

    public static ContentValues MapVideoContentValues(String tagId, Video video) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(COLUMN_ID, video.Id);
        videoValues.put(COLUMN_TITLE, video.Title);
        videoValues.put(COLUMN_COLLECTING, (video.Collecting != null && video.Collecting) ? 1 : 0);
        videoValues.put(COLUMN_ONGOING, (video.Ongoing != null && video.Ongoing) ? 1 : 0);
        videoValues.put(COLUMN_COLLECTED, (video.HasCollectedAllItems != null && video.HasCollectedAllItems) ? 1 : 0);
        videoValues.put(COLUMN_TAG, tagId);
        videoValues.put(COLUMN_COVER, compressBitmap(video.Cover));
        videoValues.put(COLUMN_GENRE, serializeGenre(video.Genre));
        videoValues.put(COLUMN_TYPE, video.Type != null ? video.Type.ordinal() : 0);
        videoValues.put(COLUMN_VIDEO_TAG, video.VideoTag != null ? video.VideoTag.ordinal() : 0);
        videoValues.put(COLUMN_COLLECTING_PRIORITY, video.CollectingPriority != null ? video.CollectingPriority.ordinal() : 0);
        return videoValues;
    }

    public static ContentValues MapVideoItemContentValues(String videoId, VideoItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_ID, UUID.randomUUID().toString());
        itemValues.put(COLUMN_SERIES, videoId);
        itemValues.put(COLUMN_DISC_NUMBER, item.Season);
        itemValues.put(COLUMN_DISC_TITLE, item.DiscTitle);
        itemValues.put(COLUMN_WATCHED, item.Watched ? 1 : 0);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_FORMAT, item.Format != null ? item.Format.ordinal() : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }

    //endregion

    //region Music

    public static void MapMusicItems(Cursor cursor, Music music) {
        MapMediaItem(cursor, music);
        music.MediaType = com.example.medialibrary.backend.models.shared.Enums.MediaType.Music;
        music.Artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
        music.Year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
        music.MusicGenre = deserializeMusicGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));


    }

    public static ContentValues MapMusicContentValues(String tagId, Music music) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, music.Id);
        values.put(COLUMN_TITLE, music.Title);
        values.put(COLUMN_COLLECTING, (music.Collecting != null && music.Collecting) ? 1 : 0);
        values.put(COLUMN_ONGOING, (music.Ongoing != null && music.Ongoing) ? 1 : 0);
        values.put(COLUMN_COLLECTED, (music.HasCollectedAllItems != null && music.HasCollectedAllItems) ? 1 : 0);
        values.put(COLUMN_TAG, tagId);
        values.put(COLUMN_COVER, music.Cover);
        values.put(COLUMN_GENRE, serializeMusicGenre(music.MusicGenre));
        values.put(COLUMN_ARTIST, music.Artist);
        values.put(COLUMN_YEAR, music.Year);
        values.put(COLUMN_COVER, compressBitmap(music.Cover));
        values.put(COLUMN_COLLECTING_PRIORITY, music.CollectingPriority != null ? music.CollectingPriority.ordinal() : 0);
        return values;
    }

    //endregion

    //region Other

    public static void MapOtherItem(Cursor cursor, OtherItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOLUME_TITLE));
        item.Series = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERIES));
        item.Owned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNED)) == 1;
        item.ItemCover = decompressBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COVER)));
    }

    public static ContentValues MapOtherItemContentValues(String seriesId, OtherItem item) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_ID, UUID.randomUUID().toString());
        itemValues.put(COLUMN_SERIES, seriesId);
        itemValues.put(COLUMN_VOLUME_TITLE, item.Title);
        itemValues.put(COLUMN_OWNED, item.Owned ? 1 : 0);
        itemValues.put(COLUMN_ITEM_COVER, compressBitmap(item.ItemCover));
        return itemValues;
    }

    public static ContentValues MapOtherContentValues(String tagId, Other other) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, other.Id);
        values.put(COLUMN_TITLE, other.Title);
        values.put(COLUMN_COLLECTING, (other.Collecting != null && other.Collecting) ? 1 : 0);
        values.put(COLUMN_ONGOING, (other.Ongoing != null && other.Ongoing) ? 1 : 0);
        values.put(COLUMN_COLLECTED, (other.HasCollectedAllItems != null && other.HasCollectedAllItems) ? 1 : 0);
        values.put(COLUMN_TAG, tagId);
        values.put(COLUMN_COLLECTING_PRIORITY, other.CollectingPriority != null ? other.CollectingPriority.ordinal() : 0);
        values.put(COLUMN_COVER, compressBitmap(other.Cover));
        values.put(COLUMN_GENRE, serializeGenre(other.Genre));
        return values;
    }

    //endregion

    private static byte[] compressBitmap(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0){
            return byteArray;
        }
        Bitmap sourceBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream);

        return outputStream.toByteArray();
    }

    private static byte[] decompressBitmap(byte[] compressedBytes) {
        return compressedBytes;
    }

    private static String serializeGenre(List<Integer> genre) {
        if (genre == null || genre.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genre.size(); i++) {
            sb.append(genre.get(i));
            if (i < genre.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private static String serializeMusicGenre(MusicGenre musicGenre) {
        if (musicGenre == null || musicGenre == MusicGenre.NoneSelected) {
            return "";
        }
        return String.valueOf(musicGenre.ordinal());
    }

    private static MusicGenre deserializeMusicGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            return MusicGenre.NoneSelected;
        }
        int musicGenre = Integer.parseInt(genre);
        return MusicGenre.values()[musicGenre];
    }

}
