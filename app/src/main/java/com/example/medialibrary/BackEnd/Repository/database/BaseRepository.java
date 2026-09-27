package com.example.medialibrary.backend.repository.database;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.OtherRepository;
import com.example.medialibrary.backend.repository.VideoRepository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseRepository {
    protected MediaLibraryDbHelper dbHelper;

    public BaseRepository(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public static void clearAllCaches() {
        BookRepository.clearCache();
        VideoRepository.clearCache();
        MusicRepository.clearCache();
        OtherRepository.clearCache();
    }

    protected void mapMediaItem(Cursor cursor, MediaItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        item.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
        item.Ongoing = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
        item.HasCollectedAllItems = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COLLECTING)) == 1;
        item.Tag = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG));
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
    }

    protected String serializeGenre(List<Integer> genre) {
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

    protected byte[] compressBitmap(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0){
            return byteArray;
        }
        Bitmap sourceBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream);

        return outputStream.toByteArray();
    }

    protected byte[] decompressBitmap(byte[] compressedBytes) {
        return compressedBytes;
    }


}
