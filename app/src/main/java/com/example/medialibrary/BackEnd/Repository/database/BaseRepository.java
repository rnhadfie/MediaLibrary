package com.example.medialibrary.backend.repository.database;

import static com.example.medialibrary.backend.utils.DatabaseKeyNames.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.medialibrary.backend.models.shared.MediaItem;
import com.example.medialibrary.backend.repository.BookRepository;
import com.example.medialibrary.backend.repository.MusicRepository;
import com.example.medialibrary.backend.repository.OtherRepository;
import com.example.medialibrary.backend.repository.VideoRepository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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


    public String AddNewTag(String tagId, String newTag, SQLiteDatabase db)
    {
        String primKey = UUID.randomUUID().toString();
        if (newTag != null && !newTag.isEmpty()) {
            ContentValues tagValues = new ContentValues();
            tagValues.put(COLUMN_ID, primKey);
            tagValues.put(COLUMN_NAME, newTag);
            db.insert(TABLE_TAGS, null, tagValues);
            tagId = primKey;
        }
        return tagId;
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
        try {
            Bitmap sourceBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (sourceBitmap == null) {
                return byteArray;
            }

            int maxDimension = 800;
            int width = sourceBitmap.getWidth();
            int height = sourceBitmap.getHeight();

            Bitmap scaledBitmap = sourceBitmap;
            if (width > maxDimension || height > maxDimension) {
                float ratio = (float) width / (float) height;
                int targetWidth, targetHeight;
                if (width >= height) {
                    targetWidth = maxDimension;
                    targetHeight = Math.max(1, (int) (maxDimension / ratio));
                } else {
                    targetHeight = maxDimension;
                    targetWidth = Math.max(1, (int) (maxDimension * ratio));
                }
                scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, targetWidth, targetHeight, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);

            if (scaledBitmap != sourceBitmap) {
                scaledBitmap.recycle();
            }
            sourceBitmap.recycle();

            return outputStream.toByteArray();
        } catch (Exception e) {
            return byteArray;
        }
    }




}
