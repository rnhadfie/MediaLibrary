package com.example.medialibrary.backend.repository.database;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.medialibrary.backend.models.video.Enums.*;
import com.example.medialibrary.backend.models.shared.MediaItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository {
    protected MediaLibraryDbHelper dbHelper;


    public BaseRepository(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    protected void mapMediaItem(Cursor cursor, MediaItem item) {
        item.Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        item.Title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        item.Collecting = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLLECTING)) == 1;
        item.HasSeriesEnded = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ENDED)) == 1;
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

    protected  List<Integer> deserializeGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            return new ArrayList<>();
        }
        String[] genres = genre.split(",");
        List<Integer> result = new ArrayList<>();
        for (String s : genres) {
            try {
                result.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return result;
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
