package repository.database;

import static utils.DatabaseKeyNames.COLUMN_ID;
import static utils.DatabaseKeyNames.COLUMN_NAME;
import static utils.DatabaseKeyNames.TABLE_TAGS;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import repository.BookRepository;
import repository.MusicRepository;
import repository.OtherRepository;
import repository.VideoRepository;

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
            tagValues.put(COLUMN_NAME, newTag.trim());
            db.insert(TABLE_TAGS, null, tagValues);
            tagId = primKey;
        }
        return tagId;
    }
}
